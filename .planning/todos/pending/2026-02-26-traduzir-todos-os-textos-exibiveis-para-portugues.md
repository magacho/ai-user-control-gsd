---
created: 2026-02-26T17:13:30.915Z
title: Traduzir todos os textos exibíveis para português
area: ui
files:
  - src/main/resources/templates/fragments/sidebar.html
  - src/main/resources/templates/pending-accounts/list.html
  - src/main/resources/templates/pending-accounts/fragments/table.html
  - src/main/resources/templates/fragments/sync-result.html
  - src/main/java/com/bemobi/aiusercontrol/web/PendingAccountsController.java
  - src/main/java/com/bemobi/aiusercontrol/web/SyncController.java
---

## Problem

Textos gerados por agentes de IA durante execução de fases frequentemente não incluem acentos e caracteres especiais do português (ç, ã, á, é, ê, ú, õ). Isso foi detectado na UAT da Phase 02.1 (Test 3) e corrigido pontualmente no commit 9ede7ef, mas o problema pode ocorrer novamente em fases futuras.

Além dos acentos, alguns textos da UI ainda estão em inglês (ex: "Users", "Dashboard", "AI Tools", headers de tabelas) e deveriam estar em português para consistência, já que o público-alvo é brasileiro.

## Solution

1. Varrer todos os templates Thymeleaf e controllers Java buscando textos user-facing
2. Garantir acentuação correta em todos os textos em português
3. Traduzir textos ainda em inglês para português (sidebar, headers, labels, mensagens)
4. Considerar criar uma regra no CLAUDE.md instruindo agentes a usar caracteres Unicode corretos em textos PT-BR
