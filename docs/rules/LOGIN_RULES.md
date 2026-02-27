# Regras de Login e Acesso

Regras que controlam quem pode acessar o sistema e como o login funciona.

---

## BR-LOGIN-01: Restricao de dominio no login

- **Regra**: Somente emails `@bemobi.com` podem fazer login via OAuth/OIDC.
- **Validacao dupla**:
  1. Email deve terminar com `@bemobi.com`
  2. Claim `hd` do OIDC deve ser `bemobi.com`
- **Falha**: `OAuth2AuthenticationException` com codigo `invalid_domain`.
- **Onde**: `CustomOidcUserService` (linhas 103-120)
- **Tipo**: Hardcoded (`BEMOBI_DOMAIN = "bemobi.com"`)

## BR-LOGIN-02: Constraint de dominio no banco

- **Regra**: Coluna `email` da tabela `users` deve satisfazer `email LIKE '%@bemobi.com'`.
- **Tipo**: CHECK constraint no PostgreSQL — impede insercao de emails de outros dominios mesmo via SQL direto.
- **Onde**: `V1__create_users_table.sql` (linha 12)

## BR-LOGIN-03: Pre-registro obrigatorio

- **Regra**: Usuario deve existir na tabela `users` OU estar na lista `app.admin-emails` para fazer login.
- **Quem cria usuarios**: O processo de sync (ver [Regras de Mapeamento](MAPPING_RULES.md)) cria usuarios automaticamente quando encontra seats AI validos com email `@bemobi.com`.
- **Falha**: `OAuth2AuthenticationException` com codigo `user_not_registered`, mensagem "Your account has not been registered. Contact an administrator."
- **Onde**: `CustomOidcUserService` (linhas 51-61)

## BR-LOGIN-04: Auto-criacao de admin

- **Regra**: Se email esta em `app.admin-emails` mas nao existe no banco, o usuario e criado automaticamente com status `ACTIVE`.
- **Dados**: Nome e avatar extraidos do OIDC UserInfo.
- **Config**: `app.admin-emails` (default: `admin@bemobi.com`)
- **Onde**: `CustomOidcUserService` (linhas 64-73)

## BR-LOGIN-05: Sync de perfil a cada login

- **Regra**: A cada login, atualiza `name`, `avatarUrl` e `lastLoginAt` do usuario.
- **Nota**: Status `INACTIVE` ou `OFFBOARDED` NAO bloqueia login — o usuario continua acessando o sistema.
- **Onde**: `CustomOidcUserService` (linhas 78-84)

## BR-LOGIN-06: Role unica

- **Regra**: Todos os usuarios autenticados recebem `ROLE_ADMIN`. Nao existe diferenciacao de permissoes.
- **Onde**: `CustomOidcUserService`

---

## Resumo Visual

```
Login Request (@bemobi.com)
    │
    ├─ Email termina com @bemobi.com?  ──NO──> Bloqueado (BR-LOGIN-01)
    │   YES
    ├─ Claim hd == bemobi.com?  ──NO──> Bloqueado (BR-LOGIN-01)
    │   YES
    ├─ Email em app.admin-emails?  ──YES──> Auto-cria usuario (BR-LOGIN-04)
    │   NO
    ├─ Email existe na tabela users?  ──NO──> Bloqueado (BR-LOGIN-03)
    │   YES
    └─ Atualiza perfil (BR-LOGIN-05) → Acesso com ROLE_ADMIN (BR-LOGIN-06)
```

---

## Changelog

| Data       | Regra        | Alteracao                          |
|------------|--------------|------------------------------------|
| 2026-02-27 | —            | Documento criado com regras existentes |
