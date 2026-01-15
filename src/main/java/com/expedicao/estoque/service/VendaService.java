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
     * Registra uma VENDA ou TRANSFERÊNCIA.
     *
     * Regras:
     * - SEMPRE baixa estoque da matriz
     * - Se for TRANSFERÊNCIA, entra estoque na filial (MG ou AL)
     * - Financeiro SEMPRE é gerado (Conta a Receber)
     * - Tudo é transacional (rollback automático)
     */
    @Transactional
    public void salvarPedido(VendaDTO dto) {

        // ===============================
        // 1️⃣ Validações básicas
        // ===============================
        if (dto.getItens() == null || dto.getItens().isEmpty()) {
            throw new RuntimeException("Nenhum item informado no pedido");
        }

        TipoMovimentacao tipoMovimentacao =
                TipoMovimentacao.valueOf(dto.getTipoMovimentacao());

        if (tipoMovimentacao == TipoMovimentacao.T) {
            if (dto.getEstadoDestino() == null || dto.getEstadoDestino().isBlank()) {
                throw new RuntimeException("Estado destino é obrigatório para transferência");
            }
        }

        // ===============================
        // 2️⃣ Criação da Venda
        // ===============================
        Venda venda = new Venda();
        venda.setClienteNome(dto.getClienteNome());
        venda.setClienteEstado(dto.getClienteEstado());
        venda.setTipoMovimentacao(tipoMovimentacao);
        venda.setEstadoDestino(dto.getEstadoDestino());
        venda.setDataSaida(LocalDate.now());

        List<VendaItem> itensVenda = new ArrayList<>();
        double valorTotal = 0.0;

        // ===============================
        // 3️⃣ Processamento dos itens
        // ===============================
        for (VendaItemDTO itemDTO : dto.getItens()) {

            if (itemDTO.getCodigoOuNome() == null || itemDTO.getCodigoOuNome().isBlank()) {
                throw new RuntimeException("Produto não informado");
            }

            if (itemDTO.getQuantidade() <= 0) {
                throw new RuntimeException("Quantidade inválida para o produto: " + itemDTO.getCodigoOuNome());
            }

            Produto produto = produtoRepository
                    .findByCodigoOuNome(itemDTO.getCodigoOuNome())
                    .orElseThrow(() ->
                            new RuntimeException("Produto não encontrado: " + itemDTO.getCodigoOuNome())
                    );

           // 🔻 SEMPRE baixa estoque da matriz
estoqueService.baixarEstoque(produto, itemDTO.getQuantidade());

// ➕ Se for TRANSFERÊNCIA, entra no estoque da filial
if (tipoMovimentacao == TipoMovimentacao.T) {

    Filial filialDestino = Filial.valueOf(dto.getEstadoDestino());

    estoqueService.entradaEstoque(
            produto,
            filialDestino,
            itemDTO.getQuantidade()
    );
}


            VendaItem item = new VendaItem();
            item.setVenda(venda);
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setValorPorCaixa(itemDTO.getValorPorCaixa());

            double subtotal = itemDTO.getQuantidade() * itemDTO.getValorPorCaixa();
            item.setSubtotal(subtotal);

            valorTotal += subtotal;
            itensVenda.add(item);
        }

        // ===============================
        // 4️⃣ Finaliza e salva a venda
        // ===============================
        venda.setItens(itensVenda);
        venda.setValorTotal(valorTotal);

        vendaRepository.save(venda);

        // ===============================
        // 5️⃣ Geração do Financeiro (SEM ALTERAÇÃO)
        // ===============================
        ContaReceber conta = new ContaReceber();
        conta.setVenda(venda);
        conta.setClienteNome(venda.getClienteNome());
        conta.setValorOriginal(valorTotal);
        conta.setValorPago(0.0);
        conta.setSaldoDevedor(valorTotal);
        conta.setDataCriacao(LocalDate.now());

        contaReceberRepository.save(conta);
    }
}