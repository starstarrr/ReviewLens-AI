package com.reviewlens.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class CodeSnippetReader {

    private static final int DEFAULT_CONTEXT_LINES = 5;

    /**
     * Reads the requested line together with five lines before and after it.
     *
     * @param repositoryPath   root directory of the cloned repository
     * @param relativeFilePath file path relative to the repository root
     * @param lineNumber       one-based line number from the Finding
     * @return formatted source-code snippet with line numbers
     */
    public String readSnippet(
            Path repositoryPath,
            String relativeFilePath,
            Integer lineNumber) {

        return readSnippet(
                repositoryPath,
                relativeFilePath,
                lineNumber,
                DEFAULT_CONTEXT_LINES);
    }

    /**
     * Reads source code surrounding a specific line.
     *
     * @param repositoryPath   root directory of the cloned repository
     * @param relativeFilePath file path relative to the repository root
     * @param lineNumber       one-based line number
     * @param contextLines     number of lines to include before and after
     * @return formatted source-code snippet with line numbers
     */
    public String readSnippet(
            Path repositoryPath,
            String relativeFilePath,
            Integer lineNumber,
            int contextLines) {

        validateInputs(
                repositoryPath,
                relativeFilePath,
                lineNumber,
                contextLines);

        Path normalizedRepositoryPath = repositoryPath
                .toAbsolutePath()
                .normalize();

        Path sourceFilePath = normalizedRepositoryPath
                .resolve(relativeFilePath)
                .normalize();

        if (!sourceFilePath.startsWith(normalizedRepositoryPath)) {
            throw new IllegalArgumentException(
                    "File path must remain inside the repository: "
                            + relativeFilePath);
        }

        if (!Files.exists(sourceFilePath)) {
            return "Source file not found: " + relativeFilePath;
        }

        if (!Files.isRegularFile(sourceFilePath)) {
            return "Source path is not a regular file: "
                    + relativeFilePath;
        }

        try {
            List<String> lines = Files.readAllLines(sourceFilePath);

            if (lines.isEmpty()) {
                return "Source file is empty: " + relativeFilePath;
            }

            if (lineNumber > lines.size()) {
                return "Requested line "
                        + lineNumber
                        + " exceeds file length of "
                        + lines.size()
                        + " lines.";
            }

            int startLine = Math.max(
                    1,
                    lineNumber - contextLines);

            int endLine = Math.min(
                    lines.size(),
                    lineNumber + contextLines);

            return formatSnippet(
                    lines,
                    startLine,
                    endLine,
                    lineNumber);

        } catch (IOException exception) {
            return "Unable to read source file "
                    + relativeFilePath
                    + ": "
                    + exception.getMessage();
        }
    }

    private String formatSnippet(
            List<String> lines,
            int startLine,
            int endLine,
            int targetLine) {

        StringBuilder snippet = new StringBuilder();

        for (int currentLine = startLine; currentLine <= endLine; currentLine++) {

            String marker = currentLine == targetLine
                    ? ">"
                    : " ";

            snippet.append(marker)
                    .append(String.format("%5d", currentLine))
                    .append(" | ")
                    .append(lines.get(currentLine - 1))
                    .append('\n');
        }

        return snippet.toString().stripTrailing();
    }

    private void validateInputs(
            Path repositoryPath,
            String relativeFilePath,
            Integer lineNumber,
            int contextLines) {

        if (repositoryPath == null) {
            throw new IllegalArgumentException(
                    "Repository path must not be null.");
        }

        if (!Files.exists(repositoryPath)) {
            throw new IllegalArgumentException(
                    "Repository path does not exist: "
                            + repositoryPath);
        }

        if (!Files.isDirectory(repositoryPath)) {
            throw new IllegalArgumentException(
                    "Repository path is not a directory: "
                            + repositoryPath);
        }

        if (relativeFilePath == null
                || relativeFilePath.isBlank()) {

            throw new IllegalArgumentException(
                    "Relative file path must not be blank.");
        }

        if (lineNumber == null || lineNumber < 1) {
            throw new IllegalArgumentException(
                    "Line number must be greater than zero.");
        }

        if (contextLines < 0) {
            throw new IllegalArgumentException(
                    "Context lines must not be negative.");
        }
    }
}