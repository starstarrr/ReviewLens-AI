package com.reviewlens.service;

import com.reviewlens.dispatcher.AnalysisDispatcher;
import com.reviewlens.dto.CreateReviewRequest;
import com.reviewlens.dto.GitHubRepositoryResponse;
import com.reviewlens.entity.Review;
import com.reviewlens.entity.ReviewStatus;
import com.reviewlens.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    /**
     * Creates a review for a repository accessible to the authenticated user.
     *
     * @param request          repository review request
     * @param authorizedClient authenticated GitHub OAuth client
     * @return the created review
     */
    public Review createReview(
            CreateReviewRequest request,
            OAuth2AuthorizedClient authorizedClient) {

        String repositoryFullName = request.repositoryFullName().trim();

        GitHubRepositoryResponse repository = gitHubService
                .getRepositories(authorizedClient)
                .stream()
                .filter(item -> item.getFullName() != null
                        && item.getFullName()
                                .equalsIgnoreCase(
                                        repositoryFullName))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Repository is not accessible to the authenticated GitHub user"));

        String repositoryUrl = "https://github.com/"
                + repository.getFullName();

        Review review = new Review(
                repositoryUrl,
                ReviewStatus.QUEUED);

        Review savedReview = reviewRepository.save(review);

        analysisDispatcher.submit(
                savedReview.getId());

        return savedReview;
    }

    /**
     * Returns a review by its identifier.
     *
     * @param id review identifier
     * @return the requested review
     */
    public Review getReview(Long id) {
        return reviewRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Review not found: " + id));
    }
}