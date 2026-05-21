package com.expedicao.estoque.repositorie;

import com.expedicao.estoque.model.PagamentoContaPagar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoContaPagarRepository
        extends JpaRepository<PagamentoContaPagar, Long> {
}