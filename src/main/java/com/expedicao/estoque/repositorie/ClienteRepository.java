package com.expedicao.estoque.repositorie;

import com.expedicao.estoque.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNomeCompletoContainingIgnoreCase(String nome);

    boolean existsByNomeCompletoIgnoreCase(String nomeCompleto);
}