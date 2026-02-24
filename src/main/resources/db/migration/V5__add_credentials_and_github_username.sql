-- Create user_ai_tool_accounts table (V3 was empty stub)
CREATE TABLE IF NOT EXISTS user_ai_tool_accounts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ai_tool_id      BIGINT NOT NULL REFERENCES ai_tools(id) ON DELETE CASCADE,
    account_identifier VARCHAR(255) NOT NULL,
    account_email    VARCHAR(255),
    status          VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_synced_at  TIMESTAMP WITH TIME ZONE,
    first_seen_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at    TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'REVOKED'))
);

CREATE INDEX idx_uata_user_id ON user_ai_tool_accounts(user_id);
CREATE INDEX idx_uata_ai_tool_id ON user_ai_tool_accounts(ai_tool_id);
CREATE INDEX idx_uata_account_email ON user_ai_tool_accounts(account_email);
CREATE INDEX idx_uata_status ON user_ai_tool_accounts(status);
-- Unique constraint: same account_identifier for same tool means same account
CREATE UNIQUE INDEX idx_uata_tool_account ON user_ai_tool_accounts(ai_tool_id, account_identifier);

-- Add github_username to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS github_username VARCHAR(255);
CREATE INDEX idx_users_github_username ON users(github_username);

-- Add credential columns to ai_tools table
ALTER TABLE ai_tools ADD COLUMN IF NOT EXISTS api_key VARCHAR(1024);
ALTER TABLE ai_tools ADD COLUMN IF NOT EXISTS api_org_id VARCHAR(255);
