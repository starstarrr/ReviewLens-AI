package com.reviewlens.service;

import com.reviewlens.entity.Finding;
import com.reviewlens.entity.Review;
import com.reviewlens.entity.ReviewStatus;
import com.reviewlens.repository.FindingRepository;
import com.reviewlens.repository.ReviewRepository;
import com.reviewlens.service.analysis.AnalysisEngine;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class ReviewProcessor {

    private final ReviewRepository reviewRepository;
    private final RepositoryCloneService repositoryCloneService;
    private final AnalysisEngine analysisEngine;
    private final FindingRepository findingRepository;

    public ReviewProcessor(
            ReviewRepository reviewRepository,
            RepositoryCloneService repositoryCloneService,
            AnalysisEngine analysisEngine,
            FindingRepository findingRepository) {
        this.reviewRepository = reviewRepository;
        this.repositoryCloneService = repositoryCloneService;
        this.analysisEngine = analysisEngine;
        this.findingRepository = findingRepository;
    }

    /**
     * Processes a review asynchronously.
     *
     * The processing flow is:
     * clone repository, analyze source files, save findings,
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