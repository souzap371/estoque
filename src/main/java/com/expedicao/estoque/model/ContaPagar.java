package com.expedicao.estoque.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.expedicao.estoque.enums.StatusContaPagar;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "contas_pagar")
@Getter
@Setter
public class ContaPagar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // descrição da conta
    private String descricao;

    // valor original
    private BigDecimal valor;

    // saldo restante
    private BigDecimal saldoDevedor;

    // valor pago
    private BigDecimal valorPago;

    // vencimento
    private LocalDate dataVencimento;

    // data pagamento
    private LocalDate dataPagamento;

    // observação
    @Column(columnDefinition = "TEXT")
    private String observacao;

    // status
    @Enumerated(EnumType.STRING)
    private StatusContaPagar status;

    // relacionamento fornecedor
    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @OneToMany(mappedBy = "conta",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY)
private List<PagamentoContaPagar> pagamentos;

    
}