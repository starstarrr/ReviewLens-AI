package com.reviewlens.repository;

import com.reviewlens.entity.Finding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FindingRepository extends JpaRepository<Finding, Long> {

    /**
     * Retrieves all findings belonging to a review.
     *
     * Results are ordered by file path and line number so that
     * API consumers receive findings in a predictable order.
     *
     * @param reviewId the review identifier
     * @return findings associated with the review
     */
    List<Finding> findByReviewIdOrderByFilePathAscLineNumberAsc(
            Long reviewId);
}