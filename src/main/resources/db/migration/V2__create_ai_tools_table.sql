CREATE TABLE ai_tools (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL UNIQUE,
    tool_type       VARCHAR(50) NOT NULL,
    description     VARCHAR(1024),
    api_base_url    VARCHAR(1024),
    enabled         BOOLEAN NOT NULL DEFAULT true,
    icon_url        VARCHAR(1024),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_tool_type CHECK (tool_type IN ('CLAUDE', 'GITHUB_COPILOT', 'CURSOR', 'CUSTOM'))
);
