
package com.expedicao.estoque.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class VendaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @ManyToOne
    private Produto produto;

    private Integer quantidade;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorPorCaixa;

    @Column(precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Enumerated(EnumType.STRING)
    private TipoMovimentacao tipoMovimentacao;

    private String estadoDestino;

    // GETTERS & SETTERS
    public Long getId() {
        return id;
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public TipoMovimentacao getTipoMovimentacao() {
        return tipoMovimentacao;
    }

    public void setTipoMovimentacao(TipoMovimentacao tipoMovimentacao) {
        this.tipoMovimentacao = tipoMovimentacao;
    }

    public String getEstadoDestino() {
        return estadoDestino;
    }

    public void setEstadoDestino(String estadoDestino) {
        this.estadoDestino = estadoDestino;
    }
}
