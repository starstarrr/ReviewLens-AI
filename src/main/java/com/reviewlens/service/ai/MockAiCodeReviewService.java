package com.reviewlens.service.ai;

import com.reviewlens.dto.AiReviewResult;
import com.reviewlens.entity.Finding;
import com.reviewlens.entity.Review;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockAiCodeReviewService implements AiCodeReviewService {

    @Override
    public AiReviewResult generateReview(
            Review review,
            List<Finding> findings) {

        return new AiReviewResult(
                "The repository demonstrates a solid full-stack architecture. "
                        + "Several maintainability issues were identified by static analysis.",

                List.of(
                        "Uses a modular project structure.",
                        "Includes automated tests.",
                        "Uses REST APIs and database persistence."),

                List.of(
                        "Large source files reduce maintainability.",
                        "Debug output should be removed before production."),

                List.of(
                        "Split very large files into smaller modules.",
                        "Replace console output with structured logging.",
                        "Continue improving test coverage."));
    }
}