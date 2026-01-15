package com.expedicao.estoque.repositorie;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expedicao.estoque.model.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
}
