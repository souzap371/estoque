package com.expedicao.estoque.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.expedicao.estoque.model.Perfil;
import com.expedicao.estoque.model.Usuario;
import com.expedicao.estoque.repositorie.PerfilRepository;
import com.expedicao.estoque.repositorie.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private BCryptPasswordEncoder encoder;

    @InjectMocks
    private UsuarioService service;

    private Perfil master;
    private Perfil relatorios;

    @BeforeEach
    void prepararPerfis() {
        master = perfil(1L, "ROLE_MASTER");
        relatorios = perfil(2L, "ROLE_RELATORIOS");
    }

    @Test
    void deveAlterarSomentePerfilSemSobrescreverSenhaExistente() {
        Usuario existente = usuario(10L, "analista", "hash-atual", Set.of());
        Usuario formulario = usuario(10L, "analista", "", Set.of());

        when(perfilRepository.findAllById(List.of(2L))).thenReturn(List.of(relatorios));
        when(usuarioRepository.findByUsername("analista")).thenReturn(Optional.of(existente));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        Usuario salvo = service.salvarComPerfis(formulario, List.of(2L), "master");

        assertEquals("hash-atual", salvo.getPassword());
        assertEquals(Set.of(relatorios), salvo.getPerfis());
        verify(encoder, never()).encode(any());
    }

    @Test
    void naoDevePermitirQueMasterRemovaOProprioPerfil() {
        Usuario existente = usuario(1L, "master", "hash", Set.of(master));
        Usuario formulario = usuario(1L, "master", "", Set.of());

        when(perfilRepository.findAllById(List.of(2L))).thenReturn(List.of(relatorios));
        when(usuarioRepository.findByUsername("master")).thenReturn(Optional.of(existente));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.salvarComPerfis(formulario, List.of(2L), "master"));

        assertTrue(erro.getMessage().contains("próprio perfil MASTER"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveExigirAoMenosUmPerfil() {
        Usuario formulario = usuario(null, "novo", "senha", Set.of());

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> service.salvarComPerfis(formulario, List.of(), "master"));

        assertTrue(erro.getMessage().contains("pelo menos um perfil"));
        verify(usuarioRepository, never()).save(any());
    }

    private Perfil perfil(Long id, String nome) {
        Perfil perfil = new Perfil(nome);
        perfil.setId(id);
        return perfil;
    }

    private Usuario usuario(Long id, String username, String password, Set<Perfil> perfis) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setPassword(password);
        usuario.setPerfis(perfis);
        usuario.setAtivo(true);
        return usuario;
    }
}
