package com.expedicao.estoque.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conta_receber_id")
    @JsonBackReference
    private ContaReceber contaReceber;

    @Column(nullable = false)
    private Double valorPago;

    @Column(nullable = false)
    private LocalDate dataPagamento;

    @Column(name = "anexo_nome")
    private String anexoNome;

    @Column(name = "anexo_tipo")
    private String anexoTipo;

    @Column(name = "anexo_path")
    private String anexoPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamento formaPagamento;

    @Column(length = 255)
    private String observacao;

    // =====================
    // GETTERS E SETTERS
    // =====================

    public Long getId() {
        return id;
    }

    public ContaReceber getContaReceber() {
        return contaReceber;
    }

    public void setContaReceber(ContaReceber contaReceber) {
        this.contaReceber = contaReceber;
    }

    public Double getValorPago() {
        return valorPago;
    }

    public void setValorPago(Double valorPago) {
        this.valorPago = valorPago;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getAnexoNome() {
        return anexoNome;
    }

    public void setAnexoNome(String anexoNome) {
        this.anexoNome = anexoNome;
    }

    public String getAnexoTipo() {
        return anexoTipo;
    }

    public void setAnexoTipo(String anexoTipo) {
        this.anexoTipo = anexoTipo;
    }

    public String getAnexoPath() {
        return anexoPath;
    }

    public void setAnexoPath(String anexoPath) {
        this.anexoPath = anexoPath;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }
}
