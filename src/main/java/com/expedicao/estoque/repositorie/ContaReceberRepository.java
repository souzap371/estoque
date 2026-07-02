package com.expedicao.estoque.repositorie;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.expedicao.estoque.model.ContaReceber;

@Repository
public interface ContaReceberRepository extends JpaRepository<ContaReceber, Long> {

        Optional<ContaReceber> findByVendaId(Long vendaId);

        void deleteByVendaId(Long vendaId);

        @Query("""
                            SELECT DISTINCT c.clienteNome
                            FROM ContaReceber c
                            WHERE c.clienteNome IS NOT NULL
                            ORDER BY c.clienteNome ASC
                        """)
        List<String> listarClientes();

        @Query("""
                            SELECT c
                            FROM ContaReceber c
                            WHERE c.clienteNome = :cliente
                            AND c.saldoDevedor > 0
                            ORDER BY c.dataCriacao ASC
                        """)
        List<ContaReceber> buscarContasAbertasCliente(
                        @Param("cliente") String cliente);

        @Query("""
                        SELECT c
                        FROM ContaReceber c
                        WHERE LOWER(c.clienteNome)
                        LIKE LOWER(CONCAT('%', :cliente, '%'))
                        AND (c.valorOriginal - c.valorPago) > 0
                        ORDER BY c.dataCriacao
                        """)
        List<ContaReceber> buscarEmAbertoPorCliente(
                        @Param("cliente") String cliente);

        List<ContaReceber> findByClienteNomeIgnoreCase(String clienteNome);

        // ATUALIZADO: JOIN FETCH na venda para carregar dataPedido no DTO
        @Query("SELECT c FROM ContaReceber c LEFT JOIN FETCH c.pagamentos LEFT JOIN FETCH c.venda")
        List<ContaReceber> buscarRelatorioCompleto();

        @Query("""
                            SELECT cr FROM ContaReceber cr
                            WHERE (:cliente IS NULL OR :cliente = '' OR cr.clienteNome = :cliente)
                        """)
        List<ContaReceber> buscarComFiltro(@Param("cliente") String cliente);

        @Query("""
                            SELECT COALESCE(SUM(c.valorPago),0)
                            FROM ContaReceber c
                            WHERE c.dataCriacao BETWEEN :inicio AND :fim
                        """)
        BigDecimal totalReceitas(
                        @Param("inicio") LocalDate inicio,
                        @Param("fim") LocalDate fim);
}