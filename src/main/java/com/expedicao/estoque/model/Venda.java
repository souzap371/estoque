package com.expedicao.estoque.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clienteNome;
    private String clienteEstado;

    // @Enumerated(EnumType.STRING)
    // private TipoMovimentacao tipoMovimentacao;

    // private String estadoDestino;

    private LocalDate dataSaida;

    private Double valorTotal;
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendaItem> itens;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public String getClienteEstado() {
        return clienteEstado;
    }

    public void setClienteEstado(String clienteEstado) {
        this.clienteEstado = clienteEstado;
    }

    // public TipoMovimentacao getTipoMovimentacao() {
    // return tipoMovimentacao;
    // }

    // public void setTipoMovimentacao(TipoMovimentacao tipoMovimentacao) {
    // this.tipoMovimentacao = tipoMovimentacao;
    // }

    // public String getEstadoDestino() {
    //     return estadoDestino;
    // }

    // public void setEstadoDestino(String estadoDestino) {
    //     this.estadoDestino = estadoDestino;
    // }

    public LocalDate getDataSaida() {
        return dataSaida;
    }

    public void setDataSaida(LocalDate dataSaida) {
        this.dataSaida = dataSaida;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<VendaItem> getItens() {
        return itens;
    }

    public void setItens(List<VendaItem> itens) {
        this.itens = itens;
    }

    // getters e setters

}
