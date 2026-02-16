# Script para rodar testes
Write-Host "🧪 Executando testes..." -ForegroundColor Cyan

mvn test

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Todos os testes passaram!" -ForegroundColor Green
} else {
    Write-Host "❌ Alguns testes falharam!" -ForegroundColor Red
    exit 1
}

