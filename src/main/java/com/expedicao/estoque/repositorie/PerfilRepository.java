package com.expedicao.estoque.repositorie;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.expedicao.estoque.model.Perfil;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByNome(String nome);
}
