package com.expedicao.estoque.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }

    @Transactional
    public Usuario salvarComPerfis(Usuario dados, List<Long> perfisIds, String operadorUsername) {
        if (perfisIds == null || perfisIds.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos um perfil de acesso");
        }

        Set<Perfil> perfis = new HashSet<>(perfilRepository.findAllById(perfisIds));
        if (perfis.size() != new HashSet<>(perfisIds).size()) {
            throw new IllegalArgumentException("Um dos perfis selecionados não foi encontrado");
        }

        validarUsername(dados);
        Usuario usuario;

        if (dados.getId() == null) {
            if (dados.getPassword() == null || dados.getPassword().isBlank()) {
                throw new IllegalArgumentException("A senha é obrigatória para um novo usuário");
            }
            usuario = new Usuario();
            usuario.setPassword(encoder.encode(dados.getPassword()));
            usuario.setAtivo(true);
        } else {
            usuario = buscarPorId(dados.getId());
            protegerPerfilMaster(usuario, perfis, operadorUsername);
            if (dados.getPassword() != null && !dados.getPassword().isBlank()) {
                usuario.setPassword(encoder.encode(dados.getPassword()));
            }
        }

        usuario.setUsername(dados.getUsername().trim());
        usuario.setPerfis(perfis);
        return usuarioRepository.save(usuario);
    }

    private void validarUsername(Usuario dados) {
        if (dados.getUsername() == null || dados.getUsername().isBlank()) {
            throw new IllegalArgumentException("Informe o nome do usuário");
        }

        usuarioRepository.findByUsername(dados.getUsername().trim())
            .filter(existente -> dados.getId() == null || !existente.getId().equals(dados.getId()))
            .ifPresent(existente -> {
                throw new IllegalArgumentException("Já existe um usuário com esse nome");
            });
    }

    private void protegerPerfilMaster(Usuario usuario, Set<Perfil> novosPerfis, String operadorUsername) {
        boolean eraMaster = usuario.getPerfis().stream()
            .anyMatch(perfil -> "ROLE_MASTER".equals(perfil.getNome()));
        boolean continuaraMaster = novosPerfis.stream()
            .anyMatch(perfil -> "ROLE_MASTER".equals(perfil.getNome()));

        if (eraMaster && !continuaraMaster && usuario.getUsername().equals(operadorUsername)) {
            throw new IllegalArgumentException("Você não pode remover o próprio perfil MASTER");
        }

        if (eraMaster && !continuaraMaster) {
            long mastersAtivos = usuarioRepository.findAll().stream()
                .filter(Usuario::isAtivo)
                .filter(item -> item.getPerfis().stream()
                    .anyMatch(perfil -> "ROLE_MASTER".equals(perfil.getNome())))
                .count();
            if (mastersAtivos <= 1) {
                throw new IllegalArgumentException("O sistema precisa manter pelo menos um usuário MASTER ativo");
            }
        }
    }
}
