package com.expedicao.estoque.controller;

import com.expedicao.estoque.dto.VendaDTO;
import com.expedicao.estoque.repositorie.ProdutoRepository;
import com.expedicao.estoque.repositorie.VendaRepository;
import com.expedicao.estoque.service.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaService vendaService;
    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private VendaRepository vendaRepository;

    // 🔹 NOVA VENDA
    @GetMapping
    public String telaVenda(Model model) {
        model.addAttribute("produtos", produtoRepository.findAll());
        return "Venda";
    }

    // 🔹 EDITAR VENDA
    @GetMapping("/editar/{id}")
    public String editarVenda(@PathVariable Long id, Model model) {
        var venda = vendaService.buscarVendaCompleta(id);

        model.addAttribute("venda", venda); // 🔥 nome que o formulário espera
        model.addAttribute("produtos", produtoRepository.findAll());

        return "Venda";
    }

    // 🔹 EXCLUIR VENDA
    @GetMapping("/excluir/{id}")
    public String excluirVenda(@PathVariable Long id) {
        vendaService.excluirVenda(id);
        return "redirect:/relatorios/vendas";
    }

    // 🔹 SALVAR (NOVO OU EDIÇÃO)
    @PostMapping("/salvar")
    @ResponseBody
    public void salvar(@RequestBody VendaDTO dto) {
        vendaService.salvarPedido(dto);
    }
}
