package com.expedicao.estoque.dto;

import java.math.BigDecimal;
import java.util.List;

public final class JarvitDTO {

    private JarvitDTO() {
    }

    public record Insight(
            String tipo,
            String severidade,
            String titulo,
            String descricao,
            String acao,
            String link) {
    }

    public record Insights(
            String resumo,
            List<Insight> alertas,
            int estoqueCritico,
            int contasAtrasadas,
            BigDecimal totalAReceber,
            BigDecimal totalAPagar) {
    }

    public record PrevisaoProduto(
            Long produtoId,
            String produto,
            int estoqueAtual,
            double mediaDiaria,
            Integer diasCobertura,
            int reposicaoSugerida,
            String nivel) {
    }

    public record Previsoes(
            List<PrevisaoProduto> produtos,
            BigDecimal vendasProjetadas30Dias,
            BigDecimal saldoFinanceiroProjetado,
            String metodologia) {
    }

    public record ChatRequest(String pergunta) {
    }

    public record ChatResponse(String resposta, String fonte, boolean iaConfigurada) {
    }

    public record ConversaRequest(String mensagem, String conversaId) {
    }

    public record AcaoPendente(String token, String tipo, String resumo) {
    }

    public record ConversaResponse(
            String conversaId,
            String resposta,
            AcaoPendente acaoPendente,
            boolean iaConfigurada) {
    }

    public record ConfirmacaoResponse(boolean sucesso, String mensagem) {
    }
}
