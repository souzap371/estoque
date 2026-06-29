// package com.expedicao.estoque.repositorie;

// import java.math.BigDecimal;
// import java.util.Optional;
// import org.springframework.data.jpa.repository.*;
// import com.expedicao.estoque.model.Venda;

// public interface VendaRepository extends JpaRepository<Venda, Long> {

//     @Query("SELECT v FROM Venda v LEFT JOIN FETCH v.itens WHERE v.id = :id")
//     Optional<Venda> findByIdComItens(Long id);

//     // =================================================
//     // 📊 KPIs - VENDAS DO MÊS ATUAL
//     // =================================================
//     @Query("""
//                 SELECT COALESCE(SUM(v.valorTotal),0)
//                 FROM Venda v
//                 WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE)
//                   AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
//             """)
//     BigDecimal totalVendasMesAtual();

//     // =================================================
//     // 📊 KPIs - VENDAS DO MÊS ANTERIOR
//     // =================================================
//     @Query("""
//                 SELECT COALESCE(SUM(v.valorTotal),0)
//                 FROM Venda v
//                 WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE) - 1
//                   AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
//             """)
//     BigDecimal totalVendasMesAnterior();

//     // =================================================
//     // 📊 KPIs - TOTAL DE PEDIDOS DO MÊS ATUAL
//     // =================================================
//     @Query("""
//                 SELECT COUNT(v)
//                 FROM Venda v
//                 WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE)
//                   AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
//             """)
//     Long totalPedidosMesAtual();

//     // =================================================
//     // 📊 KPIs - TOTAL DE PEDIDOS DO MÊS ANTERIOR
//     // =================================================
//     @Query("""
//                 SELECT COUNT(v)
//                 FROM Venda v
//                 WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE) - 1
//                   AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
//             """)
//     Long totalPedidosMesAnterior();

//     // =================================================
//     // 📊 KPIs - CLIENTES ATIVOS DO MÊS ATUAL
//     // =================================================
//     @Query("""
//                 SELECT COUNT(DISTINCT v.clienteNome)
//                 FROM Venda v
//                 WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE)
//                   AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
//             """)
//     Long totalClientesAtivosMesAtual();

//     // =================================================
//     // 📊 KPIs - CLIENTES ATIVOS DO MÊS ANTERIOR
//     // =================================================
//     @Query("""
//                 SELECT COUNT(DISTINCT v.clienteNome)
//                 FROM Venda v
//                 WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE) - 1
//                   AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
//             """)
//     Long totalClientesAtivosMesAnterior();

//     // =================================================
//     // 📊 KPIs - TOTAL DE PRODUTOS VENDIDOS NO MÊS ATUAL
//     // =================================================
//     @Query("""
//                 SELECT COALESCE(SUM(vi.quantidade), 0)
//                 FROM VendaItem vi
//                 JOIN vi.venda v
//                 WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE)
//                   AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
//             """)
//     Long totalProdutosVendidosMesAtual();

//     // =================================================
//     // 📊 KPIs - TOTAL DE PRODUTOS VENDIDOS NO MÊS ANTERIOR
//     // =================================================
//     @Query("""
//                 SELECT COALESCE(SUM(vi.quantidade), 0)
//                 FROM VendaItem vi
//                 JOIN vi.venda v
//                 WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE) - 1
//                   AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
//             """)
//     Long totalProdutosVendidosMesAnterior();

//     // =================================================
//     // 📊 KPIs - SPARKLINE CLIENTES (últimos 10 dias)
//     // =================================================
//     @Query(value = """
//                 SELECT COALESCE(COUNT(DISTINCT v.cliente_nome), 0)
//                 FROM venda v
//                 WHERE v.data_saida = CURRENT_DATE - CAST(:dias AS INTEGER)
//                 GROUP BY v.data_saida
//             """, nativeQuery = true)
//     Long totalClientesPorDia(int dias);

//     // =================================================
//     // 📊 KPIs - SPARKLINE PRODUTOS (últimos 10 dias)
//     // =================================================
//     @Query(value = """
//                 SELECT COALESCE(SUM(vi.quantidade), 0)
//                 FROM venda_item vi
//                 JOIN venda v ON vi.venda_id = v.id
//                 WHERE v.data_saida = CURRENT_DATE - CAST(:dias AS INTEGER)
//                 GROUP BY v.data_saida
//             """, nativeQuery = true)
//     Long totalProdutosPorDia(int dias);

// }


package com.expedicao.estoque.repositorie;

import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import com.expedicao.estoque.model.Venda;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    @Query("SELECT v FROM Venda v LEFT JOIN FETCH v.itens WHERE v.id = :id")
    Optional<Venda> findByIdComItens(Long id);

    // =================================================
    // 📊 KPIs - VENDAS DO MÊS ATUAL
    // =================================================
    @Query("""
                SELECT COALESCE(SUM(v.valorTotal),0)
                FROM Venda v
                WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE)
                  AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
            """)
    BigDecimal totalVendasMesAtual();

    // =================================================
    // 📊 KPIs - VENDAS DO MÊS ANTERIOR
    // =================================================
    @Query("""
                SELECT COALESCE(SUM(v.valorTotal),0)
                FROM Venda v
                WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE) - 1
                  AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
            """)
    BigDecimal totalVendasMesAnterior();

    // =================================================
    // 📊 KPIs - TOTAL DE PEDIDOS DO MÊS ATUAL
    // =================================================
    @Query("""
                SELECT COUNT(v)
                FROM Venda v
                WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE)
                  AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
            """)
    Long totalPedidosMesAtual();

    // =================================================
    // 📊 KPIs - TOTAL DE PEDIDOS DO MÊS ANTERIOR
    // =================================================
    @Query("""
                SELECT COUNT(v)
                FROM Venda v
                WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE) - 1
                  AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
            """)
    Long totalPedidosMesAnterior();

    // =================================================
    // 📊 KPIs - CLIENTES ATIVOS DO MÊS ATUAL
    // =================================================
    @Query("""
                SELECT COUNT(DISTINCT v.clienteNome)
                FROM Venda v
                WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE)
                  AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
            """)
    Long totalClientesAtivosMesAtual();

    // =================================================
    // 📊 KPIs - CLIENTES ATIVOS DO MÊS ANTERIOR
    // =================================================
    @Query("""
                SELECT COUNT(DISTINCT v.clienteNome)
                FROM Venda v
                WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE) - 1
                  AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
            """)
    Long totalClientesAtivosMesAnterior();

    // =================================================
    // 📊 KPIs - TOTAL DE PRODUTOS VENDIDOS NO MÊS ATUAL
    // =================================================
    @Query("""
                SELECT COALESCE(SUM(vi.quantidade), 0)
                FROM VendaItem vi
                JOIN vi.venda v
                WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE)
                  AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
            """)
    Long totalProdutosVendidosMesAtual();

    // =================================================
    // 📊 KPIs - TOTAL DE PRODUTOS VENDIDOS NO MÊS ANTERIOR
    // =================================================
    @Query("""
                SELECT COALESCE(SUM(vi.quantidade), 0)
                FROM VendaItem vi
                JOIN vi.venda v
                WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE) - 1
                  AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
            """)
    Long totalProdutosVendidosMesAnterior();

    // =================================================
    // 📊 KPIs - SPARKLINE VENDAS (últimos 10 dias)
    // =================================================
    @Query(value = """
                SELECT COALESCE(SUM(v.valor_total), 0)
                FROM venda v
                WHERE v.data_saida = CURRENT_DATE - CAST(:dias AS INTEGER)
                GROUP BY v.data_saida
            """, nativeQuery = true)
    BigDecimal totalVendasPorDia(int dias);

    // =================================================
    // 📊 KPIs - SPARKLINE PEDIDOS (últimos 10 dias)
    // =================================================
    @Query(value = """
                SELECT COALESCE(COUNT(*), 0)
                FROM venda v
                WHERE v.data_saida = CURRENT_DATE - CAST(:dias AS INTEGER)
                GROUP BY v.data_saida
            """, nativeQuery = true)
    Long totalPedidosPorDia(int dias);

    // =================================================
    // 📊 KPIs - SPARKLINE CLIENTES (últimos 10 dias)
    // =================================================
    @Query(value = """
                SELECT COALESCE(COUNT(DISTINCT v.cliente_nome), 0)
                FROM venda v
                WHERE v.data_saida = CURRENT_DATE - CAST(:dias AS INTEGER)
                GROUP BY v.data_saida
            """, nativeQuery = true)
    Long totalClientesPorDia(int dias);

    // =================================================
    // 📊 KPIs - SPARKLINE PRODUTOS (últimos 10 dias)
    // =================================================
    @Query(value = """
                SELECT COALESCE(SUM(vi.quantidade), 0)
                FROM venda_item vi
                JOIN venda v ON vi.venda_id = v.id
                WHERE v.data_saida = CURRENT_DATE - CAST(:dias AS INTEGER)
                GROUP BY v.data_saida
            """, nativeQuery = true)
    Long totalProdutosPorDia(int dias);
}