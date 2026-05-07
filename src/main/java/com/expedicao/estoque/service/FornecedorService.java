package com.expedicao.estoque.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expedicao.estoque.model.Fornecedor;
import com.expedicao.estoque.repositorie.FornecedorRepository;

@Service
public class FornecedorService {

    @Autowired
    private FornecedorRepository repository;

    public List<Fornecedor> listarTodos() {

        return repository.findAll();
    }

    public Fornecedor salvar(Fornecedor fornecedor) {

        return repository.save(fornecedor);
    }
}