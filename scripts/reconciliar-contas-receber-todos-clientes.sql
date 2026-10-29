-- ATENCAO: use este script somente depois de revisar o resultado da auditoria.
-- Ele considera o historico da tabela pagamento como a fonte oficial do valor pago.
-- A execucao padrao termina em ROLLBACK e, portanto, nao grava alteracoes.

BEGIN;

-- Impede reconciliacao automatica quando as baixas ultrapassam o valor do pedido.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM conta_receber cr
        LEFT JOIN pagamento p ON p.conta_receber_id = cr.id
        GROUP BY cr.id, cr.valor_original
        HAVING COALESCE(SUM(p.valor_pago), 0) > cr.valor_original
    ) THEN
        RAISE EXCEPTION 'Existem contas com baixas acima do valor original. Reconciliacao cancelada.';
    END IF;
END $$;

-- Corrige apenas contas cujos acumulados divergem do historico de baixas.
WITH totais AS (
    SELECT
        cr.id,
        COALESCE(SUM(p.valor_pago), 0)::numeric(15, 2) AS valor_pago_real
    FROM conta_receber cr
    LEFT JOIN pagamento p ON p.conta_receber_id = cr.id
    GROUP BY cr.id
)
UPDATE conta_receber cr
SET
    valor_pago = totais.valor_pago_real,
    saldo_devedor = cr.valor_original - totais.valor_pago_real
FROM totais
WHERE cr.id = totais.id
  AND (
      cr.valor_pago IS DISTINCT FROM totais.valor_pago_real
      OR cr.saldo_devedor IS DISTINCT FROM
         (cr.valor_original - totais.valor_pago_real)::numeric(15, 2)
  )
RETURNING
    cr.id AS conta_id,
    cr.venda_id,
    cr.cliente_nome,
    cr.valor_original,
    cr.valor_pago,
    cr.saldo_devedor;

-- Modo de simulacao. Troque por COMMIT somente depois de conferir todas as linhas.
ROLLBACK;
