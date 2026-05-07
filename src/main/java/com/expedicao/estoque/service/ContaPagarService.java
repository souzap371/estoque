package com.expedicao.estoque.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expedicao.estoque.enums.StatusContaPagar;
import com.expedicao.estoque.model.ContaPagar;
import com.expedicao.estoque.repositorie.ContaPagarRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ContaPagarService {

    @Autowired
    private ContaPagarRepository repository;

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
                .orElseThrow(() ->
                        new RuntimeException("Conta não encontrada"));
    }

    // pagar conta
    public void pagarConta(Long id, BigDecimal valorPagamento) {

        ContaPagar conta = buscarPorId(id);

        BigDecimal novoValorPago =
                conta.getValorPago().add(valorPagamento);

        conta.setValorPago(novoValorPago);

        BigDecimal novoSaldo =
                conta.getValor().subtract(novoValorPago);

        conta.setSaldoDevedor(novoSaldo);

        conta.setDataPagamento(LocalDate.now());

        // quitou total
        if (novoSaldo.compareTo(BigDecimal.ZERO) <= 0) {

            conta.setStatus(StatusContaPagar.PAGO);

            conta.setSaldoDevedor(BigDecimal.ZERO);

        } else {

            conta.setStatus(StatusContaPagar.PARCIAL);
        }

        repository.save(conta);
    }

    // atualizar atrasadas
    public void atualizarStatusAutomatico() {

        List<ContaPagar> contas =
                repository.findAll();

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