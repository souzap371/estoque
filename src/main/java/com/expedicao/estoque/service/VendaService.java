package com.expedicao.estoque.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.expedicao.estoque.dto.ResumoFinanceiroDTO;
import com.expedicao.estoque.dto.VendaDTO;
import com.expedicao.estoque.dto.VendaItemDTO;
import com.expedicao.estoque.model.*;
import com.expedicao.estoque.repositorie.*;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final EstoqueService estoqueService;
    private final ContaReceberRepository contaReceberRepository;
    private final ContaPagarRepository contaPagarRepository;
    private final PagamentoRepository pagamentoRepository;

    public VendaService(
            VendaRepository vendaRepository,
            ProdutoRepository produtoRepository,
            ClienteRepository clienteRepository,
            EstoqueService estoqueService,
            ContaReceberRepository contaReceberRepository,
            ContaPagarRepository contaPagarRepository,
            PagamentoRepository pagamentoRepository) {

        this.vendaRepository = vendaRepository;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
        this.estoqueService = estoqueService;
        this.contaReceberRepository = contaReceberRepository;
        this.contaPagarRepository = contaPagarRepository;
        this.pagamentoRepository = pagamentoRepository;
    }

    // =================================================
    // 💾 SALVAR / EDITAR VENDA
    // =================================================
    @Transactional
    public void salvarPedido(VendaDTO dto) {

        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new RuntimeException("Pedido sem itens");
        }

        Venda venda;

        if (dto.getId() != null) {
            venda = vendaRepository.findByIdComItens(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

            estornarEstoque(venda);
            venda.limparItens();

        } else {
            venda = new Venda();
        }

        // Busca o cliente
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        venda.setCliente(cliente);
        venda.setClienteNome(cliente.getNomeCompleto());
        venda.setClienteEstado(cliente.getUf());

        if (dto.getDataPedido() != null) {
            venda.setDataSaida(dto.getDataPedido());
        } else {
            venda.setDataSaida(LocalDate.now());
        }

        venda.setComNotaFiscal(dto.getComNotaFiscal());
        venda.setObservacao(dto.getObservacao());

        BigDecimal total = BigDecimal.ZERO;

        for (VendaItemDTO itemDTO : dto.getItens()) {

            Produto produto = produtoRepository
                    .findByCodigoOuNome(itemDTO.getCodigoOuNome())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            TipoMovimentacao tipo = TipoMovimentacao.valueOf(itemDTO.getTipoMovimentacao());

            // Baixa do estoque sempre ocorre
            estoqueService.baixarEstoque(produto, itemDTO.getQuantidade());

            String estadoDestino = null;

            if (tipo == TipoMovimentacao.T) {
                Filial filial = Filial.valueOf(itemDTO.getEstadoDestino());

                estoqueService.entradaEstoque(
                        produto,
                        filial,
                        itemDTO.getQuantidade());

                estadoDestino = filial.name();
            }

            // ===========================
            // BONIFICAÇÃO
            // ===========================
            boolean bonificacao = Boolean.TRUE.equals(itemDTO.getBonificacao());

            BigDecimal valorUnit = bonificacao
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(itemDTO.getValorPorCaixa());

            BigDecimal subtotal = valorUnit.multiply(
                    BigDecimal.valueOf(itemDTO.getQuantidade()));

            VendaItem item = new VendaItem();
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setValorPorCaixa(valorUnit);
            item.setSubtotal(subtotal);
            item.setTipoMovimentacao(tipo);
            item.setEstadoDestino(estadoDestino);
            item.setBonificacao(bonificacao);

            venda.adicionarItem(item);

            // Soma somente itens pagos
            total = total.add(subtotal);
        }

        venda.setValorTotal(total);

        vendaRepository.save(venda);

        salvarFinanceiro(venda);
    }

    // =========================================================
    // 🔍 BUSCAR VENDA COMPLETA
    // =========================================================
    public Venda buscarVendaCompleta(Long id) {
        return vendaRepository.findByIdComItens(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));
    }

    // =================================================
    // 🔁 EXCLUIR VENDA
    // =================================================
    @Transactional
    public void excluirVenda(Long id) {

        Venda venda = buscarVendaCompleta(id);

        estornarEstoque(venda);

        contaReceberRepository.deleteByVendaId(id);
        vendaRepository.delete(venda);
    }

    // =================================================
    // 🔁 ESTORNO DE ESTOQUE
    // =================================================
    private void estornarEstoque(Venda venda) {

        for (VendaItem item : venda.getItens()) {

            estoqueService.devolverEstoque(
                    item.getProduto(),
                    item.getQuantidade());

            if (item.getTipoMovimentacao() == TipoMovimentacao.T) {
                Filial filial = Filial.valueOf(item.getEstadoDestino());
                estoqueService.baixarEstoqueFilial(
                        item.getProduto(),
                        filial,
                        item.getQuantidade());
            }
        }
    }

    // =================================================
    // 💰 FINANCEIRO
    // =================================================
    private void salvarFinanceiro(Venda venda) {

        ContaReceber conta = contaReceberRepository
                .findByVendaId(venda.getId())
                .orElse(new ContaReceber());

        conta.setVenda(venda);
        conta.setClienteNome(venda.getClienteNome());
        conta.setValorOriginal(venda.getValorTotal());
        conta.setValorPago(
                conta.getValorPago() == null ? BigDecimal.ZERO : conta.getValorPago());
        conta.setDataCriacao(LocalDate.now());

        contaReceberRepository.save(conta);
    }

    // ============================================================
    // 📊 KPIs - DADOS DO DASHBOARD
    // ============================================================

    public BigDecimal getVendasMesAtual() {
        return vendaRepository.totalVendasMesAtual();
    }

    public BigDecimal getVendasMesAnterior() {
        return vendaRepository.totalVendasMesAnterior();
    }

    public Long getPedidosMesAtual() {
        return vendaRepository.totalPedidosMesAtual();
    }

    public Long getBonificacaoMesAtual() {
        Long result = vendaRepository.totalBonificacaoMesAtual();
        return result != null ? result : 0L;
    }

    public Long getBonificacaoMesAnterior() {
        Long result = vendaRepository.totalBonificacaoMesAnterior();
        return result != null ? result : 0L;
    }

    public List<Long> getSparklineBonificacao() {
        List<Long> lista = new ArrayList<>();
        for (int i = 9; i >= 0; i--) {
            Long qtd = vendaRepository.totalBonificacaoPorDia(i);
            lista.add(qtd != null ? qtd : 0L);
        }
        return lista;
    }

    public Long getPedidosMesAnterior() {
        return vendaRepository.totalPedidosMesAnterior();
    }

    public Long getClientesAtivosMesAtual() {
        return vendaRepository.totalClientesAtivosMesAtual();
    }

    public Long getClientesAtivosMesAnterior() {
        return vendaRepository.totalClientesAtivosMesAnterior();
    }

    public Long getProdutosVendidosMesAtual() {
        return vendaRepository.totalProdutosVendidosMesAtual();
    }

    public Long getProdutosVendidosMesAnterior() {
        return vendaRepository.totalProdutosVendidosMesAnterior();
    }

    public BigDecimal calcularVariacaoPercentual(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
            return atual != null && atual.compareTo(BigDecimal.ZERO) > 0
                    ? new BigDecimal("100")
                    : BigDecimal.ZERO;
        }
        if (atual == null)
            return BigDecimal.ZERO;
        return atual.subtract(anterior)
                .divide(anterior, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(1, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularVariacaoPercentual(Long atual, Long anterior) {
        BigDecimal bAtual = atual != null ? BigDecimal.valueOf(atual) : BigDecimal.ZERO;
        BigDecimal bAnterior = anterior != null ? BigDecimal.valueOf(anterior) : BigDecimal.ZERO;
        return calcularVariacaoPercentual(bAtual, bAnterior);
    }

    public List<BigDecimal> getSparklineVendas() {
        List<BigDecimal> dados = new ArrayList<>();
        for (int i = 9; i >= 0; i--) {
            BigDecimal valor = vendaRepository.totalVendasPorDia(i);
            dados.add(valor != null ? valor : BigDecimal.ZERO);
        }
        return dados;
    }

    public List<Long> getSparklinePedidos() {
        List<Long> dados = new ArrayList<>();
        for (int i = 9; i >= 0; i--) {
            Long valor = vendaRepository.totalPedidosPorDia(i);
            dados.add(valor != null ? valor : 0L);
        }
        return dados;
    }

    public List<Long> getSparklineClientes() {
        List<Long> dados = new ArrayList<>();
        for (int i = 9; i >= 0; i--) {
            Long valor = vendaRepository.totalClientesPorDia(i);
            dados.add(valor != null ? valor : 0L);
        }
        return dados;
    }

    public List<Long> getSparklineProdutos() {
        List<Long> dados = new ArrayList<>();
        for (int i = 9; i >= 0; i--) {
            Long valor = vendaRepository.totalProdutosPorDia(i);
            dados.add(valor != null ? valor : 0L);
        }
        return dados;
    }

    // ============================================================
    // 📊 RESUMO FINANCEIRO - POR PERÍODO
    // ============================================================

    public ResumoFinanceiroDTO getResumoFinanceiro(String periodo) {
        LocalDate hoje = LocalDate.now();
        LocalDate dataInicio;
        LocalDate dataFim = hoje;

        switch (periodo) {
            case "semana":
                dataInicio = hoje.minusDays(hoje.getDayOfWeek().getValue() % 7);
                break;
            case "ano":
                dataInicio = hoje.withDayOfYear(1);
                break;
            case "todos":
                dataInicio = LocalDate.of(2000, 1, 1);
                break;
            case "mes":
            default:
                dataInicio = hoje.withDayOfMonth(1);
                break;
        }

        BigDecimal receitas = pagamentoRepository.totalReceitasPorDataPagamento(dataInicio, dataFim);
        BigDecimal despesas = contaPagarRepository.totalDespesas(dataInicio, dataFim);

        return new ResumoFinanceiroDTO(receitas, despesas);
    }
}
