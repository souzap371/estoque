package com.expedicao.estoque.controller;

import com.expedicao.estoque.model.Filial;
import com.expedicao.estoque.model.Produto;
import com.expedicao.estoque.service.EstoqueService;
import com.expedicao.estoque.service.ProdutoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {

    private final ProdutoService produtoService;
    private final EstoqueService estoqueService;

    public EstoqueController(
            ProdutoService produtoService,
            EstoqueService estoqueService) {
        this.produtoService = produtoService;
        this.estoqueService = estoqueService;
    }

    // 🔹 Tela principal
    // @GetMapping
    // public String telaEstoque() {
    // return "estoque";
    // }
    @GetMapping
    public String telaEstoque(Model model) {

        model.addAttribute("produtos", produtoService.listarTodos());

        return "estoque";
    }

    // 🔹 Entrada de estoque (MATRIZ)
    // @PostMapping("/entrada")
    // public String entradaEstoque(
    // @RequestParam String codigoOuNome,
    // @RequestParam Integer quantidade) {

    // Produto produto = produtoService.buscarPorCodigoOuNome(codigoOuNome);

    // estoqueService.entradaEstoque(produto, Filial.MATRIZ, quantidade);

    // return "redirect:/estoque";
    // }

    @PostMapping("/entrada")
    public String entradaEstoque(
            @RequestParam Long produtoId,
            @RequestParam Integer quantidade) {

        Produto produto = produtoService.buscarPorId(produtoId);

        estoqueService.entradaEstoque(produto, Filial.MATRIZ, quantidade);

        return "redirect:/estoque";
    }

    // 🔹 Consulta por filial
    @GetMapping("/filial")
    public String estoquePorFilial(
            @RequestParam(defaultValue = "MATRIZ") Filial filial,
            Model model) {

        model.addAttribute("estoques", estoqueService.listarPorFilial(filial));
        model.addAttribute("filial", filial);

        return "estoque-filial";
    }
}
