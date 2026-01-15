package com.expedicao.estoque.repositorie;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expedicao.estoque.model.Estoque;
import com.expedicao.estoque.model.Filial;
import com.expedicao.estoque.model.Produto;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    // 🔹 Mantido para compatibilidade (não quebra nada existente)
    Optional<Estoque> findByProduto(Produto produto);

    // 🔹 NOVO — busca estoque por produto e filial
    Optional<Estoque> findByProdutoAndFilial(Produto produto, Filial filial);

    // 🔹 NOVO — lista estoque por filial (MG / AL / MATRIZ)
    List<Estoque> findByFilial(Filial filial);
}