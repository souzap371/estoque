package com.expedicao.estoque.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private final VendaItemRepository vendaItemRepository;

    public VendaService(VendaRepository vendaRepository,
            ProdutoRepository produtoRepository,
            EstoqueService estoqueService,
            ContaReceberRepository contaReceberRepository,
            VendaItemRepository vendaItemRepository) {
        this.vendaRepository = vendaRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueService = estoqueService;
        this.contaReceberRepository = contaReceberRepository;
        this.vendaItemRepository = vendaItemRepository;
    }

    // =========================================================
    // 💾 SALVAR OU EDITAR PEDIDO
    // =========================================================
    @Transactional
    public void salvarPedido(VendaDTO dto) {

        if (dto.getItens() == null || dto.getItens().isEmpty())
            throw new RuntimeException("Nenhum item informado no pedido");

        Venda venda;

        // ✏️ EDIÇÃO
        if (dto.getId() != null) {
            venda = vendaRepository.findByIdComItens(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

            // 🔁 ESTORNA ESTOQUE ANTIGO
            for (VendaItem antigo : venda.getItens()) {
                Produto produto = antigo.getProduto();

                estoqueService.devolverEstoque(produto, antigo.getQuantidade());

                if (antigo.getTipoMovimentacao() == TipoMovimentacao.T) {
                    Filial filial = Filial.valueOf(antigo.getEstadoDestino());
                    estoqueService.baixarEstoqueFilial(produto, filial, antigo.getQuantidade());
                }
            }

            vendaItemRepository.deleteByVendaId(venda.getId());
            venda.getItens().clear();

        } else {
            venda = new Venda();
            venda.setDataSaida(LocalDate.now());
        }

        venda.setClienteNome(dto.getClienteNome());
        venda.setClienteEstado(dto.getClienteEstado());

        List<VendaItem> novosItens = new ArrayList<>();
        BigDecimal valorTotal = BigDecimal.ZERO;

        // 📦 NOVOS ITENS
        for (VendaItemDTO itemDTO : dto.getItens()) {

            Produto produto = produtoRepository.findByCodigoOuNome(itemDTO.getCodigoOuNome())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            TipoMovimentacao tipo = TipoMovimentacao.valueOf(itemDTO.getTipoMovimentacao());

            estoqueService.baixarEstoque(produto, itemDTO.getQuantidade());

            String estadoDestino = null;

            if (tipo == TipoMovimentacao.T) {
                Filial filial = Filial.valueOf(itemDTO.getEstadoDestino());
                estoqueService.entradaEstoque(produto, filial, itemDTO.getQuantidade());
                estadoDestino = itemDTO.getEstadoDestino();
            }

            BigDecimal valorUnitario = BigDecimal.valueOf(itemDTO.getValorPorCaixa());
            BigDecimal subtotal = valorUnitario.multiply(BigDecimal.valueOf(itemDTO.getQuantidade()));

            valorTotal = valorTotal.add(subtotal);

            VendaItem item = new VendaItem();
            item.setVenda(venda);
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setValorPorCaixa(valorUnitario);
            item.setSubtotal(subtotal);
            item.setTipoMovimentacao(tipo);
            item.setEstadoDestino(estadoDestino);

            novosItens.add(item);
        }

        venda.setItens(novosItens);
        venda.setValorTotal(valorTotal);
        vendaRepository.save(venda);

        // =========================================================
        // 💰 FINANCEIRO
        // =========================================================
        ContaReceber conta = contaReceberRepository.findByVendaId(venda.getId())
                .orElse(new ContaReceber());

        conta.setVenda(venda);
        conta.setClienteNome(venda.getClienteNome());
        conta.setValorOriginal(valorTotal);
        conta.setValorPago(conta.getValorPago() == null ? BigDecimal.ZERO : conta.getValorPago());
        conta.setDataCriacao(LocalDate.now());
        contaReceberRepository.save(conta);
    }

    // =========================================================
    // 🔍 BUSCAR VENDA COMPLETA
    // =========================================================
    public Venda buscarVendaCompleta(Long id) {
        return vendaRepository.findByIdComItens(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));
    }

    // =========================================================
    // 🗑 EXCLUIR VENDA
    // =========================================================
    @Transactional
    public void excluirVenda(Long id) {
        Venda venda = vendaRepository.findByIdComItens(id)
                .orElseThrow(() -> new RuntimeException("Venda não encontrada"));

        for (VendaItem item : venda.getItens()) {
            estoqueService.devolverEstoque(item.getProduto(), item.getQuantidade());

            if (item.getTipoMovimentacao() == TipoMovimentacao.T) {
                Filial filial = Filial.valueOf(item.getEstadoDestino());
                estoqueService.baixarEstoqueFilial(item.getProduto(), filial, item.getQuantidade());
            }
        }

        contaReceberRepository.deleteByVendaId(id);
        vendaRepository.delete(venda);
    }
}
