package com.expedicao.estoque.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expedicao.estoque.model.Venda;
import com.expedicao.estoque.repositorie.VendaRepository;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private EstoqueService estoqueService;

    public void realizarVenda(Venda venda) {
        estoqueService.baixarEstoque(venda.getProduto(), venda.getQuantidade());
        vendaRepository.save(venda);
    }
}

