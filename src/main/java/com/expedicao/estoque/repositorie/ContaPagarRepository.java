package com.expedicao.estoque.repositorie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.expedicao.estoque.enums.StatusContaPagar;
import com.expedicao.estoque.model.ContaPagar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ContaPagarRepository
        extends JpaRepository<ContaPagar, Long> {

    // buscar por status
    List<ContaPagar> findByStatus(StatusContaPagar status);

    // buscar vencimento
    List<ContaPagar> findByDataVencimentoBefore(LocalDate data);

    @Query("""
                SELECT COALESCE(SUM(c.valorPago),0)
                FROM ContaPagar c
                WHERE c.dataPagamento BETWEEN :inicio AND :fim
            """)
    BigDecimal totalDespesas(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);

}