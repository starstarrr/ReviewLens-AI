package com.reviewlens.service.ai;

import com.reviewlens.dto.AiReviewResult;
import com.reviewlens.entity.Finding;
import com.reviewlens.entity.Review;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.BedrockRuntimeException;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.InferenceConfiguration;
import software.amazon.awssdk.services.bedrockruntime.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Primary
public class BedrockAiCodeReviewService implements AiCodeReviewService {

    private static final int MAX_FINDINGS_IN_PROMPT = 100;

    private static final String SUMMARY_HEADER = "SUMMARY:";
    private static final String STRENGTHS_HEADER = "STRENGTHS:";
    private static final String RISKS_HEADER = "RISKS:";
    private static final String RECOMMENDATIONS_HEADER = "RECOMMENDATIONS:";

    private final BedrockRuntimeClient bedrockRuntimeClient;
    private final String modelId;

    public BedrockAiCodeReviewService(
            BedrockRuntimeClient bedrockRuntimeClient,
            @Value("${reviewlens.ai.bedrock.model-id}") String modelId) {

        this.bedrockRuntimeClient = Objects.requireNonNull(
                bedrockRuntimeClient,
                "bedrockRuntimeClient must not be null");

        if (modelId == null || modelId.isBlank()) {
            throw new IllegalArgumentException(
                    "Bedrock model ID must not be blank.");
        }

        this.modelId = modelId;
    }

    /**
     * Generates a structured AI review using repository information
     * and static-analysis findings.
     */
    @Override
    public AiReviewResult generateReview(
            Review review,
            List<Finding> findings) {

        if (review == null) {
            throw new IllegalArgumentException(
                    "Review must not be null.");
        }

        List<Finding> safeFindings = findings == null ? List.of() : findings;

        String prompt = buildReviewPrompt(review, safeFindings);
        String response = generateReview(prompt);

        return parseReviewResponse(response);
    }

    /**
     * Sends a raw prompt to Amazon Bedrock and returns its text
     * response.
     *
     * This overload is also useful for connection testing.
     */
    public String generateReview(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException(
                    "Bedrock prompt must not be blank.");
        }

        Message userMessage = Message.builder()
                .role(ConversationRole.USER)
                .content(ContentBlock.fromText(prompt))
                .build();

        InferenceConfiguration inferenceConfiguration = InferenceConfiguration.builder()
                .maxTokens(1_500)
                .temperature(0.1F)
                .topP(0.9F)
                .build();

        ConverseRequest request = ConverseRequest.builder()
                .modelId(modelId)
                .messages(userMessage)
                .inferenceConfig(inferenceConfiguration)
                .build();

        try {
            ConverseResponse response = bedrockRuntimeClient.converse(request);

            return extractText(response);

        } catch (BedrockRuntimeException exception) {
            throw new IllegalStateException(
                    buildBedrockErrorMessage(exception),
                    exception);

        } catch (SdkClientException exception) {
            throw new IllegalStateException(
                    "Unable to connect to Amazon Bedrock. "
                            + "Check your AWS credentials, region, "
                            + "network connection, and model ID.",
                    exception);
        }
    }

    /**
     * Builds the code-review prompt from the Review entity and
     * static-analysis findings.
     */
    private String buildReviewPrompt(
            Review review,
            List<Finding> findings) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are a senior software engineer performing a professional
                repository code review.

                Review the repository information and static-analysis findings
                below.

                Return your answer using exactly these four sections:

                SUMMARY:
                Write one concise paragraph assessing the repository.

                STRENGTHS:
                - Write each supported strength as one bullet point.
                - If no strengths are supported by the provided information,
                  write: - None identified from the available evidence.

                RISKS:
                - Write each maintainability, reliability, correctness, or
                  security concern as one bullet point.
                - If no risks are supported, write:
                  - None identified from the available evidence.

                RECOMMENDATIONS:
                - Write each specific and actionable recommendation as one
                  bullet point.
                - Recommendations should correspond to the supplied findings.

                Important rules:

                - Use the section headings exactly as written.
                - Do not use Markdown code fences.
                - Do not add sections.
                - Do not invent repository features or source-code behavior.
                - Base the review only on the provided repository information
                  and static-analysis findings.
                - Keep the response concise and professional.

                Repository information:
                """);

        prompt.append("Review ID: ")
                .append(safeValue(review.getId()))
                .append('\n');

        prompt.append("Repository URL: ")
                .append(safeValue(review.getRepositoryUrl()))
                .append('\n');

        prompt.append("Review status: ")
                .append(safeValue(review.getStatus()))
                .append('\n');

        prompt.append("Created at: ")
                .append(safeValue(review.getCreatedAt()))
                .append('\n');

        prompt.append("\nStatic-analysis findings:\n");

        if (findings.isEmpty()) {
            prompt.append(
                    "No static-analysis findings were produced.\n");
            return prompt.toString();
        }

        int findingCount = Math.min(
                findings.size(),
                MAX_FINDINGS_IN_PROMPT);

        for (int index = 0; index < findingCount; index++) {
            Finding finding = findings.get(index);

            prompt.append("\nFinding ")
                    .append(index + 1)
                    .append(":\n");

            if (finding == null) {
                prompt.append("- Finding information unavailable\n");
                continue;
            }

            prompt.append("- Rule ID: ")
                    .append(safeValue(finding.getRuleId()))
                    .append('\n');

            prompt.append("- Severity: ")
                    .append(safeValue(finding.getSeverity()))
                    .append('\n');

            prompt.append("- File: ")
                    .append(safeValue(finding.getFilePath()))
                    .append('\n');

            prompt.append("- Line: ")
                    .append(safeValue(finding.getLineNumber()))
                    .append('\n');

            prompt.append("- Message: ")
                    .append(safeValue(finding.getMessage()))
                    .append('\n');

            prompt.append("- Existing suggestion: ")
                    .append(safeValue(finding.getSuggestion()))
                    .append('\n');
        }

        if (findings.size() > MAX_FINDINGS_IN_PROMPT) {
            prompt.append("\nNote: Only the first ")
                    .append(MAX_FINDINGS_IN_PROMPT)
                    .append(" findings were included out of ")
                    .append(findings.size())
                    .append(" total findings.\n");
        }

        return prompt.toString();
    }

    /**
     * Extracts all text content from the Bedrock response.
     */
    private String extractText(ConverseResponse response) {
        if (response == null
                || response.output() == null
                || response.output().message() == null
                || response.output().message().content() == null) {

            throw new IllegalStateException(
                    "Amazon Bedrock returned an empty response.");
        }

        String generatedText = response.output()
                .message()
                .content()
                .stream()
                .filter(Objects::nonNull)
                .map(ContentBlock::text)
                .filter(Objects::nonNull)
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining("\n"))
                .trim();

        if (generatedText.isBlank()) {
            throw new IllegalStateException(
                    "Amazon Bedrock returned no text content.");
        }

        return generatedText;
    }

    /**
     * Parses the model's section-based response into AiReviewResult.
     */
    private AiReviewResult parseReviewResponse(String response) {
        if (response == null || response.isBlank()) {
            return fallbackResult(
                    "Amazon Bedrock returned no readable review.");
        }

        String cleanedResponse = removeCodeFences(response);

        int summaryIndex = findHeader(cleanedResponse, SUMMARY_HEADER);

        int strengthsIndex = findHeader(cleanedResponse, STRENGTHS_HEADER);

        int risksIndex = findHeader(cleanedResponse, RISKS_HEADER);

        int recommendationsIndex = findHeader(
                cleanedResponse,
                RECOMMENDATIONS_HEADER);

        boolean validOrder = summaryIndex >= 0
                && strengthsIndex > summaryIndex
                && risksIndex > strengthsIndex
                && recommendationsIndex > risksIndex;

        if (!validOrder) {
            return fallbackResult(cleanedResponse);
        }

        String summaryText = cleanedResponse.substring(
                summaryIndex + SUMMARY_HEADER.length(),
                strengthsIndex).trim();

        String strengthsText = cleanedResponse.substring(
                strengthsIndex + STRENGTHS_HEADER.length(),
                risksIndex).trim();

        String risksText = cleanedResponse.substring(
                risksIndex + RISKS_HEADER.length(),
                recommendationsIndex).trim();

        String recommendationsText = cleanedResponse.substring(
                recommendationsIndex
                        + RECOMMENDATIONS_HEADER.length())
                .trim();

        String summary = summaryText.isBlank()
                ? "The AI review completed, but no summary was provided."
                : normalizeWhitespace(summaryText);

        List<String> strengths = parseBulletList(strengthsText);

        List<String> risks = parseBulletList(risksText);

        List<String> recommendations = parseBulletList(recommendationsText);

        return new AiReviewResult(
                summary,
                strengths,
                risks,
                recommendations);
    }

    /**
     * Converts section lines into a clean list of strings.
     */
    private List<String> parseBulletList(String sectionText) {
        if (sectionText == null || sectionText.isBlank()) {
            return List.of();
        }

        List<String> results = new ArrayList<>();
        String[] lines = sectionText.split("\\R");

        StringBuilder currentItem = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isBlank()) {
                continue;
            }

            if (isBulletLine(trimmed)) {
                addCurrentItem(results, currentItem);

                currentItem.append(
                        removeBulletPrefix(trimmed));
            } else if (!currentItem.isEmpty()) {
                currentItem.append(' ')
                        .append(trimmed);
            } else {
                currentItem.append(trimmed);
            }
        }

        addCurrentItem(results, currentItem);

        return results.stream()
                .map(this::normalizeWhitespace)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private boolean isBulletLine(String value) {
        return value.startsWith("- ")
                || value.startsWith("* ")
                || value.startsWith("• ")
                || value.matches("^\\d+[.)]\\s+.*");
    }

    private String removeBulletPrefix(String value) {
        if (value.startsWith("- ")
                || value.startsWith("* ")
                || value.startsWith("• ")) {

            return value.substring(2).trim();
        }

        return value.replaceFirst(
                "^\\d+[.)]\\s+",
                "").trim();
    }

    private void addCurrentItem(
            List<String> results,
            StringBuilder currentItem) {

        if (!currentItem.isEmpty()) {
            String item = currentItem.toString().trim();

            if (!item.isBlank()) {
                results.add(item);
            }

            currentItem.setLength(0);
        }
    }

    private int findHeader(
            String response,
            String header) {

        return response.toUpperCase()
                .indexOf(header);
    }

    private String removeCodeFences(String response) {
        String cleaned = response.trim();

        if (cleaned.startsWith("```")) {
            int firstNewline = cleaned.indexOf('\n');

            if (firstNewline >= 0) {
                cleaned = cleaned.substring(
                        firstNewline + 1);
            }
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(
                    0,
                    cleaned.length() - 3);
        }

        return cleaned.trim();
    }

    private String normalizeWhitespace(String value) {
        return value.trim()
                .replaceAll("\\s+", " ");
    }

    private String safeValue(Object value) {
        if (value == null) {
            return "Not available";
        }

        String text = value.toString().trim();

        return text.isBlank()
                ? "Not available"
                : text;
    }

    /**
     * Preserves the raw model output when its section format is
     * invalid instead of failing the entire repository review.
     */
    private AiReviewResult fallbackResult(String rawResponse) {
        String summary;

        if (rawResponse == null || rawResponse.isBlank()) {
            summary = "The AI review completed, but no readable "
                    + "content was returned.";
        } else {
            summary = normalizeWhitespace(rawResponse);
        }

        return new AiReviewResult(
                summary,
                List.of(),
                List.of(
                        "The AI response did not match the expected "
                                + "structured section format."),
                List.of(
                        "Review the raw AI response and verify its "
                                + "suggestions before applying changes."));
    }

    private String buildBedrockErrorMessage(
            BedrockRuntimeException exception) {

        if (exception.awsErrorDetails() != null
                && exception.awsErrorDetails()
                        .errorMessage() != null
                && !exception.awsErrorDetails()
                        .errorMessage()
                        .isBlank()) {

            return "Amazon Bedrock request failed: "
                    + exception.awsErrorDetails()
                            .errorMessage();
        }

        return "Amazon Bedrock request failed: "
                + exception.getMessage();
    }

    /**
     * Temporary Bedrock connection test.
     */
    public String testConnection() {
        return generateReview(
                "Reply with exactly: "
                        + "ReviewLens Bedrock connection successful");
    }
}