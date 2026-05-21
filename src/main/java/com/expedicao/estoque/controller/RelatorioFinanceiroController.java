package com.expedicao.estoque.controller;

import com.expedicao.estoque.service.RelatorioFinanceiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RelatorioFinanceiroController {

    @Autowired
    private RelatorioFinanceiroService service;

    @GetMapping("/financeiro/relatorios")
    public String dashboardFinanceiro(Model model) {

        model.addAttribute(
                "totalPagar",
                service.totalContasPagar());

        model.addAttribute(
                "totalPago",
                service.totalPago());

        model.addAttribute(
                "totalAtrasado",
                service.totalAtrasado());

        model.addAttribute(
                "quantidadePendentes",
                service.quantidadePendentes());

        model.addAttribute(
                "gastosFornecedor",
                service.gastosPorFornecedor());

        model.addAttribute(
                "pagamentosMensais",
                service.pagamentosMensais());

        return "financeiro-relatorios";
    }
}