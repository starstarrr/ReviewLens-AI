package com.reviewlens.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateReviewRequest(

        @NotBlank(message = "Repository full name is required") String repositoryFullName

) {
}