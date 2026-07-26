package com.reviewlens.dispatcher;

import com.reviewlens.service.ReviewProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisDispatcher {

    private final AnalysisWorker analysisworker;

    public void submit(Long reviewId) {
        analysisworker.processReview(reviewId);
    }
}
