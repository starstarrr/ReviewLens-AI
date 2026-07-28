package com.reviewlens.dto;

import java.util.List;

public record AiReviewReport(
        Long reviewId,
        String repositoryUrl,
        String summary,
        List<String> strengths,
        List<String> risks,
        List<String> recommendations) {
}