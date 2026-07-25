package com.reviewlens.controller;

import com.reviewlens.dto.CreateReviewRequest;
import com.reviewlens.entity.Review;
import com.reviewlens.service.ReviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review createReview(
            @RequestBody CreateReviewRequest request) {

        return reviewService.createReview(request);
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }

}