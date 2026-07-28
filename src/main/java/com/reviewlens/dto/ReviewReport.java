package com.reviewlens.dto;

public record ReviewReport(
        Long reviewId,
        String repositoryUrl,
        String status,
        Summary summary) {
    public record Summary(
            long totalFindings,
            long critical,
            long high,
            long medium,
            long low,
            long info) {
    }
}