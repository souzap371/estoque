package com.expedicao.estoque.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.expedicao.estoque.dto.VendaDTO;
import com.expedicao.estoque.dto.VendaItemEdicaoDTO;
import com.expedicao.estoque.model.Cliente;
import com.expedicao.estoque.model.Venda;
import com.expedicao.estoque.repositorie.ClienteRepository;
import com.expedicao.estoque.repositorie.ProdutoRepository;
import com.expedicao.estoque.service.VendaService;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    private final VendaService vendaService;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;

    public VendaController(
            VendaService vendaService,
            ProdutoRepository produtoRepository,
            ClienteRepository clienteRepository) {
        this.vendaService = vendaService;
        this.produtoRepository = produtoRepository;
        this.clienteRepository = clienteRepository;
    }

    // =============================
    // 🔹 NOVA VENDA
    // =============================
    @GetMapping
    public String novaVenda(Model model) {
        prepararTela(model, null, null, List.of(), null);
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
                venda.getCliente(),
                itens,
                venda.getDataSaida());

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
            e.printStackTrace();
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
            Cliente cliente,
            List<VendaItemEdicaoDTO> itens,
            LocalDate dataPedido) {

        model.addAttribute("vendaId", vendaId);
        model.addAttribute("clienteEdicao", cliente);
        model.addAttribute("itensEdicao", itens);
        model.addAttribute("dataPedidoEdicao", dataPedido);
        model.addAttribute("produtos", produtoRepository.findAll());
        model.addAttribute("clientes", clienteRepository.findAll(Sort.by(Sort.Direction.ASC, "nomeCompleto")));
    }
}
