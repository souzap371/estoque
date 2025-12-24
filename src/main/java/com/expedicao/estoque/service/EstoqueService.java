package com.expedicao.estoque.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expedicao.estoque.model.Estoque;
import com.expedicao.estoque.model.Produto;
import com.expedicao.estoque.repositorie.EstoqueRepository;

@Service
public class EstoqueService {

    @Autowired
    private EstoqueRepository estoqueRepository;

    public void entradaEstoque(Produto produto, int quantidade) {
        Estoque estoque = estoqueRepository.findByProduto(produto)
            .orElse(new Estoque());

        estoque.setProduto(produto);
        estoque.setQuantidadeAtual(
            (estoque.getQuantidadeAtual() == null ? 0 : estoque.getQuantidadeAtual()) + quantidade
        );

        estoqueRepository.save(estoque);
    }

    public void baixarEstoque(Produto produto, int quantidade) {
        Estoque estoque = estoqueRepository.findByProduto(produto)
            .orElseThrow(() -> new RuntimeException("Produto sem estoque"));

        if (estoque.getQuantidadeAtual() < quantidade) {
            throw new RuntimeException("Estoque insuficiente");
        }

        estoque.setQuantidadeAtual(estoque.getQuantidadeAtual() - quantidade);
        estoqueRepository.save(estoque);
    }
}

