package com.expedicao.estoque.controller;

import com.expedicao.estoque.model.Cliente;
import com.expedicao.estoque.service.ClienteService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public String telaCadastro(Model model) {

        model.addAttribute("cliente", new Cliente());

        return "clientes";
    }

    @GetMapping("/importar-vendas")
    public String importarClientes() {

        service.importarClientesDasVendas();

        return "redirect:/clientes/relatorio";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Cliente cliente) {

        if (cliente.getId() != null) {

            Cliente existente = service.buscarPorId(cliente.getId());

            existente.setNomeCompleto(
                    cliente.getNomeCompleto());

            existente.setUf(
                    cliente.getUf());

            existente.setReferencia(
                    cliente.getReferencia());

            service.salvar(existente);

        } else {

            service.salvar(cliente);
        }

        return "redirect:/clientes/relatorio";
    }

    @GetMapping("/relatorio")
    public String relatorio(
            @RequestParam(required = false) String nome,
            Model model) {

        model.addAttribute(
                "clientes",
                service.pesquisar(nome));

        return "relatorio-clientes";
    }

    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "cliente",
                service.buscarPorId(id));

        return "clientes";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {

        service.excluir(id);

        return "redirect:/clientes/relatorio";
    }
}