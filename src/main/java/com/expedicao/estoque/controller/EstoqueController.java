// package com.expedicao.estoque.controller;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;

// import com.expedicao.estoque.model.Filial;
// import com.expedicao.estoque.model.Produto;
// import com.expedicao.estoque.service.EstoqueService;
// import com.expedicao.estoque.service.ProdutoService;

// @Controller
// @RequestMapping("/estoque")
// public class EstoqueController {

//     @Autowired
//     private ProdutoService produtoService;

//     @Autowired
//     private EstoqueService estoqueService;

//     @GetMapping
//     public String telaEstoque() {
//         return "estoque";
//     }

//     @PostMapping("/entrada")
//     public String entradaEstoque(
//         @RequestParam String codigoOuNome,
//         @RequestParam Integer quantidade
//     ) {

//         Produto produto = produtoService.buscarPorCodigoOuNome(codigoOuNome);
//         estoqueService.entradaEstoque(produto, filial, quantidade);


//         return "redirect:/estoque";
//     }

//     @GetMapping("/filial")
//     public String estoquePorFilial(
//             @RequestParam(defaultValue = "MATRIZ") Filial filial,
//             Model model
//     ) {
//         model.addAttribute("estoques", estoqueRepository.findByFilial(filial));
//         return "estoque-filial";
//     }
// }



package com.expedicao.estoque.controller;

import com.expedicao.estoque.model.Filial;
import com.expedicao.estoque.model.Produto;
import com.expedicao.estoque.service.EstoqueService;
import com.expedicao.estoque.service.ProdutoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private EstoqueService estoqueService;

    // 🔹 Tela principal
    @GetMapping
    public String telaEstoque() {
        return "estoque";
    }

    // 🔹 Entrada manual de estoque
    // @PostMapping("/entrada")
    // public String entradaEstoque(
    //         @RequestParam String codigoOuNome,
    //         @RequestParam Filial filial,
    //         @RequestParam Integer quantidade
    // ) {
    //     Produto produto = produtoService.buscarPorCodigoOuNome(codigoOuNome);
    //     estoqueService.entradaEstoque(produto, filial, quantidade);

    //     return "redirect:/estoque";
    // }

    @PostMapping("/entrada")
public String entradaEstoque(
        @RequestParam String codigoOuNome,
        @RequestParam Integer quantidade
) {
    Produto produto = produtoService.buscarPorCodigoOuNome(codigoOuNome);

    estoqueService.entradaEstoque(produto, Filial.MATRIZ, quantidade);

    return "redirect:/estoque";
}

    // 🔹 Estoque por filial
    @GetMapping("/filial")
    public String estoquePorFilial(
            @RequestParam(defaultValue = "MATRIZ") Filial filial,
            Model model
    ) {
        model.addAttribute("estoques", estoqueService.listarPorFilial(filial));
        model.addAttribute("filial", filial);
        return "estoque-filial";
    }
}
