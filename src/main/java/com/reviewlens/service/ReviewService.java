package com.reviewlens.service;

import com.reviewlens.dto.CreateReviewRequest;
import com.reviewlens.entity.Review;
import com.reviewlens.repository.ReviewRepository;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review createReview(CreateReviewRequest request) {
        Review review = new Review(
                request.getRepositoryUrl(),
                "QUEUED");

        return reviewRepository.save(review);
    }

    public Review getReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }

}