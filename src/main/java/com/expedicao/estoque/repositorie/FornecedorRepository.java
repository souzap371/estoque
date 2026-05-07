package com.expedicao.estoque.repositorie;


import org.springframework.data.jpa.repository.JpaRepository;

import com.expedicao.estoque.model.Fornecedor;

public interface FornecedorRepository
        extends JpaRepository<Fornecedor, Long> {
}