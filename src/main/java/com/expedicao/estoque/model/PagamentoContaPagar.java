package com.expedicao.estoque.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamentos_conta_pagar")
@Getter
@Setter
public class PagamentoContaPagar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // conta vinculada
    @ManyToOne
    @JoinColumn(name = "conta_id")
    private ContaPagar conta;

    // valor pago
    private BigDecimal valorPagamento;

    // data/hora
    private LocalDateTime dataPagamento;

}