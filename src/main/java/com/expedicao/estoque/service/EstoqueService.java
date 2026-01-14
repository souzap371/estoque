// package com.expedicao.estoque.service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.expedicao.estoque.model.Estoque;
// import com.expedicao.estoque.model.Produto;
// import com.expedicao.estoque.repositorie.EstoqueRepository;

// @Service
// public class EstoqueService {

//     @Autowired
//     private EstoqueRepository estoqueRepository;

//     public void entradaEstoque(Produto produto, int quantidade) {
//         Estoque estoque = estoqueRepository.findByProduto(produto)
//             .orElse(new Estoque());

//         estoque.setProduto(produto);
//         estoque.setQuantidadeAtual(
//             (estoque.getQuantidadeAtual() == null ? 0 : estoque.getQuantidadeAtual()) + quantidade
//         );

//         estoqueRepository.save(estoque);
//     }

//     public void baixarEstoque(Produto produto, int quantidade) {
//         Estoque estoque = estoqueRepository.findByProduto(produto)
//             .orElseThrow(() -> new RuntimeException("Produto sem estoque"));

//         if (estoque.getQuantidadeAtual() < quantidade) {
//             throw new RuntimeException("Estoque insuficiente");
//         }

//         estoque.setQuantidadeAtual(estoque.getQuantidadeAtual() - quantidade);
//         estoqueRepository.save(estoque);
//     }
// }



package com.expedicao.estoque.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expedicao.estoque.model.Estoque;
import com.expedicao.estoque.model.Produto;
import com.expedicao.estoque.repositorie.EstoqueRepository;

import jakarta.transaction.Transactional;

@Service
public class EstoqueService {

    @Autowired
    private EstoqueRepository estoqueRepository;

    /**
     * Entrada de estoque (compra, ajuste, devolução, etc)
     */
    @Transactional
    public void entradaEstoque(Produto produto, int quantidade) {

        if (quantidade <= 0) {
            throw new RuntimeException("Quantidade inválida para entrada de estoque");
        }

        Estoque estoque = estoqueRepository.findByProduto(produto)
                .orElse(new Estoque());

        estoque.setProduto(produto);

        int atual = estoque.getQuantidadeAtual() == null ? 0 : estoque.getQuantidadeAtual();
        estoque.setQuantidadeAtual(atual + quantidade);

        estoqueRepository.save(estoque);
    }

    /**
     * Baixa de estoque usada pela venda
     */
    @Transactional
    public void baixarEstoque(Produto produto, int quantidade) {

        if (quantidade <= 0) {
            throw new RuntimeException("Quantidade inválida para baixa de estoque");
        }

        Estoque estoque = estoqueRepository.findByProduto(produto)
                .orElseThrow(() ->
                    new RuntimeException("Produto sem registro de estoque: " + produto.getNome())
                );

        if (estoque.getQuantidadeAtual() < quantidade) {
            throw new RuntimeException(
                "Estoque insuficiente para o produto: " + produto.getNome() +
                " | Disponível: " + estoque.getQuantidadeAtual()
            );
        }

        estoque.setQuantidadeAtual(estoque.getQuantidadeAtual() - quantidade);
        // save é opcional, mas mantido por clareza
        estoqueRepository.save(estoque);
    }
}
