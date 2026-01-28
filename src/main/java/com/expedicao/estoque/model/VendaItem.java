// package com.expedicao.estoque.model;

// import jakarta.persistence.*;

// @Entity
// public class VendaItem {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @ManyToOne(fetch = FetchType.EAGER)
//     @JoinColumn(name = "venda_id", nullable = false)
//     private Venda venda;

//     @Enumerated(EnumType.STRING)
//     private TipoMovimentacao tipoMovimentacao;

//     @ManyToOne
//     private Produto produto;

//     private Integer quantidade;
//     private Double valorPorCaixa;
//     private Double subtotal;

//     public Long getId() {
//         return id;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     public Venda getVenda() {
//         return venda;
//     }

//     public void setVenda(Venda venda) {
//         this.venda = venda;
//     }

//     public Produto getProduto() {
//         return produto;
//     }

//     public void setProduto(Produto produto) {
//         this.produto = produto;
//     }

//     public Integer getQuantidade() {
//         return quantidade;
//     }

//     public void setQuantidade(Integer quantidade) {
//         this.quantidade = quantidade;
//     }

//     public TipoMovimentacao getTipoMovimentacao() {
//         return tipoMovimentacao;
//     }

//     public void setTipoMovimentacao(TipoMovimentacao tipoMovimentacao) {
//         this.tipoMovimentacao = tipoMovimentacao;
//     }

//     public Double getValorPorCaixa() {
//         return valorPorCaixa;
//     }

//     public void setValorPorCaixa(Double valorPorCaixa) {
//         this.valorPorCaixa = valorPorCaixa;
//     }

//     public Double getSubtotal() {
//         return subtotal;
//     }

//     public void setSubtotal(Double subtotal) {
//         this.subtotal = subtotal;
//     }

//     // getters e setters

// }


package com.expedicao.estoque.model;

import jakarta.persistence.*;

@Entity
public class VendaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @ManyToOne
    private Produto produto;

    private Integer quantidade;
    private Double valorPorCaixa;
    private Double subtotal;

    // 🆕 TIPO POR ITEM
    @Enumerated(EnumType.STRING)
    private TipoMovimentacao tipoMovimentacao;

    // 🆕 DESTINO POR ITEM (usado quando for transferência)
    private String estadoDestino;

    // ================= GETTERS & SETTERS =================

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

    public Double getValorPorCaixa() {
        return valorPorCaixa;
    }

    public void setValorPorCaixa(Double valorPorCaixa) {
        this.valorPorCaixa = valorPorCaixa;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
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
