// package com.expedicao.estoque.controller;

// import java.math.BigDecimal;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.*;

// import com.expedicao.estoque.model.ContaPagar;
// import com.expedicao.estoque.service.ContaPagarService;
// import com.expedicao.estoque.service.FornecedorService;

// @Controller
// @RequestMapping("/contas-pagar")
// public class ContaPagarController {

//         @Autowired
//         private ContaPagarService contaPagarService;

//         @Autowired
//         private FornecedorService fornecedorService;

//         // =========================
//         // RELATÓRIO / LISTAGEM
//         // =========================
//         @GetMapping
//         public String listar(Model model) {

//                 model.addAttribute(
//                                 "contas",
//                                 contaPagarService.listarTodas());

//                 return "contaspagar";
//         }

//         // =========================
//         // TELA CADASTRO
//         // =========================
//         @GetMapping("/nova")
//         public String novaConta(Model model) {

//                 model.addAttribute(
//                                 "conta",
//                                 new ContaPagar());

//                 model.addAttribute(
//                                 "fornecedores",
//                                 fornecedorService.listarTodos());

//                 return "contaspagarform";
//         }

//         // =========================
//         // SALVAR
//         // =========================
//         @PostMapping("/salvar")
//         public String salvar(
//                         @ModelAttribute ContaPagar conta) {

//                 contaPagarService.salvar(conta);

//                 return "redirect:/contas-pagar";
//         }

//         // =========================
//         // EDITAR
//         // =========================
//         @GetMapping("/editar/{id}")
//         public String editar(
//                         @PathVariable Long id,
//                         Model model) {

//                 ContaPagar conta = contaPagarService.buscarPorId(id);

//                 model.addAttribute(
//                                 "conta",
//                                 conta);

//                 model.addAttribute(
//                                 "fornecedores",
//                                 fornecedorService.listarTodos());

//                 return "contaspagarform";
//         }

//         // =========================
//         // PAGAMENTO
//         // =========================
//         @PostMapping("/{id}/pagar")
//         public String pagar(
//                         @PathVariable Long id,
//                         @RequestParam BigDecimal valorPagamento,
//                         Model model) {

//                 try {

//                         contaPagarService.pagarConta(
//                                         id,
//                                         valorPagamento);

//                 } catch (RuntimeException e) {

//                         model.addAttribute(
//                                         "erro",
//                                         e.getMessage());

//                         model.addAttribute(
//                                         "contas",
//                                         contaPagarService.listarTodas());

//                         return "contaspagar";
//                 }

//                 return "redirect:/contas-pagar";
//         }

//         // =========================
//         // ATUALIZAR
//         // =========================
//         @PostMapping("/atualizar")
//         public String atualizar(
//                         @ModelAttribute ContaPagar conta) {

//                 contaPagarService.atualizar(conta);

//                 return "redirect:/contas-pagar";
//         }

//         // =========================
//         // EXCLUIR
//         // =========================
//         @GetMapping("/excluir/{id}")
//         public String excluir(@PathVariable Long id) {

//                 contaPagarService.excluir(id);

//                 return "redirect:/contas-pagar";
//         }

// }






// package com.expedicao.estoque.controller;

// import java.math.BigDecimal;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.*;

// import com.expedicao.estoque.model.ContaPagar;
// import com.expedicao.estoque.model.Fornecedor;
// import com.expedicao.estoque.service.ContaPagarService;
// import com.expedicao.estoque.service.FornecedorService;

// @Controller
// @RequestMapping("/contas-pagar")
// public class ContaPagarController {

//         @Autowired
//         private ContaPagarService contaPagarService;

//         @Autowired
//         private FornecedorService fornecedorService;

//         // =========================
//         // RELATÓRIO / LISTAGEM
//         // =========================
//         @GetMapping
//         public String listar(Model model) {

//                 model.addAttribute(
//                                 "contas",
//                                 contaPagarService.listarTodas());

//                 return "contaspagar";
//         }

//         // =========================
//         // TELA CADASTRO
//         // =========================
//         @GetMapping("/nova")
//         public String novaConta(Model model) {

//                 model.addAttribute(
//                                 "conta",
//                                 new ContaPagar());

//                 model.addAttribute(
//                                 "fornecedores",
//                                 fornecedorService.listarTodos());

//                 return "contaspagarform";
//         }

//         // =========================
//         // SALVAR
//         // =========================
//         @PostMapping("/salvar")
//         public String salvar(

//                         @ModelAttribute ContaPagar conta,

//                         @RequestParam String razaoSocialFornecedor,
//                         @RequestParam(required = false) String cnpjFornecedor,
//                         @RequestParam(required = false) String telefoneFornecedor,
//                         @RequestParam(required = false) String emailFornecedor) {

//                 // CRIA FORNECEDOR
//                 Fornecedor fornecedor = new Fornecedor();

//                 fornecedor.setRazaoSocial(razaoSocialFornecedor);

//                 fornecedor.setCnpj(cnpjFornecedor);

//                 fornecedor.setTelefone(telefoneFornecedor);

//                 fornecedor.setEmail(emailFornecedor);

//                 // SALVA FORNECEDOR
//                 fornecedor = fornecedorService.salvar(fornecedor);

//                 // VINCULA FORNECEDOR À CONTA
//                 conta.setFornecedor(fornecedor);

//                 // SALVA CONTA
//                 contaPagarService.salvar(conta);

//                 return "redirect:/contas-pagar";
//         }

//         // =========================
//         // EDITAR
//         // =========================
//         @GetMapping("/editar/{id}")
//         public String editar(
//                         @PathVariable Long id,
//                         Model model) {

//                 ContaPagar conta = contaPagarService.buscarPorId(id);

//                 model.addAttribute(
//                                 "conta",
//                                 conta);

//                 model.addAttribute(
//                                 "fornecedores",
//                                 fornecedorService.listarTodos());

//                 return "contaspagarform";
//         }

//         // =========================
//         // PAGAMENTO
//         // =========================
//         @PostMapping("/{id}/pagar")
//         public String pagar(
//                         @PathVariable Long id,
//                         @RequestParam BigDecimal valorPagamento,
//                         Model model) {

//                 try {

//                         contaPagarService.pagarConta(
//                                         id,
//                                         valorPagamento);

//                 } catch (RuntimeException e) {

//                         model.addAttribute(
//                                         "erro",
//                                         e.getMessage());

//                         model.addAttribute(
//                                         "contas",
//                                         contaPagarService.listarTodas());

//                         return "contaspagar";
//                 }

//                 return "redirect:/contas-pagar";
//         }

//         // =========================
//         // ATUALIZAR
//         // =========================
//         @PostMapping("/atualizar")
//         public String atualizar(
//                         @ModelAttribute ContaPagar conta) {

//                 contaPagarService.atualizar(conta);

//                 return "redirect:/contas-pagar";
//         }

//         // =========================
//         // EXCLUIR
//         // =========================
//         @GetMapping("/excluir/{id}")
//         public String excluir(@PathVariable Long id) {

//                 contaPagarService.excluir(id);

//                 return "redirect:/contas-pagar";
//         }
// }

package com.expedicao.estoque.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.expedicao.estoque.model.ContaPagar;
import com.expedicao.estoque.model.Fornecedor;
import com.expedicao.estoque.service.ContaPagarService;
import com.expedicao.estoque.service.FornecedorService;

@Controller
@RequestMapping("/contas-pagar")
public class ContaPagarController {

    @Autowired
    private ContaPagarService contaPagarService;

    @Autowired
    private FornecedorService fornecedorService;

    // =========================
    // LISTAGEM
    // =========================
    @GetMapping
    public String listar(Model model) {

        model.addAttribute(
                "contas",
                contaPagarService.listarTodas());

        return "contaspagar";
    }

    // =========================
    // NOVA CONTA
    // =========================
    @GetMapping("/nova")
    public String novaConta(Model model) {

        model.addAttribute(
                "conta",
                new ContaPagar());

        return "contaspagarform";
    }

    // =========================
    // SALVAR
    // =========================
    @PostMapping("/salvar")
    public String salvar(

            @ModelAttribute ContaPagar conta,

            @RequestParam String razaoSocialFornecedor,
            @RequestParam(required = false) String cnpjFornecedor,
            @RequestParam(required = false) String telefoneFornecedor,
            @RequestParam(required = false) String emailFornecedor) {

        // CRIA FORNECEDOR
        Fornecedor fornecedor = new Fornecedor();

        fornecedor.setRazaoSocial(razaoSocialFornecedor);

        fornecedor.setCnpj(cnpjFornecedor);

        fornecedor.setTelefone(telefoneFornecedor);

        fornecedor.setEmail(emailFornecedor);

        // SALVA FORNECEDOR
        fornecedor = fornecedorService.salvar(fornecedor);

        // VINCULA FORNECEDOR
        conta.setFornecedor(fornecedor);

        // SALVA CONTA
        contaPagarService.salvar(conta);

        return "redirect:/contas-pagar";
    }

    // =========================
    // EDITAR
    // =========================
    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model) {

        ContaPagar conta =
                contaPagarService.buscarPorId(id);

        model.addAttribute(
                "conta",
                conta);

        return "contaspagarform";
    }

    // =========================
    // PAGAMENTO
    // =========================
    @PostMapping("/{id}/pagar")
    public String pagar(
            @PathVariable Long id,
            @RequestParam BigDecimal valorPagamento,
            Model model) {

        try {

            contaPagarService.pagarConta(
                    id,
                    valorPagamento);

        } catch (RuntimeException e) {

            model.addAttribute(
                    "erro",
                    e.getMessage());

            model.addAttribute(
                    "contas",
                    contaPagarService.listarTodas());

            return "contaspagar";
        }

        return "redirect:/contas-pagar";
    }

    // =========================
    // ATUALIZAR
    // =========================
    @PostMapping("/atualizar")
    public String atualizar(
            @ModelAttribute ContaPagar conta) {

        contaPagarService.atualizar(conta);

        return "redirect:/contas-pagar";
    }

    // =========================
    // EXCLUIR
    // =========================
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id) {

        contaPagarService.excluir(id);

        return "redirect:/contas-pagar";
    }
}