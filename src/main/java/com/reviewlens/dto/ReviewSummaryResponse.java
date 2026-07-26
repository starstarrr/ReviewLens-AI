package com.reviewlens.dto;

import com.reviewlens.entity.ReviewStatus;

public record ReviewSummaryResponse(
        Long reviewId,
        String repositoryUrl,
        ReviewStatus status,
        long totalFindings,
        long critical,
        long high,
        long medium,
        long low,
        long info) {
}