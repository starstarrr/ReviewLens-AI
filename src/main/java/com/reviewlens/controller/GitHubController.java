package com.reviewlens.controller;

import com.reviewlens.dto.GitHubRepositoryResponse;
import com.reviewlens.service.GitHubService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/github")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    /**
     * Retrieves information for a specific GitHub repository.
     */
    @GetMapping("/repos/{owner}/{repo}")
    public GitHubRepositoryResponse getRepository(
            @PathVariable String owner,
            @PathVariable String repo) {

        return gitHubService.getRepository(owner, repo);
    }

    /**
     * Retrieves repositories accessible to the authenticated GitHub user.
     */
    @GetMapping("/repositories")
    public List<GitHubRepositoryResponse> getRepositories(
            @RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient authorizedClient) {

        return gitHubService.getRepositories(authorizedClient);
    }
}