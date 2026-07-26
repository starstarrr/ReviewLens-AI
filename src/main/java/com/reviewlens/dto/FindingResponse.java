package com.reviewlens.dto;

import com.reviewlens.entity.Finding;
import com.reviewlens.entity.FindingSeverity;

/**
 * Represents a finding returned by the review API.
 *
 * This DTO prevents JPA entities and relationships from being
 * exposed directly through the controller.
 */
public record FindingResponse(
        Long id,
        String ruleId,
        FindingSeverity severity,
        String filePath,
        Integer lineNumber,
        String message,
        String suggestion) {

    /**
     * Converts a Finding entity into an API response object.
     *
     * @param finding the finding entity to convert
     * @return the response representation of the finding
     */
    public static FindingResponse from(Finding finding) {
        return new FindingResponse(
                finding.getId(),
                finding.getRuleId(),
                finding.getSeverity(),
                finding.getFilePath(),
                finding.getLineNumber(),
                finding.getMessage(),
                finding.getSuggestion());
    }
}