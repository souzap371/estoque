package com.expedicao.estoque.repositorie;

import org.springframework.data.jpa.repository.JpaRepository;
import com.expedicao.estoque.enums.StatusContaPagar;
import com.expedicao.estoque.model.ContaPagar;

import java.time.LocalDate;
import java.util.List;

public interface ContaPagarRepository
        extends JpaRepository<ContaPagar, Long> {

    // buscar por status
    List<ContaPagar> findByStatus(StatusContaPagar status);

    // buscar vencimento
    List<ContaPagar> findByDataVencimentoBefore(LocalDate data);

}