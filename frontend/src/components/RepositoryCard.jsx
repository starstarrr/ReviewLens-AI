import ReviewResult from "./ReviewResult";

function RepositoryCard({
  repository,
  activeReview,
  startingReview,
  onStartReview,
  summary,
  findings,
  aiReview,
  reviewReport,
  loadingResults,
}) {
  const repositoryFullName =
    repository.fullName ||
    repository.full_name ||
    repository.name;

  const repositoryKey = repositoryFullName;

  const repositoryUrl =
    repository.htmlUrl ||
    repository.html_url ||
    repository.url;

  const isStarting =
    startingReview === repositoryKey;

  const reviewStatus = String(
    activeReview?.status || ""
  ).toUpperCase();

  const reviewId = activeReview?.id;

  const isReviewRunning = [
    "QUEUED",
    "CLONING",
    "RUNNING",
  ].includes(reviewStatus);

  const isReviewCompleted =
    reviewStatus === "COMPLETED";

  const reviewButtonText = () => {
    if (isStarting) {
      return "Starting...";
    }

    if (isReviewRunning) {
      return "Review Running...";
    }

    if (isReviewCompleted) {
      return "Run Review Again";
    }

    return "Review Repository";
  };

  return (
    <article className="repository-item">
      <div className="repository-header">
        <div className="repository-title-group">
          <h3>{repository.name}</h3>

          {repositoryFullName !== repository.name && (
            <p className="repository-full-name">
              {repositoryFullName}
            </p>
          )}
        </div>

        <span className="language-badge">
          {repository.language || "Unknown"}
        </span>
      </div>

      <p className="repository-description">
        {repository.description ||
          "No description provided for this repository."}
      </p>

      <div className="repository-actions">
        <button
          type="button"
          onClick={() => onStartReview(repository)}
          disabled={isStarting || isReviewRunning}
        >
          {reviewButtonText()}
        </button>

        {repositoryUrl && (
          <a
            className="secondary-link"
            href={repositoryUrl}
            target="_blank"
            rel="noreferrer"
          >
            Open GitHub
          </a>
        )}
      </div>

      {activeReview && (
        <div className="review-status-panel">
          <div className="review-status-header">
            <div>
              <p className="review-status-label">
                Review #{reviewId}
              </p>

              <p className="review-status-description">
                {isReviewCompleted
                  ? "Repository analysis completed."
                  : reviewStatus === "FAILED"
                    ? "Repository analysis failed."
                    : "Repository analysis is currently in progress."}
              </p>
            </div>

            <span
              className={`status-badge status-${reviewStatus.toLowerCase()}`}
            >
              {reviewStatus || "UNKNOWN"}
            </span>
          </div>

          {isReviewRunning && (
            <div className="review-progress">
              <div className="review-progress-bar" />
            </div>
          )}
        </div>
      )}

      <ReviewResult
        summary={summary}
        findings={findings}
        aiReview={aiReview}
        reviewReport={reviewReport}
        loadingResults={loadingResults}
      />
    </article>
  );
}

export default RepositoryCard;