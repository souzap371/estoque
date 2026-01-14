// package com.expedicao.estoque.service;

// import com.expedicao.estoque.dto.*;
// import com.expedicao.estoque.model.*;
// import com.expedicao.estoque.repositorie.VendaRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.time.LocalDate;
// import java.util.ArrayList;

// @Service
// public class VendaService {

//     @Autowired
//     private VendaRepository vendaRepository;

//     @Autowired
//     private ProdutoService produtoService;

//     @Autowired
//     private EstoqueService estoqueService;

//     public void salvarPedido(VendaDTO dto) {

//         Venda venda = new Venda();
//         venda.setClienteNome(dto.getClienteNome());
//         venda.setClienteEstado(dto.getClienteEstado());
//         venda.setTipoMovimentacao(
//             TipoMovimentacao.valueOf(dto.getTipoMovimentacao())
//         );
//         venda.setEstadoDestino(dto.getEstadoDestino());
//         venda.setDataSaida(LocalDate.now());

//         double total = 0;
//         venda.setItens(new ArrayList<>());

//         for (VendaItemDTO itemDTO : dto.getItens()) {
//             Produto produto =
//                 produtoService.buscarPorCodigoOuNome(itemDTO.getCodigoOuNome());

//             VendaItem item = new VendaItem();
//             item.setProduto(produto);
//             item.setQuantidade(itemDTO.getQuantidade());
//             item.setValorPorCaixa(itemDTO.getValorPorCaixa());

//             double subtotal =
//                 itemDTO.getQuantidade() * itemDTO.getValorPorCaixa();

//             item.setSubtotal(subtotal);
//             item.setVenda(venda);

//             estoqueService.baixarEstoque(produto, itemDTO.getQuantidade());

//             venda.getItens().add(item);
//             total += subtotal;
//         }

//         venda.setValorTotal(total);
//         vendaRepository.save(venda);
//     }
// }




// package com.expedicao.estoque.service;

// import com.expedicao.estoque.dto.*;
// import com.expedicao.estoque.model.*;
// import com.expedicao.estoque.repositorie.ProdutoRepository;
// import com.expedicao.estoque.repositorie.VendaItemRepository;
// import com.expedicao.estoque.repositorie.VendaRepository;

// import jakarta.transaction.Transactional;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.time.LocalDate;
// import java.util.ArrayList;
// import java.util.List;

// @Service
// public class VendaService {

//     @Autowired
//     private VendaRepository vendaRepository;

//     @Autowired
//     private VendaItemRepository vendaItemRepository;

//     @Autowired
//     private ProdutoRepository produtoRepository;

//     @Transactional
//     public void salvarPedido(VendaDTO dto) {

//         // 1️⃣ Criar a venda
//         Venda venda = new Venda();
//         venda.setClienteNome(dto.getClienteNome());
//         venda.setClienteEstado(dto.getClienteEstado());
//         venda.setEstadoDestino(dto.getEstadoDestino());
//         venda.setTipoMovimentacao(TipoMovimentacao.valueOf(dto.getTipoMovimentacao()));
//         venda.setDataSaida(LocalDate.now());

//         List<VendaItem> itens = new ArrayList<>();
//         double total = 0.0;

//         // 2️⃣ Criar os itens

//         for (VendaItemDTO itemDTO : dto.getItens()) {

//     if (itemDTO.getCodigoOuNome() == null || itemDTO.getCodigoOuNome().isBlank()) {
//         throw new RuntimeException("Item sem produto informado");
//     }

//     Produto produto = produtoRepository
//             .findByCodigoOuNome(itemDTO.getCodigoOuNome())
//             .orElseThrow(() ->
//                 new RuntimeException("Produto não encontrado: " + itemDTO.getCodigoOuNome())
//             );

//     VendaItem item = new VendaItem();
//     item.setVenda(venda);
//     item.setProduto(produto);
//     item.setQuantidade(itemDTO.getQuantidade());
//     item.setValorPorCaixa(itemDTO.getValorPorCaixa());

//     double subtotal = itemDTO.getQuantidade() * itemDTO.getValorPorCaixa();
//     item.setSubtotal(subtotal);

//     total += subtotal;
//     itens.add(item);
// }


//         // 3️⃣ Vínculo bidirecional
//         venda.setItens(itens);
//         venda.setValorTotal(total);

//         // 4️⃣ SALVAR SOMENTE A VENDA
//         // cascade = ALL cuida dos itens
//         vendaRepository.save(venda);
//     }
// }


package com.expedicao.estoque.service;

import com.expedicao.estoque.dto.VendaDTO;
import com.expedicao.estoque.dto.VendaItemDTO;
import com.expedicao.estoque.model.Produto;
import com.expedicao.estoque.model.TipoMovimentacao;
import com.expedicao.estoque.model.Venda;
import com.expedicao.estoque.model.VendaItem;
import com.expedicao.estoque.repositorie.ProdutoRepository;
import com.expedicao.estoque.repositorie.VendaRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueService estoqueService;

    /**
     * Registra uma venda, valida estoque e dá baixa automática.
     * Se faltar estoque, TODA a operação é revertida.
     */
    @Transactional
    public void salvarPedido(VendaDTO dto) {

        // 1️⃣ Criar a venda
        Venda venda = new Venda();
        venda.setClienteNome(dto.getClienteNome());
        venda.setClienteEstado(dto.getClienteEstado());
        venda.setEstadoDestino(dto.getEstadoDestino());
        venda.setTipoMovimentacao(TipoMovimentacao.valueOf(dto.getTipoMovimentacao()));
        venda.setDataSaida(LocalDate.now());

        List<VendaItem> itens = new ArrayList<>();
        double total = 0.0;

        // 2️⃣ Processar itens da venda
        for (VendaItemDTO itemDTO : dto.getItens()) {

            if (itemDTO.getCodigoOuNome() == null || itemDTO.getCodigoOuNome().isBlank()) {
                throw new RuntimeException("Produto não informado no item da venda");
            }

            if (itemDTO.getQuantidade() <= 0) {
                throw new RuntimeException("Quantidade inválida para o produto: " + itemDTO.getCodigoOuNome());
            }

            Produto produto = produtoRepository
                    .findByCodigoOuNome(itemDTO.getCodigoOuNome())
                    .orElseThrow(() ->
                        new RuntimeException("Produto não encontrado: " + itemDTO.getCodigoOuNome())
                    );

            // 🔥 VERIFICA E DÁ BAIXA NO ESTOQUE
            estoqueService.baixarEstoque(produto, itemDTO.getQuantidade());

            VendaItem item = new VendaItem();
            item.setVenda(venda);
            item.setProduto(produto);
            item.setQuantidade(itemDTO.getQuantidade());
            item.setValorPorCaixa(itemDTO.getValorPorCaixa());

            double subtotal = itemDTO.getQuantidade() * itemDTO.getValorPorCaixa();
            item.setSubtotal(subtotal);

            total += subtotal;
            itens.add(item);
        }

        // 3️⃣ Vínculo final
        venda.setItens(itens);
        venda.setValorTotal(total);

        // 4️⃣ Persistência (cascade cuida dos itens)
        vendaRepository.save(venda);
    }
}
