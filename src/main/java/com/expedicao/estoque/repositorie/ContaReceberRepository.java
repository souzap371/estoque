package com.expedicao.estoque.repositorie;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.expedicao.estoque.model.ContaReceber;

public interface ContaReceberRepository extends JpaRepository<ContaReceber, Long> {

    List<ContaReceber> findBySaldoDevedorGreaterThan(Double valor);

    @Query("""
                SELECT DISTINCT c.clienteNome
                FROM ContaReceber c
                ORDER BY c.clienteNome
            """)
    List<String> listarClientes();
}