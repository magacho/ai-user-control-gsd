#!/bin/bash

# Script para criar estrutura do projeto AI User Control
# Sistema de gestão e controle de uso de ferramentas de IA

echo "Criando estrutura do projeto AI User Control..."

# Estrutura base do Maven
mkdir -p src/main/java/com/bemobi/aiusercontrol
mkdir -p src/main/resources
mkdir -p src/test/java/com/bemobi/aiusercontrol
mkdir -p src/test/resources

# Pacotes da aplicação
mkdir -p src/main/java/com/bemobi/aiusercontrol/config
mkdir -p src/main/java/com/bemobi/aiusercontrol/controller
mkdir -p src/main/java/com/bemobi/aiusercontrol/service
mkdir -p src/main/java/com/bemobi/aiusercontrol/repository
mkdir -p src/main/java/com/bemobi/aiusercontrol/model
mkdir -p src/main/java/com/bemobi/aiusercontrol/dto
mkdir -p src/main/java/com/bemobi/aiusercontrol/exception
mkdir -p src/main/java/com/bemobi/aiusercontrol/security
mkdir -p src/main/java/com/bemobi/aiusercontrol/util
mkdir -p src/main/java/com/bemobi/aiusercontrol/enums

# Pacotes para cada domínio
mkdir -p src/main/java/com/bemobi/aiusercontrol/model/entity
mkdir -p src/main/java/com/bemobi/aiusercontrol/dto/request
mkdir -p src/main/java/com/bemobi/aiusercontrol/dto/response

# Módulos específicos do domínio
mkdir -p src/main/java/com/bemobi/aiusercontrol/user
mkdir -p src/main/java/com/bemobi/aiusercontrol/user/controller
mkdir -p src/main/java/com/bemobi/aiusercontrol/user/service
mkdir -p src/main/java/com/bemobi/aiusercontrol/user/repository

mkdir -p src/main/java/com/bemobi/aiusercontrol/aitool
mkdir -p src/main/java/com/bemobi/aiusercontrol/aitool/controller
mkdir -p src/main/java/com/bemobi/aiusercontrol/aitool/service
mkdir -p src/main/java/com/bemobi/aiusercontrol/aitool/repository

mkdir -p src/main/java/com/bemobi/aiusercontrol/usage
mkdir -p src/main/java/com/bemobi/aiusercontrol/usage/controller
mkdir -p src/main/java/com/bemobi/aiusercontrol/usage/service
mkdir -p src/main/java/com/bemobi/aiusercontrol/usage/repository

mkdir -p src/main/java/com/bemobi/aiusercontrol/integration
mkdir -p src/main/java/com/bemobi/aiusercontrol/integration/claude
mkdir -p src/main/java/com/bemobi/aiusercontrol/integration/github
mkdir -p src/main/java/com/bemobi/aiusercontrol/integration/cursor

# Resources
mkdir -p src/main/resources/db/migration
mkdir -p src/main/resources/templates
mkdir -p src/main/resources/static/css
mkdir -p src/main/resources/static/js
mkdir -p src/main/resources/static/images
mkdir -p src/main/resources/application-profiles

# Testes
mkdir -p src/test/java/com/bemobi/aiusercontrol/controller
mkdir -p src/test/java/com/bemobi/aiusercontrol/service
mkdir -p src/test/java/com/bemobi/aiusercontrol/repository
mkdir -p src/test/java/com/bemobi/aiusercontrol/integration

# Docker e scripts
mkdir -p docker
mkdir -p scripts

# Documentação
mkdir -p docs
mkdir -p docs/api
mkdir -p docs/architecture
mkdir -p docs/deployment

# Arquivos principais
touch pom.xml
touch README.md
touch .gitignore
touch .env.example
touch docker-compose.yml

# Arquivo principal da aplicação
touch src/main/java/com/bemobi/aiusercontrol/AiUserControlApplication.java

# Configurações
touch src/main/resources/application.yml
touch src/main/resources/application-dev.yml
touch src/main/resources/application-prod.yml
touch src/main/resources/application-test.yml

# Config classes
touch src/main/java/com/bemobi/aiusercontrol/config/DatabaseConfig.java
touch src/main/java/com/bemobi/aiusercontrol/config/SecurityConfig.java
touch src/main/java/com/bemobi/aiusercontrol/config/WebConfig.java
touch src/main/java/com/bemobi/aiusercontrol/config/OpenApiConfig.java
touch src/main/java/com/bemobi/aiusercontrol/config/SchedulingConfig.java

# Security
touch src/main/java/com/bemobi/aiusercontrol/security/JwtAuthenticationFilter.java
touch src/main/java/com/bemobi/aiusercontrol/security/JwtTokenProvider.java
touch src/main/java/com/bemobi/aiusercontrol/security/UserDetailsServiceImpl.java

# Exception handling
touch src/main/java/com/bemobi/aiusercontrol/exception/GlobalExceptionHandler.java
touch src/main/java/com/bemobi/aiusercontrol/exception/ResourceNotFoundException.java
touch src/main/java/com/bemobi/aiusercontrol/exception/BusinessException.java
touch src/main/java/com/bemobi/aiusercontrol/exception/UnauthorizedException.java

# Enums
touch src/main/java/com/bemobi/aiusercontrol/enums/AIToolType.java
touch src/main/java/com/bemobi/aiusercontrol/enums/UserStatus.java
touch src/main/java/com/bemobi/aiusercontrol/enums/UsageMetricType.java

# User domain
touch src/main/java/com/bemobi/aiusercontrol/user/controller/UserController.java
touch src/main/java/com/bemobi/aiusercontrol/user/service/UserService.java
touch src/main/java/com/bemobi/aiusercontrol/user/repository/UserRepository.java
touch src/main/java/com/bemobi/aiusercontrol/model/entity/User.java

# AI Tool domain
touch src/main/java/com/bemobi/aiusercontrol/aitool/controller/AIToolController.java
touch src/main/java/com/bemobi/aiusercontrol/aitool/service/AIToolService.java
touch src/main/java/com/bemobi/aiusercontrol/aitool/repository/AIToolRepository.java
touch src/main/java/com/bemobi/aiusercontrol/model/entity/AITool.java
touch src/main/java/com/bemobi/aiusercontrol/model/entity/UserAIToolAccount.java

# Usage domain
touch src/main/java/com/bemobi/aiusercontrol/usage/controller/UsageController.java
touch src/main/java/com/bemobi/aiusercontrol/usage/service/UsageService.java
touch src/main/java/com/bemobi/aiusercontrol/usage/service/UsageMetricsCollector.java
touch src/main/java/com/bemobi/aiusercontrol/usage/repository/UsageRepository.java
touch src/main/java/com/bemobi/aiusercontrol/model/entity/UsageMetric.java

# DTOs
touch src/main/java/com/bemobi/aiusercontrol/dto/request/UserRegistrationRequest.java
touch src/main/java/com/bemobi/aiusercontrol/dto/request/AIToolAccountRequest.java
touch src/main/java/com/bemobi/aiusercontrol/dto/response/UserResponse.java
touch src/main/java/com/bemobi/aiusercontrol/dto/response/UsageReportResponse.java
touch src/main/java/com/bemobi/aiusercontrol/dto/response/DashboardResponse.java

# Integrações com APIs externas
touch src/main/java/com/bemobi/aiusercontrol/integration/claude/ClaudeApiClient.java
touch src/main/java/com/bemobi/aiusercontrol/integration/github/GitHubCopilotClient.java
touch src/main/java/com/bemobi/aiusercontrol/integration/cursor/CursorApiClient.java

# Utilities
touch src/main/java/com/bemobi/aiusercontrol/util/DateUtils.java
touch src/main/java/com/bemobi/aiusercontrol/util/EmailUtils.java
touch src/main/java/com/bemobi/aiusercontrol/util/TokenCalculator.java

# Migrations Flyway
touch src/main/resources/db/migration/V1__create_users_table.sql
touch src/main/resources/db/migration/V2__create_ai_tools_table.sql
touch src/main/resources/db/migration/V3__create_user_ai_tool_accounts_table.sql
touch src/main/resources/db/migration/V4__create_usage_metrics_table.sql

# Templates Thymeleaf
touch src/main/resources/templates/index.html
touch src/main/resources/templates/dashboard.html
touch src/main/resources/templates/users.html
touch src/main/resources/templates/usage-report.html

# Static resources
touch src/main/resources/static/css/style.css
touch src/main/resources/static/js/app.js
touch src/main/resources/static/js/dashboard.js

# Testes
touch src/test/java/com/bemobi/aiusercontrol/AiUserControlApplicationTests.java
touch src/test/java/com/bemobi/aiusercontrol/service/UserServiceTest.java
touch src/test/java/com/bemobi/aiusercontrol/service/UsageServiceTest.java
touch src/test/java/com/bemobi/aiusercontrol/controller/UserControllerTest.java
touch src/test/java/com/bemobi/aiusercontrol/integration/UserIntegrationTest.java
touch src/test/resources/application-test.yml

# Docker
touch docker/Dockerfile
touch docker/Dockerfile.dev
touch docker/.dockerignore

# Scripts
touch scripts/start-dev.sh
touch scripts/run-tests.sh
touch scripts/db-migrate.sh

# Documentação
touch docs/README.md
touch docs/SETUP.md
touch docs/API.md
touch docs/architecture/ARCHITECTURE.md
touch docs/architecture/DATABASE_SCHEMA.md
touch docs/deployment/DEPLOYMENT.md

# CI/CD
mkdir -p .github/workflows
touch .github/workflows/ci.yml
touch .github/workflows/deploy.yml

# Arquivos de configuração adicionais
touch lombok.config
touch checkstyle.xml

echo "✅ Estrutura do projeto criada com sucesso!"
echo ""
echo "Próximos passos:"
echo "1. Execute: chmod +x setup-project.sh"
echo "2. Execute: ./setup-project.sh"
echo "3. Configure o pom.xml com as dependências necessárias"
echo "4. Configure o application.yml com as credenciais do banco"
echo "5. Execute: docker-compose up -d (para subir o PostgreSQL)"
