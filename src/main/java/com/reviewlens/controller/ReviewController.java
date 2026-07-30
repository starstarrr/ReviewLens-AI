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
import com.reviewlens.service.S3Service;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
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
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    public ReviewController(
            ReviewService reviewService,
            FindingRepository findingRepository,
            AiReviewRepository aiReviewRepository,
            S3Service s3Service,
            ObjectMapper objectMapper) {

        this.reviewService = reviewService;
        this.findingRepository = findingRepository;
        this.aiReviewRepository = aiReviewRepository;
        this.s3Service = s3Service;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new repository review for the authenticated GitHub user.
     *
     * @param request          repository review request
     * @param authorizedClient authenticated GitHub OAuth client
     * @return the created review
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review createReview(
            @Valid @RequestBody CreateReviewRequest request,

            @RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient authorizedClient) {

        return reviewService.createReview(
                request,
                authorizedClient);
    }

    /**
     * Returns a review by its identifier.
     *
     * @param id review identifier
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
     * @param id review identifier
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
     * @param id review identifier
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
     * Returns the AI-generated review stored in the database.
     *
     * @param id review identifier
     * @return AI review data
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
                readJsonList(
                        aiReview.getStrengths()),
                readJsonList(
                        aiReview.getRisks()),
                readJsonList(
                        aiReview.getRecommendations()),
                aiReview.getCreatedAt());
    }

    /**
     * Returns the complete AI report stored in S3.
     *
     * @param id review identifier
     * @return report JSON
     */
    @GetMapping(value = "/{id}/report", produces = "application/json")
    public String getReport(
            @PathVariable Long id) {

        reviewService.getReview(id);

        AiReview aiReview = aiReviewRepository
                .findByReviewId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "AI review not found for review: " + id));

        String s3ObjectKey = aiReview.getS3ObjectKey();

        if (s3ObjectKey == null
                || s3ObjectKey.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "S3 report not found for review: " + id);
        }

        return s3Service.downloadJson(
                s3ObjectKey);
    }

    /**
     * Converts a JSON array stored in the database into a list of strings.
     *
     * @param json JSON array
     * @return deserialized values
     */
    private List<String> readJsonList(
            String json) {

        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<String>>() {
                    });
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to deserialize AI review data",
                    exception);
        }
    }
}