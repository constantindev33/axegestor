$ErrorActionPreference = "Stop"

Set-Location "C:\Users\Usuario\Desktop\repositorios\axegestor"

if (-not $env:DB_USERNAME) { $env:DB_USERNAME = "postgres" }
if (-not $env:DB_URL) { $env:DB_URL = "jdbc:postgresql://localhost:5432/axegestor" }
if (-not $env:JWT_SECRET) { $env:JWT_SECRET = "configure-um-segredo-local-com-pelo-menos-32-caracteres" }

Write-Host "Iniciando backend AxéGestor em http://localhost:8080"
Write-Host "Swagger: http://localhost:8080/swagger-ui.html"
Write-Host ""

.\mvnw.cmd spring-boot:run
