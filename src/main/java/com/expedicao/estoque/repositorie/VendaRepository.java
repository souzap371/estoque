package com.expedicao.estoque.repositorie;

import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import com.expedicao.estoque.model.Venda;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    @Query("SELECT v FROM Venda v LEFT JOIN FETCH v.itens WHERE v.id = :id")
    Optional<Venda> findByIdComItens(Long id);
}
