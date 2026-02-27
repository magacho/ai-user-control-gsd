-- Standardize display name: "Github Copilot" / "GitHub Copilot" → "Copilot"
UPDATE ai_tools SET name = 'Copilot', updated_at = CURRENT_TIMESTAMP
WHERE tool_type = 'GITHUB_COPILOT' AND LOWER(name) = LOWER('Github Copilot');
