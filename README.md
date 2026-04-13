# Microsserviço de Usuário

Microsserviço responsável pela gestão de usuários, autenticação JWT e dados de perfil do sistema de agendamento de tarefas.

## 📝 Descrição do Projeto

Esta API gerencia todo o fluxo de usuários do ecossistema, incluindo cadastro, autenticação, persistência de perfis (endereços e telefones) e integração com a API ViaCEP para busca de endereços por CEP.

Este serviço é parte integrante de uma arquitetura de microsserviços. Para visualizar e rodar o sistema completo, acesse o repositório principal:

🔗 BFF Orquestrador: [github.com/Ja0Santana/BFF-Agendador](https://github.com/Ja0Santana/BFF-Agendador)

## 🐋 Docker Hub - Imagem Oficial

```bash
docker pull joaopaul0/api-usuario:latest
```

## 🛠️ Tecnologias e Ferramentas

- Java 17+ & Spring Boot 3
- Spring Security + JWT (Autenticação stateless)
- PostgreSQL (Persistência de dados relacionais)
- ViaCEP (Integração para busca de endereços por CEP)
- Docker (Containerização)
- SonarQube (Inspeção de segurança e qualidade)
- Swagger/OpenAPI (Documentação dos endpoints)

## 🔐 Segurança

- **JWT**: Autenticação stateless com tokens assinados via secret configurável.
- **IDOR Protection**: Validação de ownership em endpoints de atualização e exclusão — um usuário não consegue acessar ou modificar dados de outro.
- **Credenciais Externalizadas**: Nenhum segredo é mantido no código-fonte; tudo é configurado via variáveis de ambiente.
- **Response Sanitization**: Hash de senha nunca é retornado nas respostas da API.

## ⚙️ Variáveis de Ambiente

Copie o arquivo `.env.example` para `.env` e preencha com seus valores:

```bash
cp .env.example .env
```

| Variável | Descrição | Exemplo |
|---|---|---|
| `DB_USER` | Usuário do PostgreSQL | `postgres` |
| `DB_PASS` | Senha do PostgreSQL | `sua-senha` |
| `DB_NAME` | Nome do banco de dados | `db_usuario` |
| `JWT_SECRET` | Chave secreta JWT (Base64, mínimo 32 caracteres) | `c2VjcmV0LWtleS0xMjM0NTY3ODkw...` |

## 🚀 Endpoints Principais

| Método | Rota | Autenticação | Descrição |
|---|---|---|---|
| `POST` | `/usuario` | ❌ | Criar novo usuário |
| `POST` | `/usuario/login` | ❌ | Autenticar e obter token JWT |
| `GET` | `/usuario/me` | ✅ | Buscar dados do usuário autenticado |
| `PUT` | `/usuario` | ✅ | Atualizar dados do usuário |
| `DELETE` | `/usuario/{email}` | ✅ | Deletar conta (apenas a própria) |
| `PUT` | `/usuario/endereco?id=` | ✅ | Atualizar endereço (validação de ownership) |
| `PUT` | `/usuario/telefone?id=` | ✅ | Atualizar telefone (validação de ownership) |
| `POST` | `/usuario/endereco` | ✅ | Cadastrar novo endereço |
| `POST` | `/usuario/telefone` | ✅ | Cadastrar novo telefone |
| `GET` | `/usuario/endereco/{cep}` | ❌ | Buscar endereço via ViaCEP |

## 🚦 Como Rodar

### Via Docker (Recomendado)

O serviço é orquestrado pelo `docker-compose.yml` do repositório [BFF-Agendador](https://github.com/Ja0Santana/BFF-Agendador). Basta configurar o `.env` no BFF e executar:

```bash
docker-compose up --build
```

### Localmente

1. Clone o repositório:
```bash
git clone https://github.com/Ja0Santana/usuario.git
```

2. Copie e configure o `.env`:
```bash
cp .env.example .env
```

3. Certifique-se de ter uma instância do PostgreSQL ativa.

4. Execute:
```bash
./gradlew bootRun
```

A documentação Swagger estará disponível em: `http://localhost:8080/swagger-ui.html`

## 🛡️ Qualidade e Engenharia

- **SOLID**: Código limpo, desacoplado e de fácil manutenção.
- **Data Integrity**: Validações de constraints e segurança de dados.
- **CI/CD Ready**: Pipeline com SonarQube para inspeção contínua.
