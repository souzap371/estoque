package com.expedicao.estoque.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.expedicao.estoque.dto.JarvitDTO;
import com.expedicao.estoque.enums.StatusContaPagar;
import com.expedicao.estoque.model.ContaPagar;
import com.expedicao.estoque.model.ContaReceber;
import com.expedicao.estoque.model.Estoque;
import com.expedicao.estoque.model.Venda;
import com.expedicao.estoque.model.VendaItem;
import com.expedicao.estoque.repositorie.ContaPagarRepository;
import com.expedicao.estoque.repositorie.ContaReceberRepository;
import com.expedicao.estoque.repositorie.EstoqueRepository;
import com.expedicao.estoque.repositorie.VendaItemRepository;
import com.expedicao.estoque.repositorie.VendaRepository;

@Service
@Transactional(readOnly = true)
public class JarvitService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int JANELA_DIAS = 90;
    private static final int HORIZONTE_DIAS = 30;

    private final EstoqueRepository estoqueRepository;
    private final VendaRepository vendaRepository;
    private final VendaItemRepository vendaItemRepository;
    private final ContaReceberRepository contaReceberRepository;
    private final ContaPagarRepository contaPagarRepository;
    private final RestClient restClient;
    private final String apiKey;
    private final String modelo;

    public JarvitService(
            EstoqueRepository estoqueRepository,
            VendaRepository vendaRepository,
            VendaItemRepository vendaItemRepository,
            ContaReceberRepository contaReceberRepository,
            ContaPagarRepository contaPagarRepository,
            @Value("${jarvit.openai.api-key:}") String apiKey,
            @Value("${jarvit.openai.model:gpt-5.6-terra}") String modelo) {
        this.estoqueRepository = estoqueRepository;
        this.vendaRepository = vendaRepository;
        this.vendaItemRepository = vendaItemRepository;
        this.contaReceberRepository = contaReceberRepository;
        this.contaPagarRepository = contaPagarRepository;
        this.restClient = RestClient.builder().baseUrl("https://api.openai.com").build();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.modelo = modelo;
    }

    public JarvitDTO.Insights gerarInsights() {
        List<Estoque> estoques = estoqueRepository.findAll();
        List<ContaReceber> recebimentos = contaReceberRepository.findAll();
        List<ContaPagar> pagamentos = contaPagarRepository.findAll();
        LocalDate hoje = LocalDate.now();

        int estoqueCritico = (int) estoques.stream()
                .filter(e -> valor(e.getQuantidadeAtual()) <= 10)
                .count();
        List<ContaPagar> atrasadas = pagamentos.stream()
                .filter(c -> emAberto(c) && c.getDataVencimento() != null && c.getDataVencimento().isBefore(hoje))
                .toList();
        BigDecimal totalReceber = recebimentos.stream()
                .map(ContaReceber::getSaldoDevedor)
                .filter(Objects::nonNull)
                .filter(v -> v.signum() > 0)
                .reduce(ZERO, BigDecimal::add);
        BigDecimal totalPagar = pagamentos.stream()
                .filter(this::emAberto)
                .map(this::saldoContaPagar)
                .reduce(ZERO, BigDecimal::add);

        List<JarvitDTO.Insight> alertas = new ArrayList<>();
        if (estoqueCritico > 0) {
            alertas.add(new JarvitDTO.Insight("ESTOQUE", "ALTA", "Estoque crítico",
                    estoqueCritico + " registros de estoque possuem 10 unidades ou menos.",
                    "Revisar reposição e possíveis transferências entre filiais.", "/estoque"));
        }
        if (!atrasadas.isEmpty()) {
            BigDecimal valorAtrasado = atrasadas.stream().map(this::saldoContaPagar).reduce(ZERO, BigDecimal::add);
            alertas.add(new JarvitDTO.Insight("FINANCEIRO", "ALTA", "Contas a pagar vencidas",
                    atrasadas.size() + " conta(s) vencida(s), totalizando " + moeda(valorAtrasado) + ".",
                    "Priorizar a regularização das contas mais antigas.", "/contas-pagar"));
        }
        BigDecimal vendasAtual = nuloZero(vendaRepository.totalVendasMesAtual());
        BigDecimal vendasAnterior = nuloZero(vendaRepository.totalVendasMesAnterior());
        if (vendasAnterior.signum() > 0) {
            BigDecimal variacao = vendasAtual.subtract(vendasAnterior)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(vendasAnterior, 1, RoundingMode.HALF_UP);
            String severidade = variacao.signum() < 0 ? "MEDIA" : "BAIXA";
            alertas.add(new JarvitDTO.Insight("VENDAS", severidade, "Desempenho mensal",
                    "As vendas estão " + variacao.abs().toPlainString() + "% "
                            + (variacao.signum() < 0 ? "abaixo" : "acima") + " do mês anterior.",
                    variacao.signum() < 0 ? "Analisar produtos e clientes com redução de compras."
                            : "Acompanhar estoque para sustentar o crescimento.",
                    "/relatorios/vendas"));
        }
        if (totalReceber.signum() > 0) {
            alertas.add(new JarvitDTO.Insight("RECEBIMENTOS", "MEDIA", "Valores em aberto",
                    "Há " + moeda(totalReceber) + " pendentes de recebimento.",
                    "Acompanhar clientes com maior saldo devedor.", "/financeiro/relatorio"));
        }
        if (alertas.isEmpty()) {
            alertas.add(new JarvitDTO.Insight("GERAL", "BAIXA", "Operação estável",
                    "Nenhum alerta prioritário foi identificado neste momento.",
                    "Continue acompanhando os indicadores diariamente.", "/dashboard"));
        }

        String resumo = "Hoje o JARVIT identificou " + estoqueCritico + " estoque(s) crítico(s), "
                + atrasadas.size() + " conta(s) vencida(s), " + moeda(totalReceber)
                + " a receber e " + moeda(totalPagar) + " a pagar.";
        return new JarvitDTO.Insights(resumo, alertas.stream().limit(5).toList(), estoqueCritico,
                atrasadas.size(), totalReceber, totalPagar);
    }

    public JarvitDTO.Previsoes gerarPrevisoes() {
        LocalDate inicio = LocalDate.now().minusDays(JANELA_DIAS - 1L);
        List<VendaItem> itensRecentes = vendaItemRepository.findAll().stream()
                .filter(i -> i.getVenda() != null && i.getVenda().getDataSaida() != null)
                .filter(i -> !i.getVenda().getDataSaida().isBefore(inicio))
                .filter(i -> !Boolean.TRUE.equals(i.getBonificacao()))
                .toList();

        Map<Long, Integer> vendidoPorProduto = new HashMap<>();
        Map<Long, String> nomes = new HashMap<>();
        for (VendaItem item : itensRecentes) {
            Long id = item.getProduto().getId();
            vendidoPorProduto.merge(id, valor(item.getQuantidade()), Integer::sum);
            nomes.put(id, item.getProduto().getNome());
        }

        List<Estoque> estoques = estoqueRepository.findAll();
        estoques.forEach(e -> nomes.putIfAbsent(e.getProduto().getId(), e.getProduto().getNome()));
        Map<Long, Integer> estoquePorProduto = estoques.stream()
                .collect(Collectors.groupingBy(e -> e.getProduto().getId(),
                        Collectors.summingInt(e -> valor(e.getQuantidadeAtual()))));

        List<JarvitDTO.PrevisaoProduto> produtos = new ArrayList<>();
        estoquePorProduto.forEach((id, estoque) -> {
            int vendido = vendidoPorProduto.getOrDefault(id, 0);
            double media = vendido / (double) JANELA_DIAS;
            Integer cobertura = media > 0 ? (int) Math.floor(estoque / media) : null;
            int alvo = (int) Math.ceil(media * HORIZONTE_DIAS);
            int reposicao = Math.max(0, alvo - estoque);
            String nivel = cobertura == null ? "SEM_HISTORICO"
                    : cobertura <= 7 ? "CRITICO"
                            : cobertura <= 15 ? "ATENCAO" : "SAUDAVEL";
            produtos.add(new JarvitDTO.PrevisaoProduto(id, nomes.getOrDefault(id, "Produto " + id),
                    estoque, arredondar(media), cobertura, reposicao, nivel));
        });
        produtos.sort(Comparator
                .comparing((JarvitDTO.PrevisaoProduto p) -> prioridade(p.nivel()))
                .thenComparing(p -> p.diasCobertura() == null ? Integer.MAX_VALUE : p.diasCobertura()));

        List<Venda> vendasRecentes = vendaRepository.findAll().stream()
                .filter(v -> v.getDataSaida() != null && !v.getDataSaida().isBefore(inicio))
                .toList();
        BigDecimal totalVendas = vendasRecentes.stream()
                .map(Venda::getValorTotal).filter(Objects::nonNull).reduce(ZERO, BigDecimal::add);
        long diasObservados = Math.max(1, ChronoUnit.DAYS.between(inicio, LocalDate.now()) + 1);
        BigDecimal vendasProjetadas = totalVendas
                .divide(BigDecimal.valueOf(diasObservados), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(HORIZONTE_DIAS))
                .setScale(2, RoundingMode.HALF_UP);
        JarvitDTO.Insights insights = gerarInsights();
        BigDecimal saldoProjetado = insights.totalAReceber().add(vendasProjetadas)
                .subtract(insights.totalAPagar()).setScale(2, RoundingMode.HALF_UP);

        return new JarvitDTO.Previsoes(produtos.stream().limit(12).toList(), vendasProjetadas,
                saldoProjetado,
                "Média móvel dos últimos 90 dias, com horizonte de 30 dias. É uma estimativa e não substitui revisão humana.");
    }

    public JarvitDTO.ChatResponse responder(String pergunta) {
        String texto = pergunta == null ? "" : pergunta.trim();
        if (texto.isBlank()) {
            throw new IllegalArgumentException("Digite uma pergunta para o JARVIT.");
        }
        if (texto.length() > 500) {
            throw new IllegalArgumentException("A pergunta deve ter no máximo 500 caracteres.");
        }

        JarvitDTO.Insights insights = gerarInsights();
        JarvitDTO.Previsoes previsoes = gerarPrevisoes();
        if (apiKey.isBlank()) {
            return new JarvitDTO.ChatResponse(respostaLocal(texto, insights, previsoes),
                    "Análise local dos dados do sistema", false);
        }
        try {
            return new JarvitDTO.ChatResponse(respostaOpenAI(texto, insights, previsoes),
                    "OpenAI + dados agregados do sistema", true);
        } catch (RuntimeException ex) {
            return new JarvitDTO.ChatResponse(
                    respostaLocal(texto, insights, previsoes)
                            + "\n\nA análise avançada está temporariamente indisponível; usei o modo local.",
                    "Análise local dos dados do sistema", true);
        }
    }

    private String respostaOpenAI(String pergunta, JarvitDTO.Insights insights, JarvitDTO.Previsoes previsoes) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", modelo);
        payload.put("reasoning", Map.of("effort", "low"));
        payload.put("text", Map.of("verbosity", "low"));
        payload.put("store", false);
        payload.put("instructions",
                "Você é o JARVIT, analista do Sistema V. Responda em português do Brasil, de forma objetiva. "
                        + "Use exclusivamente os indicadores agregados fornecidos. Não invente dados. "
                        + "Diferencie fatos de estimativas e recomende confirmação humana antes de decisões financeiras.");
        payload.put("input", "Pergunta: " + pergunta + "\n\nIndicadores agregados:\n"
                + contextoAgregado(insights, previsoes));

        @SuppressWarnings("unchecked")
        Map<String, Object> resposta = restClient.post()
                .uri("/v1/responses")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);
        if (resposta == null) {
            throw new IllegalStateException("Resposta vazia da IA");
        }
        Object outputValue = resposta.get("output");
        if (outputValue instanceof List<?> outputs) {
            for (Object outputValueItem : outputs) {
                if (outputValueItem instanceof Map<?, ?> output) {
                    Object contentValue = output.get("content");
                    if (contentValue instanceof List<?> contents) {
                        for (Object contentValueItem : contents) {
                            if (contentValueItem instanceof Map<?, ?> content
                                    && "output_text".equals(content.get("type"))
                                    && content.get("text") instanceof String texto
                                    && !texto.isBlank()) {
                                return texto;
                            }
                        }
                    }
                }
            }
        }
        throw new IllegalStateException("A IA não retornou texto");
    }

    private String respostaLocal(String pergunta, JarvitDTO.Insights insights, JarvitDTO.Previsoes previsoes) {
        String p = pergunta.toLowerCase(Locale.ROOT);
        if (p.contains("menos quantidade") || p.contains("menor quantidade")
                || p.contains("menor estoque") || p.contains("estoque mais baixo")) {
            return responderMenorEstoque();
        }
        if (p.contains("estoque") || p.contains("repor") || p.contains("produto")) {
            List<JarvitDTO.PrevisaoProduto> urgentes = previsoes.produtos().stream()
                    .filter(item -> "CRITICO".equals(item.nivel()) || "ATENCAO".equals(item.nivel()))
                    .limit(5).toList();
            if (urgentes.isEmpty()) {
                return "Não identifiquei cobertura crítica pelo histórico de vendas. "
                        + responderMenorEstoque();
            }
            return "Prioridades de estoque:\n" + urgentes.stream()
                    .map(item -> "• " + item.produto() + ": " + item.estoqueAtual()
                            + " unidades, cobertura estimada de "
                            + (item.diasCobertura() == null ? "dados insuficientes" : item.diasCobertura() + " dias")
                            + ", reposição sugerida de " + item.reposicaoSugerida() + ".")
                    .collect(Collectors.joining("\n"));
        }
        if (p.contains("receber") || p.contains("pagar") || p.contains("finance") || p.contains("saldo")) {
            return "Resumo financeiro: " + moeda(insights.totalAReceber()) + " a receber, "
                    + moeda(insights.totalAPagar()) + " a pagar e saldo projetado de "
                    + moeda(previsoes.saldoFinanceiroProjetado()) + " para o horizonte analisado.";
        }
        if (p.contains("venda") || p.contains("fatur")) {
            return "A projeção de vendas para os próximos 30 dias é "
                    + moeda(previsoes.vendasProjetadas30Dias())
                    + ", calculada pela média móvel dos últimos 90 dias.";
        }
        return insights.resumo() + " Pergunte sobre estoque, reposição, vendas, valores a pagar ou a receber.";
    }

    private String responderMenorEstoque() {
        List<Estoque> estoques = estoqueRepository.findAll().stream()
                .filter(e -> e.getProduto() != null)
                .sorted(Comparator
                        .comparingInt((Estoque e) -> valor(e.getQuantidadeAtual()))
                        .thenComparing(e -> e.getProduto().getNome(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(e -> e.getFilial().name()))
                .toList();
        if (estoques.isEmpty()) {
            return "Não há registros de estoque cadastrados para comparar.";
        }

        int menorQuantidade = valor(estoques.get(0).getQuantidadeAtual());
        List<Estoque> menores = estoques.stream()
                .filter(e -> valor(e.getQuantidadeAtual()) == menorQuantidade)
                .limit(5)
                .toList();
        String registros = menores.stream()
                .map(e -> "• " + e.getProduto().getNome() + " — " + nomeFilial(e)
                        + ": " + valor(e.getQuantidadeAtual()) + " unidade(s)")
                .collect(Collectors.joining("\n"));
        String nivel = menorQuantidade <= 10 ? " Esse nível é considerado crítico pelo JARVIT." : "";
        return "O menor estoque físico cadastrado é:\n" + registros + "." + nivel;
    }

    private String nomeFilial(Estoque estoque) {
        return estoque.getFilial() == null ? "filial não informada"
                : "filial " + estoque.getFilial().name();
    }

    private boolean emAberto(ContaPagar conta) {
        return conta.getStatus() != StatusContaPagar.PAGO
                && conta.getStatus() != StatusContaPagar.CANCELADO
                && saldoContaPagar(conta).signum() > 0;
    }

    private BigDecimal saldoContaPagar(ContaPagar conta) {
        if (conta.getSaldoDevedor() != null) {
            return conta.getSaldoDevedor().max(ZERO);
        }
        BigDecimal valor = nuloZero(conta.getValor());
        return valor.subtract(nuloZero(conta.getValorPago())).max(ZERO);
    }

    private int prioridade(String nivel) {
        return switch (nivel) {
            case "CRITICO" -> 0;
            case "ATENCAO" -> 1;
            case "SAUDAVEL" -> 2;
            default -> 3;
        };
    }

    private int valor(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private BigDecimal nuloZero(BigDecimal valor) {
        return valor == null ? ZERO : valor;
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private String moeda(BigDecimal valor) {
        return java.text.NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(nuloZero(valor));
    }

    private String contextoAgregado(JarvitDTO.Insights insights, JarvitDTO.Previsoes previsoes) {
        String produtos = previsoes.produtos().stream()
                .limit(8)
                .map(p -> p.produto() + ": estoque=" + p.estoqueAtual()
                        + ", cobertura=" + (p.diasCobertura() == null ? "sem histórico" : p.diasCobertura() + " dias")
                        + ", reposição sugerida=" + p.reposicaoSugerida())
                .collect(Collectors.joining("; "));
        return insights.resumo()
                + "\nVendas projetadas em 30 dias: " + moeda(previsoes.vendasProjetadas30Dias())
                + "\nSaldo financeiro projetado: " + moeda(previsoes.saldoFinanceiroProjetado())
                + "\nProdutos prioritários: " + produtos;
    }
}
