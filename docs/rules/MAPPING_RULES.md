# Regras de Mapeamento e Sync

Regras que controlam como o sistema descobre usuarios nas ferramentas AI,
mapeia identidades (git_name → email), vincula contas e sincroniza dados.

---

## Fluxo de Sync

### BR-MAP-01: Fluxo completo de sincronizacao

- **Passos**:
  1. Fetch de seats de todas as ferramentas habilitadas (em paralelo)
  2. Extracao de emails unicos (deduplicados, normalizados)
  3. Validacao/enrichment dos emails contra Google Workspace
  4. Criacao/atualizacao de usuarios para emails validados
  5. Linking de seats a usuarios (inclui enrichment GitHub)
  6. Arquivamento de usuarios legacy sem seats AI (ver [Regras de Inatividade](INACTIVITY_RULES.md#br-inact-02))
- **Onde**: `SyncOrchestrator` (linhas 67-92)

### BR-MAP-02: Ferramenta habilitada para sync

- **Regra**: Uma ferramenta so e incluida no sync se `enabled = true` E `apiKey` nao e nulo/vazio.
- **Falha**: Log de warning, ferramenta ignorada silenciosamente.
- **Onde**: `SyncOrchestrator`

### BR-MAP-03: Normalizacao de email

- **Regra**: Todos os emails sao convertidos para `lowercase` e `trimmed` antes de qualquer comparacao ou persistencia.
- **Onde**: `AccountLinkingService.findUserByEmail()`, `SyncOrchestrator` (extracao de emails)

---

## Mapeamento git_name → Email (GitHub Copilot)

### BR-MAP-04: Lookup primario via GWS custom schema

- **Regra**: Para seats do GitHub Copilot (que tem `login` mas nao email), o sistema busca o email no Google Workspace usando o custom schema `GitHub.git_name`.
- **Query**: `GitHub.git_name='<login>'` na Directory API do Google.
- **Config**: Schema name configuravel (default: `"GitHub"`), domain: `${GWS_DOMAIN:bemobi.com}`.
- **Match multiplo**: Se encontrar mais de um usuario GWS com o mesmo git_name, usa o primeiro e loga warning.
- **Onde**: `GoogleWorkspaceService.lookupUserByGitName()` (linhas 82-110)

### BR-MAP-05: Fallback por convencao de email

- **Regra**: Se o lookup GWS (BR-MAP-04) falhar, o sistema tenta construir um email candidato: `<git_login>@bemobi.com` e busca no banco de dados.
- **Logica**: `seat.getIdentifier() + "@" + domain` → `userRepository.findByEmail(candidateEmail.toLowerCase())`
- **Onde**: `SyncOrchestrator.enrichGitHubSeatsWithEmails()` (linhas 308-316)

### BR-MAP-06: Seat sem match permanece sem email

- **Regra**: Se ambos os lookups (BR-MAP-04 e BR-MAP-05) falharem, o seat permanece sem email e aparece como "conta externa" na UI de pending accounts.
- **Onde**: `SyncOrchestrator.enrichGitHubSeatsWithEmails()`

```
GitHub Copilot Seat (login: "joao.silva")
    │
    ├─ GWS lookup: GitHub.git_name = 'joao.silva'  (BR-MAP-04)
    │   ├─ Encontrou? → Usa email do GWS
    │   └─ Não encontrou? ↓
    │
    ├─ Fallback: joao.silva@bemobi.com no banco  (BR-MAP-05)
    │   ├─ Encontrou? → Usa esse email
    │   └─ Não encontrou? ↓
    │
    └─ Sem match → conta externa na UI  (BR-MAP-06)
```

---

## Validacao e Criacao de Usuarios

### BR-MAP-07: Validacao GWS de emails

- **Regra**: Emails extraidos dos seats sao validados contra o Google Workspace. Somente emails validados resultam em criacao/atualizacao de usuario.
- **Resultado**: `validationSource = "AI_SEAT_GWS_VALIDATED"`, `gwsValidatedAt = now()`
- **Onde**: `SyncOrchestrator` (linhas 176-213)

### BR-MAP-08: Fonte de validacao do usuario

- **Valores**:
  - `"GWS_LEGACY"` — default para retrocompatibilidade (usuarios pre-existentes)
  - `"AI_SEAT_GWS_VALIDATED"` — criado pelo sync + validacao GWS
- **Campo**: `validationSource` na entidade User.
- **Onde**: `SyncOrchestrator`, `User` entity

### BR-MAP-09: Enrichment especifico do GitHub Copilot

- **Regra**: Seats do GitHub Copilot passam por enrichment de email (BR-MAP-04, BR-MAP-05) ANTES do linking geral.
- **Adicional**: Apos enrichment, `ensureUsersExistForEnrichedSeats()` cria usuarios para seats que agora tem email mas cujo usuario ainda nao existe.
- **Onde**: `SyncOrchestrator` (linhas 242-248)

---

## Account Linking

### BR-MAP-10: Unicidade de conta

- **Regra**: Constraint `UNIQUE(ai_tool_id, account_identifier)` — mesma conta so pode existir uma vez por ferramenta.
- **Onde**: `V5__add_credentials_and_github_username.sql`

### BR-MAP-11: Re-linking de conta

- **Regra**: Se uma conta existente tinha `user = null` (sem match) e agora tem email disponivel, o sistema tenta re-linkar a conta a um usuario.
- **Cenario**: Seat do GitHub Copilot que antes nao tinha match, mas agora o git_name foi adicionado ao GWS.
- **Onde**: `AccountLinkingService` (linhas 53-79)

---

## Ferramentas AI — Especificidades

### BR-MAP-12: Tipos de ferramenta

- **Valores**: `CLAUDE`, `GITHUB_COPILOT`, `CURSOR`, `CUSTOM`
- **Constraint**: CHECK no banco.
- **Onde**: `V2__create_ai_tools_table.sql`

### BR-MAP-13: Dados retornados por ferramenta

| Ferramenta       | Identificador      | Email direto? | Paginacao                    |
|------------------|---------------------|---------------|------------------------------|
| **Claude**       | email               | Sim           | Cursor-based (`after_id`)    |
| **Cursor**       | email               | Sim           | Sem paginacao (TODO >100)    |
| **GitHub Copilot** | login (git_name)  | Nao           | Page-based (`per_page=100`)  |

### BR-MAP-14: Filtro de membros removidos (Cursor)

- **Regra**: Membros com `isRemoved = true` sao ignorados no fetch do Cursor.
- **Onde**: `CursorApiClient`

### BR-MAP-15: Nome unico de ferramenta

- **Regra**: O nome de uma ferramenta AI deve ser unico. Duplicata gera `IllegalArgumentException`.
- **Onde**: `AIToolService` (linhas 38-40, 61-64)

### BR-MAP-16: Mascaramento de API key

- **Regra**: API keys sao exibidas mascaradas: `****` + ultimos 4 caracteres. Se key <= 4 chars: apenas `****`.
- **Onde**: `AIToolService` (linhas 113-121)

---

## Resumo Visual — Fluxo Completo

```
Sync Trigger (manual ou scheduler)
    │
    ├─ 1. Fetch seats (paralelo)
    │   ├─ Claude API    → [email, id]
    │   ├─ Cursor API    → [email, id] (filtra removed)
    │   └─ GitHub API    → [login, id] (sem email!)
    │
    ├─ 2. Enrichment GitHub Copilot  (BR-MAP-04 → BR-MAP-05 → BR-MAP-06)
    │   └─ git_name → GWS lookup → fallback email → ou sem match
    │
    ├─ 3. Extrair emails unicos + normalizar  (BR-MAP-03)
    │
    ├─ 4. Validar emails contra GWS  (BR-MAP-07)
    │   └─ Somente validados criam/atualizam usuarios
    │
    ├─ 5. Linking: seats ↔ usuarios  (BR-MAP-10, BR-MAP-11)
    │   └─ Re-link contas sem match anterior
    │
    └─ 6. Arquivar legacy sem seats  (ver INACTIVITY_RULES.md)
```

---

## Changelog

| Data       | Regra        | Alteracao                          |
|------------|--------------|------------------------------------|
| 2026-02-27 | —            | Documento criado com regras existentes |
