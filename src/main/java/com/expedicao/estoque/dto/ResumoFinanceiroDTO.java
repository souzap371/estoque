package com.expedicao.estoque.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ResumoFinanceiroDTO {
    private BigDecimal receitas;
    private BigDecimal despesas;
    private BigDecimal lucroLiquido;
    private double margemPercentual;

    public ResumoFinanceiroDTO() {}

    public ResumoFinanceiroDTO(BigDecimal receitas, BigDecimal despesas) {
        this.receitas = receitas != null ? receitas : BigDecimal.ZERO;
        this.despesas = despesas != null ? despesas : BigDecimal.ZERO;
        this.lucroLiquido = this.receitas.subtract(this.despesas);
        this.margemPercentual = this.receitas.compareTo(BigDecimal.ZERO) > 0
            ? this.lucroLiquido.multiply(new BigDecimal("100"))
                .divide(this.receitas, 2, RoundingMode.HALF_UP).doubleValue()
            : 0.0;
    }

    public BigDecimal getReceitas() { return receitas; }
    public void setReceitas(BigDecimal receitas) { this.receitas = receitas; }

    public BigDecimal getDespesas() { return despesas; }
    public void setDespesas(BigDecimal despesas) { this.despesas = despesas; }

    public BigDecimal getLucroLiquido() { return lucroLiquido; }
    public void setLucroLiquido(BigDecimal lucroLiquido) { this.lucroLiquido = lucroLiquido; }

    public double getMargemPercentual() { return margemPercentual; }
    public void setMargemPercentual(double margemPercentual) { this.margemPercentual = margemPercentual; }
}