package com.expedicao.estoque.repositorie;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.expedicao.estoque.model.VendaItem;

public interface VendaItemRepository extends JpaRepository<VendaItem, Long> {

        @Query(value = """
                        SELECT vi.*
                        FROM venda_item vi
                        JOIN venda v ON v.id = vi.venda_id
                        JOIN produto p ON p.id = vi.produto_id
                        WHERE
                              (:pedido IS NULL OR v.id = :pedido)
                          AND (:produto IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :produto, '%')))
                          AND (:cliente IS NULL OR LOWER(v.cliente_nome) LIKE LOWER(CONCAT('%', :cliente, '%')))
                          AND (:estado IS NULL OR LOWER(v.cliente_estado) LIKE LOWER(CONCAT('%', :estado, '%')))
                          AND (:tipo IS NULL OR vi.tipo_movimentacao = :tipo)
                          AND (:notaFiscal IS NULL OR v.com_nota_fiscal = CAST(:notaFiscal AS BOOLEAN))
                          AND v.data_saida >= :dataInicio
                          AND v.data_saida <= :dataFim
                        ORDER BY v.data_saida DESC
                        """, countQuery = """
                        SELECT COUNT(*)
                        FROM venda_item vi
                        JOIN venda v ON v.id = vi.venda_id
                        JOIN produto p ON p.id = vi.produto_id
                        WHERE
                              (:pedido IS NULL OR v.id = :pedido)
                          AND (:produto IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :produto, '%')))
                          AND (:cliente IS NULL OR LOWER(v.cliente_nome) LIKE LOWER(CONCAT('%', :cliente, '%')))
                          AND (:estado IS NULL OR LOWER(v.cliente_estado) LIKE LOWER(CONCAT('%', :estado, '%')))
                          AND (:tipo IS NULL OR vi.tipo_movimentacao = :tipo)
                          AND (:notaFiscal IS NULL OR v.com_nota_fiscal = CAST(:notaFiscal AS BOOLEAN))
                          AND v.data_saida >= :dataInicio
                          AND v.data_saida <= :dataFim
                        """, nativeQuery = true)
        Page<VendaItem> filtrar(
                        @Param("pedido") Long pedido,
                        @Param("produto") String produto,
                        @Param("cliente") String cliente,
                        @Param("estado") String estado,
                        @Param("tipo") String tipo, // <-- nativeQuery precisa String para enum
                        @Param("notaFiscal") Boolean notaFiscal,
                        @Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim,
                        Pageable pageable);

        @Query(value = """
                        SELECT vi.*
                        FROM venda_item vi
                        JOIN venda v ON v.id = vi.venda_id
                        JOIN produto p ON p.id = vi.produto_id
                        WHERE
                              (:pedido IS NULL OR v.id = :pedido)
                          AND (:produto IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :produto, '%')))
                          AND (:cliente IS NULL OR LOWER(v.cliente_nome) LIKE LOWER(CONCAT('%', :cliente, '%')))
                          AND (:estado IS NULL OR LOWER(v.cliente_estado) LIKE LOWER(CONCAT('%', :estado, '%')))
                          AND (:tipo IS NULL OR vi.tipo_movimentacao = :tipo)
                          AND (:notaFiscal IS NULL OR v.com_nota_fiscal = CAST(:notaFiscal AS BOOLEAN))
                          AND v.data_saida >= :dataInicio
                          AND v.data_saida <= :dataFim
                        ORDER BY v.data_saida DESC, v.id DESC, vi.id
                        """, nativeQuery = true)
        List<VendaItem> filtrarTodos(
                        @Param("pedido") Long pedido,
                        @Param("produto") String produto,
                        @Param("cliente") String cliente,
                        @Param("estado") String estado,
                        @Param("tipo") String tipo,
                        @Param("notaFiscal") Boolean notaFiscal,
                        @Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim);

        // ==========================================================
        // LISTAS PARA OS FILTROS
        // ==========================================================

        @Query("""
                        SELECT DISTINCT v.id
                        FROM VendaItem vi
                        JOIN vi.venda v
                        ORDER BY v.id
                        """)
        List<Long> buscarPedidos();

        @Query("""
                        SELECT DISTINCT p.nome
                        FROM VendaItem vi
                        JOIN vi.produto p
                        ORDER BY p.nome
                        """)
        List<String> buscarProdutos();

        @Query("""
                        SELECT DISTINCT v.clienteNome
                        FROM VendaItem vi
                        JOIN vi.venda v
                        ORDER BY v.clienteNome
                        """)
        List<String> buscarClientes();

        @Query("""
                        SELECT DISTINCT v.clienteEstado
                        FROM VendaItem vi
                        JOIN vi.venda v
                        ORDER BY v.clienteEstado
                        """)
        List<String> buscarEstados();

        // ==========================================================
        // EXCLUSÃO DOS ITENS DA VENDA
        // ==========================================================

        @Modifying
        @Query("""
                        DELETE FROM VendaItem vi
                        WHERE vi.venda.id = :id
                        """)
        void deleteByVendaId(@Param("id") Long id);

}
