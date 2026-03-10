CREATE TABLE accessibility_issues (
    id BIGSERIAL PRIMARY KEY,
    scan_id BIGINT NOT NULL,
    rule_code VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(100) NOT NULL,
    wcag_criterion VARCHAR(100),
    page_url TEXT,
    html_snippet TEXT,
    CONSTRAINT fk_accessibility_issues_scan FOREIGN KEY (scan_id) REFERENCES scans(id)
);

