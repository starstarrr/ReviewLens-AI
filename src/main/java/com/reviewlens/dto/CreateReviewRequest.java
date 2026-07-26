package com.reviewlens.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateReviewRequest {

    @NotBlank(message = "repositoryUrl is required")
    @Pattern(regexp = "^https://github\\.com/[^/]+/[^/]+/?$", message = "repositoryUrl must be a valid GitHub repository URL")
    private String repositoryUrl;

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }
}
