# AI User Control

Sistema de gestão e controle de uso de ferramentas de IA para empresas.

## 📋 Descrição

O **AI User Control** é uma aplicação desenvolvida para centralizar a gestão e o controle de ferramentas de IA utilizadas em empresas, como Claude, GitHub Copilot e Cursor. O sistema permite:

- ✅ Gerenciar usuários/colaboradores e suas contas em ferramentas de IA
- ✅ Vincular logins de ferramentas com emails corporativos
- ✅ Monitorar métricas de uso (tokens consumidos, último acesso, etc.)
- ✅ Automatizar a desativação de contas quando colaboradores são desligados
- ✅ Gerar relatórios de uso e dashboards
- ✅ Integração com APIs das ferramentas de IA

## 🚀 Tecnologias

- **Java 21**
- **Spring Boot 3.4.2**
- **PostgreSQL 16**
- **Spring Security + JWT**
- **Thymeleaf** (interface web)
- **Flyway** (migrations)
- **Docker & Docker Compose**
- **OpenAPI/Swagger** (documentação)
- **Maven** (gerenciamento de dependências)

## 📁 Estrutura do Projeto

```
ai-user-control/
├── src/
│   ├── main/
│   │   ├── java/com/bemobi/aiusercontrol/
│   │   │   ├── user/          # Gestão de usuários
│   │   │   ├── aitool/        # Gestão de ferramentas de IA
│   │   │   ├── usage/         # Métricas de uso
│   │   │   ├── integration/   # Integrações com APIs externas
│   │   │   ├── config/        # Configurações
│   │   │   ├── security/      # Segurança e JWT
│   │   │   └── ...
│   │   └── resources/
│   │       ├── db/migration/  # Scripts Flyway
│   │       ├── templates/     # Templates Thymeleaf
│   │       └── application.yml
│   └── test/
├── docker/
├── docs/
└── pom.xml
```

## 🛠️ Instalação e Configuração

### Pré-requisitos

- Java 21 ou superior
- Maven 3.9+
- Docker e Docker Compose
- PostgreSQL 16 (ou usar via Docker)

### 1. Clone o repositório

```bash
cd /home/flavio.magacho/Dropbox/bemobi/dev/ai-user-control-gsd
```

### 2. Configure as variáveis de ambiente

```bash
cp .env.example .env
# Edite o arquivo .env com suas configurações
```

### 3. Inicie o banco de dados

```bash
docker-compose up -d postgres
```

Isso irá iniciar:
- PostgreSQL na porta **5432**
- pgAdmin na porta **5050** (http://localhost:5050)
  - Email: admin@bemobi.com
  - Senha: admin123

### 4. Execute as migrations

```bash
./mvnw flyway:migrate
```

### 5. Compile o projeto

```bash
./mvnw clean install
```

### 6. Execute a aplicação

```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em: **http://localhost:8080**

## 🐳 Executando com Docker

Para executar toda a stack (banco + aplicação):

```bash
docker-compose --profile with-app up -d
```

## 📚 Documentação da API

Após iniciar a aplicação, acesse:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Executar testes de integração
./mvnw verify
```

## 🔑 Credenciais Padrão

**Usuário Admin:**
- Username: `admin`
- Password: `admin123`

⚠️ **IMPORTANTE**: Altere essas credenciais em produção!

## 📊 Funcionalidades Principais

### 1. Gestão de Usuários
- Cadastro de colaboradores
- Vinculação com email corporativo
- Status (ativo/inativo)

### 2. Gestão de Ferramentas de IA
- Cadastro de ferramentas (Claude, GitHub Copilot, Cursor, etc.)
- Configuração de integrações

### 3. Contas de Usuário
- Vincular usuários a ferramentas
- Mapear login da ferramenta ↔ email corporativo
- Gerenciar múltiplas contas por usuário

### 4. Métricas de Uso
- Último acesso
- Tokens consumidos
- Frequência de uso
- Relatórios e dashboards

### 5. Automações
- Coleta automática de métricas (agendada)
- Verificação de contas inativas
- Notificações por email

## 🔧 Configuração de Integrações

### Claude API

```yaml
app:
  integrations:
    claude:
      api-url: https://api.anthropic.com/v1
      api-key: ${CLAUDE_API_KEY}
      enabled: true
```

### GitHub Copilot

```yaml
app:
  integrations:
    github-copilot:
      api-url: https://api.github.com
      api-token: ${GITHUB_API_TOKEN}
      organization: bemobi
      enabled: true
```

### Cursor

```yaml
app:
  integrations:
    cursor:
      api-url: ${CURSOR_API_URL}
      api-key: ${CURSOR_API_KEY}
      enabled: true
```

## 📅 Tarefas Agendadas

- **Coleta de Métricas**: Diariamente às 2h (configurável)
- **Verificação de Contas Inativas**: Diariamente às 3h (configurável)

## 🚀 Deploy em Produção

1. Configure as variáveis de ambiente para produção
2. Altere o perfil Spring para `prod`:
   ```bash
   export SPRING_PROFILE=prod
   ```
3. Use um JWT secret seguro (mínimo 32 caracteres)
4. Configure SSL/TLS
5. Use um banco de dados PostgreSQL gerenciado
6. Configure backup automático

## 📝 TODO

- [ ] Implementar autenticação OAuth2/OIDC
- [ ] Adicionar suporte a mais ferramentas de IA
- [ ] Dashboard com gráficos em tempo real
- [ ] Exportação de relatórios (PDF, Excel)
- [ ] Notificações via Slack/Teams
- [ ] API para webhooks

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

## 📄 Licença

Este projeto é proprietário da Bemobi.

## 👥 Autores

- **Bemobi Team**

## 📞 Suporte

Para suporte, entre em contato com: dev@bemobi.com
