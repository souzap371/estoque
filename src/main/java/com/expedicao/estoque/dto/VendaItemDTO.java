package com.expedicao.estoque.dto;

public class VendaItemDTO {

    private String codigoOuNome;
    private Integer quantidade;
    private Double valorPorCaixa;
    private String tipoMovimentacao;
    private String estadoDestino;

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

    public String getTipoMovimentacao() {
        return tipoMovimentacao;
    }

    public void setTipoMovimentacao(String tipoMovimentacao) {
        this.tipoMovimentacao = tipoMovimentacao;
    }

    public String getEstadoDestino() {
        return estadoDestino;
    }

    public void setEstadoDestino(String estadoDestino) {
        this.estadoDestino = estadoDestino;
    }

}
