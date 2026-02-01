package com.expedicao.estoque.dto;

import java.util.List;

public class VendaDTO {

    private Long id; // 🆕 usado para edição

    private String clienteNome;
    private String clienteEstado;
    private List<VendaItemDTO> itens;

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

    public List<VendaItemDTO> getItens() {
        return itens;
    }

    public void setItens(List<VendaItemDTO> itens) {
        this.itens = itens;
    }
}
