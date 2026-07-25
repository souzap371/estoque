package com.expedicao.estoque;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.expedicao.estoque.service.JarvitService;
import com.expedicao.estoque.service.JarvitAgentService;
import com.expedicao.estoque.dto.JarvitDTO;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EstoqueApplicationTests {

	@Autowired
	private JarvitService jarvitService;

	@Autowired
	private JarvitAgentService jarvitAgentService;

	@Test
	void contextLoads() {
	}

	@Test
	void jarvitGeraInsightsPrevisoesEChatLocal() {
		var insights = jarvitService.gerarInsights();
		var previsoes = jarvitService.gerarPrevisoes();
		var chat = jarvitService.responder("Qual é o resumo financeiro?");
		var menorEstoque = jarvitService.responder("Qual produto tem menos quantidade?");

		assertNotNull(insights);
		assertFalse(insights.alertas().isEmpty());
		assertNotNull(previsoes);
		assertNotNull(previsoes.metodologia());
		assertNotNull(chat.resposta());
		assertFalse(chat.resposta().isBlank());
		assertTrue(menorEstoque.resposta().contains("menor estoque físico"));
		assertFalse(menorEstoque.resposta().contains("cobertura crítica"));
	}

	@Test
	void jarvitAgentInformaQuandoChaveNaoEstaConfigurada() {
		var auth = new TestingAuthenticationToken("master", "n/a", "ROLE_MASTER");
		var response = jarvitAgentService.conversar(
				new JarvitDTO.ConversaRequest("Liste os clientes que mais pagaram", null), auth);

		assertFalse(response.iaConfigurada());
		assertTrue(response.resposta().contains("OPENAI_API_KEY"));
	}
}
