package com.expedicao.estoque.dto;

import java.util.List;

public class VendaDTO {

    private String clienteNome;
    private String clienteEstado;
    private String tipoMovimentacao;
    private String estadoDestino;
    private List<VendaItemDTO> itens;
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
    public List<VendaItemDTO> getItens() {
        return itens;
    }
    public void setItens(List<VendaItemDTO> itens) {
        this.itens = itens;
    }

    // getters e setters

    
}
