// package com.expedicao.estoque.controller;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.*;

// import com.expedicao.estoque.model.ContaReceber;
// import com.expedicao.estoque.service.FinanceiroService;

// @Controller
// @RequestMapping("/financeiro")
// public class FinanceiroController {

//     @Autowired
//     private FinanceiroService financeiroService;

//     // ======================================================
//     // 🖥 TELAS (HTML)
//     // ======================================================

//     // Tela principal financeiro
//     @GetMapping
//     public String financeiro() {
//         return "financeiro"; // financeiro.html
//     }

//     // Tela do relatório financeiro
//     @GetMapping("/relatorio")
//     public String relatorioFinanceiroTela() {
//         return "financeiro-relatorio"; // financeiro-relatorio.html
//     }

//     // ======================================================
//     // 🔌 API (JSON)
//     // ======================================================

//     // Lista clientes (dropdown)
//     @GetMapping("/api/clientes")
//     @ResponseBody
//     public List<String> listarClientes() {
//         return financeiroService.listarClientes();
//     }

//     // Contas por cliente
//     @GetMapping("/api/cliente/{nome}")
//     @ResponseBody
//     public List<ContaReceber> buscarPorCliente(@PathVariable String nome) {
//         return financeiroService.buscarPorCliente(nome);
//     }

//     // Todas as contas (opção "Todos")
//     @GetMapping("/api/todos")
//     @ResponseBody
//     public List<ContaReceber> listarTodas() {
//         return financeiroService.buscarTodas();
//     }

//     // Dar baixa parcial ou total
//     @PostMapping("/api/baixar/{id}")
//     @ResponseBody
//     public void darBaixa(
//             @PathVariable Long id,
//             @RequestParam Double valor,
//             @RequestParam String data
//     ) {
//         financeiroService.darBaixa(id, valor, data);
//     }

//     // Relatório financeiro (JSON)
//     @GetMapping("/api/relatorio")
//     @ResponseBody
//     public List<ContaReceber> relatorioFinanceiro(
//             @RequestParam(required = false) String status
//     ) {
//         return financeiroService.relatorioFinanceiro(status);
//     }
// }


package com.expedicao.estoque.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.expedicao.estoque.model.ContaReceber;
import com.expedicao.estoque.service.FinanceiroService;

@Controller
@RequestMapping("/financeiro")
public class FinanceiroController {

    @Autowired
    private FinanceiroService financeiroService;

    // ======================================================
    // 🖥 TELAS (HTML)
    // ======================================================

    // Tela principal do financeiro
    @GetMapping
    public String financeiro() {
        return "financeiro"; 
    }

    // Tela do relatório financeiro
    @GetMapping("/relatorio")
    public String relatorioFinanceiroTela() {
        return "relatorio-financeiro"; 
    }

    // ======================================================
    // 🔌 API (JSON)
    // ======================================================

    // 🔹 Lista clientes (dropdown)
    @GetMapping("/api/clientes")
    @ResponseBody
    public List<String> listarClientes() {
        return financeiroService.listarClientes();
    }

    // 🔹 Contas por cliente
    // OBS: trata a opção "TODOS"
    @GetMapping("/api/cliente/{nome}")
    @ResponseBody
    public List<ContaReceber> buscarPorCliente(@PathVariable String nome) {

        if ("TODOS".equalsIgnoreCase(nome)) {
            return financeiroService.buscarTodas();
        }

        return financeiroService.buscarPorCliente(nome);
    }

    // 🔹 Todas as contas (uso direto se necessário)
    @GetMapping("/api/todos")
    @ResponseBody
    public List<ContaReceber> listarTodas() {
        return financeiroService.buscarTodas();
    }

    // 🔹 Dar baixa (parcial ou total)
    @PostMapping("/api/baixar/{id}")
    @ResponseBody
    public void darBaixa(
            @PathVariable Long id,
            @RequestParam Double valor,
            @RequestParam String data
    ) {
        financeiroService.darBaixa(id, valor, data);
    }

    // 🔹 Relatório financeiro (JSON)
    @GetMapping("/api/relatorio")
    @ResponseBody
    public List<ContaReceber> relatorioFinanceiro(
            @RequestParam(required = false) String status
    ) {
        return financeiroService.relatorioFinanceiro(status);
    }
}
