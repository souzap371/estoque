package com.expedicao.estoque.service;

import com.expedicao.estoque.dto.VendaDTO;
import com.expedicao.estoque.dto.VendaItemDTO;
import com.expedicao.estoque.model.*;
import com.expedicao.estoque.repositorie.ContaReceberRepository;
import com.expedicao.estoque.repositorie.ProdutoRepository;
import com.expedicao.estoque.repositorie.VendaRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
            ContaReceberRepository contaReceberRepository
    ) {
        this.vendaRepository = vendaRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueService = estoqueService;
        this.contaReceberRepository = contaReceberRepository;
    }

    /**
     * Registra uma venda, valida estoque e dá baixa automática.
     * Se faltar estoque, TODA a operação é revertida.
     */
    @Transactional
    public void salvarPedido(VendaDTO dto) {

        // 1️⃣ Criar a venda
        Venda venda = new Venda();
        venda.setClienteNome(dto.getClienteNome());
        venda.setClienteEstado(dto.getClienteEstado());
        venda.setEstadoDestino(dto.getEstadoDestino());
        venda.setTipoMovimentacao(TipoMovimentacao.valueOf(dto.getTipoMovimentacao()));
        venda.setDataSaida(LocalDate.now());

        List<VendaItem> itens = new ArrayList<>();
        double total = 0.0;

        // 2️⃣ Processar itens
        for (VendaItemDTO itemDTO : dto.getItens()) {

            if (itemDTO.getCodigoOuNome() == null || itemDTO.getCodigoOuNome().isBlank()) {
                throw new RuntimeException("Produto não informado no item da venda");
            }

            if (itemDTO.getQuantidade() <= 0) {
                throw new RuntimeException("Quantidade inválida para o produto: " + itemDTO.getCodigoOuNome());
            }

            Produto produto = produtoRepository
                    .findByCodigoOuNome(itemDTO.getCodigoOuNome())
                    .orElseThrow(() ->
                            new RuntimeException("Produto não encontrado: " + itemDTO.getCodigoOuNome())
                    );

            // 🔥 Baixa automática no estoque
            estoqueService.baixarEstoque(produto, itemDTO.getQuantidade());

            VendaItem item = new VendaItem();
            item.setVenda(venda);
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setValorPorCaixa(itemDTO.getValorPorCaixa());

            double subtotal = itemDTO.getQuantidade() * itemDTO.getValorPorCaixa();
            item.setSubtotal(subtotal);

            total += subtotal;
            itens.add(item);
        }

        // 3️⃣ Finaliza venda
        venda.setItens(itens);
        venda.setValorTotal(total);

        vendaRepository.save(venda);

        // 4️⃣ Cria Conta a Receber
        ContaReceber conta = new ContaReceber();
        conta.setVenda(venda);
        conta.setClienteNome(venda.getClienteNome());
        conta.setValorOriginal(venda.getValorTotal());
        conta.setValorPago(0.0);
        conta.setSaldoDevedor(venda.getValorTotal());
        conta.setDataCriacao(LocalDate.now());

        contaReceberRepository.save(conta);
    }
}
