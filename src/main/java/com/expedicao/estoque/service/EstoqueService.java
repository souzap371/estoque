package com.expedicao.estoque.service;

import com.expedicao.estoque.model.*;
import com.expedicao.estoque.repositorie.EstoqueRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;

    public EstoqueService(EstoqueRepository estoqueRepository) {
        this.estoqueRepository = estoqueRepository;
    }

    // =========================================================
    // 🔽 BAIXA DE ESTOQUE DA MATRIZ (VENDA NORMAL)
    // =========================================================
    @Transactional
    public void baixarEstoque(Produto produto, int quantidade) {

        validarProdutoEQuantidade(produto, quantidade);

        Estoque estoque = estoqueRepository
                .findByProdutoAndFilial(produto, Filial.MATRIZ)
                .orElseThrow(() -> new RuntimeException(
                        "Estoque não encontrado na MATRIZ para o produto: " + produto.getNome()));

        if (estoque.getQuantidadeAtual() < quantidade) {
            throw new RuntimeException("Estoque insuficiente na MATRIZ");
        }

        estoque.setQuantidadeAtual(estoque.getQuantidadeAtual() - quantidade);
        estoqueRepository.save(estoque);
    }

    // =========================================================
    // 🔼 ENTRADA DE ESTOQUE (COMPRA OU TRANSFERÊNCIA RECEBIDA)
    // =========================================================
    @Transactional
    public void entradaEstoque(Produto produto, Filial filial, int quantidade) {

        validarProdutoEQuantidade(produto, quantidade);

        Estoque estoque = estoqueRepository
                .findByProdutoAndFilial(produto, filial)
                .orElseGet(() -> {
                    Estoque novo = new Estoque();
                    novo.setProduto(produto);
                    novo.setFilial(filial);
                    novo.setQuantidadeAtual(0);
                    return novo;
                });

        estoque.setQuantidadeAtual(estoque.getQuantidadeAtual() + quantidade);
        estoqueRepository.save(estoque);
    }

    // =========================================================
    // 🔁 DEVOLVER ESTOQUE PARA MATRIZ (AO CANCELAR VENDA)
    // =========================================================
    @Transactional
    public void devolverEstoque(Produto produto, int quantidade) {

        validarProdutoEQuantidade(produto, quantidade);

        Estoque estoque = estoqueRepository
                .findByProdutoAndFilial(produto, Filial.MATRIZ)
                .orElseThrow(() -> new RuntimeException(
                        "Estoque não encontrado na MATRIZ para o produto: " + produto.getNome()));

        estoque.setQuantidadeAtual(estoque.getQuantidadeAtual() + quantidade);
        estoqueRepository.save(estoque);
    }

    // =========================================================
    // 🔽 BAIXAR ESTOQUE DE UMA FILIAL (TRANSFERÊNCIA)
    // =========================================================
    @Transactional
    public void baixarEstoqueFilial(Produto produto, Filial filial, int quantidade) {

        validarProdutoEQuantidade(produto, quantidade);

        Estoque estoque = estoqueRepository
                .findByProdutoAndFilial(produto, filial)
                .orElseThrow(() -> new RuntimeException(
                        "Estoque não encontrado na filial " + filial + " para o produto: " + produto.getNome()));

        if (estoque.getQuantidadeAtual() < quantidade) {
            throw new RuntimeException("Estoque insuficiente na filial " + filial);
        }

        estoque.setQuantidadeAtual(estoque.getQuantidadeAtual() - quantidade);
        estoqueRepository.save(estoque);
    }

    // =========================================================
    // 📦 CONSULTA ESTOQUE POR FILIAL
    // =========================================================
    public List<Estoque> listarPorFilial(Filial filial) {
        return estoqueRepository.findByFilial(filial);
    }

    // =========================================================
    // 🛡 VALIDAÇÃO PADRÃO
    // =========================================================
    private void validarProdutoEQuantidade(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto inválido");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
    }
}
