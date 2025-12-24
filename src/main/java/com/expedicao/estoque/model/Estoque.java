package com.expedicao.estoque.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidadeAtual = 0;

    public void setProduto(Produto produto2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setProduto'");
    }

    public Integer getQuantidadeAtual() {
    return quantidadeAtual;
}

public void setQuantidadeAtual(Integer quantidadeAtual) {
    this.quantidadeAtual = quantidadeAtual;
}
}
