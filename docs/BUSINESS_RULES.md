# Business Rules — AI User Control

Indice centralizado das regras de negocio da aplicacao.
Cada regra tem um identificador unico para referencia em codigo, testes e discussoes.

---

## Documentos

| Documento | Escopo | Regras |
|-----------|--------|--------|
| [Regras de Login](rules/LOGIN_RULES.md) | Autenticacao, dominio, pre-registro, roles | BR-LOGIN-01 a 06 |
| [Regras de Inatividade](rules/INACTIVITY_RULES.md) | Status de usuario/conta, thresholds, pending accounts | BR-INACT-01 a 11 |
| [Regras de Mapeamento](rules/MAPPING_RULES.md) | Sync, git_name→email, account linking, ferramentas | BR-MAP-01 a 16 |

---

## Convencoes

- **Prefixo**: `BR-LOGIN-`, `BR-INACT-`, `BR-MAP-`
- **Novas regras**: Adicionar no documento adequado com o proximo numero sequencial
- **Changelog**: Cada documento tem seu proprio changelog no final
- **Referencia cruzada**: Usar links relativos entre documentos quando regras se relacionam

---

## Quick Reference — Todas as Regras

### Login e Acesso
| ID | Regra | Tipo |
|----|-------|------|
| BR-LOGIN-01 | Somente `@bemobi.com` via OAuth/OIDC | Hardcoded |
| BR-LOGIN-02 | CHECK constraint de dominio no banco | DB |
| BR-LOGIN-03 | Pre-registro obrigatorio para login | Logica |
| BR-LOGIN-04 | Auto-criacao de admin via `app.admin-emails` | Config |
| BR-LOGIN-05 | Sync de perfil a cada login | Logica |
| BR-LOGIN-06 | Todos recebem `ROLE_ADMIN` | Hardcoded |

### Inatividade e Ciclo de Vida
| ID | Regra | Tipo |
|----|-------|------|
| BR-INACT-01 | Status de usuario: ACTIVE, INACTIVE, OFFBOARDED | Enum |
| BR-INACT-02 | Arquivamento automatico de legacy sem seats | Logica |
| BR-INACT-03 | OFFBOARDED protegido contra reativacao | Logica |
| BR-INACT-04 | Status de conta: ACTIVE, SUSPENDED, REVOKED | Enum |
| BR-INACT-05 | Maquina de estados: ACTIVE→SUSPENDED→REVOKED | Logica |
| BR-INACT-06 | Reaparecimento: volta para ACTIVE | Logica |
| BR-INACT-07 | Threshold UI: 60 dias | Hardcoded |
| BR-INACT-08 | Threshold scheduler: 30 dias | Config |
| BR-INACT-09 | Pending: SUSPENDED/REVOKED = "para remover" | Logica |
| BR-INACT-10 | Pending: user NULL = "conta externa" | Logica |
| BR-INACT-11 | Datas write-once vs always-update | Logica |

### Mapeamento e Sync
| ID | Regra | Tipo |
|----|-------|------|
| BR-MAP-01 | Fluxo completo de sync (6 passos) | Logica |
| BR-MAP-02 | Ferramenta precisa enabled + apiKey | Config |
| BR-MAP-03 | Normalizacao de email (lowercase, trim) | Logica |
| BR-MAP-04 | git_name → GWS custom schema lookup | Integracao |
| BR-MAP-05 | Fallback: git_login@bemobi.com no banco | Logica |
| BR-MAP-06 | Sem match = conta externa | Logica |
| BR-MAP-07 | Validacao GWS obrigatoria para criar usuario | Integracao |
| BR-MAP-08 | Fonte de validacao: GWS_LEGACY vs AI_SEAT | Logica |
| BR-MAP-09 | Enrichment GitHub antes do linking geral | Logica |
| BR-MAP-10 | Unicidade: UNIQUE(tool, identifier) | DB |
| BR-MAP-11 | Re-linking quando email fica disponivel | Logica |
| BR-MAP-12 | Tipos: CLAUDE, GITHUB_COPILOT, CURSOR, CUSTOM | Enum |
| BR-MAP-13 | Dados retornados por ferramenta (tabela) | Integracao |
| BR-MAP-14 | Cursor filtra membros removidos | Logica |
| BR-MAP-15 | Nome de ferramenta unico | DB |
| BR-MAP-16 | API key mascarada na UI | Logica |

---

## Changelog

| Data       | Alteracao                          |
|------------|------------------------------------|
| 2026-02-27 | Documento criado e separado em 3 arquivos |
