package com.expedicao.estoque.service;

import com.expedicao.estoque.model.Cliente;
import com.expedicao.estoque.model.Venda;
import com.expedicao.estoque.repositorie.ClienteRepository;
import com.expedicao.estoque.repositorie.VendaRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final VendaRepository vendaRepository;

    public ClienteService(
            ClienteRepository clienteRepository,
            VendaRepository vendaRepository) {

        this.clienteRepository = clienteRepository;
        this.vendaRepository = vendaRepository;
    }

    // ============================
    // CRUD CLIENTES
    // ============================

    public Cliente salvar(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Cliente não encontrado"));
    }

    public void excluir(Long id) {
        clienteRepository.deleteById(id);
    }

    public List<Cliente> pesquisar(String nome) {

        if (nome == null || nome.isBlank()) {
            return clienteRepository.findAll();
        }

        return clienteRepository
                .findByNomeCompletoContainingIgnoreCase(nome);
    }

    // ============================
    // IMPORTAÇÃO DE CLIENTES
    // ============================

    public int importarClientesDasVendas() {

        List<Venda> vendas = vendaRepository.findAll();

        int clientesImportados = 0;

        for (Venda venda : vendas) {

            if (venda.getClienteNome() == null ||
                venda.getClienteNome().trim().isEmpty()) {
                continue;
            }

            boolean existe =
                    clienteRepository.existsByNomeCompletoIgnoreCase(
                            venda.getClienteNome().trim());

            if (!existe) {

                Cliente cliente = new Cliente();

                cliente.setNomeCompleto(
                        venda.getClienteNome().trim());

                cliente.setUf(
                        venda.getClienteEstado());

                cliente.setReferencia("");

                clienteRepository.save(cliente);

                clientesImportados++;
            }
        }

        return clientesImportados;
    }
}