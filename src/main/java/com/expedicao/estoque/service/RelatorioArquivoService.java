package com.expedicao.estoque.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.expedicao.estoque.dto.ContaReceberDTO;
import com.expedicao.estoque.dto.PagamentoDTO;
import com.expedicao.estoque.dto.VendaClienteResumoDTO;
import com.expedicao.estoque.model.VendaItem;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class RelatorioArquivoService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final BaseColor AZUL_ESCURO = new BaseColor(30, 41, 59);
    private static final BaseColor AZUL = new BaseColor(79, 70, 229);
    private static final BaseColor CINZA_CLARO = new BaseColor(241, 245, 249);
    private static final BaseColor CINZA_TEXTO = new BaseColor(71, 85, 105);
    private static final BaseColor VERDE = new BaseColor(5, 150, 105);
    private static final BaseColor VERMELHO = new BaseColor(220, 38, 38);

    public byte[] vendasExcel(List<VendaClienteResumoDTO> clientes, String filtros) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ExcelEstilos estilos = criarEstilos(workbook);
            Sheet resumo = workbook.createSheet("Resumo por Cliente");
            Sheet detalhes = workbook.createSheet("Pedidos e Itens");

            criarCabecalhoExcel(resumo, "RELATÓRIO DE VENDAS", filtros, 9, estilos);
            String[] colunasResumo = {"Cliente", "Pedidos", "Itens", "Quantidade", "Valor Total",
                    "Estado", "Última Venda", "Tipo", "Nota Fiscal"};
            criarLinhaCabecalho(resumo, 6, colunasResumo, estilos);

            int linha = 7;
            BigDecimal valorGeral = BigDecimal.ZERO;
            long pedidosGeral = 0;
            int quantidadeGeral = 0;
            for (VendaClienteResumoDTO cliente : clientes) {
                Row row = resumo.createRow(linha++);
                texto(row, 0, cliente.getClienteNome(), estilos.texto());
                numero(row, 1, cliente.getTotalPedidos(), estilos.inteiro());
                numero(row, 2, cliente.getTotalItens(), estilos.inteiro());
                numero(row, 3, cliente.getQuantidadeTotal(), estilos.inteiro());
                decimal(row, 4, cliente.getValorTotal(), estilos.moeda());
                texto(row, 5, cliente.getClienteEstado(), estilos.centro());
                data(row, 6, cliente.getUltimaVenda(), estilos.data());
                texto(row, 7, cliente.getTipo(), estilos.centro());
                texto(row, 8, cliente.getNotaFiscal(), estilos.centro());
                aplicarFaixa(row, linha, 9, estilos);
                valorGeral = valorGeral.add(zero(cliente.getValorTotal()));
                pedidosGeral += cliente.getTotalPedidos();
                quantidadeGeral += cliente.getQuantidadeTotal();
            }
            criarTotaisExcel(resumo, linha, "TOTAIS", pedidosGeral, clientes.size(), quantidadeGeral,
                    valorGeral, estilos);
            configurarPlanilha(resumo, 6, linha, new int[]{28, 12, 12, 15, 18, 12, 16, 18, 15});

            criarCabecalhoExcel(detalhes, "DETALHAMENTO DE PEDIDOS E ITENS", filtros, 11, estilos);
            String[] colunasDetalhe = {"Cliente", "Pedido", "Produto", "Quantidade", "Valor Unitário",
                    "Subtotal", "Estado", "Data", "Tipo", "NF", "Bonificação"};
            criarLinhaCabecalho(detalhes, 6, colunasDetalhe, estilos);
            linha = 7;
            for (VendaClienteResumoDTO cliente : clientes) {
                for (VendaItem item : cliente.getItens()) {
                    Row row = detalhes.createRow(linha++);
                    texto(row, 0, cliente.getClienteNome(), estilos.texto());
                    numero(row, 1, item.getVenda().getId(), estilos.inteiro());
                    texto(row, 2, item.getProduto().getNome(), estilos.texto());
                    numero(row, 3, item.getQuantidade(), estilos.inteiro());
                    decimal(row, 4, item.getValorPorCaixa(), estilos.moeda());
                    decimal(row, 5, item.getSubtotal(), estilos.moeda());
                    texto(row, 6, item.getVenda().getClienteEstado(), estilos.centro());
                    data(row, 7, item.getVenda().getDataSaida(), estilos.data());
                    texto(row, 8, item.getTipoMovimentacao() != null
                            && "V".equals(item.getTipoMovimentacao().name()) ? "Venda" : "Transferência", estilos.centro());
                    texto(row, 9, Boolean.TRUE.equals(item.getVenda().getComNotaFiscal()) ? "Sim" : "Não", estilos.centro());
                    texto(row, 10, Boolean.TRUE.equals(item.getBonificacao()) ? "Sim" : "Não", estilos.centro());
                    aplicarFaixa(row, linha, 11, estilos);
                }
            }
            configurarPlanilha(detalhes, 6, linha, new int[]{28, 12, 28, 14, 18, 18, 12, 15, 18, 10, 14});

            workbook.write(output);
            return output.toByteArray();
        }
    }

    public byte[] financeiroExcel(List<ContaReceberDTO> contas, String filtros) throws Exception {
        List<FinanceiroResumo> clientes = agruparFinanceiro(contas);
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ExcelEstilos estilos = criarEstilos(workbook);
            Sheet resumo = workbook.createSheet("Resumo por Cliente");
            Sheet detalhes = workbook.createSheet("Pedidos e Baixas");

            criarCabecalhoExcel(resumo, "RELATÓRIO FINANCEIRO", filtros, 7, estilos);
            criarLinhaCabecalho(resumo, 6,
                    new String[]{"Cliente", "Pedidos", "Valor Original", "Valor Pago", "Saldo", "Status", "Último Pedido"},
                    estilos);
            int linha = 7;
            BigDecimal original = BigDecimal.ZERO;
            BigDecimal pago = BigDecimal.ZERO;
            BigDecimal saldo = BigDecimal.ZERO;
            long pedidos = 0;
            for (FinanceiroResumo cliente : clientes) {
                Row row = resumo.createRow(linha++);
                texto(row, 0, cliente.cliente(), estilos.texto());
                numero(row, 1, cliente.contas().size(), estilos.inteiro());
                decimal(row, 2, cliente.original(), estilos.moeda());
                decimal(row, 3, cliente.pago(), estilos.moeda());
                decimal(row, 4, cliente.saldo(), estilos.moeda());
                texto(row, 5, cliente.saldo().signum() > 0 ? "Em Aberto" : "Quitado",
                        cliente.saldo().signum() > 0 ? estilos.alerta() : estilos.sucesso());
                data(row, 6, cliente.ultimoPedido(), estilos.data());
                aplicarFaixa(row, linha, 7, estilos);
                original = original.add(cliente.original());
                pago = pago.add(cliente.pago());
                saldo = saldo.add(cliente.saldo());
                pedidos += cliente.contas().size();
            }
            Row total = resumo.createRow(linha);
            texto(total, 0, "TOTAIS", estilos.total());
            numero(total, 1, pedidos, estilos.totalInteiro());
            decimal(total, 2, original, estilos.totalMoeda());
            decimal(total, 3, pago, estilos.totalMoeda());
            decimal(total, 4, saldo, estilos.totalMoeda());
            for (int i = 5; i < 7; i++) vazio(total, i, estilos.total());
            configurarPlanilha(resumo, 6, linha, new int[]{30, 12, 20, 20, 20, 16, 16});

            criarCabecalhoExcel(detalhes, "DETALHAMENTO DE PEDIDOS E BAIXAS", filtros, 10, estilos);
            criarLinhaCabecalho(detalhes, 6,
                    new String[]{"Cliente", "Data Pedido", "Valor Original", "Pago no Pedido", "Saldo",
                            "Status", "Valor Baixa", "Data Pagamento", "Forma", "Anexo"},
                    estilos);
            linha = 7;
            for (ContaReceberDTO conta : contas) {
                List<PagamentoDTO> pagamentos = conta.getPagamentos() == null ? List.of() : conta.getPagamentos();
                if (pagamentos.isEmpty()) {
                    linhaFinanceiro(detalhes.createRow(linha++), conta, null, estilos);
                } else {
                    for (PagamentoDTO pagamento : pagamentos) {
                        linhaFinanceiro(detalhes.createRow(linha++), conta, pagamento, estilos);
                    }
                }
            }
            configurarPlanilha(detalhes, 6, linha,
                    new int[]{30, 16, 20, 20, 20, 16, 18, 18, 18, 12});
            workbook.write(output);
            return output.toByteArray();
        }
    }

    public byte[] vendasPdf(List<VendaClienteResumoDTO> clientes, String filtros) throws Exception {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document documento = novoDocumento(output, "Relatório de Vendas");
            adicionarTituloPdf(documento, "RELATÓRIO DE VENDAS", filtros);
            adicionarResumoVendasPdf(documento, clientes);
            documento.newPage();
            adicionarSubtituloPdf(documento, "Detalhamento de pedidos e itens");
            PdfPTable tabela = tabelaPdf(new float[]{1.4f, .7f, 1.8f, .8f, 1.1f, 1.1f, .7f, .9f, 1f, .6f},
                    "Cliente", "Pedido", "Produto", "Qtd", "Valor Unit.", "Subtotal", "UF", "Data", "Tipo", "NF");
            for (VendaClienteResumoDTO cliente : clientes) {
                for (VendaItem item : cliente.getItens()) {
                    celula(tabela, cliente.getClienteNome(), Element.ALIGN_LEFT);
                    celula(tabela, String.valueOf(item.getVenda().getId()), Element.ALIGN_CENTER);
                    celula(tabela, item.getProduto().getNome(), Element.ALIGN_LEFT);
                    celula(tabela, String.valueOf(item.getQuantidade()), Element.ALIGN_RIGHT);
                    celula(tabela, moeda(item.getValorPorCaixa()), Element.ALIGN_RIGHT);
                    celula(tabela, moeda(item.getSubtotal()), Element.ALIGN_RIGHT);
                    celula(tabela, item.getVenda().getClienteEstado(), Element.ALIGN_CENTER);
                    celula(tabela, formatar(item.getVenda().getDataSaida()), Element.ALIGN_CENTER);
                    celula(tabela, "V".equals(item.getTipoMovimentacao().name()) ? "Venda" : "Transferência",
                            Element.ALIGN_CENTER);
                    celula(tabela, Boolean.TRUE.equals(item.getVenda().getComNotaFiscal()) ? "Sim" : "Não",
                            Element.ALIGN_CENTER);
                }
            }
            documento.add(tabela);
            documento.close();
            return output.toByteArray();
        }
    }

    public byte[] financeiroPdf(List<ContaReceberDTO> contas, String filtros) throws Exception {
        List<FinanceiroResumo> clientes = agruparFinanceiro(contas);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document documento = novoDocumento(output, "Relatório Financeiro");
            adicionarTituloPdf(documento, "RELATÓRIO FINANCEIRO", filtros);
            adicionarResumoFinanceiroPdf(documento, clientes);
            documento.newPage();
            adicionarSubtituloPdf(documento, "Detalhamento de pedidos e baixas");
            PdfPTable tabela = tabelaPdf(new float[]{1.5f, .9f, 1.15f, 1.15f, 1.15f, .9f, 1.05f, 1f, 1f},
                    "Cliente", "Pedido", "Original", "Pago", "Saldo", "Status", "Baixa", "Pagamento", "Forma");
            for (ContaReceberDTO conta : contas) {
                List<PagamentoDTO> pagamentos = conta.getPagamentos() == null ? List.of() : conta.getPagamentos();
                if (pagamentos.isEmpty()) {
                    linhaFinanceiroPdf(tabela, conta, null);
                } else {
                    pagamentos.forEach(pagamento -> linhaFinanceiroPdf(tabela, conta, pagamento));
                }
            }
            documento.add(tabela);
            documento.close();
            return output.toByteArray();
        }
    }

    private ExcelEstilos criarEstilos(XSSFWorkbook workbook) {
        Font tituloFont = workbook.createFont();
        tituloFont.setBold(true);
        tituloFont.setFontHeightInPoints((short) 20);
        tituloFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle titulo = workbook.createCellStyle();
        titulo.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
        titulo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titulo.setFont(tituloFont);
        titulo.setAlignment(HorizontalAlignment.LEFT);
        titulo.setVerticalAlignment(VerticalAlignment.CENTER);

        Font subtituloFont = workbook.createFont();
        subtituloFont.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        subtituloFont.setItalic(true);
        CellStyle subtitulo = workbook.createCellStyle();
        subtitulo.setFont(subtituloFont);

        Font cabecalhoFont = workbook.createFont();
        cabecalhoFont.setBold(true);
        cabecalhoFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle cabecalho = workbook.createCellStyle();
        cabecalho.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        cabecalho.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cabecalho.setFont(cabecalhoFont);
        cabecalho.setAlignment(HorizontalAlignment.CENTER);
        cabecalho.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle texto = base(workbook);
        CellStyle centro = base(workbook);
        centro.setAlignment(HorizontalAlignment.CENTER);
        CellStyle inteiro = base(workbook);
        inteiro.setAlignment(HorizontalAlignment.RIGHT);
        inteiro.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        CellStyle moeda = base(workbook);
        moeda.setAlignment(HorizontalAlignment.RIGHT);
        moeda.setDataFormat(workbook.createDataFormat().getFormat("[$R$-416] #,##0.00;[Red]([$R$-416] #,##0.00);-"));
        CellStyle data = base(workbook);
        data.setAlignment(HorizontalAlignment.CENTER);
        data.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));

        CellStyle faixa = base(workbook);
        faixa.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        faixa.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle alerta = copiar(workbook, centro);
        alerta.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        alerta.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        CellStyle sucesso = copiar(workbook, centro);
        sucesso.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        sucesso.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font totalFont = workbook.createFont();
        totalFont.setBold(true);
        totalFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle total = base(workbook);
        total.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        total.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        total.setFont(totalFont);
        CellStyle totalInteiro = copiar(workbook, total);
        totalInteiro.setAlignment(HorizontalAlignment.RIGHT);
        totalInteiro.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        CellStyle totalMoeda = copiar(workbook, total);
        totalMoeda.setAlignment(HorizontalAlignment.RIGHT);
        totalMoeda.setDataFormat(workbook.createDataFormat().getFormat("[$R$-416] #,##0.00;[Red]([$R$-416] #,##0.00);-"));
        return new ExcelEstilos(titulo, subtitulo, cabecalho, texto, centro, inteiro, moeda, data,
                faixa, alerta, sucesso, total, totalInteiro, totalMoeda);
    }

    private CellStyle base(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.HAIR);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }

    private CellStyle copiar(XSSFWorkbook workbook, CellStyle origem) {
        CellStyle copia = workbook.createCellStyle();
        copia.cloneStyleFrom(origem);
        return copia;
    }

    private void criarCabecalhoExcel(Sheet sheet, String titulo, String filtros, int colunas, ExcelEstilos estilos) {
        sheet.setDisplayGridlines(false);
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(34);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colunas - 1));
        texto(titleRow, 0, titulo, estilos.titulo());
        Row meta = sheet.createRow(2);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, colunas - 1));
        texto(meta, 0, "Gerado em " + LocalDateTime.now().format(DATA_HORA), estilos.subtitulo());
        Row filter = sheet.createRow(3);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 0, colunas - 1));
        texto(filter, 0, "Filtros: " + filtros, estilos.subtitulo());
    }

    private void criarLinhaCabecalho(Sheet sheet, int indice, String[] colunas, ExcelEstilos estilos) {
        Row row = sheet.createRow(indice);
        row.setHeightInPoints(25);
        for (int i = 0; i < colunas.length; i++) texto(row, i, colunas[i], estilos.cabecalho());
    }

    private void criarTotaisExcel(Sheet sheet, int linha, String label, long pedidos, int clientes,
            int quantidade, BigDecimal valor, ExcelEstilos estilos) {
        Row row = sheet.createRow(linha);
        texto(row, 0, label, estilos.total());
        numero(row, 1, pedidos, estilos.totalInteiro());
        numero(row, 2, clientes, estilos.totalInteiro());
        numero(row, 3, quantidade, estilos.totalInteiro());
        decimal(row, 4, valor, estilos.totalMoeda());
        for (int i = 5; i < 9; i++) vazio(row, i, estilos.total());
    }

    private void configurarPlanilha(Sheet sheet, int headerRow, int ultimaLinha, int[] larguras) {
        sheet.createFreezePane(0, headerRow + 1);
        if (ultimaLinha >= headerRow) {
            sheet.setAutoFilter(new CellRangeAddress(headerRow, Math.max(headerRow, ultimaLinha - 1), 0, larguras.length - 1));
        }
        for (int i = 0; i < larguras.length; i++) sheet.setColumnWidth(i, larguras[i] * 256);
        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.setFitToPage(true);
        sheet.setRepeatingRows(new CellRangeAddress(headerRow, headerRow, -1, -1));
    }

    private void aplicarFaixa(Row row, int linha, int colunas, ExcelEstilos estilos) {
        if (linha % 2 != 0) return;
        for (int i = 0; i < colunas; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellStyle() == estilos.texto()) cell.setCellStyle(estilos.faixa());
        }
    }

    private void linhaFinanceiro(Row row, ContaReceberDTO conta, PagamentoDTO pagamento, ExcelEstilos estilos) {
        texto(row, 0, conta.getClienteNome(), estilos.texto());
        data(row, 1, conta.getDataPedido(), estilos.data());
        decimal(row, 2, conta.getValorOriginal(), estilos.moeda());
        decimal(row, 3, conta.getValorPago(), estilos.moeda());
        decimal(row, 4, conta.getSaldoDevedor(), estilos.moeda());
        texto(row, 5, zero(conta.getSaldoDevedor()).signum() > 0 ? "Em Aberto" : "Quitado",
                zero(conta.getSaldoDevedor()).signum() > 0 ? estilos.alerta() : estilos.sucesso());
        decimal(row, 6, pagamento == null ? null : pagamento.getValorPago(), estilos.moeda());
        data(row, 7, pagamento == null ? null : pagamento.getDataPagamento(), estilos.data());
        texto(row, 8, pagamento == null || pagamento.getFormaPagamento() == null
                ? "-" : pagamento.getFormaPagamento().name().replace('_', ' '), estilos.centro());
        texto(row, 9, pagamento != null && pagamento.getAnexoPath() != null ? "Sim" : "Não", estilos.centro());
    }

    private void texto(Row row, int coluna, String valor, CellStyle estilo) {
        Cell cell = row.createCell(coluna);
        cell.setCellValue(valor == null ? "-" : valor);
        cell.setCellStyle(estilo);
    }

    private void numero(Row row, int coluna, Number valor, CellStyle estilo) {
        Cell cell = row.createCell(coluna);
        cell.setCellValue(valor == null ? 0 : valor.doubleValue());
        cell.setCellStyle(estilo);
    }

    private void vazio(Row row, int coluna, CellStyle estilo) {
        Cell cell = row.createCell(coluna);
        cell.setBlank();
        cell.setCellStyle(estilo);
    }

    private void decimal(Row row, int coluna, BigDecimal valor, CellStyle estilo) {
        Cell cell = row.createCell(coluna);
        if (valor == null) cell.setBlank(); else cell.setCellValue(valor.doubleValue());
        cell.setCellStyle(estilo);
    }

    private void data(Row row, int coluna, LocalDate valor, CellStyle estilo) {
        Cell cell = row.createCell(coluna);
        if (valor == null) cell.setBlank(); else cell.setCellValue(valor);
        cell.setCellStyle(estilo);
    }

    private Document novoDocumento(ByteArrayOutputStream output, String titulo) throws Exception {
        Document documento = new Document(PageSize.A4.rotate(), 28, 28, 34, 34);
        PdfWriter writer = PdfWriter.getInstance(documento, output);
        writer.setPageEvent(new RodapePdf(titulo));
        documento.open();
        return documento;
    }

    private void adicionarTituloPdf(Document documento, String titulo, String filtros) throws Exception {
        Paragraph heading = new Paragraph(titulo,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 19, BaseColor.WHITE));
        PdfPTable faixa = new PdfPTable(1);
        faixa.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(heading);
        cell.setBackgroundColor(AZUL);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(14);
        faixa.addCell(cell);
        documento.add(faixa);
        Paragraph meta = new Paragraph("Gerado em " + LocalDateTime.now().format(DATA_HORA)
                + "  |  Filtros: " + filtros,
                FontFactory.getFont(FontFactory.HELVETICA, 8, CINZA_TEXTO));
        meta.setSpacingBefore(7);
        meta.setSpacingAfter(12);
        documento.add(meta);
    }

    private void adicionarSubtituloPdf(Document documento, String texto) throws Exception {
        Paragraph p = new Paragraph(texto,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, AZUL_ESCURO));
        p.setSpacingAfter(8);
        documento.add(p);
    }

    private void adicionarResumoVendasPdf(Document documento, List<VendaClienteResumoDTO> clientes) throws Exception {
        adicionarSubtituloPdf(documento, "Resumo consolidado por cliente");
        PdfPTable tabela = tabelaPdf(new float[]{2f, .7f, .7f, .9f, 1.3f, .7f, 1f, 1f, .8f},
                "Cliente", "Pedidos", "Itens", "Qtd", "Valor Total", "UF", "Última Venda", "Tipo", "NF");
        for (VendaClienteResumoDTO cliente : clientes) {
            celula(tabela, cliente.getClienteNome(), Element.ALIGN_LEFT);
            celula(tabela, String.valueOf(cliente.getTotalPedidos()), Element.ALIGN_RIGHT);
            celula(tabela, String.valueOf(cliente.getTotalItens()), Element.ALIGN_RIGHT);
            celula(tabela, String.valueOf(cliente.getQuantidadeTotal()), Element.ALIGN_RIGHT);
            celula(tabela, moeda(cliente.getValorTotal()), Element.ALIGN_RIGHT);
            celula(tabela, cliente.getClienteEstado(), Element.ALIGN_CENTER);
            celula(tabela, formatar(cliente.getUltimaVenda()), Element.ALIGN_CENTER);
            celula(tabela, cliente.getTipo(), Element.ALIGN_CENTER);
            celula(tabela, cliente.getNotaFiscal(), Element.ALIGN_CENTER);
        }
        documento.add(tabela);
    }

    private void adicionarResumoFinanceiroPdf(Document documento, List<FinanceiroResumo> clientes) throws Exception {
        adicionarSubtituloPdf(documento, "Resumo consolidado por cliente");
        PdfPTable tabela = tabelaPdf(new float[]{2.2f, .8f, 1.3f, 1.3f, 1.3f, 1f, 1f},
                "Cliente", "Pedidos", "Original", "Pago", "Saldo", "Status", "Último Pedido");
        for (FinanceiroResumo cliente : clientes) {
            celula(tabela, cliente.cliente(), Element.ALIGN_LEFT);
            celula(tabela, String.valueOf(cliente.contas().size()), Element.ALIGN_RIGHT);
            celula(tabela, moeda(cliente.original()), Element.ALIGN_RIGHT);
            celula(tabela, moeda(cliente.pago()), Element.ALIGN_RIGHT);
            celula(tabela, moeda(cliente.saldo()), Element.ALIGN_RIGHT);
            celula(tabela, cliente.saldo().signum() > 0 ? "Em Aberto" : "Quitado", Element.ALIGN_CENTER);
            celula(tabela, formatar(cliente.ultimoPedido()), Element.ALIGN_CENTER);
        }
        documento.add(tabela);
    }

    private PdfPTable tabelaPdf(float[] larguras, String... colunas) throws Exception {
        PdfPTable tabela = new PdfPTable(larguras);
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1);
        tabela.setSpacingAfter(10);
        for (String coluna : colunas) {
            PdfPCell cell = new PdfPCell(new Phrase(coluna,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, BaseColor.WHITE)));
            cell.setBackgroundColor(AZUL_ESCURO);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderColor(AZUL_ESCURO);
            tabela.addCell(cell);
        }
        return tabela;
    }

    private void celula(PdfPTable tabela, String texto, int alinhamento) {
        PdfPCell cell = new PdfPCell(new Phrase(texto == null ? "-" : texto,
                FontFactory.getFont(FontFactory.HELVETICA, 7.2f, CINZA_TEXTO)));
        cell.setPadding(5);
        cell.setHorizontalAlignment(alinhamento);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(CINZA_CLARO);
        cell.setBackgroundColor(tabela.size() % (tabela.getNumberOfColumns() * 2)
                < tabela.getNumberOfColumns() ? BaseColor.WHITE : CINZA_CLARO);
        tabela.addCell(cell);
    }

    private void linhaFinanceiroPdf(PdfPTable tabela, ContaReceberDTO conta, PagamentoDTO pagamento) {
        celula(tabela, conta.getClienteNome(), Element.ALIGN_LEFT);
        celula(tabela, formatar(conta.getDataPedido()), Element.ALIGN_CENTER);
        celula(tabela, moeda(conta.getValorOriginal()), Element.ALIGN_RIGHT);
        celula(tabela, moeda(conta.getValorPago()), Element.ALIGN_RIGHT);
        celula(tabela, moeda(conta.getSaldoDevedor()), Element.ALIGN_RIGHT);
        celula(tabela, zero(conta.getSaldoDevedor()).signum() > 0 ? "Em Aberto" : "Quitado", Element.ALIGN_CENTER);
        celula(tabela, pagamento == null ? "-" : moeda(pagamento.getValorPago()), Element.ALIGN_RIGHT);
        celula(tabela, pagamento == null ? "-" : formatar(pagamento.getDataPagamento()), Element.ALIGN_CENTER);
        celula(tabela, pagamento == null || pagamento.getFormaPagamento() == null
                ? "-" : pagamento.getFormaPagamento().name().replace('_', ' '), Element.ALIGN_CENTER);
    }

    private List<FinanceiroResumo> agruparFinanceiro(List<ContaReceberDTO> contas) {
        Map<String, List<ContaReceberDTO>> grupos = new LinkedHashMap<>();
        Map<String, String> nomes = new LinkedHashMap<>();
        for (ContaReceberDTO conta : contas) {
            String nome = conta.getClienteNome() == null || conta.getClienteNome().isBlank()
                    ? "Cliente não informado" : conta.getClienteNome();
            String chave = nome.trim().toLowerCase(PT_BR);
            nomes.putIfAbsent(chave, nome);
            grupos.computeIfAbsent(chave, ignorada -> new ArrayList<>()).add(conta);
        }
        return grupos.entrySet().stream().map(entry -> {
            List<ContaReceberDTO> lista = entry.getValue();
            return new FinanceiroResumo(nomes.get(entry.getKey()), lista,
                    somar(lista, ContaReceberDTO::getValorOriginal),
                    somar(lista, ContaReceberDTO::getValorPago),
                    somar(lista, ContaReceberDTO::getSaldoDevedor),
                    lista.stream().map(ContaReceberDTO::getDataPedido).filter(Objects::nonNull)
                            .max(LocalDate::compareTo).orElse(null));
        }).toList();
    }

    private BigDecimal somar(List<ContaReceberDTO> contas,
            java.util.function.Function<ContaReceberDTO, BigDecimal> campo) {
        return contas.stream().map(campo).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(zero(valor));
    }

    private String formatar(LocalDate data) {
        return data == null ? "-" : data.format(DATA);
    }

    private BigDecimal zero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private record FinanceiroResumo(String cliente, List<ContaReceberDTO> contas,
            BigDecimal original, BigDecimal pago, BigDecimal saldo, LocalDate ultimoPedido) {}

    private record ExcelEstilos(CellStyle titulo, CellStyle subtitulo, CellStyle cabecalho,
            CellStyle texto, CellStyle centro, CellStyle inteiro, CellStyle moeda, CellStyle data,
            CellStyle faixa, CellStyle alerta, CellStyle sucesso, CellStyle total,
            CellStyle totalInteiro, CellStyle totalMoeda) {}

    private static class RodapePdf extends PdfPageEventHelper {
        private final String titulo;

        RodapePdf(String titulo) {
            this.titulo = titulo;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(2);
            try {
                footer.setWidths(new float[]{4, 1});
                footer.setTotalWidth(document.right() - document.left());
                footer.getDefaultCell().setBorder(Rectangle.TOP);
                footer.getDefaultCell().setBorderColor(CINZA_CLARO);
                footer.getDefaultCell().setPaddingTop(5);
                footer.addCell(new Phrase("Sistema V - " + titulo,
                        FontFactory.getFont(FontFactory.HELVETICA, 7, CINZA_TEXTO)));
                PdfPCell pagina = new PdfPCell(new Phrase("Página " + writer.getPageNumber(),
                        FontFactory.getFont(FontFactory.HELVETICA, 7, CINZA_TEXTO)));
                pagina.setBorder(Rectangle.TOP);
                pagina.setBorderColor(CINZA_CLARO);
                pagina.setHorizontalAlignment(Element.ALIGN_RIGHT);
                pagina.setPaddingTop(5);
                footer.addCell(pagina);
                footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 8, writer.getDirectContent());
            } catch (Exception ignored) {
                // O rodapé não deve impedir a geração do relatório.
            }
        }
    }
}
