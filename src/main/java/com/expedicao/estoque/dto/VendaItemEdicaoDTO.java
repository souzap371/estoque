package com.expedicao.estoque.dto;

import java.math.BigDecimal;

import com.expedicao.estoque.model.VendaItem;

public class VendaItemEdicaoDTO {

    private String produtoNome;
    private String produtoCodigo;
    private Integer quantidade;
    private BigDecimal valorPorCaixa;
    private String tipoMovimentacao;
    private String estadoDestino;

    public VendaItemEdicaoDTO(VendaItem item) {
        this.produtoNome = item.getProduto().getNome();
        this.produtoCodigo = item.getProduto().getCodigo();
        this.quantidade = item.getQuantidade();
        this.valorPorCaixa = item.getValorPorCaixa();
        this.tipoMovimentacao = item.getTipoMovimentacao().name();
        this.estadoDestino = item.getEstadoDestino();
    }

    public String getProdutoNome() {
        return produtoNome;
    }

    public void setProdutoNome(String produtoNome) {
        this.produtoNome = produtoNome;
    }

    public String getProdutoCodigo() {
        return produtoCodigo;
    }

    public void setProdutoCodigo(String produtoCodigo) {
        this.produtoCodigo = produtoCodigo;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorPorCaixa() {
        return valorPorCaixa;
    }

    public void setValorPorCaixa(BigDecimal valorPorCaixa) {
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

    // getters

}
