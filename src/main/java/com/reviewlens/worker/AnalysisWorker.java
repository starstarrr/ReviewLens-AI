package com.reviewlens.worker;

import com.reviewlens.service.RepositoryCloneService;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.reviewlens.dto.AiReviewResult;
import com.reviewlens.entity.AiReview;
import com.reviewlens.entity.Finding;
import com.reviewlens.entity.Review;
import com.reviewlens.entity.ReviewStatus;
import com.reviewlens.repository.AiReviewRepository;
import com.reviewlens.repository.FindingRepository;
import com.reviewlens.repository.ReviewRepository;
import com.reviewlens.service.ai.AiCodeReviewService;
import com.reviewlens.service.analysis.AnalysisEngine;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class AnalysisWorker {

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
     * The processing flow is:
     * clone repository, analyze source files, save findings,
     * generate an AI review, save the AI review,
     * and update the final review status.
     *
     * @param reviewId the identifier of the review to process
     */
    @Async
    public void processReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException(
                        "Review not found: " + reviewId));

        try {
            updateStatus(review, ReviewStatus.CLONING);

            System.out.println(
                    "Cloning repository for review: " + reviewId);

            Path repositoryPath = repositoryCloneService.cloneRepository(
                    review.getRepositoryUrl(),
                    reviewId);

            System.out.println(
                    "Repository cloned to: "
                            + repositoryPath.toAbsolutePath());

            updateStatus(review, ReviewStatus.ANALYZING);

            System.out.println(
                    "Analyzing repository for review: " + reviewId);

            List<Finding> findings = analysisEngine.analyzeRepository(
                    review,
                    repositoryPath);

            findingRepository.saveAll(findings);

            System.out.println(
                    "Saved "
                            + findings.size()
                            + " findings for review: "
                            + reviewId);

            System.out.println(
                    "Generating AI review for review: " + reviewId);

            AiReviewResult aiResult = aiCodeReviewService.generateReview(review, findings);

            AiReview aiReview = createAiReview(review, aiResult);

            aiReviewRepository.save(aiReview);

            System.out.println(
                    "Saved AI review for review: " + reviewId);

            updateStatus(review, ReviewStatus.COMPLETED);

            System.out.println(
                    "Review completed: " + reviewId);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            updateStatus(review, ReviewStatus.FAILED);

            System.err.println(
                    "Repository clone interrupted for review: "
                            + reviewId);

        } catch (Exception exception) {
            updateStatus(review, ReviewStatus.FAILED);

            System.err.println(
                    "Review failed: "
                            + reviewId
                            + " - "
                            + exception.getMessage());

            exception.printStackTrace();
        }
    }

    /**
     * Creates a persistent AI review entity from the generated result.
     *
     * Lists are serialized into JSON strings before being stored.
     *
     * @param review the associated repository review
     * @param result the AI-generated result
     * @return the AI review entity
     */
    private AiReview createAiReview(
            Review review,
            AiReviewResult result) throws JacksonException {
        String strengthsJson = objectMapper.writeValueAsString(result.strengths());

        String risksJson = objectMapper.writeValueAsString(result.risks());

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
     * Updates and persists the current review status.
     *
     * @param review the review being processed
     * @param status the new processing status
     */
    private void updateStatus(
            Review review,
            ReviewStatus status) {
        review.setStatus(status);
        reviewRepository.save(review);
    }
}