package com.reviewlens.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "ai_reviews")
public class AiReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "s3_object_key")
    private String s3ObjectKey;

    /**
     * The repository review associated with this AI-generated report.
     *
     * Each repository review can have at most one AI review.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false, unique = true)
    private Review review;

    /**
     * Overall AI-generated assessment of the repository.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    /**
     * Positive aspects identified in the repository.
     *
     * Stored as JSON text for now. This can later be migrated
     * to PostgreSQL JSONB if needed.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String strengths;

    /**
     * Important maintainability, reliability, or security concerns.
     *
     * Stored as JSON text.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String risks;

    /**
     * AI-generated improvement recommendations.
     *
     * Stored as JSON text.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendations;

    /**
     * Time when the AI review was generated.
     */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected AiReview() {
    }

    public AiReview(
            Review review,
            String summary,
            String strengths,
            String risks,
            String recommendations) {
        this.review = review;
        this.summary = summary;
        this.strengths = strengths;
        this.risks = risks;
        this.recommendations = recommendations;
    }

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getRisks() {
        return risks;
    }

    public void setRisks(String risks) {
        this.risks = risks;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getS3ObjectKey() {
        return s3ObjectKey;
    }

    public void setS3ObjectKey(String s3ObjectKey) {
        this.s3ObjectKey = s3ObjectKey;
    }

}