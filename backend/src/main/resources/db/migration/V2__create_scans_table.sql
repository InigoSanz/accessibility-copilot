CREATE TABLE scans (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_scans_project FOREIGN KEY (project_id) REFERENCES projects(id)
);

