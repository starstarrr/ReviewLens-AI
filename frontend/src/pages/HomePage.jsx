import { useEffect, useRef, useState } from "react";

import GitHubCard from "../components/GitHubCard";
import RepositoryList from "../components/RepositoryList";

import {
  getCurrentUser,
  getRepositories,
  redirectToGitHubLogin,
} from "../api/githubApi";

import {
    createReview,
    getReview,
    getReviewSummary,
    getReviewFindings,
    getAiReview,
  } from "../api/reviewApi";

const POLLING_INTERVAL_MS = 2000;

function HomePage() {
  const [user, setUser] = useState(null);
  const [loadingUser, setLoadingUser] = useState(true);

  const [repositories, setRepositories] = useState([]);
  const [loadingRepositories, setLoadingRepositories] =
    useState(false);
  const [repositoryError, setRepositoryError] =
    useState("");

  const [startingReview, setStartingReview] =
    useState("");
  const [reviewError, setReviewError] = useState("");

  const [activeReviews, setActiveReviews] =
    useState({});
    const [reviewSummaries, setReviewSummaries] =
    useState({});

const [reviewFindings, setReviewFindings] =
    useState({});

const [aiReviews, setAiReviews] =
    useState({});

const [loadingResults, setLoadingResults] =
    useState({});

  const pollingTimersRef = useRef({});

  useEffect(() => {
    async function loadCurrentUser() {
      try {
        const currentUser = await getCurrentUser();
        setUser(currentUser);
      } catch {
        setUser(null);
      } finally {
        setLoadingUser(false);
      }
    }

    loadCurrentUser();
  }, []);

  useEffect(() => {
    return () => {
      Object.values(pollingTimersRef.current).forEach(
        (timerId) => {
          clearTimeout(timerId);
        }
      );

      pollingTimersRef.current = {};
    };
  }, []);

  const handleLoadRepositories = async () => {
    setLoadingRepositories(true);
    setRepositoryError("");
    setReviewError("");

    try {
      const repositoryData = await getRepositories();
      setRepositories(repositoryData);
    } catch (error) {
      setRepositoryError(
        error.message || "Failed to load repositories."
      );
    } finally {
      setLoadingRepositories(false);
    }
  };

  const stopPolling = (repositoryKey) => {
    const timerId =
      pollingTimersRef.current[repositoryKey];

    if (timerId) {
      clearTimeout(timerId);
      delete pollingTimersRef.current[repositoryKey];
    }
  };

  const updateActiveReview = (
    repositoryKey,
    review
  ) => {
    setActiveReviews((currentReviews) => ({
      ...currentReviews,
      [repositoryKey]: review,
    }));
  };
  const loadReviewResults = async (
    repositoryKey,
    reviewId
  ) => {
    setLoadingResults((current) => ({
      ...current,
      [repositoryKey]: true,
    }));
  
    try {
      const [
        summaryData,
        findingsData,
        aiReviewData,
      ] = await Promise.all([
        getReviewSummary(reviewId),
        getReviewFindings(reviewId),
        getAiReview(reviewId),
      ]);
  
      setReviewSummaries((current) => ({
        ...current,
        [repositoryKey]: summaryData,
      }));
  
      setReviewFindings((current) => ({
        ...current,
        [repositoryKey]: findingsData,
      }));
  
      setAiReviews((current) => ({
        ...current,
        [repositoryKey]: aiReviewData,
      }));
    } catch (error) {
      setReviewError(
        error.message ||
          "Failed to load review results."
      );
    } finally {
      setLoadingResults((current) => ({
        ...current,
        [repositoryKey]: false,
      }));
    }
  };

  const pollReviewStatus = async (
    repositoryKey,
    reviewId
  ) => {
    try {
      const latestReview = await getReview(reviewId);
  
      updateActiveReview(
        repositoryKey,
        latestReview
      );
  
      const status = String(
        latestReview.status || ""
      ).toUpperCase();
  
      if (status === "COMPLETED") {
        stopPolling(repositoryKey);
  
        await loadReviewResults(
          repositoryKey,
          reviewId
        );
  
        return;
      }
  
      if (status === "FAILED") {
        stopPolling(repositoryKey);
        return;
      }
  
      pollingTimersRef.current[repositoryKey] =
        setTimeout(() => {
          pollReviewStatus(
            repositoryKey,
            reviewId
          );
        }, POLLING_INTERVAL_MS);
    } catch (error) {
      stopPolling(repositoryKey);
  
      setReviewError(
        error.message ||
          "Failed to refresh review status."
      );
    }
  };

  const handleStartReview = async (repository) => {
    const repositoryFullName =
      repository.fullName ||
      repository.full_name ||
      (user?.login && repository.name
        ? `${user.login}/${repository.name}`
        : null);

    if (!repositoryFullName) {
      setReviewError(
        "Unable to determine the repository full name."
      );
      return;
    }

    stopPolling(repositoryFullName);

    setStartingReview(repositoryFullName);
    setReviewError("");

    try {
      const createdReview = await createReview(
        repositoryFullName
      );

      updateActiveReview(
        repositoryFullName,
        createdReview
      );

      pollReviewStatus(
        repositoryFullName,
        createdReview.id
      );
    } catch (error) {
      setReviewError(
        error.message ||
          "Failed to start repository review."
      );
    } finally {
      setStartingReview("");
    }
  };

  return (
    <div className="app">
      <header className="header">
        <div className="header-content">
          <p className="eyebrow">
            Cloud-Native Code Analysis
          </p>

          <h1>ReviewLens AI</h1>

          <p className="header-description">
            AI-Powered GitHub Code Review Platform
          </p>
        </div>
      </header>

      <main className="content">
        <GitHubCard
          user={user}
          loadingUser={loadingUser}
          onLogin={redirectToGitHubLogin}
        />

        <section className="card">
          <div className="section-heading">
            <div>
              <h2>Repositories</h2>

              <p className="muted-text">
                Select one of your GitHub repositories
                and start a code review.
              </p>
            </div>

            <button
              type="button"
              onClick={handleLoadRepositories}
              disabled={
                !user || loadingRepositories
              }
            >
              {loadingRepositories
                ? "Loading..."
                : "Load Repositories"}
            </button>
          </div>

          {!user && !loadingUser && (
            <p className="message message-info">
              Log in with GitHub before loading
              repositories.
            </p>
          )}

          {repositoryError && (
            <p className="message message-error">
              {repositoryError}
            </p>
          )}

          {reviewError && (
            <p className="message message-error">
              {reviewError}
            </p>
          )}

          <RepositoryList
             repositories={repositories}
             activeReviews={activeReviews}
             startingReview={startingReview}
             onStartReview={handleStartReview}
             reviewSummaries={reviewSummaries}
             reviewFindings={reviewFindings}
             aiReviews={aiReviews}
             loadingResults={loadingResults}
          />
        </section>
      </main>
    </div>
  );
}

export default HomePage;
