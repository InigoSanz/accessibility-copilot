ALTER TABLE accessibility_issues
    ADD COLUMN selector VARCHAR(500),
    ADD COLUMN recommendation TEXT,
    ADD COLUMN help_url TEXT;

