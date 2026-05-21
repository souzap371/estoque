package com.expedicao.estoque.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expedicao.estoque.enums.StatusContaPagar;
import com.expedicao.estoque.model.ContaPagar;
import com.expedicao.estoque.model.PagamentoContaPagar;
import com.expedicao.estoque.repositorie.ContaPagarRepository;
import com.expedicao.estoque.repositorie.PagamentoContaPagarRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContaPagarService {

    @Autowired
    private ContaPagarRepository repository;

    @Autowired
    private PagamentoContaPagarRepository pagamentoRepository;

    // salvar conta
    public ContaPagar salvar(ContaPagar conta) {

        conta.setValorPago(BigDecimal.ZERO);

        conta.setSaldoDevedor(conta.getValor());

        conta.setStatus(StatusContaPagar.PENDENTE);

        return repository.save(conta);
    }

    // listar todas
    public List<ContaPagar> listarTodas() {

        atualizarStatusAutomatico();

        return repository.findAll();
    }

    // buscar id
    public ContaPagar buscarPorId(Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
    }

    // // pagar conta
    // public void pagarConta(Long id, BigDecimal valorPagamento) {

    // ContaPagar conta = buscarPorId(id);

    // BigDecimal novoValorPago =
    // conta.getValorPago().add(valorPagamento);

    // conta.setValorPago(novoValorPago);

    // BigDecimal novoSaldo =
    // conta.getValor().subtract(novoValorPago);

    // conta.setSaldoDevedor(novoSaldo);

    // conta.setDataPagamento(LocalDate.now());

    // // quitou total
    // if (novoSaldo.compareTo(BigDecimal.ZERO) <= 0) {

    // conta.setStatus(StatusContaPagar.PAGO);

    // conta.setSaldoDevedor(BigDecimal.ZERO);

    // } else {

    // conta.setStatus(StatusContaPagar.PARCIAL);
    // }

    // repository.save(conta);
    // }

    // pagar conta
    // public void pagarConta(Long id, BigDecimal valorPagamento) {

    // ContaPagar conta = buscarPorId(id);

    // // =========================
    // // VALIDAÇÕES
    // // =========================

    // // valor nulo ou zero
    // if (valorPagamento == null ||
    // valorPagamento.compareTo(BigDecimal.ZERO) <= 0) {

    // throw new RuntimeException(
    // "O valor do pagamento deve ser maior que zero.");
    // }

    // // não permitir pagar mais que o saldo
    // if (valorPagamento.compareTo(conta.getSaldoDevedor()) > 0) {

    // throw new RuntimeException(
    // "O valor informado é maior que o saldo devedor da conta.");
    // }

    // // =========================
    // // PROCESSAMENTO
    // // =========================

    // BigDecimal novoValorPago = conta.getValorPago().add(valorPagamento);

    // conta.setValorPago(novoValorPago);

    // BigDecimal novoSaldo = conta.getValor().subtract(novoValorPago);

    // conta.setSaldoDevedor(novoSaldo);

    // conta.setDataPagamento(LocalDate.now());

    // // quitado
    // if (novoSaldo.compareTo(BigDecimal.ZERO) == 0) {

    // conta.setStatus(StatusContaPagar.PAGO);

    // } else {

    // conta.setStatus(StatusContaPagar.PARCIAL);
    // }

    // repository.save(conta);
    // }

    //PAGAR CONTA

    public void pagarConta(Long id, BigDecimal valorPagamento) {

        ContaPagar conta = buscarPorId(id);

        // validações
        if (valorPagamento == null ||
                valorPagamento.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "O valor do pagamento deve ser maior que zero.");
        }

        if (valorPagamento.compareTo(conta.getSaldoDevedor()) > 0) {

            throw new RuntimeException(
                    "O valor informado é maior que o saldo devedor.");
        }

        // =========================
        // REGISTRA PAGAMENTO
        // =========================

        PagamentoContaPagar pagamento = new PagamentoContaPagar();

        pagamento.setConta(conta);

        pagamento.setValorPagamento(valorPagamento);

        pagamento.setDataPagamento(LocalDateTime.now());

        pagamentoRepository.save(pagamento);

        // =========================
        // ATUALIZA TOTAIS
        // =========================

        BigDecimal novoValorPago = conta.getValorPago().add(valorPagamento);

        conta.setValorPago(novoValorPago);

        BigDecimal novoSaldo = conta.getValor().subtract(novoValorPago);

        conta.setSaldoDevedor(novoSaldo);

        conta.setDataPagamento(LocalDate.now());

        // quitado
        if (novoSaldo.compareTo(BigDecimal.ZERO) == 0) {

            conta.setStatus(StatusContaPagar.PAGO);

        } else {

            conta.setStatus(StatusContaPagar.PARCIAL);
        }

        repository.save(conta);
    }

    // atualizar conta
    public ContaPagar atualizar(ContaPagar contaEditada) {

        ContaPagar conta = buscarPorId(contaEditada.getId());

        conta.setDescricao(contaEditada.getDescricao());

        conta.setValor(contaEditada.getValor());

        conta.setDataVencimento(
                contaEditada.getDataVencimento());

        conta.setObservacao(
                contaEditada.getObservacao());

        // recalcula saldo
        BigDecimal novoSaldo = conta.getValor().subtract(conta.getValorPago());

        conta.setSaldoDevedor(novoSaldo);

        // ajusta status
        if (novoSaldo.compareTo(BigDecimal.ZERO) == 0) {

            conta.setStatus(StatusContaPagar.PAGO);

        } else if (conta.getValorPago()
                .compareTo(BigDecimal.ZERO) > 0) {

            conta.setStatus(StatusContaPagar.PARCIAL);

        } else {

            conta.setStatus(StatusContaPagar.PENDENTE);
        }

        return repository.save(conta);
    }

    // excluir conta
    public void excluir(Long id) {

        repository.deleteById(id);
    }

    // atualizar atrasadas
    public void atualizarStatusAutomatico() {

        List<ContaPagar> contas = repository.findAll();

        for (ContaPagar conta : contas) {

            if (conta.getStatus() == StatusContaPagar.PAGO) {
                continue;
            }

            if (conta.getDataVencimento()
                    .isBefore(LocalDate.now())) {

                conta.setStatus(StatusContaPagar.ATRASADO);

                repository.save(conta);
            }
        }
    }
}