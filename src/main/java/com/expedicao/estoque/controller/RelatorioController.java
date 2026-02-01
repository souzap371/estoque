package com.expedicao.estoque.controller;

import java.time.LocalDate;

import com.expedicao.estoque.model.TipoMovimentacao;
import com.expedicao.estoque.model.VendaItem;
import com.expedicao.estoque.repositorie.EstoqueRepository;
import com.expedicao.estoque.repositorie.ProdutoRepository;
import com.expedicao.estoque.repositorie.VendaItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private VendaItemRepository vendaItemRepository;
    @Autowired
    private EstoqueRepository estoqueRepository;

    @GetMapping
    public String menuRelatorios() {
        return "relatorios";
    }

    @GetMapping("/vendas")
    public String relatorioVendas(
            @RequestParam(required = false) String pedido,
            @RequestParam(required = false) String produto,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        var pageable = PageRequest.of(page, 10);

        // 🔹 Conversão segura do Pedido
        Long pedidoId = null;
        if (pedido != null && !pedido.isBlank()) {
            pedidoId = Long.valueOf(pedido);
        }

        // 🔹 Conversão segura do ENUM TipoMovimentacao
        TipoMovimentacao tipoEnum = null;
        if (tipo != null && !tipo.isBlank()) {
            tipoEnum = TipoMovimentacao.valueOf(tipo);
        }

        Page<VendaItem> pagina = vendaItemRepository.filtrar(
                pedidoId, produto, cliente, estado, tipoEnum, dataInicio, dataFim, pageable);

        model.addAttribute("pagina", pagina);
        model.addAttribute("itens", pagina.getContent());

        // 📊 TOTAIS
        model.addAttribute("totalPedidos",
                pagina.getContent().stream()
                        .map(i -> i.getVenda().getId())
                        .distinct()
                        .count());

        model.addAttribute("totalItens", pagina.getTotalElements());

        // ✅ TOTALIZAR QTD
        model.addAttribute("totalQuantidade",
                pagina.getContent().stream()
                        .mapToInt(VendaItem::getQuantidade)
                        .sum());

        // 📋 LISTAS PARA FILTROS
        model.addAttribute("listaPedidos", vendaItemRepository.buscarPedidos());
        model.addAttribute("listaProdutos", vendaItemRepository.buscarProdutos());
        model.addAttribute("listaClientes", vendaItemRepository.buscarClientes());
        model.addAttribute("listaEstados", vendaItemRepository.buscarEstados());
        model.addAttribute("listaTipos", TipoMovimentacao.values());

        return "relatorio-vendas";
    }

    @GetMapping("/produtos")
    public String relatorioProdutos(Model model) {
        model.addAttribute("produtos", produtoRepository.findAll());
        return "relatorio-produtos";
    }

    @GetMapping("/estoque")
    public String relatorioEstoque(Model model) {
        model.addAttribute("estoques", estoqueRepository.findAll());
        return "relatorio-estoque";
    }
}
