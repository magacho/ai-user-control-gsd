-- Usage metrics table (V4 was empty stub)
-- Stores normalized usage metrics from all AI tool providers
CREATE TABLE IF NOT EXISTS usage_metrics (
    id                       BIGSERIAL PRIMARY KEY,
    user_ai_tool_account_id  BIGINT NOT NULL REFERENCES user_ai_tool_accounts(id) ON DELETE CASCADE,
    metric_date              DATE NOT NULL,
    metric_type              VARCHAR(50) NOT NULL,
    value                    NUMERIC(20, 4) NOT NULL DEFAULT 0,
    raw_data                 JSONB,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_metric_type CHECK (metric_type IN ('TOKEN_INPUT', 'TOKEN_OUTPUT', 'REQUEST_COUNT', 'LAST_ACCESS', 'SEAT_ACTIVE'))
);

-- Unique constraint for idempotency: same account + date + type = same metric
CREATE UNIQUE INDEX idx_usage_metrics_idempotent
    ON usage_metrics(user_ai_tool_account_id, metric_date, metric_type);

CREATE INDEX idx_usage_metrics_account_id ON usage_metrics(user_ai_tool_account_id);
CREATE INDEX idx_usage_metrics_date ON usage_metrics(metric_date);
CREATE INDEX idx_usage_metrics_type ON usage_metrics(metric_type);
