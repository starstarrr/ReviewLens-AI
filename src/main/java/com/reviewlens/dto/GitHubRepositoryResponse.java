package com.reviewlens.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitHubRepositoryResponse {

    private String name;

    @JsonProperty("full_name")
    private String fullName;

    private String description;

    private String language;

    @JsonProperty("default_branch")
    private String defaultBranch;

    @JsonProperty("stargazers_count")
    private int stars;

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDescription() {
        return description;
    }

    public String getLanguage() {
        return language;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public int getStars() {
        return stars;
    }
}