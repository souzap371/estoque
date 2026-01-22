package com.expedicao.estoque.repositorie;

import com.expedicao.estoque.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByProdutoAndFilial(Produto produto, Filial filial);

    List<Estoque> findByFilial(Filial filial);
}
