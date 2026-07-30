import RepositoryCard from "./RepositoryCard";

function RepositoryList({
  repositories,
  activeReviews,
  startingReview,
  onStartReview,
  reviewSummaries,
  reviewFindings,
  aiReviews,
  reviewReports,
  loadingResults,
}) {
  if (repositories.length === 0) {
    return (
      <p className="muted-text">
        No repositories loaded.
      </p>
    );
  }

  return (
    <div className="repository-list">
      {repositories.map((repository) => {
        const repositoryKey =
          repository.fullName ||
          repository.full_name ||
          repository.name;

        return (
          <RepositoryCard
            key={repositoryKey}
            repository={repository}
            activeReview={
              activeReviews[repositoryKey] || null
            }
            startingReview={startingReview}
            onStartReview={onStartReview}
            summary={
              reviewSummaries[repositoryKey] || null
            }
            findings={
              reviewFindings[repositoryKey] || null
            }
            aiReview={
              aiReviews[repositoryKey] || null
            }
            reviewReport={
              reviewReports[repositoryKey] || null
            }
            loadingResults={
              loadingResults[repositoryKey] || false
            }
          />
        );
      })}
    </div>
  );
}

export default RepositoryList;