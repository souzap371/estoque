package com.expedicao.estoque.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class VendaDTO {

    private Long id; // usado para edição

    private Long clienteId;
    private List<VendaItemDTO> itens;
    private Boolean comNotaFiscal;
    private String observacao;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataPedido;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<VendaItemDTO> getItens() {
        return itens;
    }

    public void setItens(List<VendaItemDTO> itens) {
        this.itens = itens;
    }

    public Boolean getComNotaFiscal() {
        return comNotaFiscal;
    }

    public void setComNotaFiscal(Boolean comNotaFiscal) {
        this.comNotaFiscal = comNotaFiscal;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDate getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDate dataPedido) {
        this.dataPedido = dataPedido;
    }
}