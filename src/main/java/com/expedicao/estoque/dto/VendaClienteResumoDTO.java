package com.expedicao.estoque.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.expedicao.estoque.model.VendaItem;

public class VendaClienteResumoDTO {

    private final String clienteNome;
    private final String clienteEstado;
    private final long totalPedidos;
    private final int totalItens;
    private final int quantidadeTotal;
    private final BigDecimal valorTotal;
    private final LocalDate ultimaVenda;
    private final String tipo;
    private final String notaFiscal;
    private final List<VendaItem> itens;

    public VendaClienteResumoDTO(String clienteNome, List<VendaItem> itens) {
        this.clienteNome = clienteNome;
        this.itens = List.copyOf(itens);
        this.clienteEstado = resumoEstados(itens);
        this.totalPedidos = itens.stream()
                .map(item -> item.getVenda().getId())
                .distinct()
                .count();
        this.totalItens = itens.size();
        this.quantidadeTotal = itens.stream()
                .map(VendaItem::getQuantidade)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        this.valorTotal = itens.stream()
                .map(VendaItem::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.ultimaVenda = itens.stream()
                .map(item -> item.getVenda().getDataSaida())
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
        this.tipo = resumoTipos(itens);
        this.notaFiscal = resumoNotasFiscais(itens);
    }

    private String resumoEstados(List<VendaItem> itens) {
        List<String> estados = itens.stream()
                .map(item -> item.getVenda().getClienteEstado())
                .filter(Objects::nonNull)
                .filter(estado -> !estado.isBlank())
                .distinct()
                .toList();
        return estados.size() == 1 ? estados.get(0) : estados.isEmpty() ? "-" : "Diversos";
    }

    private String resumoTipos(List<VendaItem> itens) {
        List<String> tipos = itens.stream()
                .map(VendaItem::getTipoMovimentacao)
                .filter(Objects::nonNull)
                .map(Enum::name)
                .distinct()
                .toList();
        if (tipos.size() != 1) {
            return tipos.isEmpty() ? "-" : "Diversos";
        }
        return "V".equals(tipos.get(0)) ? "Venda" : "Transferência";
    }

    private String resumoNotasFiscais(List<VendaItem> itens) {
        List<Boolean> notas = itens.stream()
                .map(item -> item.getVenda().getComNotaFiscal())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (notas.size() != 1) {
            return notas.isEmpty() ? "-" : "Mistas";
        }
        return notas.get(0) ? "Sim" : "Não";
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public String getClienteEstado() {
        return clienteEstado;
    }

    public long getTotalPedidos() {
        return totalPedidos;
    }

    public int getTotalItens() {
        return totalItens;
    }

    public int getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public LocalDate getUltimaVenda() {
        return ultimaVenda;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNotaFiscal() {
        return notaFiscal;
    }

    public List<VendaItem> getItens() {
        return itens;
    }
}
