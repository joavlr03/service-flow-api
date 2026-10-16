ALTER TABLE tb_ordens_servicos ADD COLUMN forma_pagamento VARCHAR(20);
ALTER TABLE tb_ordens_servicos ADD COLUMN pago BOOLEAN NOT NULL DEFAULT FALSE;
-- Atendimentos finalizados antes desta versão são considerados recebidos (forma não informada).
UPDATE tb_ordens_servicos SET pago = TRUE WHERE status = 'FINALIZADO';
