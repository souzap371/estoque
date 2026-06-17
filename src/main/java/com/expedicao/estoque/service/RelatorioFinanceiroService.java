package com.expedicao.estoque.service;

import com.expedicao.estoque.enums.StatusContaPagar;
import com.expedicao.estoque.model.ContaPagar;
import com.expedicao.estoque.model.Fornecedor;
import com.expedicao.estoque.repositorie.ContaPagarRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expedicao.estoque.model.Fornecedor;
import com.expedicao.estoque.model.ContaPagar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioFinanceiroService {

        @Autowired
        private ContaPagarRepository repository;

        @Autowired
        private ContaPagarRepository contaPagarRepository;

        // ==========================================
        // FILTRO BASE
        // ==========================================
        private List<ContaPagar> filtrar(
                        Long fornecedorId,
                        LocalDate dataInicio,
                        LocalDate dataFim) {

                return repository.findAll()
                                .stream()

                                // FILTRO FORNECEDOR
                                .filter(conta -> {

                                        if (fornecedorId == null)
                                                return true;

                                        if (conta.getFornecedor() == null)
                                                return false;

                                        return conta.getFornecedor()
                                                        .getId()
                                                        .equals(fornecedorId);
                                })

                                // FILTRO DATA INICIO
                                .filter(conta -> {

                                        if (dataInicio == null)
                                                return true;

                                        if (conta.getDataVencimento() == null)
                                                return false;

                                        return !conta.getDataVencimento()
                                                        .isBefore(dataInicio);
                                })

                                // FILTRO DATA FIM
                                .filter(conta -> {

                                        if (dataFim == null)
                                                return true;

                                        if (conta.getDataVencimento() == null)
                                                return false;

                                        return !conta.getDataVencimento()
                                                        .isAfter(dataFim);
                                })

                                .toList();
        }

        // ==========================================
        // TOTAL EM ABERTO
        // ==========================================
        public BigDecimal totalContasPagar(
                        Long fornecedorId,
                        LocalDate dataInicio,
                        LocalDate dataFim) {

                return filtrar(
                                fornecedorId,
                                dataInicio,
                                dataFim)

                                .stream()

                                .map(conta -> conta.getSaldoDevedor() != null
                                                ? conta.getSaldoDevedor()
                                                : BigDecimal.ZERO)

                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // ==========================================
        // TOTAL PAGO
        // ==========================================
        public BigDecimal totalPago(
                        Long fornecedorId,
                        LocalDate dataInicio,
                        LocalDate dataFim) {

                return filtrar(
                                fornecedorId,
                                dataInicio,
                                dataFim)

                                .stream()

                                .map(conta -> conta.getValorPago() != null
                                                ? conta.getValorPago()
                                                : BigDecimal.ZERO)

                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // ==========================================
        // TOTAL ATRASADO
        // ==========================================
        public BigDecimal totalAtrasado(
                        Long fornecedorId,
                        LocalDate dataInicio,
                        LocalDate dataFim) {

                return filtrar(
                                fornecedorId,
                                dataInicio,
                                dataFim)

                                .stream()

                                .filter(conta -> conta.getStatus() == StatusContaPagar.ATRASADO)

                                .map(conta -> conta.getSaldoDevedor() != null
                                                ? conta.getSaldoDevedor()
                                                : BigDecimal.ZERO)

                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // ==========================================
        // QUANTIDADE PENDENTES
        // ==========================================
        public Long quantidadePendentes(
                        Long fornecedorId,
                        LocalDate dataInicio,
                        LocalDate dataFim) {

                return filtrar(
                                fornecedorId,
                                dataInicio,
                                dataFim)

                                .stream()

                                .filter(conta -> conta.getStatus() == StatusContaPagar.PENDENTE)

                                .count();
        }

        // ==========================================
        // GASTOS POR FORNECEDOR
        // ==========================================
        public Map<String, BigDecimal> gastosPorFornecedor(
                        Long fornecedorId,
                        LocalDate dataInicio,
                        LocalDate dataFim) {

                Map<String, BigDecimal> mapa = new LinkedHashMap<>();

                for (ContaPagar conta : filtrar(
                                fornecedorId,
                                dataInicio,
                                dataFim)) {

                        // NOME FORNECEDOR
                        String fornecedor = "SEM FORNECEDOR";

                        if (conta.getFornecedor() != null &&
                                        conta.getFornecedor().getRazaoSocial() != null) {

                                fornecedor = conta.getFornecedor()
                                                .getRazaoSocial();
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

        // ==========================================
        // PAGAMENTOS MENSAIS
        // ==========================================
        public Map<String, BigDecimal> pagamentosMensais(
                        Long fornecedorId,
                        LocalDate dataInicio,
                        LocalDate dataFim) {

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

                filtrar(
                                fornecedorId,
                                dataInicio,
                                dataFim)

                                .forEach(conta -> {

                                        if (conta.getDataPagamento() != null) {

                                                int mes = conta.getDataPagamento()
                                                                .getMonthValue();

                                                String nomeMes = switch (mes) {

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

                                                BigDecimal valorPago = conta.getValorPago() != null
                                                                ? conta.getValorPago()
                                                                : BigDecimal.ZERO;

                                                mapa.put(
                                                                nomeMes,
                                                                mapa.get(nomeMes)
                                                                                .add(valorPago));
                                        }
                                });

                return mapa;
        }

        // =========================
        // CONTAS VENCENDO NA SEMANA
        // =========================
        public Long contasVencendoSemana(
                        Long fornecedorId,
                        LocalDate dataInicio,
                        LocalDate dataFim) {

                List<ContaPagar> contas = repository.findAll();

                LocalDate hoje = LocalDate.now();

                LocalDate limite = hoje.plusDays(7);

                return contas.stream()

                                // IGNORA PAGAS
                                .filter(conta -> conta.getStatus() != StatusContaPagar.PAGO)

                                // VENCENDO NOS PRÓXIMOS 7 DIAS
                                .filter(conta -> conta.getDataVencimento() != null
                                                &&
                                                !conta.getDataVencimento().isBefore(hoje)
                                                &&
                                                !conta.getDataVencimento().isAfter(limite))

                                // FILTRO FORNECEDOR
                                .filter(conta -> {

                                        if (fornecedorId == null) {

                                                return true;
                                        }

                                        return conta.getFornecedor() != null
                                                        &&
                                                        conta.getFornecedor()
                                                                        .getId()
                                                                        .equals(fornecedorId);
                                })

                                // FILTRO DATA INICIAL
                                .filter(conta -> {

                                        if (dataInicio == null) {

                                                return true;
                                        }

                                        return !conta.getDataVencimento()
                                                        .isBefore(dataInicio);
                                })

                                // FILTRO DATA FINAL
                                .filter(conta -> {

                                        if (dataFim == null) {

                                                return true;
                                        }

                                        return !conta.getDataVencimento()
                                                        .isAfter(dataFim);
                                })

                                .count();
        }

        // =========================
        // FORNECEDORES COM CONTAS
        // =========================
        public List<Fornecedor> listarFornecedoresComContas() {

                List<ContaPagar> contas = contaPagarRepository.findAll();

                return contas.stream()

                                .filter(conta -> conta.getFornecedor() != null)

                                .map(ContaPagar::getFornecedor)

                                .distinct()

                                .toList();
        }
}