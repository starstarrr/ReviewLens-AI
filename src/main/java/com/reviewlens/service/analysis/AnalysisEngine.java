package com.reviewlens.service.analysis;

import com.reviewlens.entity.Finding;
import com.reviewlens.entity.Review;
import com.reviewlens.service.RepositoryFileScanner;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates repository scanning and source code analysis.
 *
 * Spring automatically injects every component that implements
 * the AnalysisRule interface.
 */
@Service
public class AnalysisEngine {

    private final RepositoryFileScanner repositoryFileScanner;
    private final List<AnalysisRule> analysisRules;

    public AnalysisEngine(
            RepositoryFileScanner repositoryFileScanner,
            List<AnalysisRule> analysisRules) {
        this.repositoryFileScanner = repositoryFileScanner;
        this.analysisRules = analysisRules;
    }

    /**
     * Scans and analyzes all supported source files in a repository.
     *
     * @param review         the review associated with the repository
     * @param repositoryPath the root directory of the cloned repository
     * @return all findings detected in the repository
     * @throws IOException if the repository cannot be scanned or read
     */
    public List<Finding> analyzeRepository(
            Review review,
            Path repositoryPath) throws IOException {
        List<Path> sourceFiles = repositoryFileScanner.scanRepository(repositoryPath);

        List<Finding> allFindings = new ArrayList<>();

        for (Path sourceFile : sourceFiles) {
            analyzeFile(
                    review,
                    repositoryPath,
                    sourceFile,
                    allFindings);
        }

        return allFindings;
    }

    /**
     * Runs every registered analysis rule against one source file.
     */
    private void analyzeFile(
            Review review,
            Path repositoryPath,
            Path sourceFile,
            List<Finding> allFindings) throws IOException {
        for (AnalysisRule rule : analysisRules) {
            List<Finding> ruleFindings = rule.analyze(sourceFile);

            for (Finding finding : ruleFindings) {
                finding.setReview(review);
                finding.setFilePath(
                        toRelativePath(repositoryPath, sourceFile));

                allFindings.add(finding);
            }
        }
    }

    /**
     * Converts an absolute repository file path into a relative path.
     *
     * Example:
     * repositories/6/client/src/App.jsx
     * becomes:
     * client/src/App.jsx
     */
    private String toRelativePath(
            Path repositoryPath,
            Path sourceFile) {
        return repositoryPath
                .toAbsolutePath()
                .normalize()
                .relativize(
                        sourceFile.toAbsolutePath().normalize())
                .toString();
    }
}