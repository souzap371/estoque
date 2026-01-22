package com.expedicao.estoque.dto;

public class VendaItemDTO {

    private String codigoOuNome;
    private Integer quantidade;
    private Double valorPorCaixa;

    public String getCodigoOuNome() {
        return codigoOuNome;
    }

    public void setCodigoOuNome(String codigoOuNome) {
        this.codigoOuNome = codigoOuNome;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Double getValorPorCaixa() {
        return valorPorCaixa;
    }

    public void setValorPorCaixa(Double valorPorCaixa) {
        this.valorPorCaixa = valorPorCaixa;
    }

    // getters e setters

}
