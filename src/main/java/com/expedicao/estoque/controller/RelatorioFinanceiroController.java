// package com.expedicao.estoque.controller;

// import com.expedicao.estoque.repositorie.FornecedorRepository;
// import com.expedicao.estoque.service.RelatorioFinanceiroService;

// import java.time.LocalDate;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.format.annotation.DateTimeFormat;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// @Controller
// public class RelatorioFinanceiroController {

//         @Autowired
//         private RelatorioFinanceiroService service;

//         @Autowired
//         private FornecedorRepository fornecedorRepository;

//         @GetMapping("/financeiro/relatorios")
//         public String dashboardFinanceiro(

//                         @RequestParam(required = false) Long fornecedorId,

//                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,

//                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,

//                         Model model) {

//                 model.addAttribute(
//                                 "totalPagar",
//                                 service.totalContasPagar(
//                                                 fornecedorId,
//                                                 dataInicio,
//                                                 dataFim));

//                 model.addAttribute(
//                                 "totalPago",
//                                 service.totalPago(
//                                                 fornecedorId,
//                                                 dataInicio,
//                                                 dataFim));

//                 model.addAttribute(
//                                 "totalAtrasado",
//                                 service.totalAtrasado(
//                                                 fornecedorId,
//                                                 dataInicio,
//                                                 dataFim));

//                 model.addAttribute(
//                                 "quantidadePendentes",
//                                 service.quantidadePendentes(
//                                                 fornecedorId,
//                                                 dataInicio,
//                                                 dataFim));

//                 model.addAttribute(
//                                 "pagamentosMensais",
//                                 service.pagamentosMensais(
//                                                 fornecedorId,
//                                                 dataInicio,
//                                                 dataFim));

//                 model.addAttribute(
//                                 "contasSemana",
//                                 service.contasVencendoSemana());

//                 model.addAttribute(
//                                 "fornecedores",
//                                 fornecedorRepository.findAll());

//                 model.addAttribute("fornecedorId", fornecedorId);
//                 model.addAttribute("dataInicio", dataInicio);
//                 model.addAttribute("dataFim", dataFim);

//                 return "financeiro-relatorios";
//         }
// }
package com.expedicao.estoque.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.expedicao.estoque.repositorie.FornecedorRepository;
import com.expedicao.estoque.service.RelatorioFinanceiroService;

@Controller
public class RelatorioFinanceiroController {

        @Autowired
        private RelatorioFinanceiroService relatorioFinanceiroService;

        @Autowired
        private FornecedorRepository fornecedorRepository;

        // =========================
        // DASHBOARD FINANCEIRO
        // =========================
        @GetMapping("/financeiro/relatorios")
        public String dashboardFinanceiro(

                        @RequestParam(required = false) Long fornecedorId,

                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,

                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim,

                        Model model) {

                // =========================
                // CARDS
                // =========================
                model.addAttribute(
                                "totalPagar",
                                relatorioFinanceiroService.totalContasPagar(
                                                fornecedorId,
                                                dataInicio,
                                                dataFim));

                model.addAttribute(
                                "totalPago",
                                relatorioFinanceiroService.totalPago(
                                                fornecedorId,
                                                dataInicio,
                                                dataFim));

                model.addAttribute(
                                "totalAtrasado",
                                relatorioFinanceiroService.totalAtrasado(
                                                fornecedorId,
                                                dataInicio,
                                                dataFim));

                model.addAttribute(
                                "quantidadePendentes",
                                relatorioFinanceiroService.quantidadePendentes(
                                                fornecedorId,
                                                dataInicio,
                                                dataFim));

                model.addAttribute(
                                "contasSemana",
                                relatorioFinanceiroService.contasVencendoSemana(
                                                fornecedorId,
                                                dataInicio,
                                                dataFim));

                // =========================
                // GRÁFICO FILTRADO
                // =========================
                model.addAttribute(
                                "pagamentosMensais",
                                relatorioFinanceiroService.pagamentosMensais(
                                                fornecedorId,
                                                dataInicio,
                                                dataFim));

                // =========================
                // FILTROS
                // =========================
                model.addAttribute(
                                "fornecedores",
                                // fornecedorRepository.findAll());
                                 relatorioFinanceiroService.listarFornecedoresComContas());

                model.addAttribute(
                                "fornecedorId",
                                fornecedorId);

                model.addAttribute(
                                "dataInicio",
                                dataInicio);

                model.addAttribute(
                                "dataFim",
                                dataFim);

                return "financeiro-relatorios";
        }
}