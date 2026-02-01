package com.expedicao.estoque.controller;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.expedicao.estoque.dto.ContaReceberDTO;
import com.expedicao.estoque.model.FormaPagamento;
import com.expedicao.estoque.model.Pagamento;
import com.expedicao.estoque.service.FinanceiroService;

@Controller
@RequestMapping("/financeiro")
public class FinanceiroController {

    private final FinanceiroService financeiroService;

    public FinanceiroController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    // =========================
    // TELAS
    // =========================
    @GetMapping
    public String financeiro() {
        return "financeiro";
    }

    @GetMapping("/relatorio")
    public String relatorioFinanceiroTela() {
        return "relatorio-financeiro";
    }

    @GetMapping("/baixar/{id}")
    public String telaDarBaixa(@PathVariable Long id, Model model) {
        model.addAttribute("contaId", id);
        return "baixa";
    }

    // =========================
    // API JSON
    // =========================
    @GetMapping("/api/clientes")
    @ResponseBody
    public List<String> listarClientes() {
        return financeiroService.listarClientes();
    }

    @GetMapping("/api/cliente/{nome}")
    @ResponseBody
    public List<ContaReceberDTO> buscarPorCliente(@PathVariable String nome) {
        return financeiroService.buscarPorCliente(nome);
    }

    @GetMapping("/api/todos")
    @ResponseBody
    public List<ContaReceberDTO> listarTodas() {
        return financeiroService.buscarTodas();
    }

    @PostMapping("/api/baixar/{id}")
    @ResponseBody
    public ResponseEntity<?> darBaixa(
            @PathVariable Long id,
            @RequestParam BigDecimal valor,
            @RequestParam String data,
            @RequestParam FormaPagamento formaPagamento,
            @RequestParam(required = false) MultipartFile anexo) {

        financeiroService.darBaixa(id, valor, data, formaPagamento, anexo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/relatorio")
    @ResponseBody
    public List<ContaReceberDTO> relatorioFinanceiro(@RequestParam(required = false) String status) {
        return financeiroService.relatorioFinanceiro(status);
    }

    @GetMapping("/api/pagamento/anexo/{id}")
    public ResponseEntity<Resource> baixarAnexo(@PathVariable Long id) {
        Pagamento pagamento = financeiroService.buscarPagamento(id);
        File file = new File(pagamento.getAnexoPath());
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + pagamento.getAnexoNome() + "\"")
                .contentType(MediaType.parseMediaType(pagamento.getAnexoTipo()))
                .body(resource);
    }

    @GetMapping("/api/filtrar")
    @ResponseBody
    public List<ContaReceberDTO> filtrar(
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String status) {

        return financeiroService.filtrar(cliente, status);
    }

}
