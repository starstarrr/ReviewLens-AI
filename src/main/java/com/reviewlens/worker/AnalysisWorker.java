package com.reviewlens.worker;

import com.reviewlens.dto.AiReviewResult;
import com.reviewlens.entity.AiReview;
import com.reviewlens.entity.Finding;
import com.reviewlens.entity.Review;
import com.reviewlens.entity.ReviewStatus;
import com.reviewlens.repository.AiReviewRepository;
import com.reviewlens.repository.FindingRepository;
import com.reviewlens.repository.ReviewRepository;
import com.reviewlens.service.RepositoryCloneService;
import com.reviewlens.service.ai.AiCodeReviewService;
import com.reviewlens.service.analysis.AnalysisEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.List;

@Service
public class AnalysisWorker {

    private static final Logger log = LoggerFactory.getLogger(AnalysisWorker.class);

    private final ReviewRepository reviewRepository;
    private final RepositoryCloneService repositoryCloneService;
    private final AnalysisEngine analysisEngine;
    private final FindingRepository findingRepository;
    private final AiCodeReviewService aiCodeReviewService;
    private final AiReviewRepository aiReviewRepository;
    private final ObjectMapper objectMapper;

    public AnalysisWorker(
            ReviewRepository reviewRepository,
            RepositoryCloneService repositoryCloneService,
            AnalysisEngine analysisEngine,
            FindingRepository findingRepository,
            AiCodeReviewService aiCodeReviewService,
            AiReviewRepository aiReviewRepository,
            ObjectMapper objectMapper) {

        this.reviewRepository = reviewRepository;
        this.repositoryCloneService = repositoryCloneService;
        this.analysisEngine = analysisEngine;
        this.findingRepository = findingRepository;
        this.aiCodeReviewService = aiCodeReviewService;
        this.aiReviewRepository = aiReviewRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Processes a repository review asynchronously.
     *
     * Flow:
     * clone repository -> analyze files -> save findings ->
     * generate AI review -> save AI review -> mark completed.
     *
     * @param reviewId identifier of the review to process
     */
    @Async
    public void processReview(Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Review not found: " + reviewId));

        try {
            updateStatus(review, ReviewStatus.CLONING);

            log.info(
                    "Cloning repository for review: {}",
                    reviewId);

            Path repositoryPath = repositoryCloneService.cloneRepository(
                    review.getRepositoryUrl(),
                    reviewId);

            log.info(
                    "Repository cloned to: {}",
                    repositoryPath.toAbsolutePath());

            updateStatus(review, ReviewStatus.ANALYZING);

            log.info(
                    "Analyzing repository for review: {}",
                    reviewId);

            List<Finding> findings = analysisEngine.analyzeRepository(
                    review,
                    repositoryPath);

            findingRepository.saveAll(findings);

            log.info(
                    "Saved {} findings for review: {}",
                    findings.size(),
                    reviewId);

            log.info(
                    "Generating AI review for review: {}",
                    reviewId);

            long aiStartTime = System.currentTimeMillis();

            AiReviewResult aiResult = aiCodeReviewService.generateReview(
                    review,
                    findings);

            long aiDuration = System.currentTimeMillis() - aiStartTime;

            log.info(
                    "AI review generated for review {} in {} ms",
                    reviewId,
                    aiDuration);

            AiReview aiReview = createAiReview(
                    review,
                    aiResult);

            aiReviewRepository.save(aiReview);

            log.info(
                    "Saved AI review for review: {}",
                    reviewId);

            updateStatus(review, ReviewStatus.COMPLETED);

            log.info(
                    "Review completed: {}",
                    reviewId);

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            markReviewFailed(review, reviewId);

            log.error(
                    "Repository clone interrupted for review: {}",
                    reviewId,
                    exception);

        } catch (Exception exception) {

            markReviewFailed(review, reviewId);

            log.error(
                    "Review failed: {}. Exception type: {}. Message: {}",
                    reviewId,
                    exception.getClass().getName(),
                    exception.getMessage(),
                    exception);
        }
    }

    /**
     * Creates a persistent AI review entity from the generated result.
     *
     * @param review associated repository review
     * @param result AI-generated result
     * @return AI review entity
     * @throws JacksonException if list serialization fails
     */
    private AiReview createAiReview(
            Review review,
            AiReviewResult result) throws JacksonException {

        String strengthsJson = objectMapper.writeValueAsString(
                result.strengths());

        String risksJson = objectMapper.writeValueAsString(
                result.risks());

        String recommendationsJson = objectMapper.writeValueAsString(
                result.recommendations());

        return new AiReview(
                review,
                result.summary(),
                strengthsJson,
                risksJson,
                recommendationsJson);
    }

    /**
     * Marks a review as failed while preserving the original exception.
     */
    private void markReviewFailed(
            Review review,
            Long reviewId) {

        try {
            updateStatus(review, ReviewStatus.FAILED);

            log.info(
                    "Review status updated to FAILED: {}",
                    reviewId);

        } catch (Exception statusException) {

            log.error(
                    "Unable to update review {} to FAILED",
                    reviewId,
                    statusException);
        }
    }

    /**
     * Updates and persists the current review status.
     *
     * @param review review being processed
     * @param status new processing status
     */
    private void updateStatus(
            Review review,
            ReviewStatus status) {

        review.setStatus(status);
        reviewRepository.save(review);
    }
}