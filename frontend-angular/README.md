# AxeGestor Angular

Frontend Angular do AxeGestor.

## Como rodar

Primeiro suba o backend Spring Boot na raiz do projeto:

```powershell
cd C:\Users\Usuario\Desktop\repositorios\axegestor
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="sua-senha-local-do-postgres"
$env:DB_URL="jdbc:postgresql://localhost:5432/axegestor"
$env:JWT_SECRET="configure-um-segredo-local-com-pelo-menos-32-caracteres"
.\mvnw.cmd spring-boot:run
```

Depois abra outro terminal e rode o Angular:

```powershell
cd C:\Users\Usuario\Desktop\repositorios\axegestor\frontend-angular
npm.cmd start
```

Acesse:

```text
http://localhost:4200
```

Use um usuário criado no seu banco local. Se o banco estiver vazio, configure antes de subir o backend:

```powershell
$env:ADMIN_NAME="Administrador"
$env:ADMIN_EMAIL="admin@axegestor.local"
$env:ADMIN_PASSWORD="troque-esta-senha-local"
```

## Estrutura

```text
src/app/core
```

Serviços compartilhados da aplicação:

```text
api.service.ts
auth.service.ts
auth.guard.ts
models.ts
```

```text
src/app/layout
```

Layout principal com menu lateral.

```text
src/app/pages
```

Telas:

```text
login
dashboard
assistencias
financeiro
estoque
membros
```

## Backend usado

O Angular chama a API Spring Boot em:

```text
http://localhost:8080
```

Principais endpoints:

```text
POST /auth/login
GET /assistencias
GET /financeiro
GET /financeiro/mensalidades/membros
GET /almoxarifado/materiais
GET /membros
```
