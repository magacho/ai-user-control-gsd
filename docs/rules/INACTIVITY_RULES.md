# Regras de Inatividade e Ciclo de Vida

Regras que controlam como usuarios e contas sao classificados como inativos,
suspensos ou revogados, e como aparecem na UI de pending accounts.

---

## Status de Usuario

### BR-INACT-01: Status possiveis de usuario

- **Valores**: `ACTIVE`, `INACTIVE`, `OFFBOARDED`
- **Onde**: `UserStatus` enum, constraint no banco `('ACTIVE', 'INACTIVE', 'OFFBOARDED')`

### BR-INACT-02: Arquivamento automatico de usuarios legacy

- **Regra**: Durante o sync, usuarios com `validationSource = "GWS_LEGACY"` que NAO possuem nenhuma conta de ferramenta AI sao automaticamente marcados como `INACTIVE`.
- **Condicao**: `user.status == ACTIVE && user.validationSource == "GWS_LEGACY" && aiToolAccounts.isEmpty()`
- **Nota**: Somente afeta usuarios legacy. Usuarios criados pelo sync (`AI_SEAT_GWS_VALIDATED`) NAO sao arquivados por esta regra.
- **Onde**: `SyncOrchestrator` (linhas 365-380)

### BR-INACT-03: Protecao de offboarded

- **Regra**: Usuarios com status `OFFBOARDED` nao tem seu status alterado para `ACTIVE` durante o sync, mesmo que tenham seats AI validos.
- **Motivo**: Offboarding e uma decisao administrativa manual que o sync nao deve reverter.
- **Onde**: `SyncOrchestrator` (criacao/atualizacao de usuarios)

---

## Status de Conta (AI Tool Account)

### BR-INACT-04: Status possiveis de conta

- **Valores**: `ACTIVE`, `SUSPENDED`, `REVOKED`
- **Onde**: `AccountStatus` enum, constraint no banco

### BR-INACT-05: Maquina de estados — desaparecimento

- **Regra**: Quando uma conta NAO aparece no fetch da ferramenta:
  - 1o sync sem a conta: `ACTIVE → SUSPENDED`
  - 2o sync sem a conta: `SUSPENDED → REVOKED`
  - 3o+ sync sem a conta: permanece `REVOKED`
- **Onde**: `AccountLinkingService` (linhas 117-138)

```
                   aparece no fetch
    ┌──────────────────────────────────────────┐
    │                                          │
    v                                          │
 ACTIVE ──não aparece──> SUSPENDED ──não aparece──> REVOKED
    ^                       │                       │
    │        reaparece      │        reaparece      │
    └───────────────────────┘                       │
    └───────────────────────────────────────────────┘
```

### BR-INACT-06: Reaparecimento

- **Regra**: Se uma conta reaparece no fetch, retorna para `ACTIVE` e atualiza `lastSeenAt`.
- **Onde**: `AccountLinkingService` (linhas 53-79)

---

## Thresholds de Inatividade

### BR-INACT-07: Threshold de inatividade na UI (60 dias)

- **Regra**: Na pagina de pending accounts, contas com `lastActivityAt` anterior a 60 dias sao destacadas visualmente como inativas.
- **Constante**: `INACTIVITY_THRESHOLD_DAYS = 60`
- **Tipo**: Hardcoded no controller.
- **Onde**: `PendingAccountsController` (linha 34)

### BR-INACT-08: Threshold de inatividade no scheduler (30 dias)

- **Regra**: O scheduler de verificacao de inativos usa threshold configuravel.
- **Config**: `${INACTIVE_DAYS_THRESHOLD:30}` (default: 30 dias)
- **Cron**: `0 0 3 * * ?` (diariamente as 3h)
- **Onde**: `application.yml`

> **Atencao**: Os thresholds BR-INACT-07 (60 dias, UI) e BR-INACT-08 (30 dias, scheduler) sao diferentes. Avaliar se devem ser unificados ou se a diferenca e intencional.

---

## Pending Accounts

### BR-INACT-09: Contas para remover

- **Regra**: Contas com status `SUSPENDED` ou `REVOKED` sao listadas na secao "contas para remover".
- **Acao sugerida**: Remover a conta da plataforma da ferramenta AI.
- **Onde**: `PendingAccountsController`, query `findAccountsToRemove()`

### BR-INACT-10: Contas externas

- **Regra**: Contas sem usuario associado (`user IS NULL`) sao listadas na secao "contas externas".
- **Motivo**: Nenhum match GWS encontrado para o email/identifier (ver [Regras de Mapeamento](MAPPING_RULES.md)).
- **Acao sugerida**: Verificar email ou remover conta.
- **Onde**: `PendingAccountsController`, query `findExternalAccounts()`

---

## Datas de Rastreamento

### BR-INACT-11: Datas de origem (write-once vs always-update)

- **Regra**:
  - `createdAtSource`: Gravado apenas uma vez (write-once). Se ja existe, nao sobrescreve.
  - `lastActivityAt`: Sempre sobrescrito com valor nao-nulo da fonte.
  - `firstSeenAt`: Quando detectado pela primeira vez no sync (write-once).
  - `lastSeenAt`: Atualizado a cada sync que encontra a conta.
- **Onde**: `AccountLinkingService` (linhas 53-79)

---

## Resumo Visual

```
Conta aparece no sync
    │
    ├─ Conta ja existe?
    │   ├─ SIM → status = ACTIVE, atualiza lastSeenAt, lastActivityAt
    │   └─ NAO → cria com ACTIVE, firstSeenAt = now
    │
Conta NAO aparece no sync
    │
    ├─ Status atual ACTIVE?     → SUSPENDED  (BR-INACT-05)
    ├─ Status atual SUSPENDED?  → REVOKED    (BR-INACT-05)
    └─ Status atual REVOKED?    → permanece  (BR-INACT-05)

UI Pending Accounts
    │
    ├─ SUSPENDED ou REVOKED?  → "Contas para remover"  (BR-INACT-09)
    ├─ user IS NULL?          → "Contas externas"      (BR-INACT-10)
    └─ lastActivityAt > 60d?  → Destacada como inativa (BR-INACT-07)
```

---

## Changelog

| Data       | Regra        | Alteracao                          |
|------------|--------------|------------------------------------|
| 2026-02-27 | —            | Documento criado com regras existentes |
