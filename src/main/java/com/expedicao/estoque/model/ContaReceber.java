package com.expedicao.estoque.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
@Table(name = "conta_receber")
public class ContaReceber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "venda_id", nullable = false, unique = true)
    @JsonIgnore
    private Venda venda;

    @Column(nullable = false, length = 150)
    private String clienteNome;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorOriginal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoDevedor = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate dataCriacao = LocalDate.now();

    // @OneToMany(mappedBy = "contaReceber", cascade = CascadeType.ALL,
    // orphanRemoval = true)
    // @JsonManagedReference
    // private List<Pagamento> pagamentos;
    @OneToMany(mappedBy = "contaReceber", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Pagamento> pagamentos = new ArrayList<>();

    // =========================
    // REGRAS DE NEGÓCIO
    // =========================

    public void registrarPagamento(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero");
        }

        this.valorPago = this.valorPago.add(valor);
        this.saldoDevedor = this.valorOriginal.subtract(this.valorPago);

        if (this.saldoDevedor.compareTo(BigDecimal.ZERO) < 0) {
            this.saldoDevedor = BigDecimal.ZERO;
        }
    }

    // =========================
    // GETTERS E SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public BigDecimal getValorOriginal() {
        return valorOriginal;
    }

    public void setValorOriginal(BigDecimal valorOriginal) {
        this.valorOriginal = valorOriginal;
        atualizarSaldo();
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public void setValorPago(BigDecimal valorPago) {
        this.valorPago = valorPago;
        atualizarSaldo();
    }

    public BigDecimal getSaldoDevedor() {
        return saldoDevedor;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    public void setPagamentos(List<Pagamento> pagamentos) {
        this.pagamentos = pagamentos;
    }

    private void atualizarSaldo() {
        if (valorOriginal != null && valorPago != null) {
            this.saldoDevedor = valorOriginal.subtract(valorPago);
            if (this.saldoDevedor.compareTo(BigDecimal.ZERO) < 0) {
                this.saldoDevedor = BigDecimal.ZERO;
            }
        }
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSaldoDevedor(BigDecimal saldoDevedor) {
        this.saldoDevedor = saldoDevedor;
    }

    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
