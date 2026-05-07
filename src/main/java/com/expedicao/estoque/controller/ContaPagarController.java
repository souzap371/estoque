package com.expedicao.estoque.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.expedicao.estoque.model.ContaPagar;
import com.expedicao.estoque.service.ContaPagarService;
import com.expedicao.estoque.service.FornecedorService;

@Controller
@RequestMapping("/contas-pagar")
public class ContaPagarController {

    @Autowired
    private ContaPagarService contaPagarService;

    @Autowired
    private FornecedorService fornecedorService;

    // =========================
    // RELATÓRIO / LISTAGEM
    // =========================
    @GetMapping
    public String listar(Model model) {

        model.addAttribute(
                "contas",
                contaPagarService.listarTodas());

        return "contaspagar";
    }

    // =========================
    // TELA CADASTRO
    // =========================
    @GetMapping("/nova")
    public String novaConta(Model model) {

        model.addAttribute(
                "conta",
                new ContaPagar());

        model.addAttribute(
                "fornecedores",
                fornecedorService.listarTodos());

        return "contaspagarform";
    }

    // =========================
    // SALVAR
    // =========================
    @PostMapping("/salvar")
    public String salvar(
            @ModelAttribute ContaPagar conta) {

        contaPagarService.salvar(conta);

        return "redirect:/contas-pagar";
    }

    // =========================
    // EDITAR
    // =========================
    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model) {

        ContaPagar conta =
                contaPagarService.buscarPorId(id);

        model.addAttribute(
                "conta",
                conta);

        model.addAttribute(
                "fornecedores",
                fornecedorService.listarTodos());

        return "contaspagarform";
    }

    // =========================
    // PAGAMENTO
    // =========================
    @PostMapping("/{id}/pagar")
    public String pagar(
            @PathVariable Long id,
            @RequestParam BigDecimal valorPagamento) {

        contaPagarService.pagarConta(
                id,
                valorPagamento);

        return "redirect:/contas-pagar";
    }
}