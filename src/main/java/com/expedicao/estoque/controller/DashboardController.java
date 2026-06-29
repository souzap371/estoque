package com.expedicao.estoque.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.expedicao.estoque.service.VendaService;

@Controller
public class DashboardController {

    @Autowired
    private VendaService vendaService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 1. Vendas (BigDecimal)
        BigDecimal vendasAtual = vendaService.getVendasMesAtual();
        BigDecimal vendasAnt = vendaService.getVendasMesAnterior();
        BigDecimal varVendas = vendaService.calcularVariacaoPercentual(vendasAtual, vendasAnt);
        model.addAttribute("kpiVendasValor", vendasAtual);
        model.addAttribute("kpiVendasVariacao", varVendas);
        model.addAttribute("kpiVendasPositiva", varVendas.compareTo(BigDecimal.ZERO) >= 0);
        model.addAttribute("sparklineVendas", calcularAlturas(vendaService.getSparklineVendas()));

        // 2. Pedidos (Long)
        Long pedAtual = vendaService.getPedidosMesAtual();
        Long pedAnt = vendaService.getPedidosMesAnterior();
        BigDecimal varPed = vendaService.calcularVariacaoPercentual(pedAtual, pedAnt);
        model.addAttribute("kpiPedidosValor", pedAtual);
        model.addAttribute("kpiPedidosVariacao", varPed);
        model.addAttribute("kpiPedidosPositiva", varPed.compareTo(BigDecimal.ZERO) >= 0);
        model.addAttribute("sparklinePedidos", calcularAlturasLong(vendaService.getSparklinePedidos()));

        // 3. Clientes (Long)
        Long cliAtual = vendaService.getClientesAtivosMesAtual();
        Long cliAnt = vendaService.getClientesAtivosMesAnterior();
        BigDecimal varCli = vendaService.calcularVariacaoPercentual(cliAtual, cliAnt);
        model.addAttribute("kpiClientesValor", cliAtual);
        model.addAttribute("kpiClientesVariacao", varCli);
        model.addAttribute("kpiClientesPositiva", varCli.compareTo(BigDecimal.ZERO) >= 0);
        model.addAttribute("sparklineClientes", calcularAlturasLong(vendaService.getSparklineClientes()));

        // 4. Produtos (Long)
        Long prodAtual = vendaService.getProdutosVendidosMesAtual();
        Long prodAnt = vendaService.getProdutosVendidosMesAnterior();
        BigDecimal varProd = vendaService.calcularVariacaoPercentual(prodAtual, prodAnt);
        model.addAttribute("kpiProdutosValor", prodAtual);
        model.addAttribute("kpiProdutosVariacao", varProd);
        model.addAttribute("kpiProdutosPositiva", varProd.compareTo(BigDecimal.ZERO) >= 0);
        model.addAttribute("sparklineProdutos", calcularAlturasLong(vendaService.getSparklineProdutos()));

        return "Dashboard";
    }

    // =================================================
    // Helper para BigDecimal (Vendas)
    // =================================================
    private List<Integer> calcularAlturas(List<BigDecimal> valores) {
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

    // =================================================
    // Helper para Long (Pedidos, Clientes, Produtos)
    // =================================================
    private List<Integer> calcularAlturasLong(List<Long> valores) {
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