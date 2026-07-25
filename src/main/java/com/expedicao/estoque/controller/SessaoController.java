package com.expedicao.estoque.controller;

import java.security.Principal;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessaoController {

    @GetMapping("/api/sessao")
    public Map<String, String> sessao(Principal principal) {
        String username = principal == null ? "usuario" : principal.getName();
        String nome = nomeExibicao(username);
        String identificador = username.contains("@") ? username : username + "@sistemav.com";
        String inicial = nome.isBlank() ? "U" : nome.substring(0, 1).toUpperCase(Locale.ROOT);

        return Map.of(
                "username", username,
                "nome", nome,
                "identificador", identificador,
                "inicial", inicial);
    }

    private String nomeExibicao(String username) {
        String base = username.contains("@") ? username.substring(0, username.indexOf('@')) : username;
        return Stream.of(base.split("[._\\-\\s]+"))
                .filter(parte -> !parte.isBlank())
                .map(parte -> parte.substring(0, 1).toUpperCase(Locale.ROOT)
                        + parte.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }
}
