package com.expedicao.estoque.repositorie;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.expedicao.estoque.model.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    @Query("""
                SELECT COALESCE(SUM(p.valorPago),0)
                FROM Pagamento p
                WHERE p.dataPagamento BETWEEN :inicio AND :fim
            """)
    BigDecimal totalReceitasPorDataPagamento(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);
}
