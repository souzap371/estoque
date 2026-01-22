package com.expedicao.estoque.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.expedicao.estoque.repositorie.EstoqueRepository;
import com.expedicao.estoque.repositorie.ProdutoRepository;
import com.expedicao.estoque.repositorie.VendaItemRepository;
import com.expedicao.estoque.repositorie.VendaRepository;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private VendaItemRepository vendaItemRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    // MENU DE RELATÓRIOS
    @GetMapping
    public String menuRelatorios() {
        return "relatorios";
    }

    // RELATÓRIO DE PRODUTOS
    @GetMapping("/produtos")
    public String relatorioProdutos(Model model) {
        model.addAttribute("produtos", produtoRepository.findAll());
        return "relatorio-produtos";
    }

    @GetMapping("/vendas")
    public String relatorioVendas(Model model) {

        var itens = vendaItemRepository.findAll();
        System.out.println("TOTAL DE ITENS DE VENDA: " + itens.size());

        model.addAttribute("itens", itens);
        return "relatorio-vendas";
    }

    // RELATÓRIO DE ESTOQUE
    @GetMapping("/estoque")
    public String relatorioEstoque(Model model) {
        model.addAttribute("estoques", estoqueRepository.findAll());
        return "relatorio-estoque";
    }
}
