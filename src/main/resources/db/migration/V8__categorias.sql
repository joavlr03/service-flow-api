-- Categorias de despesa (e, depois, de entrada) gerenciadas por empresa.
CREATE TABLE tb_categorias (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    nome VARCHAR(40) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_categorias_empresa FOREIGN KEY (empresa_id) REFERENCES tb_empresas(id),
    CONSTRAINT uk_categoria_empresa_tipo_nome UNIQUE (empresa_id, tipo, nome)
);

-- As 8 categorias que eram fixas no app passam a ser o padrão de cada empresa existente.
INSERT INTO tb_categorias (empresa_id, tipo, nome)
SELECT e.id, 'DESPESA', c.nome
FROM tb_empresas e
CROSS JOIN (VALUES ('Produtos'), ('Água e Energia'), ('Aluguel'), ('Funcionários'),
                   ('Manutenção'), ('Impostos'), ('Fornecedores'), ('Outros')) AS c(nome);

-- Categoria já usada numa despesa e fora dessa lista continua disponível.
INSERT INTO tb_categorias (empresa_id, tipo, nome)
SELECT DISTINCT d.empresa_id, 'DESPESA', d.categoria
FROM tb_despesas d
WHERE NOT EXISTS (SELECT 1 FROM tb_categorias c
                  WHERE c.empresa_id = d.empresa_id AND c.tipo = 'DESPESA' AND c.nome = d.categoria);
