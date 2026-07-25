package com.expedicao.estoque.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expedicao.estoque.dto.JarvitDTO;
import com.expedicao.estoque.service.JarvitService;
import com.expedicao.estoque.service.JarvitAgentService;

@RestController
@RequestMapping("/api/jarvit")
public class JarvitController {

    private final JarvitService jarvitService;
    private final JarvitAgentService jarvitAgentService;

    public JarvitController(JarvitService jarvitService, JarvitAgentService jarvitAgentService) {
        this.jarvitService = jarvitService;
        this.jarvitAgentService = jarvitAgentService;
    }

    @GetMapping("/insights")
    public JarvitDTO.Insights insights() {
        return jarvitService.gerarInsights();
    }

    @GetMapping("/previsoes")
    public JarvitDTO.Previsoes previsoes() {
        return jarvitService.gerarPrevisoes();
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody JarvitDTO.ChatRequest request) {
        try {
            return ResponseEntity.ok(jarvitService.responder(request.pergunta()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("erro", ex.getMessage()));
        }
    }

    @PostMapping("/conversar")
    public ResponseEntity<?> conversar(
            @RequestBody JarvitDTO.ConversaRequest request,
            Authentication authentication) {
        try {
            return ResponseEntity.ok(jarvitAgentService.conversar(request, authentication));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("erro", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.internalServerError()
                    .body(java.util.Map.of("erro", "Não foi possível processar a conversa: " + ex.getMessage()));
        }
    }

    @PostMapping("/acoes/{token}/confirmar")
    public ResponseEntity<?> confirmar(
            @org.springframework.web.bind.annotation.PathVariable String token,
            Authentication authentication) {
        try {
            return ResponseEntity.ok(jarvitAgentService.confirmar(token, authentication));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("erro", ex.getMessage()));
        }
    }

    @PostMapping("/acoes/{token}/cancelar")
    public ResponseEntity<?> cancelar(
            @org.springframework.web.bind.annotation.PathVariable String token,
            Authentication authentication) {
        try {
            return ResponseEntity.ok(jarvitAgentService.cancelar(token, authentication));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("erro", ex.getMessage()));
        }
    }
}
