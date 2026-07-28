package com.reviewlens.worker;

import com.reviewlens.dto.AiReviewReport;
import com.reviewlens.dto.AiReviewResult;
import com.reviewlens.entity.AiReview;
import com.reviewlens.entity.Finding;
import com.reviewlens.entity.Review;
import com.reviewlens.entity.ReviewStatus;
import com.reviewlens.repository.AiReviewRepository;
import com.reviewlens.repository.FindingRepository;
import com.reviewlens.repository.ReviewRepository;
import com.reviewlens.service.RepositoryCloneService;
import com.reviewlens.service.S3Service;
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
    private final S3Service s3Service;

    public AnalysisWorker(
            ReviewRepository reviewRepository,
            RepositoryCloneService repositoryCloneService,
            AnalysisEngine analysisEngine,
            FindingRepository findingRepository,
            AiCodeReviewService aiCodeReviewService,
            AiReviewRepository aiReviewRepository,
            ObjectMapper objectMapper,
            S3Service s3Service) {

        this.reviewRepository = reviewRepository;
        this.repositoryCloneService = repositoryCloneService;
        this.analysisEngine = analysisEngine;
        this.findingRepository = findingRepository;
        this.aiCodeReviewService = aiCodeReviewService;
        this.aiReviewRepository = aiReviewRepository;
        this.objectMapper = objectMapper;
        this.s3Service = s3Service;
    }

    /**
     * Processes a repository review asynchronously.
     *
     * Flow:
     * 1. Clone repository
     * 2. Analyze repository
     * 3. Save static-analysis findings
     * 4. Generate AI review
     * 5. Generate JSON report
     * 6. Upload JSON report to S3
     * 7. Save AI review and S3 object key
     * 8. Mark review as completed
     *
     * @param reviewId ID of the review to process
     */
    @Async
    public void processReview(Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Review not found: " + reviewId));

        try {
            /*
             * Step 1: Clone repository
             */
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

            /*
             * Step 2: Run static analysis
             */
            updateStatus(review, ReviewStatus.ANALYZING);

            log.info(
                    "Analyzing repository for review: {}",
                    reviewId);

            List<Finding> findings = analysisEngine.analyzeRepository(
                    review,
                    repositoryPath);

            /*
             * Step 3: Save findings to PostgreSQL
             */
            findingRepository.saveAll(findings);

            log.info(
                    "Saved {} findings for review: {}",
                    findings.size(),
                    reviewId);

            /*
             * Step 4: Generate AI review
             */
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

            /*
             * Step 5: Build the formal report
             */
            AiReviewReport report = new AiReviewReport(
                    review.getId(),
                    review.getRepositoryUrl(),
                    aiResult.summary(),
                    aiResult.strengths(),
                    aiResult.risks(),
                    aiResult.recommendations());

            /*
             * Convert Java report object to JSON.
             */
            String reportJson = objectMapper.writeValueAsString(report);

            /*
             * Example:
             * reviews/7/report.json
             */
            String objectKey = String.format(
                    "reviews/%d/report.json",
                    review.getId());

            /*
             * Step 6: Upload report JSON to S3
             */
            s3Service.uploadJson(
                    objectKey,
                    reportJson);

            log.info(
                    "Uploaded AI review report to S3 with key: {}",
                    objectKey);

            /*
             * Step 7: Save AI review data and S3 key
             */
            AiReview aiReview = createAiReview(
                    review,
                    aiResult);

            aiReview.setS3ObjectKey(objectKey);

            aiReviewRepository.save(aiReview);

            log.info(
                    "Saved AI review for review: {}",
                    reviewId);

            /*
             * Step 8: Mark the entire review as completed
             */
            updateStatus(
                    review,
                    ReviewStatus.COMPLETED);

            log.info(
                    "Review completed: {}",
                    reviewId);

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            markReviewFailed(
                    review,
                    reviewId);

            log.error(
                    "Repository clone interrupted for review: {}",
                    reviewId,
                    exception);

        } catch (Exception exception) {

            markReviewFailed(
                    review,
                    reviewId);

            log.error(
                    "Review failed: {}. Exception type: {}. Message: {}",
                    reviewId,
                    exception.getClass().getName(),
                    exception.getMessage(),
                    exception);
        }
    }

    /**
     * Creates an AiReview database entity from the generated AI result.
     *
     * Lists are stored as JSON strings in the database.
     *
     * @param review associated repository review
     * @param result generated AI review result
     * @return persistent AI review entity
     * @throws JacksonException if JSON serialization fails
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
     * Marks the review as failed.
     *
     * @param review   review being processed
     * @param reviewId ID of the failed review
     */
    private void markReviewFailed(
            Review review,
            Long reviewId) {

        try {
            updateStatus(
                    review,
                    ReviewStatus.FAILED);

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
     * @param status new status
     */
    private void updateStatus(
            Review review,
            ReviewStatus status) {

        review.setStatus(status);
        reviewRepository.save(review);
    }
}