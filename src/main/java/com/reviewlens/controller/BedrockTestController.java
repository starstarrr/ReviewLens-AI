package com.reviewlens.controller;

import com.reviewlens.service.ai.BedrockAiCodeReviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test/bedrock")
public class BedrockTestController {

    private final BedrockAiCodeReviewService bedrockAiCodeReviewService;

    public BedrockTestController(
            BedrockAiCodeReviewService bedrockAiCodeReviewService) {
        this.bedrockAiCodeReviewService = bedrockAiCodeReviewService;
    }

    @GetMapping
    public Map<String, String> testBedrock() {
        String response = bedrockAiCodeReviewService.testConnection();

        return Map.of(
                "status", "success",
                "model", "qwen.qwen3-coder-next",
                "response", response);
    }
}
