package com.reviewlens.service.ai;

import com.reviewlens.dto.AiReviewResult;
import com.reviewlens.entity.Finding;
import com.reviewlens.entity.Review;

import java.util.List;

/**
 * Generates an AI-assisted assessment of a repository review.
 *
 * Implementations may use OpenAI, AWS Bedrock, or another
 * large language model provider.
 */
public interface AiCodeReviewService {

    /**
     * Generates an AI review using the repository information
     * and findings produced by static analysis.
     *
     * @param review   the repository review
     * @param findings static analysis findings associated with the review
     * @return the structured AI-generated review
     */
    AiReviewResult generateReview(
            Review review,
            List<Finding> findings);
}
