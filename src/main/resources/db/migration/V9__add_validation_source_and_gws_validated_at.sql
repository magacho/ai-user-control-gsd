-- Track how users were discovered and when GWS last validated them
ALTER TABLE users ADD COLUMN IF NOT EXISTS validation_source VARCHAR(50) DEFAULT 'GWS_LEGACY';
ALTER TABLE users ADD COLUMN IF NOT EXISTS gws_validated_at TIMESTAMP WITH TIME ZONE;
