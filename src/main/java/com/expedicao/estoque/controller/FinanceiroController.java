package com.expedicao.estoque.controller;

import java.io.File;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.expedicao.estoque.model.ContaReceber;
import com.expedicao.estoque.model.FormaPagamento;
import com.expedicao.estoque.model.Pagamento;
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

    // 🔹 TELA DE DAR BAIXA (⬅️ CORREÇÃO AQUI)
    @GetMapping("/baixar/{id}")
    public String telaDarBaixa(@PathVariable Long id, Model model) {
        model.addAttribute("contaId", id);
        return "baixa";
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

    // 🔹 Todas as contas
    @GetMapping("/api/todos")
    @ResponseBody
    public List<ContaReceber> listarTodas() {
        return financeiroService.buscarTodas();
    }

    @PostMapping("/api/baixar/{id}")
    @ResponseBody
    public ResponseEntity<?> darBaixa(
            @PathVariable Long id,
            @RequestParam Double valor,
            @RequestParam String data,
            @RequestParam FormaPagamento formaPagamento,
            @RequestParam(required = false) MultipartFile anexo) {

        financeiroService.darBaixa(id, valor, data, formaPagamento, anexo);
        return ResponseEntity.ok().build();
    }

    // 🔹 Relatório financeiro (JSON)
    @GetMapping("/api/relatorio")
    @ResponseBody
    public List<ContaReceber> relatorioFinanceiro(
            @RequestParam(required = false) String status) {

        return financeiroService.relatorioFinanceiro(status);
    }

    @GetMapping("/api/pagamento/anexo/{id}")
    public ResponseEntity<Resource> baixarAnexo(@PathVariable Long id) {

        Pagamento pagamento = financeiroService.buscarPagamento(id);

        File file = new File(pagamento.getAnexoPath());
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + pagamento.getAnexoNome() + "\"")
                .contentType(MediaType.parseMediaType(pagamento.getAnexoTipo()))
                .body(resource);
    }

}
