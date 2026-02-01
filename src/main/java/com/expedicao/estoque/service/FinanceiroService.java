package com.expedicao.estoque.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.expedicao.estoque.dto.ContaReceberDTO;
import com.expedicao.estoque.model.*;
import com.expedicao.estoque.repositorie.ContaReceberRepository;
import com.expedicao.estoque.repositorie.PagamentoRepository;

import jakarta.transaction.Transactional;

@Service
public class FinanceiroService {

    private static final String DIRETORIO_UPLOAD = "uploads/financeiro";
    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of(".pdf", ".jpg", ".jpeg", ".png");

    private final ContaReceberRepository contaReceberRepository;
    private final PagamentoRepository pagamentoRepository;

    public FinanceiroService(ContaReceberRepository contaReceberRepository,
            PagamentoRepository pagamentoRepository) {
        this.contaReceberRepository = contaReceberRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    // =========================
    // CONSULTAS
    // =========================
    public List<String> listarClientes() {
        return contaReceberRepository.listarClientes();
    }

    @Transactional
    public List<ContaReceberDTO> buscarTodas() {
        return contaReceberRepository.findAll()
                .stream()
                .map(ContaReceberDTO::new)
                .collect(Collectors.toList());
    }

    public List<ContaReceberDTO> buscarPorCliente(String cliente) {
        if ("TODOS".equalsIgnoreCase(cliente)) {
            return buscarTodas();
        }

        return contaReceberRepository.findByClienteNomeIgnoreCase(cliente)
                .stream()
                .map(ContaReceberDTO::new)
                .collect(Collectors.toList());
    }

    public Pagamento buscarPagamento(Long id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    @Transactional
    public List<ContaReceberDTO> filtrar(String cliente, String status) {

        List<ContaReceber> contas = contaReceberRepository.buscarComFiltro(cliente);

        // Filtrar status manualmente
        if (status != null && !status.equals("TODOS")) {

            if (status.equals("ABERTO")) {
                contas = contas.stream()
                        .filter(c -> c.getSaldoDevedor().compareTo(BigDecimal.ZERO) > 0)
                        .toList();
            }

            if (status.equals("QUITADO")) {
                contas = contas.stream()
                        .filter(c -> c.getSaldoDevedor().compareTo(BigDecimal.ZERO) == 0)
                        .toList();
            }
        }

        return contas.stream()
                .map(ContaReceberDTO::new)
                .toList();
    }

    public List<ContaReceberDTO> relatorioFinanceiro(String status) {

        return contaReceberRepository.buscarRelatorioCompleto()
                .stream()
                .filter(c -> {
                    if (status == null || status.isBlank())
                        return true;
                    if ("ABERTO".equalsIgnoreCase(status))
                        return c.getSaldoDevedor().compareTo(BigDecimal.ZERO) > 0;
                    if ("QUITADO".equalsIgnoreCase(status))
                        return c.getSaldoDevedor().compareTo(BigDecimal.ZERO) == 0;
                    return true;
                })
                .map(ContaReceberDTO::new)
                .collect(Collectors.toList());
    }

    // =========================
    // BAIXA FINANCEIRA
    // =========================
    @Transactional
    public void darBaixa(Long id, BigDecimal valor, String data, FormaPagamento formaPagamento, MultipartFile anexo) {

        ContaReceber conta = contaReceberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        validarValor(valor, conta);

        Pagamento pagamento = new Pagamento();
        pagamento.setContaReceber(conta);
        pagamento.setValorPago(valor);
        pagamento.setDataPagamento(LocalDate.parse(data));
        pagamento.setFormaPagamento(formaPagamento);

        if (anexo != null && !anexo.isEmpty()) {
            processarAnexo(anexo, pagamento);
        }

        conta.getPagamentos().add(pagamento);

        // Atualiza saldo corretamente
        conta.setValorPago(conta.getValorPago().add(valor));
        conta.setSaldoDevedor(conta.getValorOriginal().subtract(conta.getValorPago()));
        if (conta.getSaldoDevedor().compareTo(BigDecimal.ZERO) < 0) {
            conta.setSaldoDevedor(BigDecimal.ZERO);
        }

        contaReceberRepository.save(conta);
    }

    private void validarValor(BigDecimal valor, ContaReceber conta) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor inválido");
        }
        if (valor.compareTo(conta.getSaldoDevedor()) > 0) {
            throw new RuntimeException("Valor maior que o saldo devedor");
        }
    }

    // =========================
    // MÉTODOS AUXILIARES
    // =========================
    private void processarAnexo(MultipartFile anexo, Pagamento pagamento) {

        String nomeOriginal = anexo.getOriginalFilename();
        if (nomeOriginal == null)
            throw new RuntimeException("Arquivo inválido");

        String nomeLower = nomeOriginal.toLowerCase();
        boolean permitido = EXTENSOES_PERMITIDAS.stream().anyMatch(nomeLower::endsWith);
        if (!permitido)
            throw new RuntimeException("Tipo de arquivo não permitido");

        try {
            Path pasta = Paths.get(DIRETORIO_UPLOAD);
            if (!Files.exists(pasta))
                Files.createDirectories(pasta);

            String nomeArquivo = UUID.randomUUID() + "_" + nomeOriginal;
            Path destino = pasta.resolve(nomeArquivo);

            Files.copy(anexo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            pagamento.setAnexoNome(nomeOriginal);
            pagamento.setAnexoTipo(anexo.getContentType());
            pagamento.setAnexoPath(destino.toAbsolutePath().toString());

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar anexo", e);
        }
    }
}
