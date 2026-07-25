package com.reviewlens.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repository_url", nullable = false, length = 500)
    private String repositoryUrl;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Review() {
    }

    public Review(String repositoryUrl, String status) {
        this.repositoryUrl = repositoryUrl;
        this.status = status;
        this.createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}