CREATE TABLE tb_empresas (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    segmento VARCHAR(100) NOT NULL,
    nome_proprietario VARCHAR(150) NOT NULL,
    email VARCHAR(180) NOT NULL,
    plano VARCHAR(80) NOT NULL,
    ativo BOOLEAN NOT NULL,
    criado_em DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE tb_usuarios (
    id BIGINT NOT NULL AUTO_INCREMENT,
    empresa_id BIGINT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(180) NOT NULL,
    senha_hash VARCHAR(100) NOT NULL,
    perfil VARCHAR(30) NOT NULL,
    ativo BOOLEAN NOT NULL,
    ultimo_login_em DATETIME(6),
    criado_em DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT fk_usuarios_empresa FOREIGN KEY (empresa_id) REFERENCES tb_empresas (id)
);

CREATE INDEX idx_usuarios_empresa ON tb_usuarios (empresa_id);
