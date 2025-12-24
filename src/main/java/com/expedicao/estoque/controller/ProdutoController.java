package com.expedicao.estoque.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.expedicao.estoque.model.Produto;
import com.expedicao.estoque.service.ProdutoService;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    // ✅ ABRE A TELA DE CADASTRO
    @GetMapping
    public String abrirTelaCadastroProduto() {
        return "produto"; // produto.html
    }

    // ✅ SALVA O PRODUTO
    @PostMapping("/salvar")
    public String salvar(Produto produto) {
        produtoService.salvar(produto);
        return "redirect:/produtos";
    }
}
