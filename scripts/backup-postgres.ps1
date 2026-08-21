$ErrorActionPreference = "Stop"

Set-Location "C:\Users\Usuario\Desktop\repositorios\axegestor"

$backupDir = Join-Path (Get-Location) "backups"
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$database = if ($env:POSTGRES_DATABASE) { $env:POSTGRES_DATABASE } else { "axegestor" }
$hostName = if ($env:POSTGRES_HOST) { $env:POSTGRES_HOST } else { "localhost" }
$port = if ($env:POSTGRES_PORT) { $env:POSTGRES_PORT } else { "5432" }
$username = if ($env:DB_USERNAME) { $env:DB_USERNAME } else { "postgres" }
$outputFile = Join-Path $backupDir "axegestor-$timestamp.backup"

if (-not (Test-Path $backupDir)) {
    New-Item -ItemType Directory -Path $backupDir | Out-Null
}

if (-not (Get-Command pg_dump -ErrorAction SilentlyContinue)) {
    throw "pg_dump não foi encontrado no PATH. Abra este script em um terminal onde o PostgreSQL esteja configurado ou adicione a pasta bin do PostgreSQL ao PATH."
}

if (-not $env:DB_PASSWORD) {
    Write-Host "DB_PASSWORD não está configurada. O pg_dump pode pedir a senha do PostgreSQL."
} else {
    $env:PGPASSWORD = $env:DB_PASSWORD
}

Write-Host "Gerando backup do banco '$database' em:"
Write-Host $outputFile

pg_dump `
    --host $hostName `
    --port $port `
    --username $username `
    --format custom `
    --blobs `
    --verbose `
    --file $outputFile `
    $database

Write-Host ""
Write-Host "Backup concluído com sucesso."
Write-Host "Arquivo: $outputFile"
