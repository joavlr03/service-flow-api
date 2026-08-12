<div align="center">

# 🚗 Service Flow

### Gestão operacional e financeira para prestadores de serviços

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=111827)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)

[Aplicação](https://serviceflow-frontend-xi.vercel.app) · [API](https://github.com/joavlr03/service-flow-api) · [Frontend](https://github.com/joavlr03/serviceflow-frontend)

</div>

---

## Sobre o Service Flow

O **Service Flow** é uma plataforma de gestão para empresas prestadoras de serviços. O produto centraliza clientes, veículos, catálogo de serviços, agenda, ordens de serviço, despesas e indicadores financeiros em um único fluxo.

O projeto nasceu para atender a operação de um lava-rápido, substituindo controles dispersos em papel, planilhas e conversas por uma experiência simples, mobile-first e preparada para evoluir como uma solução SaaS multiempresa.

## Repositórios

| Projeto | Responsabilidade | Tecnologias | Repositório |
| --- | --- | --- | --- |
| **Service Flow API** | Regras de negócio, autenticação, persistência e API REST | Java 21, Spring Boot 3.5, Spring Security, JPA, Flyway e MySQL | [Acessar backend](https://github.com/joavlr03/service-flow-api) |
| **Service Flow Frontend** | Interface web responsiva e aplicativo Android via Capacitor | React 19, TypeScript, TanStack, Tailwind CSS, Vite e Capacitor | [Acessar frontend](https://github.com/joavlr03/serviceflow-frontend) |

## Arquitetura

```mermaid
flowchart LR
    U["Usuário"] --> F["Frontend<br/>React + TanStack"]
    F -->|"REST / JSON + JWT"| A["API<br/>Spring Boot"]
    A --> D[("MySQL")]
    A --> M["Serviço de e-mail"]
```

O frontend consome a API REST versionada em `/api/v2`. A API concentra as regras de negócio, protege os recursos com JWT e utiliza migrações Flyway para manter a estrutura do banco de dados.

## Funcionalidades

- Autenticação com renovação de sessão e recuperação de senha;
- cadastro inicial da empresa e do proprietário;
- gestão de clientes e seus veículos;
- catálogo de tipos de serviço;
- criação e acompanhamento de ordens de serviço;
- agenda diária de atendimentos;
- atualização de status de serviços;
- registro de despesas;
- dashboard operacional e resumo financeiro;
- separação dos dados por empresa.

## Executando o ecossistema localmente

### Requisitos

- Git;
- Java 21;
- Maven 3.9 ou superior;
- Node.js e npm;
- Docker, recomendado para executar o MySQL.

### 1. Backend

```bash
git clone https://github.com/joavlr03/service-flow-api.git
cd service-flow-api
docker compose up -d
mvn spring-boot:run
```

A API será iniciada em `http://localhost:9000`. A documentação Swagger fica disponível na rota raiz.

### 2. Frontend

Em outro terminal:

```bash
git clone https://github.com/joavlr03/serviceflow-frontend.git
cd serviceflow-frontend
cp .env.example .env
npm install
npm run dev
```

Para desenvolvimento local, configure o arquivo `.env` com:

```env
VITE_API_URL=http://localhost:9000/api/v2
```

O frontend será iniciado normalmente em `http://localhost:8080`.

> No PowerShell, use `Copy-Item .env.example .env` no lugar de `cp .env.example .env`.

## Estrutura dos projetos

```text
Service Flow
├── service-flow-api/          # API REST e regras de negócio
│   ├── docs/                  # Contrato OpenAPI
│   └── src/
│       ├── main/java/         # Aplicação Spring Boot
│       └── main/resources/    # Configurações e migrações Flyway
│
└── serviceflow-frontend/      # Interface web e aplicativo Android
    ├── android/               # Projeto nativo gerado pelo Capacitor
    ├── public/                # Recursos públicos
    └── src/
        ├── components/        # Componentes da interface
        ├── lib/               # Cliente da API, estado e utilitários
        └── routes/            # Telas e rotas da aplicação
```

## Fluxo principal

1. O administrador cadastra o cliente, o veículo e o serviço.
2. Uma ordem de serviço reúne os dados do atendimento, data, horário e valor.
3. A ordem passa a compor a agenda e o faturamento previsto.
4. A mudança de status registra a conclusão ou o cancelamento.
5. Serviços finalizados alimentam a receita realizada e os indicadores.

## Testes e qualidade

No backend:

```bash
mvn test
```

No frontend:

```bash
npm run lint
npm run build
```

## Documentação da API

O contrato OpenAPI versionado está disponível em [`docs/openapi.yaml`](docs/openapi.yaml). Com a API em execução, use o Swagger para explorar e testar os endpoints.

---

<div align="center">

Desenvolvido para simplificar a rotina de quem presta serviços.

</div>
