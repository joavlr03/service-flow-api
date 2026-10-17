-- Modo de uso da empresa: SERVICOS (atendimentos, como sempre) ou VENDAS (produtos e estoque).
ALTER TABLE tb_empresas ADD COLUMN modo_uso VARCHAR(20) NOT NULL DEFAULT 'SERVICOS';

CREATE TABLE tb_produtos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    preco DECIMAL(12,2) NOT NULL,
    estoque INTEGER NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_produtos_empresa FOREIGN KEY (empresa_id) REFERENCES tb_empresas(id),
    CONSTRAINT ck_produtos_estoque CHECK (estoque >= 0)
);
CREATE INDEX idx_produtos_empresa ON tb_produtos (empresa_id);

-- Itens de uma venda; o preço é copiado do produto na hora, para o histórico não mudar com o cadastro.
CREATE TABLE tb_itens_ordem (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ordem_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL,
    preco_unitario DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_itens_ordem FOREIGN KEY (ordem_id) REFERENCES tb_ordens_servicos(id) ON DELETE CASCADE,
    CONSTRAINT fk_itens_produto FOREIGN KEY (produto_id) REFERENCES tb_produtos(id),
    CONSTRAINT ck_itens_quantidade CHECK (quantidade > 0)
);
CREATE INDEX idx_itens_ordem ON tb_itens_ordem (ordem_id);

-- Venda não tem serviço: o atendimento passa a ter serviço ou itens.
ALTER TABLE tb_ordens_servicos ALTER COLUMN tipo_servico_id DROP NOT NULL;
