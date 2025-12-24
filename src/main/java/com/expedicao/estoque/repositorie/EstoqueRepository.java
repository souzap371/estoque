package com.expedicao.estoque.repositorie;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expedicao.estoque.model.Estoque;
import com.expedicao.estoque.model.Produto;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    Optional<Estoque> findByProduto(Produto produto);
}

