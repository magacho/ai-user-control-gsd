#!/bin/bash

echo "🚀 Iniciando ambiente de desenvolvimento..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java não está instalado. Por favor, instale Java 21 ou superior."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Java 21 ou superior é necessário. Versão atual: $JAVA_VERSION"
    exit 1
fi

# Start PostgreSQL with Docker Compose
echo "📦 Iniciando PostgreSQL..."
docker-compose up -d postgres pgadmin

# Wait for PostgreSQL to be ready
echo "⏳ Aguardando PostgreSQL inicializar..."
sleep 5

# Run migrations
echo "🔄 Executando migrations..."
./mvnw flyway:migrate

# Start Spring Boot application
echo "🎯 Iniciando aplicação Spring Boot..."
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

