package com.reviewlens.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class RepositoryFileScanner {

    private static final Set<String> EXCLUDED_DIRECTORIES = Set.of(
            ".git",
            ".idea",
            ".vscode",
            "node_modules",
            "target",
            "build",
            "dist",
            "coverage",
            ".next",
            "out");

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            ".java",
            ".js",
            ".jsx",
            ".ts",
            ".tsx",
            ".py");

    public List<Path> scanRepository(Path repositoryPath) throws IOException {
        validateRepositoryPath(repositoryPath);

        try (Stream<Path> paths = Files.walk(repositoryPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !isInsideExcludedDirectory(
                            repositoryPath,
                            path))
                    .filter(this::isSupportedSourceFile)
                    .sorted()
                    .toList();
        }
    }

    private void validateRepositoryPath(Path repositoryPath) {
        if (repositoryPath == null) {
            throw new IllegalArgumentException(
                    "Repository path must not be null.");
        }

        if (!Files.exists(repositoryPath)) {
            throw new IllegalArgumentException(
                    "Repository path does not exist: " + repositoryPath);
        }

        if (!Files.isDirectory(repositoryPath)) {
            throw new IllegalArgumentException(
                    "Repository path is not a directory: " + repositoryPath);
        }
    }

    private boolean isInsideExcludedDirectory(
            Path repositoryPath,
            Path filePath) {
        Path relativePath = repositoryPath.relativize(filePath);

        for (Path pathPart : relativePath) {
            if (EXCLUDED_DIRECTORIES.contains(pathPart.toString())) {
                return true;
            }
        }

        return false;
    }

    private boolean isSupportedSourceFile(Path filePath) {
        String fileName = filePath
                .getFileName()
                .toString()
                .toLowerCase();

        return SUPPORTED_EXTENSIONS.stream()
                .anyMatch(fileName::endsWith);
    }
}