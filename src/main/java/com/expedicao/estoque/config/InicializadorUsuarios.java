package com.expedicao.estoque.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.expedicao.estoque.model.*;
import com.expedicao.estoque.repositorie.*;

@Component
public class InicializadorUsuarios {

    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private PerfilRepository perfilRepo;
    @Autowired
    private BCryptPasswordEncoder encoder;

    @PostConstruct
    public void init() {

        Perfil master = perfilRepo.findByNome("ROLE_MASTER")
                .orElseGet(() -> perfilRepo.save(new Perfil("ROLE_MASTER")));

        perfilRepo.findByNome("ROLE_PRODUTO")
                .orElseGet(() -> perfilRepo.save(new Perfil("ROLE_PRODUTO")));

        perfilRepo.findByNome("ROLE_ESTOQUE")
                .orElseGet(() -> perfilRepo.save(new Perfil("ROLE_ESTOQUE")));

        perfilRepo.findByNome("ROLE_VENDAS")
                .orElseGet(() -> perfilRepo.save(new Perfil("ROLE_VENDAS")));

        perfilRepo.findByNome("ROLE_FINANCEIRO")
                .orElseGet(() -> perfilRepo.save(new Perfil("ROLE_FINANCEIRO")));

        if (usuarioRepo.findByUsername("master").isEmpty()) {
            Usuario u = new Usuario();
            u.setUsername("master");
            u.setPassword(encoder.encode("master123"));
            u.getPerfis().add(master);
            usuarioRepo.save(u);
        }
    }
}
