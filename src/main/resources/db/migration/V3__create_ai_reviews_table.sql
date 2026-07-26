CREATE TABLE ai_reviews (
    id BIGSERIAL PRIMARY KEY,

    review_id BIGINT NOT NULL UNIQUE,

    summary TEXT NOT NULL,

    strengths TEXT NOT NULL,

    risks TEXT NOT NULL,

    recommendations TEXT NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_ai_reviews_review
        FOREIGN KEY (review_id)
        REFERENCES reviews(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_ai_reviews_review_id
    ON ai_reviews(review_id);