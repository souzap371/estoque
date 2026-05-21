package com.expedicao.estoque.controller;

import com.expedicao.estoque.model.ContaPagar;
import com.expedicao.estoque.repositorie.ContaPagarRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.List;

@Controller
public class RelatorioExportController {

    @Autowired
    private ContaPagarRepository repository;

    // =========================
    // EXPORTAR EXCEL
    // =========================
    @GetMapping("/financeiro/exportar/excel")
    public void exportarExcel(
            HttpServletResponse response) throws IOException {

        response.setContentType(
                "application/octet-stream");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=relatorio-financeiro.xlsx");

        List<ContaPagar> contas =
                repository.findAll();

        XSSFWorkbook workbook =
                new XSSFWorkbook();

        XSSFSheet sheet =
                workbook.createSheet("Financeiro");

        int rowNum = 0;

        Row header =
                sheet.createRow(rowNum++);

        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Descrição");
        header.createCell(2).setCellValue("Valor");
        header.createCell(3).setCellValue("Valor Pago");
        header.createCell(4).setCellValue("Saldo");
        header.createCell(5).setCellValue("Status");

        for (ContaPagar conta : contas) {

            Row row = sheet.createRow(rowNum++);

            row.createCell(0)
                    .setCellValue(conta.getId());

            row.createCell(1)
                    .setCellValue(conta.getDescricao());

            row.createCell(2)
                    .setCellValue(
                            conta.getValor() != null
                                    ? conta.getValor().doubleValue()
                                    : 0
                    );

            row.createCell(3)
                    .setCellValue(
                            conta.getValorPago() != null
                                    ? conta.getValorPago().doubleValue()
                                    : 0
                    );

            row.createCell(4)
                    .setCellValue(
                            conta.getSaldoDevedor() != null
                                    ? conta.getSaldoDevedor().doubleValue()
                                    : 0
                    );

            row.createCell(5)
                    .setCellValue(
                            conta.getStatus().name()
                    );
        }

        workbook.write(response.getOutputStream());

        workbook.close();
    }

    // =========================
    // EXPORTAR PDF
    // =========================
    @GetMapping("/financeiro/exportar/pdf")
    public void exportarPDF(
            HttpServletResponse response) throws Exception {

        response.setContentType("application/pdf");

        response.setHeader(
                "Content-Disposition",
                "attachment; filename=relatorio-financeiro.pdf");

        Document document =
                new Document();

        PdfWriter.getInstance(
                document,
                response.getOutputStream());

        document.open();

        Font titulo =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        18);

        Paragraph p =
                new Paragraph(
                        "RELATÓRIO FINANCEIRO",
                        titulo);

        p.setSpacingAfter(20);

        document.add(p);

        PdfPTable table =
                new PdfPTable(5);

        table.addCell("Descrição");
        table.addCell("Valor");
        table.addCell("Pago");
        table.addCell("Saldo");
        table.addCell("Status");

        List<ContaPagar> contas =
                repository.findAll();

        for (ContaPagar conta : contas) {

            table.addCell(conta.getDescricao());

            table.addCell(
                    String.valueOf(conta.getValor()));

            table.addCell(
                    String.valueOf(conta.getValorPago()));

            table.addCell(
                    String.valueOf(conta.getSaldoDevedor()));

            table.addCell(
                    conta.getStatus().name());
        }

        document.add(table);

        document.close();
    }
}