package com.reviewlens.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Represents a request to create a review for a GitHub repository.
 *
 * @param repositoryFullName repository name in owner/repository format
 */
public record CreateReviewRequest(

        @NotBlank(message = "Repository full name is required") @Pattern(regexp = "^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$", message = "Repository must use the owner/repository format") String repositoryFullName

) {
}