package com.expedicao.estoque.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clienteNome;
    private String clienteEstado;
    private LocalDate dataSaida;

    @Column(precision = 14, scale = 2)
    private BigDecimal valorTotal;

    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VendaItem> itens = new ArrayList<>();

    // =====================
    // MÉTODOS AUXILIARES
    // =====================
    public void adicionarItem(VendaItem item) {
        item.setVenda(this);
        this.itens.add(item);
    }

    public void limparItens() {
        this.itens.clear(); // orphanRemoval cuida do delete
    }

    // GETTERS & SETTERS
    public Long getId() {
        return id;
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

    public LocalDate getDataSaida() {
        return dataSaida;
    }

    public void setDataSaida(LocalDate dataSaida) {
        this.dataSaida = dataSaida;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<VendaItem> getItens() {
        return itens;
    }
}
