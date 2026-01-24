package com.expedicao.estoque.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.expedicao.estoque.model.Perfil;
import com.expedicao.estoque.model.Usuario;
import com.expedicao.estoque.repositorie.PerfilRepository;
import com.expedicao.estoque.repositorie.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private BCryptPasswordEncoder encoder;

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario salvarComPerfis(Usuario usuario, List<Long> perfisIds) {

        usuario.setPassword(encoder.encode(usuario.getPassword()));

        Set<Perfil> perfis = new HashSet<>();
        for (Long id : perfisIds) {
            Perfil perfil = perfilRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
            perfis.add(perfil);
        }

        usuario.setPerfis(perfis);
        usuario.setAtivo(true);

        return usuarioRepository.save(usuario);
    }
}
