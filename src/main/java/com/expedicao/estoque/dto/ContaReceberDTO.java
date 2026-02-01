package com.expedicao.estoque.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.expedicao.estoque.model.ContaReceber;

public class ContaReceberDTO {

    private Long id;
    private String clienteNome;
    private BigDecimal valorOriginal;
    private BigDecimal valorPago;
    private BigDecimal saldoDevedor;
    private LocalDate dataCriacao;

    private List<PagamentoDTO> pagamentos;

    public ContaReceberDTO(ContaReceber conta) {

        this.id = conta.getId();
        this.clienteNome = conta.getClienteNome();
        this.valorOriginal = conta.getValorOriginal();
        this.valorPago = conta.getValorPago();
        this.saldoDevedor = conta.getSaldoDevedor();
        this.dataCriacao = conta.getDataCriacao();

        // ✅ CARREGA PAGAMENTOS
        if (conta.getPagamentos() != null) {
            this.pagamentos = conta.getPagamentos()
                    .stream()
                    .map(PagamentoDTO::new)
                    .collect(Collectors.toList());
        }
    }

    public Long getId() {
        return id;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public BigDecimal getValorOriginal() {
        return valorOriginal;
    }

    public BigDecimal getValorPago() {
        return valorPago;
    }

    public BigDecimal getSaldoDevedor() {
        return saldoDevedor;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }

    public List<PagamentoDTO> getPagamentos() {
        return pagamentos;
    }
}
