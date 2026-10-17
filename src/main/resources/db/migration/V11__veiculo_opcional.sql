-- Veículo opcional: só empresas que atendem veículos (lava-rápido, oficina) precisam dele.
ALTER TABLE tb_empresas ADD COLUMN usa_veiculo BOOLEAN NOT NULL DEFAULT TRUE;
-- Modo vendas não tem veículo (spec 006).
UPDATE tb_empresas SET usa_veiculo = FALSE WHERE modo_uso = 'VENDAS';
ALTER TABLE tb_ordens_servicos ALTER COLUMN veiculo_id DROP NOT NULL;
