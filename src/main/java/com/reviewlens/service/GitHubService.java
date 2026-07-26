package com.reviewlens.service;

import com.reviewlens.dto.GitHubRepositoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitHubService {

    private final RestClient restClient;

    public GitHubService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    public GitHubRepositoryResponse getRepository(
            String owner,
            String repositoryName) {

        return restClient.get()
                .uri("/repos/{owner}/{repo}", owner, repositoryName)
                .retrieve()
                .body(GitHubRepositoryResponse.class);
    }
}