package com.reviewlens.dispatcher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.reviewlens.worker.AnalysisWorker;

@Service
@RequiredArgsConstructor
public class AnalysisDispatcher {

    private final AnalysisWorker analysisWorker;

    public void submit(Long reviewId) {
        analysisWorker.processReview(reviewId);
    }
}
