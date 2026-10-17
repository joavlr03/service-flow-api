-- Assistente de primeiro acesso (spec 001).
ALTER TABLE tb_empresas ADD COLUMN onboarding_concluido BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tb_empresas ADD COLUMN usa_duracao BOOLEAN NOT NULL DEFAULT TRUE;
-- Empresas que já existem já estão configuradas e não passam pelo assistente.
UPDATE tb_empresas SET onboarding_concluido = TRUE;
