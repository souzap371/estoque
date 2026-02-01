package com.expedicao.estoque.repositorie;

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

    List<ContaReceber> findByClienteNomeIgnoreCase(String clienteNome);

    @Query("SELECT c FROM ContaReceber c LEFT JOIN FETCH c.pagamentos")
    List<ContaReceber> buscarRelatorioCompleto();

    @Query("""
                SELECT cr FROM ContaReceber cr
                WHERE (:cliente IS NULL OR :cliente = '' OR cr.clienteNome = :cliente)
            """)
    List<ContaReceber> buscarComFiltro(@Param("cliente") String cliente);

}
