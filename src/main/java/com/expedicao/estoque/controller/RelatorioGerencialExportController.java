package com.expedicao.estoque.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expedicao.estoque.dto.ContaReceberDTO;
import com.expedicao.estoque.dto.VendaClienteResumoDTO;
import com.expedicao.estoque.model.FormaPagamento;
import com.expedicao.estoque.model.TipoMovimentacao;
import com.expedicao.estoque.model.VendaItem;
import com.expedicao.estoque.repositorie.VendaItemRepository;
import com.expedicao.estoque.service.FinanceiroService;
import com.expedicao.estoque.service.RelatorioArquivoService;

@RestController
public class RelatorioGerencialExportController {

    private final VendaItemRepository vendaItemRepository;
    private final FinanceiroService financeiroService;
    private final RelatorioArquivoService arquivoService;

    public RelatorioGerencialExportController(VendaItemRepository vendaItemRepository,
            FinanceiroService financeiroService, RelatorioArquivoService arquivoService) {
        this.vendaItemRepository = vendaItemRepository;
        this.financeiroService = financeiroService;
        this.arquivoService = arquivoService;
    }

    @GetMapping("/relatorios/vendas/exportar/excel")
    public ResponseEntity<byte[]> vendasExcel(
            @RequestParam(required = false) String pedido,
            @RequestParam(required = false) String produto,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Boolean notaFiscal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim)
            throws Exception {
        List<VendaItem> itens = vendasFiltradas(pedido, produto, cliente, estado, tipo,
                notaFiscal, dataInicio, dataFim);
        byte[] arquivo = arquivoService.vendasExcel(agruparVendas(itens),
                filtrosVendas(pedido, produto, cliente, estado, tipo, notaFiscal, dataInicio, dataFim));
        return download(arquivo, "relatorio-vendas.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/relatorios/vendas/exportar/pdf")
    public ResponseEntity<byte[]> vendasPdf(
            @RequestParam(required = false) String pedido,
            @RequestParam(required = false) String produto,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Boolean notaFiscal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim)
            throws Exception {
        List<VendaItem> itens = vendasFiltradas(pedido, produto, cliente, estado, tipo,
                notaFiscal, dataInicio, dataFim);
        byte[] arquivo = arquivoService.vendasPdf(agruparVendas(itens),
                filtrosVendas(pedido, produto, cliente, estado, tipo, notaFiscal, dataInicio, dataFim));
        return download(arquivo, "relatorio-vendas.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/financeiro/relatorio/exportar/excel")
    public ResponseEntity<byte[]> financeiroExcel(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) FormaPagamento formaPagamento) throws Exception {
        List<ContaReceberDTO> contas = financeiroService.relatorioFinanceiro(status, cliente, formaPagamento);
        byte[] arquivo = arquivoService.financeiroExcel(contas,
                filtrosFinanceiro(status, cliente, formaPagamento));
        return download(arquivo, "relatorio-financeiro.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/financeiro/relatorio/exportar/pdf")
    public ResponseEntity<byte[]> financeiroPdf(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) FormaPagamento formaPagamento) throws Exception {
        List<ContaReceberDTO> contas = financeiroService.relatorioFinanceiro(status, cliente, formaPagamento);
        byte[] arquivo = arquivoService.financeiroPdf(contas,
                filtrosFinanceiro(status, cliente, formaPagamento));
        return download(arquivo, "relatorio-financeiro.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    private List<VendaItem> vendasFiltradas(String pedido, String produto, String cliente, String estado,
            String tipo, Boolean notaFiscal, LocalDate dataInicio, LocalDate dataFim) {
        Long pedidoId = pedido == null || pedido.isBlank() ? null : Long.valueOf(pedido);
        String tipoNome = tipo == null || tipo.isBlank() ? null : TipoMovimentacao.valueOf(tipo).name();
        return vendaItemRepository.filtrarTodos(pedidoId, vazioNulo(produto), vazioNulo(cliente),
                vazioNulo(estado), tipoNome, notaFiscal,
                dataInicio == null ? LocalDate.of(1900, 1, 1) : dataInicio,
                dataFim == null ? LocalDate.of(2999, 12, 31) : dataFim);
    }

    private List<VendaClienteResumoDTO> agruparVendas(List<VendaItem> itens) {
        Map<String, List<VendaItem>> grupos = new LinkedHashMap<>();
        Map<String, String> nomes = new LinkedHashMap<>();
        for (VendaItem item : itens) {
            String nome = item.getVenda().getClienteNome();
            if (nome == null || nome.isBlank()) nome = "Cliente não informado";
            String chave = nome.trim().toLowerCase(Locale.ROOT);
            nomes.putIfAbsent(chave, nome);
            grupos.computeIfAbsent(chave, ignorada -> new ArrayList<>()).add(item);
        }
        return grupos.entrySet().stream()
                .map(entry -> new VendaClienteResumoDTO(nomes.get(entry.getKey()), entry.getValue()))
                .toList();
    }

    private ResponseEntity<byte[]> download(byte[] conteudo, String nome, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(nome, StandardCharsets.UTF_8).build());
        headers.setContentLength(conteudo.length);
        return ResponseEntity.ok().headers(headers).body(conteudo);
    }

    private String filtrosVendas(String pedido, String produto, String cliente, String estado,
            String tipo, Boolean notaFiscal, LocalDate inicio, LocalDate fim) {
        List<String> filtros = new ArrayList<>();
        adicionar(filtros, "Pedido", pedido);
        adicionar(filtros, "Produto", produto);
        adicionar(filtros, "Cliente", cliente);
        adicionar(filtros, "Estado", estado);
        adicionar(filtros, "Tipo", tipo);
        if (notaFiscal != null) adicionar(filtros, "NF", notaFiscal ? "Com NF" : "Sem NF");
        if (inicio != null) adicionar(filtros, "De", inicio.toString());
        if (fim != null) adicionar(filtros, "Até", fim.toString());
        return filtros.isEmpty() ? "Todos os registros" : String.join(" | ", filtros);
    }

    private String filtrosFinanceiro(String status, String cliente, FormaPagamento forma) {
        List<String> filtros = new ArrayList<>();
        adicionar(filtros, "Status", status);
        adicionar(filtros, "Cliente", cliente);
        adicionar(filtros, "Forma", forma == null ? null : forma.name().replace('_', ' '));
        return filtros.isEmpty() ? "Todos os registros" : String.join(" | ", filtros);
    }

    private void adicionar(List<String> filtros, String nome, String valor) {
        if (valor != null && !valor.isBlank()) filtros.add(nome + ": " + valor);
    }

    private String vazioNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor;
    }
}
