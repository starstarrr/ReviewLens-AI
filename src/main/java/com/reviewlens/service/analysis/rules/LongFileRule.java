package com.reviewlens.service.analysis.rules;

import com.reviewlens.entity.Finding;
import com.reviewlens.entity.FindingSeverity;
import com.reviewlens.service.analysis.AnalysisRule;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Detects source files that may contain too many responsibilities.
 */
@Component
public class LongFileRule implements AnalysisRule {

    private static final String RULE_ID = "LONG_FILE";
    private static final int MAXIMUM_LINE_COUNT = 500;

    @Override
    public List<Finding> analyze(Path file) throws IOException {
        long lineCount;

        try (var lines = Files.lines(
                file,
                StandardCharsets.UTF_8)) {
            lineCount = lines.count();
        }

        if (lineCount <= MAXIMUM_LINE_COUNT) {
            return List.of();
        }

        Finding finding = new Finding();

        finding.setRuleId(RULE_ID);
        finding.setSeverity(FindingSeverity.MEDIUM);
        finding.setLineNumber(1);
        finding.setMessage(
                "This file contains "
                        + lineCount
                        + " lines, which exceeds the recommended limit of "
                        + MAXIMUM_LINE_COUNT
                        + " lines.");
        finding.setSuggestion(
                "Consider splitting this file into smaller modules with focused responsibilities.");

        return List.of(finding);
    }
}