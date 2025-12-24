package com.expedicao.estoque.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.expedicao.estoque.model.Produto;
import com.expedicao.estoque.model.Venda;
import com.expedicao.estoque.service.ProdutoService;
import com.expedicao.estoque.service.VendaService;


@Controller
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private VendaService vendaService;

    // ✅ ABRE A TELA
    @GetMapping
    public String abrirTelaVenda() {
        return "Venda.html"; // venda.html
    }

    // ✅ PROCESSA A VENDA
    @PostMapping("/realizar")
    public String vender(
        @RequestParam String codigoOuNome,
        @RequestParam Integer quantidade,
        @RequestParam String clienteNome,
        @RequestParam String clienteEstado,
        @RequestParam Boolean comNotaFiscal
    ) {

        Produto produto = produtoService.buscarPorCodigoOuNome(codigoOuNome);

        Venda venda = new Venda();
        venda.setProduto(produto);
        venda.setQuantidade(quantidade);
        venda.setClienteNome(clienteNome);
        venda.setClienteEstado(clienteEstado);
        venda.setDataSaida(LocalDate.now());
        venda.setComNotaFiscal(comNotaFiscal);

        vendaService.realizarVenda(venda);
        return "redirect:/vendas";
    }
}
