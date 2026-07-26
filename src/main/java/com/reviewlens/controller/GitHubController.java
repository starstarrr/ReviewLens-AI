package com.reviewlens.controller;

import com.reviewlens.dto.GitHubRepositoryResponse;
import com.reviewlens.service.GitHubService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/github")
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/repos/{owner}/{repo}")
    public GitHubRepositoryResponse getRepository(
            @PathVariable String owner,
            @PathVariable String repo) {

        return gitHubService.getRepository(owner, repo);
    }
}
