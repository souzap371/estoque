package com.expedicao.estoque.repositorie;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expedicao.estoque.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    Optional<Produto> findByCodigo(String codigo);
    Optional<Produto> findByNome(String nome);
}
