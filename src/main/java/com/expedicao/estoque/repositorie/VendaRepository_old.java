// package com.expedicao.estoque.repositorie;

// import java.math.BigDecimal;
// import java.util.Optional;
// import org.springframework.data.jpa.repository.*;
// import com.expedicao.estoque.model.Venda;

// public interface VendaRepository extends JpaRepository<Venda, Long> {

//     @Query("SELECT v FROM Venda v LEFT JOIN FETCH v.itens WHERE v.id = :id")
//     Optional<Venda> findByIdComItens(Long id);

//     @Query("""
//                 SELECT COALESCE(SUM(v.valorTotal),0)
//                 FROM Venda v
//                 WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE)
//                   AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
//             """)
//     BigDecimal totalVendasMes();

//     @Query("""
//                 SELECT COUNT(v)
//                 FROM Venda v
//                 WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE)
//                   AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
//             """)
//     Long totalPedidosMes();

//     @Query("""
//                 SELECT COUNT(DISTINCT v.clienteNome)
//                 FROM Venda v
//                 WHERE MONTH(v.dataSaida)=MONTH(CURRENT_DATE)
//                   AND YEAR(v.dataSaida)=YEAR(CURRENT_DATE)
//             """)
//     Long totalClientesAtivos();
// }
