package com.expedicao.estoque.repositorie;

import com.expedicao.estoque.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstoqueFilialRepository
        extends JpaRepository<EstoqueFilial, Long> {

    Optional<EstoqueFilial> findByProdutoAndFilial(Produto produto, Filial filial);
}
