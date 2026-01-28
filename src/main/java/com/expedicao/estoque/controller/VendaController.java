// package com.expedicao.estoque.controller;

// import com.expedicao.estoque.dto.VendaDTO;
// import com.expedicao.estoque.service.VendaService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.*;

// @Controller
// @RequestMapping("/vendas")
// public class VendaController {

//     @Autowired
//     private VendaService vendaService;

//     @GetMapping
//     public String telaVenda() {
//         return "Venda.html";
//     }

//     @PostMapping("/salvar")
//     @ResponseBody
//     public void salvar(@RequestBody VendaDTO dto) {
//         vendaService.salvarPedido(dto);
//     }
// }


package com.expedicao.estoque.controller;

import com.expedicao.estoque.dto.VendaDTO;
import com.expedicao.estoque.repositorie.ProdutoRepository;
import com.expedicao.estoque.service.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private ProdutoRepository produtoRepository;

    // 🔹 ABRE TELA DE VENDA COM LISTA DE PRODUTOS
    @GetMapping
    public String telaVenda(Model model) {
        model.addAttribute("produtos", produtoRepository.findAll());
        return "Venda.html"; // NÃO usar .html aqui
    }

    // 🔹 SALVA PEDIDO
    @PostMapping("/salvar")
    @ResponseBody
    public void salvar(@RequestBody VendaDTO dto) {
        vendaService.salvarPedido(dto);
    }
}

