package com.reviewlens.controller;

import com.reviewlens.dto.CreateReviewRequest;
import com.reviewlens.dto.FindingResponse;
import com.reviewlens.dto.ReviewSummaryResponse;
import com.reviewlens.entity.FindingSeverity;
import com.reviewlens.entity.Review;
import com.reviewlens.repository.FindingRepository;
import com.reviewlens.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final FindingRepository findingRepository;

    public ReviewController(
            ReviewService reviewService,
            FindingRepository findingRepository) {
        this.reviewService = reviewService;
        this.findingRepository = findingRepository;
    }

    /**
     * Creates a new repository review.
     *
     * @param request the repository review request
     * @return the created review
     */
    @PostMapping
    public Review createReview(
            @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.createReview(request);
    }

    /**
     * Returns a review by its identifier.
     *
     * @param id the review identifier
     * @return the requested review
     */
    @GetMapping("/{id}")
    public Review getReview(
            @PathVariable Long id) {
        return reviewService.getReview(id);
    }

    /**
     * Returns all findings generated for a review.
     *
     * @param id the review identifier
     * @return findings associated with the review
     */
    @GetMapping("/{id}/findings")
    public List<FindingResponse> getReviewFindings(
            @PathVariable Long id) {
        reviewService.getReview(id);

        return findingRepository
                .findByReviewIdOrderByFilePathAscLineNumberAsc(id)
                .stream()
                .map(FindingResponse::from)
                .toList();
    }

    /**
     * Returns a summary of the review findings.
     *
     * @param id the review identifier
     * @return summary statistics for the review
     */
    @GetMapping("/{id}/summary")
    public ReviewSummaryResponse getReviewSummary(
            @PathVariable Long id) {
        Review review = reviewService.getReview(id);

        var findings = findingRepository
                .findByReviewIdOrderByFilePathAscLineNumberAsc(id);

        long critical = findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.CRITICAL)
                .count();

        long high = findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.HIGH)
                .count();

        long medium = findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.MEDIUM)
                .count();

        long low = findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.LOW)
                .count();

        long info = findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.INFO)
                .count();

        return new ReviewSummaryResponse(
                review.getId(),
                review.getRepositoryUrl(),
                review.getStatus(),
                findings.size(),
                critical,
                high,
                medium,
                low,
                info);
    }
}