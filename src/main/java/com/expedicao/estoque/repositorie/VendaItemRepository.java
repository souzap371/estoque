package com.expedicao.estoque.repositorie;

import java.time.LocalDate;
import java.util.List;

import com.expedicao.estoque.model.TipoMovimentacao;
import com.expedicao.estoque.model.VendaItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface VendaItemRepository extends JpaRepository<VendaItem, Long> {

        @Query(value = """
                        SELECT vi FROM VendaItem vi
                        JOIN FETCH vi.venda v
                        JOIN FETCH vi.produto p
                        WHERE (:pedido IS NULL OR v.id = :pedido)

                        AND (:produto IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', CAST(:produto
                        AS string), '%')))
                        AND (:cliente IS NULL OR LOWER(v.clienteNome) LIKE LOWER(CONCAT('%',
                        CAST(:cliente AS string), '%')))
                        AND (:estado IS NULL OR LOWER(v.clienteEstado) LIKE LOWER(CONCAT('%',
                        CAST(:estado AS string), '%')))

                        AND (:tipo IS NULL OR vi.tipoMovimentacao = :tipo)
                        AND (:notaFiscal IS NULL OR v.comNotaFiscal = :notaFiscal)
                        AND (:dataInicio IS NULL OR v.dataSaida >= :dataInicio)
                        AND (:dataFim IS NULL OR v.dataSaida <= :dataFim)
                        ORDER BY v.dataSaida DESC
                        """, countQuery = """
                        SELECT COUNT(vi) FROM VendaItem vi
                        JOIN vi.venda v
                        JOIN vi.produto p
                        WHERE (:pedido IS NULL OR v.id = :pedido)

                        AND (:produto IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', CAST(:produto
                        AS string), '%')))
                        AND (:cliente IS NULL OR LOWER(v.clienteNome) LIKE LOWER(CONCAT('%',
                        CAST(:cliente AS string), '%')))
                        AND (:estado IS NULL OR LOWER(v.clienteEstado) LIKE LOWER(CONCAT('%',
                        CAST(:estado AS string), '%')))

                        AND (:tipo IS NULL OR vi.tipoMovimentacao = :tipo)
                        AND (:dataInicio IS NULL OR v.dataSaida >= :dataInicio)
                        AND (:dataFim IS NULL OR v.dataSaida <= :dataFim)
                        """)

        Page<VendaItem> filtrar(
                        @Param("pedido") Long pedido,
                        @Param("produto") String produto,
                        @Param("cliente") String cliente,
                        @Param("estado") String estado,
                        @Param("tipo") TipoMovimentacao tipo,
                        @Param("notaFiscal") Boolean notaFiscal,
                        @Param("dataInicio") LocalDate dataInicio,
                        @Param("dataFim") LocalDate dataFim,
                        Pageable pageable);

        // 📋 LISTAS PARA OS FILTROS (mantidas como estavam)

        @Query("SELECT DISTINCT v.id FROM VendaItem vi JOIN vi.venda v ORDER BY v.id")
        List<Long> buscarPedidos();

        @Query("SELECT DISTINCT p.nome FROM VendaItem vi JOIN vi.produto p ORDER BY p.nome")
        List<String> buscarProdutos();

        @Query("SELECT DISTINCT v.clienteNome FROM VendaItem vi JOIN vi.venda v ORDER BY v.clienteNome")
        List<String> buscarClientes();

        @Query("SELECT DISTINCT v.clienteEstado FROM VendaItem vi JOIN vi.venda v ORDER BY v.clienteEstado")
        List<String> buscarEstados();

        // 🗑 EXCLUSÃO EM CASCATA DOS ITENS DA VENDA
        @Modifying
        @Query("DELETE FROM VendaItem vi WHERE vi.venda.id = :id")
        void deleteByVendaId(@Param("id") Long id);
}
