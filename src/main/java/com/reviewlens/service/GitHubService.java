package com.reviewlens.service;

import com.reviewlens.dto.GitHubRepositoryResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

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

    /**
     * Retrieves information for a specific GitHub repository.
     */
    public GitHubRepositoryResponse getRepository(
            String owner,
            String repositoryName) {

        return restClient.get()
                .uri("/repos/{owner}/{repo}", owner, repositoryName)
                .retrieve()
                .body(GitHubRepositoryResponse.class);
    }

    /**
     * Retrieves repositories accessible to the authenticated GitHub user.
     */
    public List<GitHubRepositoryResponse> getRepositories(
            OAuth2AuthorizedClient authorizedClient) {

        String accessToken = authorizedClient
                .getAccessToken()
                .getTokenValue();

        return restClient.get()
                .uri("/user/repos")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(
                        new ParameterizedTypeReference<
                                List<GitHubRepositoryResponse>>() {
                        }
                );
    }
}
