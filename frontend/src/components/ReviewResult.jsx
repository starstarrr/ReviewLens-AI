import {
    useMemo,
    useState,
  } from "react";
  
  function ReviewResult({
    summary,
    findings,
    aiReview,
    reviewReport,
    loadingResults,
  }) {
    const [showFullReport, setShowFullReport] =
      useState(false);
  
    const safeFindings = Array.isArray(findings)
      ? findings
      : [];
  
    const groupedFindings = useMemo(() => {
      return safeFindings.reduce((groups, finding) => {
        const ruleId =
          finding?.ruleId || "UNKNOWN_RULE";
  
        if (!groups[ruleId]) {
          groups[ruleId] = [];
        }
  
        groups[ruleId].push(finding);
  
        return groups;
      }, {});
    }, [safeFindings]);
  
    const findingGroups = Object.entries(
      groupedFindings
    ).sort(([, firstItems], [, secondItems]) => {
      return secondItems.length - firstItems.length;
    });
  
    const getSeverityClassName = (severity) => {
      const normalizedSeverity = (
        severity || "UNKNOWN"
      ).toLowerCase();
  
      return `finding-severity severity-${normalizedSeverity}`;
    };
  
    const formattedReport = useMemo(() => {
      if (!reviewReport) {
        return "";
      }
  
      if (typeof reviewReport === "string") {
        try {
          return JSON.stringify(
            JSON.parse(reviewReport),
            null,
            2
          );
        } catch {
          return reviewReport;
        }
      }
  
      return JSON.stringify(
        reviewReport,
        null,
        2
      );
    }, [reviewReport]);
  
    if (loadingResults) {
      return (
        <div className="review-result">
          <p className="muted-text">
            Loading review results...
          </p>
        </div>
      );
    }
  
    if (
      !summary &&
      !findings &&
      !aiReview &&
      !reviewReport
    ) {
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
                <strong>
                  {summary.critical ?? 0}
                </strong>
              </div>
  
              <div className="summary-item">
                <span>High</span>
                <strong>
                  {summary.high ?? 0}
                </strong>
              </div>
  
              <div className="summary-item">
                <span>Medium</span>
                <strong>
                  {summary.medium ?? 0}
                </strong>
              </div>
  
              <div className="summary-item">
                <span>Low</span>
                <strong>
                  {summary.low ?? 0}
                </strong>
              </div>
  
              <div className="summary-item">
                <span>Info</span>
                <strong>
                  {summary.info ?? 0}
                </strong>
              </div>
            </div>
          </section>
        )}
  
        {aiReview && (
          <section className="result-section">
            <h4>AI Review</h4>
  
            <div className="ai-review-card">
              <h5>Summary</h5>
  
              <p className="ai-summary">
                {aiReview.summary ||
                  "No AI summary available."}
              </p>
            </div>
  
            {Array.isArray(aiReview.strengths) &&
              aiReview.strengths.length > 0 && (
                <div className="ai-review-card ai-list">
                  <h5>Strengths</h5>
  
                  <ul>
                    {aiReview.strengths.map(
                      (strength, index) => (
                        <li
                          key={`strength-${index}`}
                        >
                          {strength}
                        </li>
                      )
                    )}
                  </ul>
                </div>
              )}
  
            {Array.isArray(aiReview.risks) &&
              aiReview.risks.length > 0 && (
                <div className="ai-review-card ai-list">
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
  
            {Array.isArray(
              aiReview.recommendations
            ) &&
              aiReview.recommendations.length >
                0 && (
                <div className="ai-review-card ai-list">
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
  
            {reviewReport && (
              <div className="full-report-actions">
                <button
                  type="button"
                  onClick={() =>
                    setShowFullReport(true)
                  }
                >
                  View Raw JSON
                </button>
              </div>
            )}
          </section>
        )}
  
        {Array.isArray(findings) && (
          <section className="result-section">
            <h4>
              Findings ({safeFindings.length})
            </h4>
  
            {safeFindings.length === 0 ? (
              <p className="muted-text">
                No findings were detected.
              </p>
            ) : (
              <div className="findings-list">
                {findingGroups.map(
                  ([ruleId, groupFindings]) => (
                    <details
                      className="finding-group"
                      key={ruleId}
                    >
                      <summary className="finding-group-summary">
                        <span className="finding-group-title">
                          {ruleId}
                        </span>
  
                        <span className="finding-group-count">
                          {groupFindings.length}
                        </span>
                      </summary>
  
                      <div className="finding-group-content">
                        {groupFindings.map(
                          (finding, index) => (
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
  
                                <span
                                  className={getSeverityClassName(
                                    finding.severity
                                  )}
                                >
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
                                  <strong>
                                    Suggestion:
                                  </strong>{" "}
                                  {
                                    finding.suggestion
                                  }
                                </p>
                              )}
                            </article>
                          )
                        )}
                      </div>
                    </details>
                  )
                )}
              </div>
            )}
          </section>
        )}
  
        {showFullReport && (
          <div
            className="report-modal-backdrop"
            role="presentation"
            onClick={() =>
              setShowFullReport(false)
            }
          >
            <div
              className="report-modal"
              role="dialog"
              aria-modal="true"
              aria-labelledby="full-report-title"
              onClick={(event) =>
                event.stopPropagation()
              }
            >
              <div className="report-modal-header">
                <h4 id="full-report-title">
                  Raw Review JSON
                </h4>
  
                <button
                  type="button"
                  onClick={() =>
                    setShowFullReport(false)
                  }
                  aria-label="Close full report"
                >
                  Close
                </button>
              </div>
  
              <pre className="full-report-json">
                {formattedReport}
              </pre>
            </div>
          </div>
        )}
      </div>
    );
  }
  
  export default ReviewResult;