// package com.expedicao.estoque.controller;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.*;

// import com.expedicao.estoque.model.Perfil;
// import com.expedicao.estoque.model.Usuario;
// import com.expedicao.estoque.repositorie.PerfilRepository;
// import com.expedicao.estoque.service.UsuarioService;

// @Controller
// @RequestMapping("/usuarios")
// // @PreAuthorize("hasAuthority('ROLE_MASTER')")
// @PreAuthorize("hasRole('MASTER')")

// public class UsuarioController {

//     @Autowired
//     private UsuarioService usuarioService;

//     @Autowired
//     private PerfilRepository perfilRepository;

//     @GetMapping
//     public String listar(Model model) {
//         model.addAttribute("usuarios", usuarioService.listar());
//         return "usuarios/lista";
//     }

//     @GetMapping("/novo")
//     public String novo(Model model) {
//         model.addAttribute("usuario", new Usuario());
//         model.addAttribute("perfis", perfilRepository.findAll());
//         return "usuarios/form";
//     }

//     @PostMapping("/salvar")
//     public String salvar(
//             @ModelAttribute Usuario usuario,
//             @RequestParam List<Long> perfis) {

//         usuarioService.salvarComPerfis(usuario, perfis);
//         return "redirect:/usuarios";
//     }
// }

package com.expedicao.estoque.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
        return "usuarios/form";
    }

    @PostMapping("/salvar")
    @PreAuthorize("hasRole('MASTER')")
    public String salvar(
            @ModelAttribute Usuario usuario,
            @RequestParam List<Long> perfis) {

        usuarioService.salvarComPerfis(usuario, perfis);
        return "redirect:/usuarios";
    }
}
