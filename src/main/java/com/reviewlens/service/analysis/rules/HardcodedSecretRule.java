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
import java.util.regex.Pattern;

/**
 * Detects source code lines that may contain hardcoded credentials
 * or sensitive configuration values.
 *
 * This rule only reports the location of the possible secret.
 * It never stores the detected value inside the finding.
 */
@Component
public class HardcodedSecretRule implements AnalysisRule {

    private static final String RULE_ID = "HARDCODED_SECRET";

    private static final Pattern SECRET_ASSIGNMENT_PATTERN = Pattern.compile(
            "(?i)"
                    + "(password|passwd|secret|api[_-]?key|"
                    + "access[_-]?token|auth[_-]?token|private[_-]?key)"
                    + "\\s*[:=]\\s*"
                    + "[\"'][^\"']{6,}[\"']");

    @Override
    public List<Finding> analyze(Path file) throws IOException {
        List<String> lines = Files.readAllLines(
                file,
                StandardCharsets.UTF_8);

        List<Finding> findings = new ArrayList<>();

        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);

            if (containsPossibleSecret(line)
                    && !containsPlaceholderValue(line)) {
                findings.add(createFinding(index + 1));
            }
        }

        return findings;
    }

    /**
     * Checks whether a line resembles a hardcoded secret assignment.
     */
    private boolean containsPossibleSecret(String line) {
        return SECRET_ASSIGNMENT_PATTERN
                .matcher(line)
                .find();
    }

    /**
     * Ignores common placeholder and environment-variable examples.
     */
    private boolean containsPlaceholderValue(String line) {
        String normalizedLine = line.toLowerCase(Locale.ROOT);

        return normalizedLine.contains("process.env")
                || normalizedLine.contains("system.getenv")
                || normalizedLine.contains("${")
                || normalizedLine.contains("your_api_key")
                || normalizedLine.contains("your-api-key")
                || normalizedLine.contains("your_secret")
                || normalizedLine.contains("your-secret")
                || normalizedLine.contains("example")
                || normalizedLine.contains("placeholder")
                || normalizedLine.contains("changeme");
    }

    /**
     * Creates a finding without exposing the possible secret value.
     */
    private Finding createFinding(int lineNumber) {
        Finding finding = new Finding();

        finding.setRuleId(RULE_ID);
        finding.setSeverity(FindingSeverity.HIGH);
        finding.setLineNumber(lineNumber);
        finding.setMessage(
                "A possible hardcoded credential or secret was found.");
        finding.setSuggestion(
                "Move sensitive values to environment variables or a secure secret-management service.");

        return finding;
    }
}
