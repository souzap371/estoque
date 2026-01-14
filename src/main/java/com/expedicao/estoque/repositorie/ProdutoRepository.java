package com.expedicao.estoque.repositorie;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.expedicao.estoque.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Optional<Produto> findByCodigo(String codigo);

    Optional<Produto> findByNome(String nome);

    @Query("""
        SELECT p FROM Produto p
        WHERE p.codigo = :valor OR p.nome = :valor
    """)
    Optional<Produto> findByCodigoOuNome(@Param("valor") String valor);
}

