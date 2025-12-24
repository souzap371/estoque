package com.expedicao.estoque.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.expedicao.estoque.model.Produto;
import com.expedicao.estoque.service.EstoqueService;
import com.expedicao.estoque.service.ProdutoService;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private EstoqueService estoqueService;

    @GetMapping
    public String telaEstoque() {
        return "estoque";
    }

    @PostMapping("/entrada")
    public String entradaEstoque(
        @RequestParam String codigoOuNome,
        @RequestParam Integer quantidade
    ) {

        Produto produto = produtoService.buscarPorCodigoOuNome(codigoOuNome);
        estoqueService.entradaEstoque(produto, quantidade);

        return "redirect:/estoque";
    }
}

