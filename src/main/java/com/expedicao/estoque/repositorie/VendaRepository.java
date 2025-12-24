package com.expedicao.estoque.repositorie;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expedicao.estoque.model.Venda;

public interface VendaRepository extends JpaRepository<Venda, Long> {
}
