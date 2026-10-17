-- Entradas avulsas: receita que não vem de atendimento (venda de produto, comissão etc.).
CREATE TABLE tb_entradas (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    descricao VARCHAR(180) NOT NULL,
    categoria VARCHAR(40) NOT NULL,
    valor DECIMAL(12,2) NOT NULL,
    data DATE NOT NULL,
    observacoes TEXT,
    criado_em TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_entradas_empresa FOREIGN KEY (empresa_id) REFERENCES tb_empresas(id)
);
CREATE INDEX idx_entradas_empresa_data ON tb_entradas (empresa_id, data);

-- Categorias de entrada padrão para cada empresa existente (as novas recebem no cadastro).
INSERT INTO tb_categorias (empresa_id, tipo, nome)
SELECT e.id, 'ENTRADA', c.nome
FROM tb_empresas e
CROSS JOIN (VALUES ('Venda de produto'), ('Serviço avulso'), ('Comissão'), ('Outros')) AS c(nome);
