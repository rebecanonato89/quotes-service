# Script para rodar o projeto no Windows
Write-Host "🚀 Iniciando Quotes Service..." -ForegroundColor Green

# Compilar
Write-Host "📦 Compilando projeto..." -ForegroundColor Yellow
mvn clean install -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilação concluída!" -ForegroundColor Green

    # Rodar
    Write-Host "🏃 Iniciando aplicação..." -ForegroundColor Yellow
    mvn spring-boot:run
} else {
    Write-Host "❌ Erro na compilação!" -ForegroundColor Red
    exit 1
}

