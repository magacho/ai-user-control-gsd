#!/bin/bash

echo "🧪 Executando testes..."

# Run unit tests
echo "📝 Executando testes unitários..."
./mvnw test

# Run integration tests
echo "🔗 Executando testes de integração..."
./mvnw verify

echo "✅ Testes concluídos!"

