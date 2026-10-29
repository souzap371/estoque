-- Auditoria somente leitura: nao altera nenhum dado.
-- Compara os acumulados de conta_receber com a soma real das baixas.

WITH baixas_por_conta AS (
    SELECT
        cr.id AS conta_id,
        COUNT(p.id) AS quantidade_baixas,
        COALESCE(SUM(p.valor_pago), 0)::numeric(15, 2) AS valor_pago_pelas_baixas
    FROM conta_receber cr
    LEFT JOIN pagamento p ON p.conta_receber_id = cr.id
    GROUP BY cr.id
)
SELECT
    cr.id AS conta_id,
    cr.venda_id,
    cr.cliente_nome,
    cr.valor_original,
    cr.valor_pago AS valor_pago_na_conta,
    b.valor_pago_pelas_baixas,
    (cr.valor_pago - b.valor_pago_pelas_baixas)::numeric(15, 2) AS diferenca_pagamentos,
    cr.saldo_devedor AS saldo_na_conta,
    GREATEST(cr.valor_original - b.valor_pago_pelas_baixas, 0)::numeric(15, 2) AS saldo_pelas_baixas,
    b.quantidade_baixas
FROM conta_receber cr
JOIN baixas_por_conta b ON b.conta_id = cr.id
WHERE cr.valor_pago IS DISTINCT FROM b.valor_pago_pelas_baixas
   OR cr.saldo_devedor IS DISTINCT FROM
      GREATEST(cr.valor_original - b.valor_pago_pelas_baixas, 0)::numeric(15, 2)
ORDER BY cr.cliente_nome, cr.id;

-- Para obter um resumo por cliente, selecione e execute separadamente
-- o bloco abaixo no pgAdmin.
WITH baixas_por_conta AS (
    SELECT
        cr.id AS conta_id,
        COALESCE(SUM(p.valor_pago), 0)::numeric(15, 2) AS valor_pago_pelas_baixas
    FROM conta_receber cr
    LEFT JOIN pagamento p ON p.conta_receber_id = cr.id
    GROUP BY cr.id
), divergencias AS (
    SELECT
        cr.id,
        cr.cliente_nome,
        cr.valor_original,
        cr.valor_pago,
        cr.saldo_devedor,
        b.valor_pago_pelas_baixas
    FROM conta_receber cr
    JOIN baixas_por_conta b ON b.conta_id = cr.id
    WHERE cr.valor_pago IS DISTINCT FROM b.valor_pago_pelas_baixas
       OR cr.saldo_devedor IS DISTINCT FROM
          GREATEST(cr.valor_original - b.valor_pago_pelas_baixas, 0)::numeric(15, 2)
)
SELECT
    cliente_nome,
    COUNT(*) AS quantidade_contas_divergentes,
    SUM(valor_pago)::numeric(15, 2) AS pago_registrado_nas_contas,
    SUM(valor_pago_pelas_baixas)::numeric(15, 2) AS pago_comprovado_pelas_baixas,
    SUM(valor_pago - valor_pago_pelas_baixas)::numeric(15, 2) AS diferenca_total
FROM divergencias
GROUP BY cliente_nome
ORDER BY ABS(SUM(valor_pago - valor_pago_pelas_baixas)) DESC, cliente_nome;
