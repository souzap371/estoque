package com.expedicao.estoque.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Produto produto;

    private Integer quantidade;

    private String clienteNome;
    private String clienteEstado;

    private LocalDate dataSaida;

    private Boolean comNotaFiscal;

    public Produto getProduto() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProduto'");
    }

    public int getQuantidade() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getQuantidade'");
    }

    public void setProduto(Produto produto2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setProduto'");
    }

    public void setQuantidade(Integer quantidade2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setQuantidade'");
    }

    public void setClienteNome(String clienteNome2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setClienteNome'");
    }

    public void setClienteEstado(String clienteEstado2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setClienteEstado'");
    }

    public void setDataSaida(LocalDate now) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDataSaida'");
    }

    public void setComNotaFiscal(Boolean comNotaFiscal2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setComNotaFiscal'");
    }
}
