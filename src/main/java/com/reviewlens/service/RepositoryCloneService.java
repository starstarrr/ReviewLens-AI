package com.reviewlens.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class RepositoryCloneService {

    public Path cloneRepository(String repositoryUrl, Long reviewId)
            throws IOException, InterruptedException {

        Path repositoriesDirectory = Path.of("repositories");
        Path targetDirectory = repositoriesDirectory.resolve(
                reviewId.toString());

        Files.createDirectories(repositoriesDirectory);

        if (Files.exists(targetDirectory)) {
            throw new IllegalStateException(
                    "Repository directory already exists: "
                            + targetDirectory);
        }

        Process process = new ProcessBuilder(
                "git",
                "clone",
                "--depth",
                "1",
                repositoryUrl,
                targetDirectory.toString())
                .redirectErrorStream(true)
                .inheritIO()
                .start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IllegalStateException(
                    "Failed to clone repository. Git exit code: "
                            + exitCode);
        }

        return targetDirectory;
    }
}