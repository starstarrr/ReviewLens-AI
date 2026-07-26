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

/**
 * Detects debugging output statements that may have been left
 * in production source code.
 */
@Component
public class ConsoleLogRule implements AnalysisRule {

    private static final String RULE_ID = "DEBUG_OUTPUT";

    @Override
    public List<Finding> analyze(Path file) throws IOException {
        List<String> lines = Files.readAllLines(
                file,
                StandardCharsets.UTF_8);

        List<Finding> findings = new ArrayList<>();

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);

            if (containsDebugOutput(line)) {
                findings.add(createFinding(index + 1));
            }
        }

        return findings;
    }

    /**
     * Detects common JavaScript, TypeScript, Java, and Python
     * debugging output statements.
     */
    private boolean containsDebugOutput(String line) {
        String trimmedLine = line.trim();

        return trimmedLine.contains("console.log(")
                || trimmedLine.contains("console.debug(")
                || trimmedLine.contains("System.out.print(")
                || trimmedLine.contains("System.out.println(")
                || trimmedLine.startsWith("print(");
    }

    /**
     * Creates a finding for a debugging output statement.
     */
    private Finding createFinding(int lineNumber) {
        Finding finding = new Finding();

        finding.setRuleId(RULE_ID);
        finding.setSeverity(FindingSeverity.LOW);
        finding.setLineNumber(lineNumber);
        finding.setMessage(
                "A debugging output statement was found.");
        finding.setSuggestion(
                "Remove the debugging statement or replace it with structured logging.");

        return finding;
    }
}