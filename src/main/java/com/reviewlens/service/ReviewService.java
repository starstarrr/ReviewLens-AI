package com.reviewlens.service;

import com.reviewlens.dispatcher.AnalysisDispatcher;
import com.reviewlens.dto.CreateReviewRequest;
import com.reviewlens.dto.GitHubRepositoryResponse;
import com.reviewlens.entity.Review;
import com.reviewlens.entity.ReviewStatus;
import com.reviewlens.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final GitHubService gitHubService;
    private final AnalysisDispatcher analysisDispatcher;

    public ReviewService(
            ReviewRepository reviewRepository,
            GitHubService gitHubService,
            AnalysisDispatcher analysisDispatcher) {

        this.reviewRepository = reviewRepository;
        this.gitHubService = gitHubService;
        this.analysisDispatcher = analysisDispatcher;
    }

    public Review createReview(CreateReviewRequest request) {

        String repositoryUrl = request.getRepositoryUrl();

        URI uri;
        try {
            uri = URI.create(repositoryUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid GitHub repository URL");
        }

        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException(
                    "Repository URL must use HTTPS");
        }

        if (!"github.com".equalsIgnoreCase(uri.getHost())) {
            throw new IllegalArgumentException(
                    "Only GitHub repository URLs are supported");
        }

        String[] pathParts = uri.getPath().split("/");

        if (pathParts.length < 3
                || pathParts[1].isBlank()
                || pathParts[2].isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid GitHub repository URL");
        }

        String owner = pathParts[1];
        String repositoryName = pathParts[2];

        if (repositoryName.endsWith(".git")) {
            repositoryName = repositoryName.substring(
                    0,
                    repositoryName.length() - 4);
        }

        System.out.println("Repository URL: " + repositoryUrl);
        System.out.println("Owner: " + owner);
        System.out.println("Repo: " + repositoryName);

        GitHubRepositoryResponse repository = gitHubService.getRepository(owner, repositoryName);

        Review review = new Review(
                repositoryUrl,
                ReviewStatus.QUEUED);

        Review savedReview = reviewRepository.save(review);

        // Dispatch analysis task
        analysisDispatcher.submit(savedReview.getId());

        return savedReview;
    }

    public Review getReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found: " + id));
    }
}