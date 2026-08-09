# ServiceFlow API

API REST v2 para o aplicativo ServiceFlow, construída com Java 21, Spring Boot, MySQL, Flyway, Spring Security e JWT.

## Requisitos

- Java 21
- Maven 3.9+
- Docker (opcional, recomendado para o MySQL)

## Execução local

```bash
docker compose up -d
mvn spring-boot:run
```

A API inicia em `http://localhost:9000` e o Swagger fica disponível na rota raiz.

Em banco vazio, cadastre o lava-rápido e o proprietário uma única vez com `POST /api/v2/setup`. Depois disso, o endpoint é bloqueado automaticamente.

Altere-as por variáveis de ambiente fora do ambiente local:

```text
BOOTSTRAP_ADMIN_EMAIL
BOOTSTRAP_ADMIN_PASSWORD
BOOTSTRAP_ENABLED
JWT_SECRET
DB_URL
DB_USERNAME
DB_PASSWORD
CORS_ALLOWED_ORIGINS
MAIL_HOST
MAIL_PORT
MAIL_USERNAME
MAIL_PASSWORD
SUPPORT_EMAIL
SUPPORT_RESET_KEY
```

Para o fluxo de recuperacao assistida, `SUPPORT_EMAIL` e o endereco que recebera
os pedidos e `SUPPORT_RESET_KEY` e uma chave longa e secreta usada somente pelo
responsavel pelo sistema. Com Gmail, use em `MAIL_PASSWORD` uma senha de
aplicativo, nunca a senha normal da conta.

Exemplo no Prompt de Comando do Windows:

```bat
set MAIL_USERNAME=seuemail@gmail.com
set MAIL_PASSWORD=sua-senha-de-aplicativo
set SUPPORT_EMAIL=qorelab.suporte@gmail.com
set SUPPORT_RESET_KEY=uma-chave-longa-e-aleatoria
mvn spring-boot:run
```

Ao receber o pedido, o responsavel redefine a senha do usuario existente pelo
Swagger usando `POST /api/v2/auth/suporte/redefinir-senha`, o cabecalho
`X-Support-Key` e o corpo:

```json
{
  "email": "usuario@empresa.com",
  "temporaryPassword": "Temporaria@8472"
}
```

Todas as sessoes desse usuario sao revogadas. No primeiro login com a senha
temporaria, o frontend exige a definicao de uma senha definitiva.

## Endpoints implementados

```text
POST /api/v2/auth/login
POST /api/v2/auth/refresh
POST /api/v2/auth/logout
POST /api/v2/auth/esqueci-minha-senha
POST /api/v2/auth/suporte/redefinir-senha
POST /api/v2/auth/redefinir-senha
POST /api/v2/auth/alterar-senha
POST /api/v2/setup
GET  /api/v2/auth/me
GET  /api/v2/empresa
POST /api/v2/clientes
GET  /api/v2/clientes
GET  /api/v2/clientes/{id}
POST /api/v2/veiculos
GET  /api/v2/clientes/{id}/veiculos
POST /api/v2/servicos
GET  /api/v2/servicos
POST /api/v2/ordens-servico
GET  /api/v2/ordens-servico
GET  /api/v2/ordens-servico/{id}
PATCH /api/v2/ordens-servico/{id}/status
POST /api/v2/despesas
GET  /api/v2/despesas
GET  /api/v2/financeiro/resumo
GET  /api/v2/dashboard/diario
```

Todos os recursos de negócio expõem operações de consulta por ID, criação, atualização (`PUT`) e exclusão (`DELETE`). Exclusões que destruiriam histórico são bloqueadas com `BUSINESS_ERROR`.

O contrato versionado está em `docs/openapi.yaml`.

O frontend fica no projeto separado `C:\Users\joave\OneDrive\Documentos\gestao lava rapido frontend`. Execute a API na porta 9000 e, em outro terminal, `npm.cmd run dev` dentro dessa pasta.

## Testes

```bash
mvn test
```
