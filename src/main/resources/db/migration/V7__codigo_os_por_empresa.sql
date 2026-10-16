-- O código do atendimento é sequencial por empresa, então só precisa ser único dentro dela.
ALTER TABLE tb_ordens_servicos DROP CONSTRAINT uk_os_codigo;
ALTER TABLE tb_ordens_servicos ADD CONSTRAINT uk_os_empresa_codigo UNIQUE (empresa_id, codigo);
