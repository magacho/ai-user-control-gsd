-- Allow user_id to be NULL for unmatched tool accounts (accounts that don't match any corporate user)
-- Required for identity resolution: accounts with non-@bemobi.com emails are tracked with user=null
ALTER TABLE user_ai_tool_accounts ALTER COLUMN user_id DROP NOT NULL;
