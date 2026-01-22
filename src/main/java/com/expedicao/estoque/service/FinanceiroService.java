package com.expedicao.estoque.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.expedicao.estoque.model.ContaReceber;
import com.expedicao.estoque.model.FormaPagamento;
import com.expedicao.estoque.model.Pagamento;
import com.expedicao.estoque.repositorie.ContaReceberRepository;
import com.expedicao.estoque.repositorie.PagamentoRepository;

import jakarta.transaction.Transactional;

@Service
public class FinanceiroService {

    private static final String DIRETORIO_UPLOAD = "uploads/financeiro";
    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of(".pdf", ".jpg", ".jpeg", ".png");

    @Autowired
    private ContaReceberRepository contaReceberRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    /*
     * =========================
     * CONSULTAS
     * ==========================
     */

    public List<String> listarClientes() {
        return contaReceberRepository.listarClientes();
    }

    public List<ContaReceber> buscarTodas() {
        return contaReceberRepository.findAll();
    }

    public List<ContaReceber> buscarPorCliente(String cliente) {
        return contaReceberRepository.findAll()
                .stream()
                .filter(c -> c.getClienteNome().equalsIgnoreCase(cliente))
                .toList();
    }

    public Pagamento buscarPagamento(Long id) {
        return pagamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
    }

    public List<ContaReceber> relatorioFinanceiro(String status) {

        List<ContaReceber> contas = contaReceberRepository.findAll();

        if (status == null || status.isBlank()) {
            return contas;
        }

        return contas.stream()
                .filter(c -> {
                    if ("ABERTO".equalsIgnoreCase(status)) {
                        return c.getSaldoDevedor() > 0;
                    }
                    if ("QUITADO".equalsIgnoreCase(status)) {
                        return c.getSaldoDevedor() == 0;
                    }
                    return true;
                })
                .toList();
    }

    /*
     * =========================
     * BAIXA FINANCEIRA
     * ==========================
     */

    @Transactional
    public void darBaixa(
            Long id,
            Double valor,
            String data,
            FormaPagamento formaPagamento,
            MultipartFile anexo) {

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
        conta.setValorPago(conta.getValorPago() + valor);
        conta.setSaldoDevedor(conta.getSaldoDevedor() - valor);

        contaReceberRepository.save(conta);
    }

    /*
     * =========================
     * MÉTODOS AUXILIARES
     * ==========================
     */

    private void validarValor(Double valor, ContaReceber conta) {
        if (valor == null || valor <= 0) {
            throw new RuntimeException("Valor inválido");
        }
        if (valor > conta.getSaldoDevedor()) {
            throw new RuntimeException("Valor maior que o saldo devedor");
        }
    }

    private void processarAnexo(MultipartFile anexo, Pagamento pagamento) {

        String nomeOriginal = anexo.getOriginalFilename();
        if (nomeOriginal == null) {
            throw new RuntimeException("Arquivo inválido");
        }

        String nomeLower = nomeOriginal.toLowerCase();
        boolean permitido = EXTENSOES_PERMITIDAS.stream()
                .anyMatch(nomeLower::endsWith);

        if (!permitido) {
            throw new RuntimeException("Tipo de arquivo não permitido");
        }

        try {
            Path pasta = Paths.get(DIRETORIO_UPLOAD);

            if (!Files.exists(pasta)) {
                Files.createDirectories(pasta);
            }

            String nomeArquivo = UUID.randomUUID() + "_" + nomeOriginal;
            Path destino = pasta.resolve(nomeArquivo);

            Files.copy(
                    anexo.getInputStream(),
                    destino,
                    StandardCopyOption.REPLACE_EXISTING);

            pagamento.setAnexoNome(nomeOriginal);
            pagamento.setAnexoTipo(anexo.getContentType());
            pagamento.setAnexoPath(destino.toAbsolutePath().toString());

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar anexo", e);
        }
    }
}
