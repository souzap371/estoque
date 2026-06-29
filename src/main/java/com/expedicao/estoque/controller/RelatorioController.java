package com.expedicao.estoque.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.expedicao.estoque.model.TipoMovimentacao;
import com.expedicao.estoque.model.VendaItem;
import com.expedicao.estoque.repositorie.EstoqueRepository;
import com.expedicao.estoque.repositorie.ProdutoRepository;
import com.expedicao.estoque.repositorie.VendaItemRepository;
import com.expedicao.estoque.service.VendaService;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private VendaItemRepository vendaItemRepository;
    @Autowired
    private EstoqueRepository estoqueRepository;
    @Autowired
    private VendaService vendaService;

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
            @RequestParam(required = false) Boolean notaFiscal,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        var pageable = PageRequest.of(page, 10);

        // Conversão segura do Pedido
        Long pedidoId = null;
        if (pedido != null && !pedido.isBlank()) {
            pedidoId = Long.valueOf(pedido);
        }

        // Conversão segura do ENUM TipoMovimentacao
        TipoMovimentacao tipoEnum = null;
        if (tipo != null && !tipo.isBlank()) {
            tipoEnum = TipoMovimentacao.valueOf(tipo);
        }

        Page<VendaItem> pagina = vendaItemRepository.filtrar(
                pedidoId, produto, cliente, estado, tipoEnum, notaFiscal, dataInicio, dataFim, pageable);

        model.addAttribute("pagina", pagina);
        model.addAttribute("itens", pagina.getContent());

        // TOTAIS
        model.addAttribute("totalPedidos",
                pagina.getContent().stream()
                        .map(i -> i.getVenda().getId())
                        .distinct()
                        .count());

        model.addAttribute("totalItens", pagina.getTotalElements());

        model.addAttribute("totalQuantidade",
                pagina.getContent().stream()
                        .mapToInt(VendaItem::getQuantidade)
                        .sum());

        // LISTAS PARA FILTROS
        model.addAttribute("listaPedidos", vendaItemRepository.buscarPedidos());
        model.addAttribute("listaProdutos", vendaItemRepository.buscarProdutos());
        model.addAttribute("listaClientes", vendaItemRepository.buscarClientes());
        model.addAttribute("listaEstados", vendaItemRepository.buscarEstados());
        model.addAttribute("listaTipos", TipoMovimentacao.values());

        // =================================================
        // 📊 KPIs - DADOS REAIS DO BANCO
        // =================================================

        // 1. Vendas (Mês)
        BigDecimal vendasMesAtual = vendaService.getVendasMesAtual();
        BigDecimal vendasMesAnterior = vendaService.getVendasMesAnterior();
        BigDecimal variacaoVendas = vendaService.calcularVariacaoPercentual(vendasMesAtual, vendasMesAnterior);
        List<BigDecimal> sparklineVendas = vendaService.getSparklineVendas();

        model.addAttribute("kpiVendasValor", vendasMesAtual);
        model.addAttribute("kpiVendasVariacao", variacaoVendas);
        model.addAttribute("kpiVendasPositiva", variacaoVendas.compareTo(BigDecimal.ZERO) >= 0);
        model.addAttribute("sparklineVendas", calcularAlturasSparklineBigDecimal(sparklineVendas));

        // 2. Pedidos (Mês)
        Long pedidosMesAtual = vendaService.getPedidosMesAtual();
        Long pedidosMesAnterior = vendaService.getPedidosMesAnterior();
        BigDecimal variacaoPedidos = vendaService.calcularVariacaoPercentual(pedidosMesAtual, pedidosMesAnterior);
        List<Long> sparklinePedidos = vendaService.getSparklinePedidos();

        model.addAttribute("kpiPedidosValor", pedidosMesAtual);
        model.addAttribute("kpiPedidosVariacao", variacaoPedidos);
        model.addAttribute("kpiPedidosPositiva", variacaoPedidos.compareTo(BigDecimal.ZERO) >= 0);
        model.addAttribute("sparklinePedidos", calcularAlturasSparklineLong(sparklinePedidos));

        // 3. Clientes Ativos
        Long clientesMesAtual = vendaService.getClientesAtivosMesAtual();
        Long clientesMesAnterior = vendaService.getClientesAtivosMesAnterior();
        BigDecimal variacaoClientes = vendaService.calcularVariacaoPercentual(clientesMesAtual, clientesMesAnterior);
        List<Long> sparklineClientes = vendaService.getSparklineClientes();

        model.addAttribute("kpiClientesValor", clientesMesAtual);
        model.addAttribute("kpiClientesVariacao", variacaoClientes);
        model.addAttribute("kpiClientesPositiva", variacaoClientes.compareTo(BigDecimal.ZERO) >= 0);
        model.addAttribute("sparklineClientes", calcularAlturasSparklineLong(sparklineClientes));

        // 4. Produtos Vendidos
        Long produtosMesAtual = vendaService.getProdutosVendidosMesAtual();
        Long produtosMesAnterior = vendaService.getProdutosVendidosMesAnterior();
        BigDecimal variacaoProdutos = vendaService.calcularVariacaoPercentual(produtosMesAtual, produtosMesAnterior);
        List<Long> sparklineProdutos = vendaService.getSparklineProdutos();

        model.addAttribute("kpiProdutosValor", produtosMesAtual);
        model.addAttribute("kpiProdutosVariacao", variacaoProdutos);
        model.addAttribute("kpiProdutosPositiva", variacaoProdutos.compareTo(BigDecimal.ZERO) >= 0);
        model.addAttribute("sparklineProdutos", calcularAlturasSparklineLong(sparklineProdutos));

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

    // =================================================
    // HELPERS - Calcular alturas da sparkline (0-100%)
    // =================================================
    private List<Integer> calcularAlturasSparklineBigDecimal(List<BigDecimal> valores) {
        List<Integer> alturas = new ArrayList<>();
        if (valores == null || valores.isEmpty()) {
            for (int i = 0; i < 10; i++) alturas.add(10);
            return alturas;
        }
        BigDecimal max = valores.stream()
                .filter(v -> v != null)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
        if (max.compareTo(BigDecimal.ZERO) == 0) max = BigDecimal.ONE;

        for (BigDecimal val : valores) {
            BigDecimal v = val != null ? val : BigDecimal.ZERO;
            int pct = v.multiply(BigDecimal.valueOf(100))
                    .divide(max, 0, RoundingMode.HALF_UP)
                    .intValue();
            alturas.add(Math.max(pct, 5));
        }
        return alturas;
    }

    private List<Integer> calcularAlturasSparklineLong(List<Long> valores) {
        List<Integer> alturas = new ArrayList<>();
        if (valores == null || valores.isEmpty()) {
            for (int i = 0; i < 10; i++) alturas.add(10);
            return alturas;
        }
        long max = valores.stream()
                .filter(v -> v != null)
                .max(Long::compareTo)
                .orElse(1L);
        if (max == 0) max = 1;

        for (Long val : valores) {
            long v = val != null ? val : 0L;
            int pct = (int) (v * 100 / max);
            alturas.add(Math.max(pct, 5));
        }
        return alturas;
    }
}
