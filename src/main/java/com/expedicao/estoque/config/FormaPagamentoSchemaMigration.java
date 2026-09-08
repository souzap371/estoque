package com.expedicao.estoque.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.expedicao.estoque.model.FormaPagamento;

/**
 * Mantém a restrição PostgreSQL do enum sincronizada com as opções da aplicação.
 * O ddl-auto atual cria a restrição, mas não a atualiza quando o enum recebe valores.
 */
@Component
@ConditionalOnProperty(
        name = "spring.datasource.driver-class-name",
        havingValue = "org.postgresql.Driver")
public class FormaPagamentoSchemaMigration implements ApplicationRunner {

    private static final String CONSTRAINT = "pagamento_forma_pagamento_check";

    private final JdbcTemplate jdbcTemplate;

    public FormaPagamentoSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String definicaoAtual = jdbcTemplate.query(
                """
                SELECT pg_get_constraintdef(oid)
                  FROM pg_constraint
                 WHERE conrelid = 'pagamento'::regclass
                   AND conname = ?
                """,
                resultado -> resultado.next() ? resultado.getString(1) : null,
                CONSTRAINT);

        List<String> formas = Arrays.stream(FormaPagamento.values())
                .map(Enum::name)
                .toList();

        if (definicaoAtual != null && formas.stream().allMatch(definicaoAtual::contains)) {
            return;
        }

        String valoresPermitidos = formas.stream()
                .map(valor -> "'" + valor + "'")
                .collect(Collectors.joining(", "));

        jdbcTemplate.execute("ALTER TABLE pagamento DROP CONSTRAINT IF EXISTS " + CONSTRAINT);
        jdbcTemplate.execute("ALTER TABLE pagamento ADD CONSTRAINT " + CONSTRAINT
                + " CHECK (forma_pagamento IN (" + valoresPermitidos + "))");
    }
}
