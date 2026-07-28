const API_BASE_URL = "http://localhost:8080";

export async function createReview(repositoryFullName) {
  console.log(
    "Starting review for repository:",
    repositoryFullName
  );

  const response = await fetch(`${API_BASE_URL}/reviews`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      repositoryFullName,
    }),
  });

  if (!response.ok) {
    const responseText = await response.text();

    console.error("Create review failed:", {
      status: response.status,
      body: responseText,
    });

    throw new Error(
      responseText ||
        `Failed to start review: ${response.status}`
    );
  }

  return response.json();
}

export async function getReview(reviewId) {
  const response = await fetch(
    `${API_BASE_URL}/reviews/${reviewId}`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Failed to load review status.");
  }

  return response.json();
}

export async function getReviewSummary(reviewId) {
  const response = await fetch(
    `${API_BASE_URL}/reviews/${reviewId}/summary`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Failed to load review summary.");
  }

  return response.json();
}

export async function getReviewFindings(reviewId) {
  const response = await fetch(
    `${API_BASE_URL}/reviews/${reviewId}/findings`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Failed to load review findings.");
  }

  return response.json();
}

export async function getAiReview(reviewId) {
  const response = await fetch(
    `${API_BASE_URL}/reviews/${reviewId}/ai-review`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    throw new Error("Failed to load AI review.");
  }

  return response.json();
}