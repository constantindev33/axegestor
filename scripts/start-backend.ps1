$ErrorActionPreference = "Stop"

Set-Location "C:\Users\Usuario\Desktop\repositorios\axegestor"

$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "123456"
$env:DB_URL = "jdbc:postgresql://localhost:5432/axegestor"

Write-Host "Iniciando backend AxéGestor em http://localhost:8080"
Write-Host "Swagger: http://localhost:8080/swagger-ui.html"
Write-Host ""

.\mvnw.cmd spring-boot:run
