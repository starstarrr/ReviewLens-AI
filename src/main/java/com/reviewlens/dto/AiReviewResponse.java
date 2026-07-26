package com.reviewlens.dto;

import java.time.Instant;
import java.util.List;

/**
 * Represents the AI review returned by the REST API.
 *
 * @param reviewId        associated repository review identifier
 * @param summary         overall assessment of the repository
 * @param strengths       positive aspects identified in the repository
 * @param risks           maintainability, reliability, or security concerns
 * @param recommendations suggested improvements
 * @param createdAt       time when the AI review was generated
 */
public record AiReviewResponse(
        Long reviewId,
        String summary,
        List<String> strengths,
        List<String> risks,
        List<String> recommendations,
        Instant createdAt) {
}