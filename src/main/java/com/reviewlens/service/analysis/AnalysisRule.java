package com.reviewlens.service.analysis;

import com.reviewlens.entity.Finding;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface AnalysisRule {

    List<Finding> analyze(Path file) throws IOException;

}
