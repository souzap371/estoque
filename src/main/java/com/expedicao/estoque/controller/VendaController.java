package com.expedicao.estoque.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.expedicao.estoque.dto.VendaDTO;
import com.expedicao.estoque.dto.VendaItemEdicaoDTO;
import com.expedicao.estoque.model.Venda;
import com.expedicao.estoque.repositorie.ProdutoRepository;
import com.expedicao.estoque.service.VendaService;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    private final VendaService vendaService;
    private final ProdutoRepository produtoRepository;

    public VendaController(
            VendaService vendaService,
            ProdutoRepository produtoRepository) {
        this.vendaService = vendaService;
        this.produtoRepository = produtoRepository;
    }

    // =============================
    // 🔹 NOVA VENDA
    // =============================
    @GetMapping
    public String novaVenda(Model model) {
        prepararTela(model, null, "", "", List.of());
        return "Venda";
    }

    // =============================
    // 🔹 EDITAR VENDA
    // =============================
    @GetMapping("/editar/{id}")
    public String editarVenda(@PathVariable Long id, Model model) {

        Venda venda = vendaService.buscarVendaCompleta(id);

        List<VendaItemEdicaoDTO> itens = venda.getItens()
                .stream()
                .map(VendaItemEdicaoDTO::new)
                .toList();

        prepararTela(
                model,
                venda.getId(),
                venda.getClienteNome(),
                venda.getClienteEstado(),
                itens);

        return "Venda";
    }

    // =============================
    // 🔹 SALVAR / ATUALIZAR
    // =============================
    @PostMapping("/salvar")
    @ResponseBody
    public ResponseEntity<?> salvar(@RequestBody VendaDTO dto) {

        try {
            vendaService.salvarPedido(dto);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 isso vai mostrar o erro REAL no console
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao salvar venda: " + e.getMessage());
        }
    }

    // =============================
    // 🔹 EXCLUIR
    // =============================
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {
        vendaService.excluirVenda(id);
        return "redirect:/relatorios/vendas";
    }

    // =============================
    // 🔒 AUXILIAR
    // =============================
    private void prepararTela(
            Model model,
            Long vendaId,
            String clienteNome,
            String clienteEstado,
            List<VendaItemEdicaoDTO> itens) {

        model.addAttribute("vendaId", vendaId);
        model.addAttribute("clienteNomeEdicao", clienteNome);
        model.addAttribute("clienteEstadoEdicao", clienteEstado);
        model.addAttribute("itensEdicao", itens);
        model.addAttribute("produtos", produtoRepository.findAll());
    }
}
