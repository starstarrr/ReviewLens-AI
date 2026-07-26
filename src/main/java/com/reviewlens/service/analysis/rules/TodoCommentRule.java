package com.reviewlens.service.analysis.rules;

import com.reviewlens.entity.Finding;
import com.reviewlens.entity.FindingSeverity;
import com.reviewlens.service.analysis.AnalysisRule;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Detects TODO and FIXME markers in source code.
 */
@Component
public class TodoCommentRule implements AnalysisRule {

    private static final String RULE_ID = "TODO_COMMENT";

    @Override
    public List<Finding> analyze(Path file) throws IOException {
        List<String> lines = Files.readAllLines(
                file,
                StandardCharsets.UTF_8);

        List<Finding> findings = new ArrayList<>();

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);

            if (containsPendingTaskMarker(line)) {
                findings.add(createFinding(index + 1));
            }
        }

        return findings;
    }

    /**
     * Checks whether a line contains a TODO or FIXME marker.
     */
    private boolean containsPendingTaskMarker(String line) {
        String normalizedLine = line.toUpperCase(Locale.ROOT);

        return normalizedLine.contains("TODO")
                || normalizedLine.contains("FIXME");
    }

    /**
     * Creates a finding for a pending task marker.
     */
    private Finding createFinding(int lineNumber) {
        Finding finding = new Finding();

        finding.setRuleId(RULE_ID);
        finding.setSeverity(FindingSeverity.LOW);
        finding.setLineNumber(lineNumber);
        finding.setMessage(
                "A TODO or FIXME marker was found.");
        finding.setSuggestion(
                "Resolve the pending task or track it in an issue before merging.");

        return finding;
    }
}
