// // package com.expedicao.estoque.service;

// // import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.stereotype.Service;

// // import com.expedicao.estoque.enums.StatusContaPagar;
// // import com.expedicao.estoque.model.ContaPagar;
// // import com.expedicao.estoque.model.PagamentoContaPagar;
// // import com.expedicao.estoque.repositorie.ContaPagarRepository;
// // import com.expedicao.estoque.repositorie.PagamentoContaPagarRepository;

// // import java.math.BigDecimal;
// // import java.time.LocalDate;
// // import java.time.LocalDateTime;
// // import java.util.List;

// // @Service
// // public class ContaPagarService {

// //     @Autowired
// //     private ContaPagarRepository repository;

// //     @Autowired
// //     private PagamentoContaPagarRepository pagamentoRepository;

// //     // salvar conta
// //     public ContaPagar salvar(ContaPagar conta) {

// //         conta.setValorPago(BigDecimal.ZERO);

// //         conta.setSaldoDevedor(conta.getValor());

// //         conta.setStatus(StatusContaPagar.PENDENTE);

// //         return repository.save(conta);
// //     }

// //     // listar todas
// //     public List<ContaPagar> listarTodas() {

// //         atualizarStatusAutomatico();

// //         return repository.findAll();
// //     }

// //     // buscar id
// //     public ContaPagar buscarPorId(Long id) {

// //         return repository.findById(id)
// //                 .orElseThrow(() -> new RuntimeException("Conta não encontrada"));
// //     }

// //     //PAGAR CONTA

// //     public void pagarConta(Long id, BigDecimal valorPagamento) {

// //         ContaPagar conta = buscarPorId(id);

// //         // validações
// //         if (valorPagamento == null ||
// //                 valorPagamento.compareTo(BigDecimal.ZERO) <= 0) {

// //             throw new RuntimeException(
// //                     "O valor do pagamento deve ser maior que zero.");
// //         }

// //         if (valorPagamento.compareTo(conta.getSaldoDevedor()) > 0) {

// //             throw new RuntimeException(
// //                     "O valor informado é maior que o saldo devedor.");
// //         }

// //         // =========================
// //         // REGISTRA PAGAMENTO
// //         // =========================

// //         PagamentoContaPagar pagamento = new PagamentoContaPagar();

// //         pagamento.setConta(conta);

// //         pagamento.setValorPagamento(valorPagamento);

// //         pagamento.setDataPagamento(LocalDateTime.now());

// //         pagamentoRepository.save(pagamento);

// //         // =========================
// //         // ATUALIZA TOTAIS
// //         // =========================

// //         BigDecimal novoValorPago = conta.getValorPago().add(valorPagamento);

// //         conta.setValorPago(novoValorPago);

// //         BigDecimal novoSaldo = conta.getValor().subtract(novoValorPago);

// //         conta.setSaldoDevedor(novoSaldo);

// //         conta.setDataPagamento(LocalDate.now());

// //         // quitado
// //         if (novoSaldo.compareTo(BigDecimal.ZERO) == 0) {

// //             conta.setStatus(StatusContaPagar.PAGO);

// //         } else {

// //             conta.setStatus(StatusContaPagar.PARCIAL);
// //         }

// //         repository.save(conta);
// //     }

// //     // atualizar conta
// //     public ContaPagar atualizar(ContaPagar contaEditada) {

// //         ContaPagar conta = buscarPorId(contaEditada.getId());

// //         conta.setDescricao(contaEditada.getDescricao());

// //         conta.setValor(contaEditada.getValor());

// //         conta.setDataVencimento(
// //                 contaEditada.getDataVencimento());

// //         conta.setObservacao(
// //                 contaEditada.getObservacao());

// //         // recalcula saldo
// //         BigDecimal novoSaldo = conta.getValor().subtract(conta.getValorPago());

// //         conta.setSaldoDevedor(novoSaldo);

// //         // ajusta status
// //         if (novoSaldo.compareTo(BigDecimal.ZERO) == 0) {

// //             conta.setStatus(StatusContaPagar.PAGO);

// //         } else if (conta.getValorPago()
// //                 .compareTo(BigDecimal.ZERO) > 0) {

// //             conta.setStatus(StatusContaPagar.PARCIAL);

// //         } else {

// //             conta.setStatus(StatusContaPagar.PENDENTE);
// //         }

// //         return repository.save(conta);
// //     }

// //     // excluir conta
// //     public void excluir(Long id) {

// //         repository.deleteById(id);
// //     }

// //     // atualizar atrasadas
// //     public void atualizarStatusAutomatico() {

// //         List<ContaPagar> contas = repository.findAll();

// //         for (ContaPagar conta : contas) {

// //             if (conta.getStatus() == StatusContaPagar.PAGO) {
// //                 continue;
// //             }

// //             if (conta.getDataVencimento()
// //                     .isBefore(LocalDate.now())) {

// //                 conta.setStatus(StatusContaPagar.ATRASADO);

// //                 repository.save(conta);
// //             }
// //         }
// //     }
// // }

// package com.expedicao.estoque.service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.expedicao.estoque.enums.StatusContaPagar;
// import com.expedicao.estoque.model.ContaPagar;
// import com.expedicao.estoque.model.PagamentoContaPagar;
// import com.expedicao.estoque.repositorie.ContaPagarRepository;
// import com.expedicao.estoque.repositorie.PagamentoContaPagarRepository;

// import java.math.BigDecimal;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.util.List;

// @Service
// public class ContaPagarService {

//     @Autowired
//     private ContaPagarRepository repository;

//     @Autowired
//     private PagamentoContaPagarRepository pagamentoRepository;

//     // =========================
//     // SALVAR
//     // =========================
//     public ContaPagar salvar(ContaPagar conta) {

//         conta.setValorPago(BigDecimal.ZERO);

//         conta.setSaldoDevedor(conta.getValor());

//         conta.setStatus(StatusContaPagar.PENDENTE);

//         return repository.save(conta);
//     }

//     // =========================
//     // LISTAR TODAS
//     // =========================
//     public List<ContaPagar> listarTodas() {

//         atualizarStatusAutomatico();

//         List<ContaPagar> contas = repository.findAll();

//         // FORÇA CARREGAMENTO DO FORNECEDOR
//         contas.forEach(conta -> {

//             if (conta.getFornecedor() != null) {

//                 conta.getFornecedor().getRazaoSocial();
//             }
//         });

//         return contas;
//     }

//     // =========================
//     // BUSCAR POR ID
//     // =========================
//     public ContaPagar buscarPorId(Long id) {

//         ContaPagar conta = repository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

//         // FORÇA CARREGAMENTO
//         if (conta.getFornecedor() != null) {

//             conta.getFornecedor().getRazaoSocial();
//         }

//         return conta;
//     }

//     // =========================
//     // PAGAR CONTA
//     // =========================
//     public void pagarConta(Long id, BigDecimal valorPagamento) {

//         ContaPagar conta = buscarPorId(id);

//         if (valorPagamento == null ||
//                 valorPagamento.compareTo(BigDecimal.ZERO) <= 0) {

//             throw new RuntimeException(
//                     "O valor do pagamento deve ser maior que zero.");
//         }

//         if (valorPagamento.compareTo(conta.getSaldoDevedor()) > 0) {

//             throw new RuntimeException(
//                     "O valor informado é maior que o saldo devedor.");
//         }

//         // REGISTRA PAGAMENTO
//         PagamentoContaPagar pagamento = new PagamentoContaPagar();

//         pagamento.setConta(conta);

//         pagamento.setValorPagamento(valorPagamento);

//         pagamento.setDataPagamento(LocalDateTime.now());

//         pagamentoRepository.save(pagamento);

//         // ATUALIZA TOTAIS
//         BigDecimal novoValorPago = conta.getValorPago().add(valorPagamento);

//         conta.setValorPago(novoValorPago);

//         BigDecimal novoSaldo = conta.getValor().subtract(novoValorPago);

//         conta.setSaldoDevedor(novoSaldo);

//         conta.setDataPagamento(LocalDate.now());

//         // STATUS
//         if (novoSaldo.compareTo(BigDecimal.ZERO) == 0) {

//             conta.setStatus(StatusContaPagar.PAGO);

//         } else {

//             conta.setStatus(StatusContaPagar.PARCIAL);
//         }

//         repository.save(conta);
//     }

//     // =========================
//     // ATUALIZAR
//     // =========================
//     public ContaPagar atualizar(ContaPagar contaEditada) {

//         ContaPagar conta = buscarPorId(contaEditada.getId());

//         // PRESERVA O FORNECEDOR
//         conta.setFornecedor(conta.getFornecedor());

//         conta.setDescricao(contaEditada.getDescricao());

//         conta.setValor(contaEditada.getValor());

//         conta.setDataVencimento(
//                 contaEditada.getDataVencimento());

//         conta.setObservacao(
//                 contaEditada.getObservacao());

//         // RECALCULA SALDO
//         BigDecimal novoSaldo =
//                 conta.getValor().subtract(conta.getValorPago());

//         conta.setSaldoDevedor(novoSaldo);

//         // AJUSTA STATUS
//         if (novoSaldo.compareTo(BigDecimal.ZERO) == 0) {

//             conta.setStatus(StatusContaPagar.PAGO);

//         } else if (conta.getValorPago()
//                 .compareTo(BigDecimal.ZERO) > 0) {

//             conta.setStatus(StatusContaPagar.PARCIAL);

//         } else {

//             conta.setStatus(StatusContaPagar.PENDENTE);
//         }

//         return repository.save(conta);
//     }

//     // =========================
//     // EXCLUIR
//     // =========================
//     public void excluir(Long id) {

//         repository.deleteById(id);
//     }

//     // =========================
//     // ATUALIZAR STATUS AUTOMÁTICO
//     // =========================
//     public void atualizarStatusAutomatico() {

//         List<ContaPagar> contas = repository.findAll();

//         for (ContaPagar conta : contas) {

//             if (conta.getStatus() == StatusContaPagar.PAGO) {

//                 continue;
//             }

//             if (conta.getDataVencimento()
//                     .isBefore(LocalDate.now())) {

//                 conta.setStatus(StatusContaPagar.ATRASADO);

//                 repository.save(conta);
//             }
//         }
//     }
// }

package com.expedicao.estoque.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expedicao.estoque.enums.StatusContaPagar;
import com.expedicao.estoque.model.ContaPagar;
import com.expedicao.estoque.model.PagamentoContaPagar;
import com.expedicao.estoque.repositorie.ContaPagarRepository;
import com.expedicao.estoque.repositorie.PagamentoContaPagarRepository;

@Service
public class ContaPagarService {

    @Autowired
    private ContaPagarRepository repository;

    @Autowired
    private PagamentoContaPagarRepository pagamentoRepository;

    // =========================
    // SALVAR
    // =========================
    public ContaPagar salvar(ContaPagar conta) {

        conta.setValorPago(BigDecimal.ZERO);

        conta.setSaldoDevedor(conta.getValor());

        conta.setStatus(StatusContaPagar.PENDENTE);

        return repository.save(conta);
    }

    // =========================
    // LISTAR TODAS
    // =========================
    public List<ContaPagar> listarTodas() {

        atualizarStatusAutomatico();

        List<ContaPagar> contas = repository.findAll();

        // FORÇA CARREGAMENTO DO FORNECEDOR
        contas.forEach(conta -> {

            if (conta.getFornecedor() != null) {

                conta.getFornecedor().getRazaoSocial();
            }
        });

        return contas;
    }

    // =========================
    // BUSCAR POR ID
    // =========================
    public ContaPagar buscarPorId(Long id) {

        ContaPagar conta = repository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Conta não encontrada"));

        // FORÇA CARREGAMENTO DO FORNECEDOR
        if (conta.getFornecedor() != null) {

            conta.getFornecedor().getRazaoSocial();
        }

        return conta;
    }

    // =========================
    // PAGAR CONTA
    // =========================
    public void pagarConta(
            Long id,
            BigDecimal valorPagamento) {

        ContaPagar conta = buscarPorId(id);

        // VALIDAÇÕES
        if (valorPagamento == null ||
                valorPagamento.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "😡 Informe um valor válido para realizar a baixa.");
        }

        if (valorPagamento.compareTo(
                conta.getSaldoDevedor()) > 0) {

            throw new RuntimeException(
                    "😡 O valor informado é maior que o saldo devedor.");
        }

        // =========================
        // REGISTRA PAGAMENTO
        // =========================
        PagamentoContaPagar pagamento = new PagamentoContaPagar();

        pagamento.setConta(conta);

        pagamento.setValorPagamento(valorPagamento);

        pagamento.setDataPagamento(
                LocalDateTime.now());

        pagamentoRepository.save(pagamento);

        // =========================
        // ATUALIZA VALORES
        // =========================
        BigDecimal novoValorPago = conta.getValorPago()
                .add(valorPagamento);

        conta.setValorPago(novoValorPago);

        BigDecimal novoSaldo = conta.getValor()
                .subtract(novoValorPago);

        conta.setSaldoDevedor(novoSaldo);

        conta.setDataPagamento(
                LocalDate.now());

        // =========================
        // ATUALIZA STATUS
        // =========================
        if (novoSaldo.compareTo(BigDecimal.ZERO) == 0) {

            conta.setStatus(StatusContaPagar.PAGO);

        } else {

            conta.setStatus(StatusContaPagar.PARCIAL);
        }

        repository.save(conta);
    }

    // =========================
    // ATUALIZAR
    // =========================
    public ContaPagar atualizar(
            ContaPagar contaEditada) {

        ContaPagar conta = buscarPorId(contaEditada.getId());

        // PRESERVA FORNECEDOR
        conta.setFornecedor(
                conta.getFornecedor());

        conta.setDescricao(
                contaEditada.getDescricao());

        conta.setValor(
                contaEditada.getValor());

        conta.setDataVencimento(
                contaEditada.getDataVencimento());

        conta.setObservacao(
                contaEditada.getObservacao());

        // =========================
        // RECALCULA SALDO
        // =========================
        BigDecimal novoSaldo = conta.getValor()
                .subtract(conta.getValorPago());

        conta.setSaldoDevedor(novoSaldo);

        // =========================
        // AJUSTA STATUS
        // =========================
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

    // =========================
    // EXCLUIR
    // =========================
    public void excluir(Long id) {

        repository.deleteById(id);
    }

    // =========================
    // STATUS AUTOMÁTICO
    // =========================
    public void atualizarStatusAutomatico() {

        List<ContaPagar> contas = repository.findAll();

        for (ContaPagar conta : contas) {

            if (conta.getStatus() == StatusContaPagar.PAGO) {

                continue;
            }

            if (conta.getDataVencimento() != null &&
                    conta.getDataVencimento()
                            .isBefore(LocalDate.now())) {

                conta.setStatus(
                        StatusContaPagar.ATRASADO);

                repository.save(conta);
            }
        }
    }

    // =========================
    // PAGAMENTOS MENSAIS
    // =========================
    // UTILIZADO NO GRÁFICO DO BI
    // =========================
    public Map<String, BigDecimal> pagamentosMensaisFiltrados(
            Long fornecedorId,
            LocalDate dataInicio,
            LocalDate dataFim) {

        List<ContaPagar> contas = repository.findAll();

        Map<String, BigDecimal> dados = new LinkedHashMap<>();

        for (ContaPagar conta : contas) {

            // IGNORA SEM PAGAMENTO
            if (conta.getValorPago() == null ||
                    conta.getValorPago()
                            .compareTo(BigDecimal.ZERO) <= 0) {

                continue;
            }

            // FILTRO FORNECEDOR
            if (fornecedorId != null) {

                if (conta.getFornecedor() == null ||
                        !conta.getFornecedor()
                                .getId()
                                .equals(fornecedorId)) {

                    continue;
                }
            }

            // FILTRO DATA
            if (dataInicio != null &&
                    conta.getDataPagamento() != null &&
                    conta.getDataPagamento()
                            .isBefore(dataInicio)) {

                continue;
            }

            if (dataFim != null &&
                    conta.getDataPagamento() != null &&
                    conta.getDataPagamento()
                            .isAfter(dataFim)) {

                continue;
            }

            // AGRUPAMENTO MENSAL
            YearMonth mes = YearMonth.from(
                    conta.getDataPagamento());

            String nomeMes = mes.getMonth()
                    .getDisplayName(
                            TextStyle.SHORT,
                            new Locale("pt", "BR"))
                    + "/"
                    + mes.getYear();

            dados.put(
                    nomeMes,
                    dados.getOrDefault(
                            nomeMes,
                            BigDecimal.ZERO)
                            .add(conta.getValorPago()));
        }

        return dados;
    }
}