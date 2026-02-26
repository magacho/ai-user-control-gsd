-- Add source date columns to user_ai_tool_accounts
-- These store dates from the tool APIs (not DB timestamps)
ALTER TABLE user_ai_tool_accounts ADD COLUMN IF NOT EXISTS created_at_source TIMESTAMP WITH TIME ZONE;
ALTER TABLE user_ai_tool_accounts ADD COLUMN IF NOT EXISTS last_activity_at TIMESTAMP WITH TIME ZONE;
