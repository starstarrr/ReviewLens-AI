package com.reviewlens.controller;

import com.reviewlens.dto.AiReviewResponse;
import com.reviewlens.dto.CreateReviewRequest;
import com.reviewlens.dto.FindingResponse;
import com.reviewlens.dto.ReviewSummaryResponse;
import com.reviewlens.entity.AiReview;
import com.reviewlens.entity.FindingSeverity;
import com.reviewlens.entity.Review;
import com.reviewlens.repository.AiReviewRepository;
import com.reviewlens.repository.FindingRepository;
import com.reviewlens.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final FindingRepository findingRepository;
    private final AiReviewRepository aiReviewRepository;
    private final ObjectMapper objectMapper;

    public ReviewController(
            ReviewService reviewService,
            FindingRepository findingRepository,
            AiReviewRepository aiReviewRepository,
            ObjectMapper objectMapper) {
        this.reviewService = reviewService;
        this.findingRepository = findingRepository;
        this.aiReviewRepository = aiReviewRepository;
        this.objectMapper = objectMapper;
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
                .filter(finding -> finding.getSeverity() == FindingSeverity.CRITICAL)
                .count();

        long high = findings.stream()
                .filter(finding -> finding.getSeverity() == FindingSeverity.HIGH)
                .count();

        long medium = findings.stream()
                .filter(finding -> finding.getSeverity() == FindingSeverity.MEDIUM)
                .count();

        long low = findings.stream()
                .filter(finding -> finding.getSeverity() == FindingSeverity.LOW)
                .count();

        long info = findings.stream()
                .filter(finding -> finding.getSeverity() == FindingSeverity.INFO)
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

    /**
     * Returns the AI-generated report for a repository review.
     *
     * @param id the repository review identifier
     * @return the AI-generated review report
     */
    @GetMapping("/{id}/ai-review")
    public AiReviewResponse getAiReview(
            @PathVariable Long id) {
        reviewService.getReview(id);

        AiReview aiReview = aiReviewRepository
                .findByReviewId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "AI review not found for review: " + id));

        return new AiReviewResponse(
                aiReview.getReview().getId(),
                aiReview.getSummary(),
                readJsonList(aiReview.getStrengths()),
                readJsonList(aiReview.getRisks()),
                readJsonList(aiReview.getRecommendations()),
                aiReview.getCreatedAt());
    }

    /**
     * Converts a JSON array stored in the database into a list of strings.
     *
     * @param json JSON array text
     * @return deserialized list
     */
    private List<String> readJsonList(String json) {
        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<String>>() {
                    });
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to deserialize AI review data",
                    exception);
        }
    }
}