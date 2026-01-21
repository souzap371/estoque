// package com.expedicao.estoque.service;

// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.expedicao.estoque.model.ContaReceber;
// import com.expedicao.estoque.model.FormaPagamento;
// import com.expedicao.estoque.model.Pagamento;
// import com.expedicao.estoque.repositorie.ContaReceberRepository;

// import jakarta.transaction.Transactional;

// @Service
// public class FinanceiroService {

//     @Autowired
//     private ContaReceberRepository contaReceberRepository;

//     public List<String> listarClientes() {
//         return contaReceberRepository.listarClientes();
//     }

//     public List<ContaReceber> buscarPorCliente(String cliente) {
//         return contaReceberRepository.findAll()
//                 .stream()
//                 .filter(c -> c.getClienteNome().equalsIgnoreCase(cliente))
//                 .toList();
//     }

//     @Transactional
//     public void darBaixa(
//             Long id,
//             Double valor,
//             String data,
//             FormaPagamento formaPagamento) {

//         ContaReceber conta = contaReceberRepository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

//         if (valor <= 0 || valor > conta.getSaldoDevedor()) {
//             throw new RuntimeException("Valor inválido");
//         }

//         Pagamento pagamento = new Pagamento();
//         pagamento.setContaReceber(conta);
//         pagamento.setValorPago(valor);
//         pagamento.setDataPagamento(
//                 LocalDate.parse(data, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
//         pagamento.setFormaPagamento(formaPagamento);

//         conta.getPagamentos().add(pagamento);

//         conta.setValorPago(conta.getValorPago() + valor);
//         conta.setSaldoDevedor(conta.getSaldoDevedor() - valor);

//         contaReceberRepository.save(conta);
//     }

//     public List<ContaReceber> buscarTodas() {
//         return contaReceberRepository.findAll();
//     }

//     public List<ContaReceber> relatorioFinanceiro(String status) {

//         List<ContaReceber> contas = contaReceberRepository.findAll();

//         if (status == null || status.isBlank()) {
//             return contas;
//         }

//         return contas.stream()
//                 .filter(c -> {
//                     if ("ABERTO".equalsIgnoreCase(status)) {
//                         return c.getSaldoDevedor() > 0;
//                     }
//                     if ("QUITADO".equalsIgnoreCase(status)) {
//                         return c.getSaldoDevedor() == 0;
//                     }
//                     return true;
//                 })
//                 .toList();
//     }

// }

package com.expedicao.estoque.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expedicao.estoque.model.ContaReceber;
import com.expedicao.estoque.model.FormaPagamento;
import com.expedicao.estoque.model.Pagamento;
import com.expedicao.estoque.repositorie.ContaReceberRepository;

import jakarta.transaction.Transactional;

@Service
public class FinanceiroService {

    @Autowired
    private ContaReceberRepository contaReceberRepository;

    public List<String> listarClientes() {
        return contaReceberRepository.listarClientes();
    }

    public List<ContaReceber> buscarPorCliente(String cliente) {
        return contaReceberRepository.findAll()
                .stream()
                .filter(c -> c.getClienteNome().equalsIgnoreCase(cliente))
                .toList();
    }

    @Transactional
    public void darBaixa(
            Long id,
            Double valor,
            String data,
            FormaPagamento formaPagamento) {

        ContaReceber conta = contaReceberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        if (valor == null || valor <= 0 || valor > conta.getSaldoDevedor()) {
            throw new RuntimeException("Valor inválido");
        }

        // 🔹 Correção: parse direto no padrão ISO (yyyy-MM-dd)
        LocalDate dataPagamento = LocalDate.parse(data);

        Pagamento pagamento = new Pagamento();
        pagamento.setContaReceber(conta);
        pagamento.setValorPago(valor);
        pagamento.setDataPagamento(dataPagamento);
        pagamento.setFormaPagamento(formaPagamento);

        conta.getPagamentos().add(pagamento);

        conta.setValorPago(conta.getValorPago() + valor);
        conta.setSaldoDevedor(conta.getSaldoDevedor() - valor);

        contaReceberRepository.save(conta);
    }

    public List<ContaReceber> buscarTodas() {
        return contaReceberRepository.findAll();
    }

    public List<ContaReceber> relatorioFinanceiro(String status) {

        List<ContaReceber> contas = contaReceberRepository.findAll();

        if (status == null || status.isBlank()) {
            return contas;
        }

        return contas.stream()
                .filter(c -> {
                    if ("ABERTO".equalsIgnoreCase(status)) {
                        return c.getSaldoDevedor() > 0;
                    }
                    if ("QUITADO".equalsIgnoreCase(status)) {
                        return c.getSaldoDevedor() == 0;
                    }
                    return true;
                })
                .toList();
    }
}
