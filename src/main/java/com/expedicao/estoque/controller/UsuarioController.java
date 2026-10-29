package com.expedicao.estoque.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.expedicao.estoque.model.Usuario;
import com.expedicao.estoque.repositorie.PerfilRepository;
import com.expedicao.estoque.service.UsuarioService;

@Controller
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('MASTER')")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PerfilRepository perfilRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("perfis", perfilRepository.findAll());
        model.addAttribute("perfisSelecionados", Set.of());
        return "usuarios/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Usuario existente = usuarioService.buscarPorId(id);
        Usuario formulario = new Usuario();
        formulario.setId(existente.getId());
        formulario.setUsername(existente.getUsername());
        formulario.setAtivo(existente.isAtivo());
        model.addAttribute("usuario", formulario);
        model.addAttribute("perfis", perfilRepository.findAll());
        model.addAttribute("perfisSelecionados", existente.getPerfis().stream()
                .map(perfil -> perfil.getId())
                .collect(Collectors.toSet()));
        return "usuarios/form";
    }

    @PostMapping("/salvar")
    @PreAuthorize("hasRole('MASTER')")
    public String salvar(
            @ModelAttribute Usuario usuario,
            @RequestParam(required = false) List<Long> perfis,
            Authentication authentication,
            Model model) {

        try {
            usuarioService.salvarComPerfis(usuario, perfis, authentication.getName());
            return "redirect:/usuarios?sucesso";
        } catch (IllegalArgumentException ex) {
            usuario.setPassword(null);
            model.addAttribute("erro", ex.getMessage());
            model.addAttribute("perfis", perfilRepository.findAll());
            model.addAttribute("perfisSelecionados", perfis == null ? Set.of() : Set.copyOf(perfis));
            return "usuarios/form";
        }
    }
}
