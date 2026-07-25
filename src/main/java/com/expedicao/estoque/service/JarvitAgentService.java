package com.expedicao.estoque.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.expedicao.estoque.dto.JarvitDTO;
import com.expedicao.estoque.dto.VendaDTO;
import com.expedicao.estoque.dto.VendaItemDTO;
import com.expedicao.estoque.model.Cliente;
import com.expedicao.estoque.model.ContaReceber;
import com.expedicao.estoque.model.Estoque;
import com.expedicao.estoque.model.FormaPagamento;
import com.expedicao.estoque.model.Produto;
import com.expedicao.estoque.repositorie.ClienteRepository;
import com.expedicao.estoque.repositorie.ContaReceberRepository;
import com.expedicao.estoque.repositorie.EstoqueRepository;
import com.expedicao.estoque.repositorie.PagamentoRepository;
import com.expedicao.estoque.repositorie.ProdutoRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class JarvitAgentService {

    private static final int MAX_TOOL_ROUNDS = 6;
    private static final long ACAO_EXPIRA_MINUTOS = 15;

    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final PagamentoRepository pagamentoRepository;
    private final VendaService vendaService;
    private final FinanceiroService financeiroService;
    private final JarvitService jarvitService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;
    private final String modelo;

    private final Map<String, ConversationState> conversas = new ConcurrentHashMap<>();
    private final Map<String, PendingAction> acoesPendentes = new ConcurrentHashMap<>();

    public JarvitAgentService(
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            EstoqueRepository estoqueRepository,
            ContaReceberRepository contaReceberRepository,
            PagamentoRepository pagamentoRepository,
            VendaService vendaService,
            FinanceiroService financeiroService,
            JarvitService jarvitService,
            @Value("${jarvit.openai.api-key:}") String apiKey,
            @Value("${jarvit.openai.model:gpt-5.6-terra}") String modelo) {
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueRepository = estoqueRepository;
        this.contaReceberRepository = contaReceberRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.vendaService = vendaService;
        this.financeiroService = financeiroService;
        this.jarvitService = jarvitService;
        this.restClient = RestClient.builder().baseUrl("https://api.openai.com").build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.modelo = modelo;
    }

    public JarvitDTO.ConversaResponse conversar(
            JarvitDTO.ConversaRequest request,
            Authentication authentication) {
        String mensagem = request == null || request.mensagem() == null ? "" : request.mensagem().trim();
        if (mensagem.isBlank()) {
            throw new IllegalArgumentException("Digite uma mensagem para conversar com o JARVIT.");
        }
        if (mensagem.length() > 1000) {
            throw new IllegalArgumentException("A mensagem deve ter no máximo 1.000 caracteres.");
        }
        String conversaId = normalizarConversaId(request.conversaId(), authentication.getName());
        if (apiKey.isBlank()) {
            return new JarvitDTO.ConversaResponse(conversaId,
                    "O modo de agente ainda não está conectado à OpenAI. Configure OPENAI_API_KEY no servidor "
                            + "e reinicie a aplicação. Os Insights e Previsões continuam funcionando localmente.",
                    null, false);
        }

        ConversationState state = conversas.computeIfAbsent(conversaId,
                id -> new ConversationState(authentication.getName()));
        if (!state.username().equals(authentication.getName())) {
            throw new IllegalArgumentException("Conversa inválida para este usuário.");
        }

        Map<String, Object> payload = payloadBase(authentication);
        payload.put("input", mensagem);
        if (state.previousResponseId() != null) {
            payload.put("previous_response_id", state.previousResponseId());
        }

        JsonNode response = enviar(payload);
        PendingAction pending = null;
        for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
            state.previousResponseId(response.path("id").asText());
            List<Map<String, Object>> outputs = new ArrayList<>();
            boolean encontrouTool = false;
            for (JsonNode item : response.path("output")) {
                if (!"function_call".equals(item.path("type").asText())) {
                    continue;
                }
                encontrouTool = true;
                String name = item.path("name").asText();
                String callId = item.path("call_id").asText();
                JsonNode args = lerArgumentos(item.path("arguments").asText());
                ToolResult result = executarFerramenta(name, args, authentication, conversaId);
                if (result.pendingAction() != null) {
                    pending = result.pendingAction();
                }
                outputs.add(Map.of(
                        "type", "function_call_output",
                        "call_id", callId,
                        "output", json(result.output())));
            }
            if (!encontrouTool) {
                return new JarvitDTO.ConversaResponse(conversaId, extrairTexto(response),
                        toDto(pending), true);
            }
            Map<String, Object> continuation = payloadBase(authentication);
            continuation.put("previous_response_id", state.previousResponseId());
            continuation.put("input", outputs);
            response = enviar(continuation);
        }
        return new JarvitDTO.ConversaResponse(conversaId,
                "A solicitação exigiu etapas demais. Reformule com mais detalhes.", toDto(pending), true);
    }

    @Transactional
    public JarvitDTO.ConfirmacaoResponse confirmar(String token, Authentication authentication) {
        PendingAction action = obterAcao(token, authentication);
        try {
            String mensagem = switch (action.type()) {
                case "CRIAR_PEDIDO" -> executarPedido(action.arguments(), authentication);
                case "REGISTRAR_BAIXA" -> executarBaixa(action.arguments(), authentication);
                default -> throw new IllegalArgumentException("Tipo de ação não suportado.");
            };
            acoesPendentes.remove(token);
            return new JarvitDTO.ConfirmacaoResponse(true, mensagem);
        } catch (RuntimeException ex) {
            return new JarvitDTO.ConfirmacaoResponse(false,
                    "A operação não foi realizada: " + ex.getMessage());
        }
    }

    public JarvitDTO.ConfirmacaoResponse cancelar(String token, Authentication authentication) {
        obterAcao(token, authentication);
        acoesPendentes.remove(token);
        return new JarvitDTO.ConfirmacaoResponse(true, "Operação cancelada. Nenhum dado foi alterado.");
    }

    private ToolResult executarFerramenta(
            String name, JsonNode args, Authentication authentication, String conversaId) {
        return switch (name) {
            case "consultar_clientes" -> new ToolResult(consultarClientes(args.path("nome").asText("")), null);
            case "consultar_produtos" -> new ToolResult(consultarProdutos(args.path("termo").asText("")), null);
            case "consultar_estoque" -> new ToolResult(consultarEstoque(args.path("produto").asText("")), null);
            case "consultar_contas_cliente" ->
                new ToolResult(consultarContas(args.path("cliente").asText("")), null);
            case "consultar_resumo_operacional" ->
                new ToolResult(Map.of("insights", jarvitService.gerarInsights(),
                        "previsoes", jarvitService.gerarPrevisoes()), null);
            case "consultar_clientes_que_mais_pagaram" ->
                new ToolResult(clientesQueMaisPagaram(args.path("limite").asInt(5)), null);
            case "preparar_pedido" -> prepararAcao("CRIAR_PEDIDO", args, authentication, conversaId);
            case "preparar_baixa" -> prepararAcao("REGISTRAR_BAIXA", args, authentication, conversaId);
            default -> new ToolResult(Map.of("erro", "Ferramenta desconhecida: " + name), null);
        };
    }

    private ToolResult prepararAcao(
            String tipo, JsonNode args, Authentication authentication, String conversaId) {
        if ("CRIAR_PEDIDO".equals(tipo)) {
            exigirPerfil(authentication, "ROLE_VENDAS", "ROLE_VENDA", "ROLE_MASTER");
            validarPedido(args);
        } else {
            exigirPerfil(authentication, "ROLE_FINANCEIRO", "ROLE_MASTER");
            validarBaixa(args);
        }
        String token = UUID.randomUUID().toString();
        String resumo = resumoAcao(tipo, args);
        PendingAction action = new PendingAction(token, tipo, resumo, args.deepCopy(),
                authentication.getName(), conversaId, Instant.now());
        acoesPendentes.put(token, action);
        return new ToolResult(Map.of(
                "status", "AGUARDANDO_CONFIRMACAO",
                "resumo", resumo,
                "instrucao", "Informe que a operação ainda não foi executada e peça confirmação ao usuário."),
                action);
    }

    private List<Map<String, Object>> consultarClientes(String nome) {
        return clienteRepository.findByNomeCompletoContainingIgnoreCase(nome).stream().limit(20)
                .map(c -> Map.<String, Object>of(
                        "id", c.getId(), "nome", c.getNomeCompleto(), "uf", c.getUf()))
                .toList();
    }

    private List<Map<String, Object>> consultarProdutos(String termo) {
        String filtro = termo.toLowerCase();
        return produtoRepository.findAll().stream()
                .filter(p -> filtro.isBlank()
                        || p.getNome().toLowerCase().contains(filtro)
                        || p.getCodigo().toLowerCase().contains(filtro))
                .limit(20)
                .map(p -> Map.<String, Object>of("id", p.getId(), "codigo", p.getCodigo(), "nome", p.getNome()))
                .toList();
    }

    private List<Map<String, Object>> consultarEstoque(String produto) {
        String filtro = produto.toLowerCase();
        return estoqueRepository.findAll().stream()
                .filter(e -> filtro.isBlank()
                        || e.getProduto().getNome().toLowerCase().contains(filtro)
                        || e.getProduto().getCodigo().toLowerCase().contains(filtro))
                .sorted(Comparator.comparingInt(e -> e.getQuantidadeAtual() == null ? 0 : e.getQuantidadeAtual()))
                .limit(30)
                .map(e -> Map.<String, Object>of(
                        "produtoId", e.getProduto().getId(),
                        "codigo", e.getProduto().getCodigo(),
                        "produto", e.getProduto().getNome(),
                        "filial", e.getFilial().name(),
                        "quantidade", e.getQuantidadeAtual()))
                .toList();
    }

    private List<Map<String, Object>> consultarContas(String cliente) {
        return contaReceberRepository.buscarComFiltro(cliente).stream()
                .filter(c -> c.getSaldoDevedor() != null && c.getSaldoDevedor().signum() > 0)
                .limit(30)
                .map(c -> Map.<String, Object>of(
                        "contaId", c.getId(),
                        "cliente", c.getClienteNome(),
                        "valorOriginal", c.getValorOriginal(),
                        "valorPago", c.getValorPago(),
                        "saldoDevedor", c.getSaldoDevedor(),
                        "data", c.getDataCriacao()))
                .toList();
    }

    private List<Map<String, Object>> clientesQueMaisPagaram(int limite) {
        Map<String, BigDecimal> totais = new LinkedHashMap<>();
        pagamentoRepository.findAll().forEach(p -> {
            if (p.getContaReceber() != null && p.getContaReceber().getClienteNome() != null) {
                totais.merge(p.getContaReceber().getClienteNome(),
                        Objects.requireNonNullElse(p.getValorPago(), BigDecimal.ZERO), BigDecimal::add);
            }
        });
        return totais.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(Math.max(1, Math.min(limite, 20)))
                .map(e -> Map.<String, Object>of("cliente", e.getKey(), "totalPago", e.getValue()))
                .toList();
    }

    private String executarPedido(JsonNode args, Authentication authentication) {
        exigirPerfil(authentication, "ROLE_VENDAS", "ROLE_VENDA", "ROLE_MASTER");
        validarPedido(args);
        VendaDTO dto = new VendaDTO();
        dto.setClienteId(args.path("clienteId").asLong());
        dto.setComNotaFiscal(args.path("comNotaFiscal").asBoolean(false));
        dto.setObservacao(args.path("observacao").asText("Criado com auxílio do JARVIT"));
        dto.setDataPedido(data(args.path("dataPedido").asText(null)));
        List<VendaItemDTO> itens = new ArrayList<>();
        for (JsonNode item : args.path("itens")) {
            VendaItemDTO vendaItem = new VendaItemDTO();
            vendaItem.setCodigoOuNome(item.path("produto").asText());
            vendaItem.setQuantidade(item.path("quantidade").asInt());
            vendaItem.setValorPorCaixa(item.path("valorPorCaixa").asDouble());
            vendaItem.setTipoMovimentacao("V");
            vendaItem.setBonificacao(item.path("bonificacao").asBoolean(false));
            itens.add(vendaItem);
        }
        dto.setItens(itens);
        vendaService.salvarPedido(dto);
        return "Pedido criado com sucesso.";
    }

    private String executarBaixa(JsonNode args, Authentication authentication) {
        exigirPerfil(authentication, "ROLE_FINANCEIRO", "ROLE_MASTER");
        validarBaixa(args);
        long contaId = args.path("contaId").asLong();
        BigDecimal valor = args.path("valor").decimalValue();
        LocalDate data = data(args.path("data").asText(null));
        FormaPagamento forma = FormaPagamento.valueOf(args.path("formaPagamento").asText());
        BigDecimal restante = financeiroService.darBaixa(contaId, valor, data, forma, null);
        return restante.signum() > 0
                ? "Baixa registrada. Restaram " + restante + " do valor informado sem aplicação."
                : "Baixa registrada com sucesso.";
    }

    private void validarPedido(JsonNode args) {
        long clienteId = args.path("clienteId").asLong(0);
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
        if (!args.path("itens").isArray() || args.path("itens").isEmpty()) {
            throw new IllegalArgumentException("O pedido precisa ter pelo menos um item.");
        }
        for (JsonNode item : args.path("itens")) {
            String termo = item.path("produto").asText();
            Produto produto = produtoRepository.findByCodigoOuNome(termo)
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + termo));
            int quantidade = item.path("quantidade").asInt();
            if (quantidade <= 0 || item.path("valorPorCaixa").decimalValue().signum() < 0) {
                throw new IllegalArgumentException("Quantidade ou valor inválido para " + produto.getNome());
            }
            int matriz = estoqueRepository.findByProdutoAndFilial(produto,
                    com.expedicao.estoque.model.Filial.MATRIZ)
                    .map(Estoque::getQuantidadeAtual).orElse(0);
            if (matriz < quantidade) {
                throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome()
                        + ". Disponível na matriz: " + matriz);
            }
        }
        if (cliente.getNomeCompleto() == null) {
            throw new IllegalArgumentException("Cliente inválido.");
        }
    }

    private void validarBaixa(JsonNode args) {
        long contaId = args.path("contaId").asLong(0);
        ContaReceber conta = contaReceberRepository.findById(contaId)
                .orElseThrow(() -> new IllegalArgumentException("Conta a receber não encontrada."));
        BigDecimal valor = args.path("valor").decimalValue();
        if (valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor da baixa deve ser maior que zero.");
        }
        if (conta.getSaldoDevedor() == null || conta.getSaldoDevedor().signum() <= 0) {
            throw new IllegalArgumentException("A conta já está quitada.");
        }
        try {
            FormaPagamento.valueOf(args.path("formaPagamento").asText());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Forma de pagamento inválida.");
        }
    }

    private String resumoAcao(String tipo, JsonNode args) {
        if ("CRIAR_PEDIDO".equals(tipo)) {
            Cliente cliente = clienteRepository.findById(args.path("clienteId").asLong()).orElseThrow();
            StringBuilder resumo = new StringBuilder("Criar pedido para ").append(cliente.getNomeCompleto()).append(": ");
            for (JsonNode item : args.path("itens")) {
                resumo.append(item.path("quantidade").asInt()).append(" × ")
                        .append(item.path("produto").asText()).append(" a R$ ")
                        .append(item.path("valorPorCaixa").decimalValue()).append("; ");
            }
            return resumo.toString();
        }
        ContaReceber conta = contaReceberRepository.findById(args.path("contaId").asLong()).orElseThrow();
        return "Registrar baixa de R$ " + args.path("valor").decimalValue() + " para "
                + conta.getClienteNome() + ", conta nº " + conta.getId() + ", via "
                + args.path("formaPagamento").asText() + ".";
    }

    private PendingAction obterAcao(String token, Authentication authentication) {
        PendingAction action = acoesPendentes.get(token);
        if (action == null || !action.username().equals(authentication.getName())) {
            throw new IllegalArgumentException("Ação pendente não encontrada.");
        }
        if (ChronoUnit.MINUTES.between(action.createdAt(), Instant.now()) > ACAO_EXPIRA_MINUTOS) {
            acoesPendentes.remove(token);
            throw new IllegalArgumentException("A confirmação expirou. Solicite a operação novamente.");
        }
        return action;
    }

    private Map<String, Object> payloadBase(Authentication authentication) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", modelo);
        payload.put("store", true);
        payload.put("reasoning", Map.of("effort", "low", "context", "all_turns"));
        payload.put("text", Map.of("verbosity", "low"));
        payload.put("safety_identifier", hash(authentication.getName()));
        payload.put("instructions", """
                Você é o JARVIT, agente operacional do Sistema V. Responda em português do Brasil.
                Use ferramentas para toda afirmação sobre dados do sistema; nunca invente IDs, saldos ou estoque.
                Para criar pedido ou registrar baixa, primeiro consulte cliente/produto/conta quando houver ambiguidade.
                Nunca diga que uma ação foi executada ao chamar preparar_pedido ou preparar_baixa: essas ferramentas
                apenas criam uma prévia que exige confirmação explícita na interface.
                Faça perguntas objetivas quando faltarem cliente, produto, quantidade, preço, conta, valor ou forma.
                Não tente executar exclusões, estornos ou mudanças não oferecidas pelas ferramentas.
                """);
        payload.put("tools", toolDefinitions());
        return payload;
    }

    private List<Map<String, Object>> toolDefinitions() {
        return List.of(
                tool("consultar_clientes", "Busca clientes por parte do nome.",
                        objectSchema(Map.of("nome", stringSchema("Nome ou parte do nome")), List.of("nome"))),
                tool("consultar_produtos", "Busca produtos por código ou nome.",
                        objectSchema(Map.of("termo", stringSchema("Código, nome ou parte do nome")), List.of("termo"))),
                tool("consultar_estoque", "Consulta quantidade física por produto e filial.",
                        objectSchema(Map.of("produto", stringSchema("Código, nome ou vazio para todos")),
                                List.of("produto"))),
                tool("consultar_contas_cliente", "Lista contas a receber abertas de um cliente.",
                        objectSchema(Map.of("cliente", stringSchema("Nome exato do cliente")), List.of("cliente"))),
                tool("consultar_resumo_operacional", "Consulta insights, previsões, vendas e financeiro.",
                        objectSchema(Map.of(), List.of())),
                tool("consultar_clientes_que_mais_pagaram", "Lista clientes pelo total efetivamente pago.",
                        objectSchema(Map.of("limite", integerSchema("Quantidade de clientes, entre 1 e 20")),
                                List.of("limite"))),
                tool("preparar_pedido", "Prepara um pedido para confirmação; não executa imediatamente.",
                        pedidoSchema()),
                tool("preparar_baixa", "Prepara uma baixa financeira para confirmação; não executa imediatamente.",
                        baixaSchema()));
    }

    private Map<String, Object> pedidoSchema() {
        Map<String, Object> item = objectSchema(Map.of(
                "produto", stringSchema("Código ou nome exato do produto"),
                "quantidade", integerSchema("Quantidade positiva"),
                "valorPorCaixa", numberSchema("Preço por caixa"),
                "bonificacao", booleanSchema("Se o item é bonificação")), List.of(
                        "produto", "quantidade", "valorPorCaixa", "bonificacao"));
        return objectSchema(Map.of(
                "clienteId", integerSchema("ID do cliente confirmado por consulta"),
                "itens", Map.of("type", "array", "items", item),
                "comNotaFiscal", booleanSchema("Pedido com nota fiscal"),
                "observacao", stringSchema("Observação do pedido"),
                "dataPedido", stringSchema("Data ISO yyyy-MM-dd ou vazio para hoje")),
                List.of("clienteId", "itens", "comNotaFiscal", "observacao", "dataPedido"));
    }

    private Map<String, Object> baixaSchema() {
        return objectSchema(Map.of(
                "contaId", integerSchema("ID exato da conta a receber"),
                "valor", numberSchema("Valor positivo da baixa"),
                "data", stringSchema("Data ISO yyyy-MM-dd ou vazio para hoje"),
                "formaPagamento", Map.of("type", "string", "enum",
                        java.util.Arrays.stream(FormaPagamento.values()).map(Enum::name).toList())),
                List.of("contaId", "valor", "data", "formaPagamento"));
    }

    private Map<String, Object> tool(String name, String description, Map<String, Object> parameters) {
        return Map.of("type", "function", "name", name, "description", description,
                "parameters", parameters, "strict", true);
    }

    private Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required) {
        return Map.of("type", "object", "properties", properties,
                "required", required, "additionalProperties", false);
    }

    private Map<String, Object> stringSchema(String description) {
        return Map.of("type", "string", "description", description);
    }

    private Map<String, Object> integerSchema(String description) {
        return Map.of("type", "integer", "description", description);
    }

    private Map<String, Object> numberSchema(String description) {
        return Map.of("type", "number", "description", description);
    }

    private Map<String, Object> booleanSchema(String description) {
        return Map.of("type", "boolean", "description", description);
    }

    private JsonNode enviar(Map<String, Object> payload) {
        JsonNode response = restClient.post().uri("/v1/responses")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload).retrieve().body(JsonNode.class);
        if (response == null) {
            throw new IllegalStateException("A OpenAI retornou uma resposta vazia.");
        }
        return response;
    }

    private JsonNode lerArgumentos(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new IllegalArgumentException("A IA retornou argumentos inválidos.", ex);
        }
    }

    private String extrairTexto(JsonNode response) {
        for (JsonNode output : response.path("output")) {
            for (JsonNode content : output.path("content")) {
                if ("output_text".equals(content.path("type").asText())) {
                    String text = content.path("text").asText();
                    if (!text.isBlank()) {
                        return text;
                    }
                }
            }
        }
        return "Concluí a consulta, mas não recebi uma resposta textual. Tente reformular a solicitação.";
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao serializar resultado da ferramenta.", ex);
        }
    }

    private LocalDate data(String value) {
        return value == null || value.isBlank() ? LocalDate.now() : LocalDate.parse(value);
    }

    private void exigirPerfil(Authentication authentication, String... roles) {
        boolean permitido = authentication.getAuthorities().stream()
                .anyMatch(a -> java.util.Arrays.asList(roles).contains(a.getAuthority()));
        if (!permitido) {
            throw new IllegalArgumentException("Seu perfil não possui permissão para esta operação.");
        }
    }

    private String normalizarConversaId(String id, String username) {
        if (id == null || id.isBlank()) {
            return username + "-" + UUID.randomUUID();
        }
        return id.length() > 150 ? id.substring(0, 150) : id;
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private JarvitDTO.AcaoPendente toDto(PendingAction action) {
        return action == null ? null
                : new JarvitDTO.AcaoPendente(action.token(), action.type(), action.summary());
    }

    private record ToolResult(Object output, PendingAction pendingAction) {
    }

    private record PendingAction(
            String token, String type, String summary, JsonNode arguments,
            String username, String conversationId, Instant createdAt) {
    }

    private static final class ConversationState {
        private final String username;
        private volatile String previousResponseId;

        private ConversationState(String username) {
            this.username = username;
        }

        private String username() {
            return username;
        }

        private String previousResponseId() {
            return previousResponseId;
        }

        private void previousResponseId(String id) {
            this.previousResponseId = id;
        }
    }
}
