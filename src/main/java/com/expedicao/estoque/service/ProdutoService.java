package com.expedicao.estoque.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expedicao.estoque.model.Produto;
import com.expedicao.estoque.repositorie.ProdutoRepository;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Produto salvar(Produto produto) {
        return produtoRepository.save(produto);
    }

    public Produto buscarPorCodigoOuNome(String valor) {
        return produtoRepository.findByCodigo(valor)
            .orElse(produtoRepository.findByNome(valor).orElse(null));
    }
}
