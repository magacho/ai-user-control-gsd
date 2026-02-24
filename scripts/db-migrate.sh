#!/bin/bash

echo "🔄 Executando migrations do banco de dados..."

# Check if .env file exists
if [ -f .env ]; then
    echo "📄 Carregando variáveis de ambiente de .env"
    export $(grep -v '^#' .env | xargs)
fi

# Run Flyway migrations
./mvnw flyway:migrate

echo "✅ Migrations executadas com sucesso!"

