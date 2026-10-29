-- Reconciliacao das contas a receber do cliente PC com o historico real de baixas.
-- PostgreSQL. Execute primeiro como esta: a transacao termina em ROLLBACK.
-- Depois de conferir o resultado retornado pelo UPDATE, substitua ROLLBACK por COMMIT
-- e execute novamente para gravar a correcao.

BEGIN;

-- Situacao atual e valor calculado exclusivamente a partir das baixas registradas.
SELECT
    cr.id AS conta_id,
    cr.cliente_nome,
    cr.valor_original,
    cr.valor_pago AS valor_pago_atual,
    COALESCE(SUM(p.valor_pago), 0)::numeric(15, 2) AS valor_pago_pelas_baixas,
    cr.saldo_devedor AS saldo_atual,
    (cr.valor_original - COALESCE(SUM(p.valor_pago), 0))::numeric(15, 2) AS saldo_pelas_baixas,
    (cr.valor_pago - COALESCE(SUM(p.valor_pago), 0))::numeric(15, 2) AS diferenca
FROM conta_receber cr
LEFT JOIN pagamento p ON p.conta_receber_id = cr.id
WHERE LOWER(TRIM(cr.cliente_nome)) = LOWER(TRIM('PC'))
GROUP BY cr.id, cr.cliente_nome, cr.valor_original, cr.valor_pago, cr.saldo_devedor
ORDER BY cr.id;

-- Interrompe a operacao se houver baixas acima do valor original do pedido.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM conta_receber cr
        LEFT JOIN pagamento p ON p.conta_receber_id = cr.id
        WHERE LOWER(TRIM(cr.cliente_nome)) = LOWER(TRIM('PC'))
        GROUP BY cr.id, cr.valor_original
        HAVING COALESCE(SUM(p.valor_pago), 0) > cr.valor_original
    ) THEN
        RAISE EXCEPTION 'Existem baixas acima do valor original. Reconciliacao cancelada.';
    END IF;
END $$;

-- O historico de pagamentos passa a ser a fonte auditavel do total pago.
WITH totais AS (
    SELECT
        cr.id,
        COALESCE(SUM(p.valor_pago), 0)::numeric(15, 2) AS valor_pago_real
    FROM conta_receber cr
    LEFT JOIN pagamento p ON p.conta_receber_id = cr.id
    WHERE LOWER(TRIM(cr.cliente_nome)) = LOWER(TRIM('PC'))
    GROUP BY cr.id
)
UPDATE conta_receber cr
SET
    valor_pago = totais.valor_pago_real,
    saldo_devedor = cr.valor_original - totais.valor_pago_real
FROM totais
WHERE cr.id = totais.id
RETURNING
    cr.id AS conta_id,
    cr.cliente_nome,
    cr.valor_original,
    cr.valor_pago,
    cr.saldo_devedor;

-- Modo seguro para a primeira conferencia.
ROLLBACK;
