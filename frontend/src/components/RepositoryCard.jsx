import ReviewResult from "./ReviewResult";

function RepositoryCard({
  repository,
  activeReview,
  startingReview,
  onStartReview,
  summary,
  findings,
  aiReview,
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

  const reviewStatus = activeReview?.status;
  const reviewId = activeReview?.id;

  return (
    <article className="repository-item">
      <div className="repository-header">
        <div>
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
          "No description provided."}
      </p>

      <div className="repository-actions">
        <button
          type="button"
          onClick={() => onStartReview(repository)}
          disabled={isStarting}
        >
          {isStarting ? "Starting..." : "Review"}
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
          <p>
            <strong>Review ID:</strong> {reviewId}
          </p>

          <p>
            <strong>Status:</strong>{" "}
            <span
              className={`status-badge status-${String(
                reviewStatus
              ).toLowerCase()}`}
            >
              {reviewStatus}
            </span>
          </p>
        </div>
      )}

      <ReviewResult
        summary={summary}
        findings={findings}
        aiReview={aiReview}
        loadingResults={loadingResults}
      />
    </article>
  );
}

export default RepositoryCard;