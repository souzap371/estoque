package com.expedicao.estoque.service;

import com.expedicao.estoque.enums.StatusContaPagar;
import com.expedicao.estoque.model.ContaPagar;
import com.expedicao.estoque.repositorie.ContaPagarRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class RelatorioFinanceiroService {

    @Autowired
    private ContaPagarRepository repository;

    // TOTAL EM ABERTO
    public BigDecimal totalContasPagar() {

        return repository.findAll()
                .stream()
                .map(conta ->
                        conta.getSaldoDevedor() != null
                                ? conta.getSaldoDevedor()
                                : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // TOTAL PAGO
    public BigDecimal totalPago() {

        return repository.findAll()
                .stream()
                .map(conta ->
                        conta.getValorPago() != null
                                ? conta.getValorPago()
                                : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // TOTAL ATRASADO
    public BigDecimal totalAtrasado() {

        return repository.findByStatus(StatusContaPagar.ATRASADO)
                .stream()
                .map(conta ->
                        conta.getSaldoDevedor() != null
                                ? conta.getSaldoDevedor()
                                : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // QUANTIDADE PENDENTES
    public Long quantidadePendentes() {

        return (long) repository.findByStatus(
                StatusContaPagar.PENDENTE
        ).size();
    }

    // GASTOS POR FORNECEDOR
    public Map<String, BigDecimal> gastosPorFornecedor() {

        Map<String, BigDecimal> mapa = new LinkedHashMap<>();

        for (ContaPagar conta : repository.findAll()) {

            // FORNECEDOR
            String fornecedor = "SEM FORNECEDOR";

            if (conta.getFornecedor() != null &&
                conta.getFornecedor().getRazaoSocial() != null) {

                fornecedor =
                        conta.getFornecedor().getRazaoSocial();
            }

            // VALOR PAGO
            BigDecimal valorPago = BigDecimal.ZERO;

            if (conta.getValorPago() != null) {

                valorPago = conta.getValorPago();
            }

            mapa.putIfAbsent(
                    fornecedor,
                    BigDecimal.ZERO);

            mapa.put(
                    fornecedor,
                    mapa.get(fornecedor)
                            .add(valorPago));
        }

        return mapa;
    }

    // PAGAMENTOS MENSAIS
    public Map<String, BigDecimal> pagamentosMensais() {

        Map<String, BigDecimal> mapa = new LinkedHashMap<>();

        mapa.put("Jan", BigDecimal.ZERO);
        mapa.put("Fev", BigDecimal.ZERO);
        mapa.put("Mar", BigDecimal.ZERO);
        mapa.put("Abr", BigDecimal.ZERO);
        mapa.put("Mai", BigDecimal.ZERO);
        mapa.put("Jun", BigDecimal.ZERO);
        mapa.put("Jul", BigDecimal.ZERO);
        mapa.put("Ago", BigDecimal.ZERO);
        mapa.put("Set", BigDecimal.ZERO);
        mapa.put("Out", BigDecimal.ZERO);
        mapa.put("Nov", BigDecimal.ZERO);
        mapa.put("Dez", BigDecimal.ZERO);

        repository.findAll().forEach(conta -> {

            if (conta.getDataPagamento() != null) {

                int mes =
                        conta.getDataPagamento().getMonthValue();

                String nomeMes =
                        switch (mes) {
                            case 1 -> "Jan";
                            case 2 -> "Fev";
                            case 3 -> "Mar";
                            case 4 -> "Abr";
                            case 5 -> "Mai";
                            case 6 -> "Jun";
                            case 7 -> "Jul";
                            case 8 -> "Ago";
                            case 9 -> "Set";
                            case 10 -> "Out";
                            case 11 -> "Nov";
                            default -> "Dez";
                        };

                BigDecimal valorPago = BigDecimal.ZERO;

                if (conta.getValorPago() != null) {

                    valorPago = conta.getValorPago();
                }

                mapa.put(
                        nomeMes,
                        mapa.get(nomeMes)
                                .add(valorPago));
            }
        });

        return mapa;
    }
}