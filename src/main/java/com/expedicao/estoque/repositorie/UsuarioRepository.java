package com.expedicao.estoque.repositorie;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.expedicao.estoque.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}
