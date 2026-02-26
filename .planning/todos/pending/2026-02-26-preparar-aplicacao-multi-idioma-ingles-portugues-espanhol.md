---
created: 2026-02-26T17:15:00.000Z
title: Preparar aplicação para ser multi-idioma (PT/EN/ES)
area: ui
files:
  - src/main/resources/templates/
  - src/main/java/com/bemobi/aiusercontrol/web/
  - src/main/java/com/bemobi/aiusercontrol/config/
---

## Problem

A aplicação atualmente tem textos hardcoded em português (e alguns em inglês) nos templates Thymeleaf e controllers Java. Para suportar equipes em diferentes países (Brasil, outros escritórios), a UI precisa ser multi-idioma com suporte a Português, Inglês e Espanhol, escolhendo o idioma baseado no usuário logado ou preferência do browser.

## Solution

1. Implementar Spring MessageSource com arquivos `messages.properties` (EN default), `messages_pt_BR.properties`, `messages_es.properties`
2. Configurar LocaleResolver (CookieLocaleResolver ou SessionLocaleResolver) com LocaleChangeInterceptor
3. Substituir todos os textos hardcoded nos templates por `th:text="#{chave.mensagem}"`
4. Extrair textos de controllers Java para message bundles
5. Adicionar seletor de idioma na UI (dropdown no header ou sidebar)
6. Considerar persistir preferência de idioma no perfil do usuário (campo locale na tabela users)
