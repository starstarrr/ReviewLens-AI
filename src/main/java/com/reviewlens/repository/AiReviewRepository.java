package com.reviewlens.repository;

import com.reviewlens.entity.AiReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiReviewRepository
        extends JpaRepository<AiReview, Long> {

    /**
     * Finds the AI-generated review associated with a repository review.
     *
     * @param reviewId the repository review identifier
     * @return the AI review, if one exists
     */
    Optional<AiReview> findByReviewId(Long reviewId);
}