function ReviewResult({
    summary,
    findings,
    aiReview,
    loadingResults,
  }) {
    if (loadingResults) {
      return (
        <div className="review-result">
          <p className="muted-text">
            Loading review results...
          </p>
        </div>
      );
    }
  
    if (!summary && !findings && !aiReview) {
      return null;
    }
  
    return (
      <div className="review-result">
        {summary && (
          <section className="result-section">
            <h4>Summary</h4>
  
            <div className="summary-grid">
              <div className="summary-item">
                <span>Total</span>
                <strong>
                  {summary.totalFindings ?? 0}
                </strong>
              </div>
  
              <div className="summary-item">
                <span>Critical</span>
                <strong>{summary.critical ?? 0}</strong>
              </div>
  
              <div className="summary-item">
                <span>High</span>
                <strong>{summary.high ?? 0}</strong>
              </div>
  
              <div className="summary-item">
                <span>Medium</span>
                <strong>{summary.medium ?? 0}</strong>
              </div>
  
              <div className="summary-item">
                <span>Low</span>
                <strong>{summary.low ?? 0}</strong>
              </div>
  
              <div className="summary-item">
                <span>Info</span>
                <strong>{summary.info ?? 0}</strong>
              </div>
            </div>
          </section>
        )}
  
        {aiReview && (
          <section className="result-section">
            <h4>AI Review</h4>
  
            <p className="ai-summary">
              {aiReview.summary ||
                "No AI summary available."}
            </p>
  
            {Array.isArray(aiReview.strengths) &&
              aiReview.strengths.length > 0 && (
                <div className="ai-list">
                  <h5>Strengths</h5>
  
                  <ul>
                    {aiReview.strengths.map(
                      (strength, index) => (
                        <li key={`strength-${index}`}>
                          {strength}
                        </li>
                      )
                    )}
                  </ul>
                </div>
              )}
  
            {Array.isArray(aiReview.risks) &&
              aiReview.risks.length > 0 && (
                <div className="ai-list">
                  <h5>Risks</h5>
  
                  <ul>
                    {aiReview.risks.map(
                      (risk, index) => (
                        <li key={`risk-${index}`}>
                          {risk}
                        </li>
                      )
                    )}
                  </ul>
                </div>
              )}
  
            {Array.isArray(aiReview.recommendations) &&
              aiReview.recommendations.length > 0 && (
                <div className="ai-list">
                  <h5>Recommendations</h5>
  
                  <ul>
                    {aiReview.recommendations.map(
                      (recommendation, index) => (
                        <li
                          key={`recommendation-${index}`}
                        >
                          {recommendation}
                        </li>
                      )
                    )}
                  </ul>
                </div>
              )}
          </section>
        )}
  
        {Array.isArray(findings) && (
          <section className="result-section">
            <h4>
              Findings ({findings.length})
            </h4>
  
            {findings.length === 0 ? (
              <p className="muted-text">
                No findings were detected.
              </p>
            ) : (
              <div className="findings-list">
                {findings.map((finding, index) => (
                  <article
                    className="finding-item"
                    key={
                      finding.id ??
                      `${finding.filePath}-${finding.lineNumber}-${index}`
                    }
                  >
                    <div className="finding-heading">
                      <strong>
                        {finding.ruleId ||
                          "Code finding"}
                      </strong>
  
                      <span className="finding-severity">
                        {finding.severity ||
                          "UNKNOWN"}
                      </span>
                    </div>
  
                    <p className="finding-location">
                      {finding.filePath ||
                        "Unknown file"}
  
                      {finding.lineNumber
                        ? `:${finding.lineNumber}`
                        : ""}
                    </p>
  
                    <p>
                      {finding.message ||
                        "No finding message provided."}
                    </p>
  
                    {finding.suggestion && (
                      <p className="finding-suggestion">
                        <strong>Suggestion:</strong>{" "}
                        {finding.suggestion}
                      </p>
                    )}
                  </article>
                ))}
              </div>
            )}
          </section>
        )}
      </div>
    );
  }
  
  export default ReviewResult;