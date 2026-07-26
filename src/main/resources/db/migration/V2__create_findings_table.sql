CREATE TABLE findings (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL,
    rule_id VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    line_number INTEGER,
    message VARCHAR(1000) NOT NULL,
    suggestion VARCHAR(2000),

    CONSTRAINT fk_findings_review
        FOREIGN KEY (review_id)
        REFERENCES reviews(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_findings_review_id
    ON findings(review_id);