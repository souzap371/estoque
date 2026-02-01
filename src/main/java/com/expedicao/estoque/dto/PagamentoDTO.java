package com.expedicao.estoque.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.expedicao.estoque.model.FormaPagamento;
import com.expedicao.estoque.model.Pagamento;

public class PagamentoDTO {

    private Long id;
    private BigDecimal valorPago;
    private LocalDate dataPagamento;
    private FormaPagamento formaPagamento;
    private String anexoPath;

    public PagamentoDTO(Pagamento pagamento) {
        this.id = pagamento.getId();
        this.valorPago = pagamento.getValorPago();
        this.dataPagamento = pagamento.getDataPagamento();
        this.formaPagamento = pagamento.getFormaPagamento();
        this.anexoPath = pagamento.getAnexoPath();
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public String getAnexoPath() {
        return anexoPath;
    }
}
