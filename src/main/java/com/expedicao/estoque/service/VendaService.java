package com.expedicao.estoque.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.expedicao.estoque.dto.VendaDTO;
import com.expedicao.estoque.dto.VendaItemDTO;
import com.expedicao.estoque.model.*;
import com.expedicao.estoque.repositorie.*;

@Service
public class VendaService {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueService estoqueService;
    private final ContaReceberRepository contaReceberRepository;

    public VendaService(
            VendaRepository vendaRepository,
            ProdutoRepository produtoRepository,
            EstoqueService estoqueService,
            ContaReceberRepository contaReceberRepository) {

        this.vendaRepository = vendaRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueService = estoqueService;
        this.contaReceberRepository = contaReceberRepository;
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
            venda.setDataSaida(LocalDate.now());
        }

        venda.setClienteNome(dto.getClienteNome());
        venda.setClienteEstado(dto.getClienteEstado());

        BigDecimal total = BigDecimal.ZERO;

        for (VendaItemDTO itemDTO : dto.getItens()) {

            Produto produto = produtoRepository
                    .findByCodigoOuNome(itemDTO.getCodigoOuNome())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            TipoMovimentacao tipo = TipoMovimentacao.valueOf(itemDTO.getTipoMovimentacao());

            estoqueService.baixarEstoque(produto, itemDTO.getQuantidade());

            String estadoDestino = null;

            if (tipo == TipoMovimentacao.T) {
                Filial filial = Filial.valueOf(itemDTO.getEstadoDestino());
                estoqueService.entradaEstoque(produto, filial, itemDTO.getQuantidade());
                estadoDestino = filial.name();
            }

            BigDecimal valorUnit = BigDecimal.valueOf(itemDTO.getValorPorCaixa());
            BigDecimal subtotal = valorUnit.multiply(
                    BigDecimal.valueOf(itemDTO.getQuantidade()));

            VendaItem item = new VendaItem();
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setValorPorCaixa(valorUnit);
            item.setSubtotal(subtotal);
            item.setTipoMovimentacao(tipo);
            item.setEstadoDestino(estadoDestino);

            venda.adicionarItem(item);
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
}
