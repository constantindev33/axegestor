# Piloto de atendimento de quarta-feira

Este roteiro prepara o AxéGestor para ser usado em modo piloto durante uma quarta-feira de atendimento.

## Antes do atendimento

1. Ligue o PostgreSQL.
2. Abra o terminal na pasta do projeto.
3. Configure as variáveis do banco:

```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="sua-senha-do-postgres"
$env:DB_URL="jdbc:postgresql://localhost:5432/axegestor"
```

4. Faça backup antes de começar:

```powershell
.\scripts\backup-postgres.ps1
```

5. Inicie backend e frontend:

```powershell
.\scripts\start-backend.ps1
```

Em outro terminal:

```powershell
.\scripts\start-frontend.ps1
```

## Durante o atendimento

Use o menu **Atendimento**.

Fluxo sugerido:

1. Cadastre a pessoa no bloco **Cadastro rápido**.
2. Preencha nome e WhatsApp sempre que possível.
3. Marque tratamentos apenas quando já houver indicação.
4. Use a busca por nome ou WhatsApp para evitar duplicidade no dia.
5. Ao concluir, clique em **Finalizar**.
6. No fim da noite, clique em **Exportar relatório do dia**.

## Rotina de estoque e almoxarifado

Use o menu **Estoque** durante a semana.

Fluxo sugerido para substituir a planilha aos poucos:

1. Cadastre cada material com nome, categoria, quantidade atual, quantidade mínima, unidade e local.
2. Quando comprar ou receber material, registre uma movimentação de **Entrada**.
3. Quando usar material no atendimento ou em trabalho da casa, registre uma movimentação de **Saída**.
4. Sempre preencha o responsável pela movimentação.
5. Use o filtro **Estoque baixo** para saber o que precisa comprar.
6. Clique em **Exportar CSV** para gerar uma planilha do estoque e comparar com o controle antigo.

Na primeira semana, mantenha o Google Sheets junto com o AxéGestor. Depois de conferir se as quantidades estão batendo, o AxéGestor pode virar o controle principal do almoxarifado.

## Depois do atendimento

1. Exporte o relatório do dia.
2. Faça backup novamente:

```powershell
.\scripts\backup-postgres.ps1
```

3. Guarde o arquivo da pasta `backups` em um local seguro.

## Importante

Nas primeiras quartas, mantenha uma lista simples em papel ou planilha como redundância. Depois de 2 ou 3 atendimentos sem problemas, o sistema pode virar o controle principal da recepção.
