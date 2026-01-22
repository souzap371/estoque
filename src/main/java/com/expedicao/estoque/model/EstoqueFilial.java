package com.expedicao.estoque.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estoque_filial", uniqueConstraints = @UniqueConstraint(columnNames = { "produto_id", "filial" }))
public class EstoqueFilial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Produto produto;

    @Enumerated(EnumType.STRING)
    private Filial filial;

    private Integer quantidade;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Filial getFilial() {
        return filial;
    }

    public void setFilial(Filial filial) {
        this.filial = filial;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    // getters e setters

}
