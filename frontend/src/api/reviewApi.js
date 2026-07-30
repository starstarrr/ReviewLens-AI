const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "";

async function readErrorResponse(
  response,
  fallbackMessage
) {
  const responseText = await response.text();

  if (!responseText) {
    return `${fallbackMessage}: ${response.status}`;
  }

  try {
    const errorData = JSON.parse(responseText);

    return (
      errorData.message ||
      errorData.error ||
      responseText
    );
  } catch {
    return responseText;
  }
}

export async function createReview(
  repositoryFullName
) {
  console.log(
    "Starting review for repository:",
    repositoryFullName
  );

  const response = await fetch(
    `${API_BASE_URL}/reviews`,
    {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        repositoryFullName,
      }),
    }
  );

  if (!response.ok) {
    const errorMessage =
      await readErrorResponse(
        response,
        "Failed to start review"
      );

    console.error("Create review failed:", {
      status: response.status,
      message: errorMessage,
    });

    throw new Error(errorMessage);
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
    const errorMessage =
      await readErrorResponse(
        response,
        "Failed to load review status"
      );

    throw new Error(errorMessage);
  }

  return response.json();
}

export async function getReviewSummary(
  reviewId
) {
  const response = await fetch(
    `${API_BASE_URL}/reviews/${reviewId}/summary`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    const errorMessage =
      await readErrorResponse(
        response,
        "Failed to load review summary"
      );

    throw new Error(errorMessage);
  }

  return response.json();
}

export async function getReviewFindings(
  reviewId
) {
  const response = await fetch(
    `${API_BASE_URL}/reviews/${reviewId}/findings`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    const errorMessage =
      await readErrorResponse(
        response,
        "Failed to load review findings"
      );

    throw new Error(errorMessage);
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
    const errorMessage =
      await readErrorResponse(
        response,
        "Failed to load AI review"
      );

    throw new Error(errorMessage);
  }

  return response.json();
}

export async function getReviewReport(
  reviewId
) {
  const response = await fetch(
    `${API_BASE_URL}/reviews/${reviewId}/report`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    const errorMessage =
      await readErrorResponse(
        response,
        "Failed to load full review report"
      );

    throw new Error(errorMessage);
  }

  const responseText = await response.text();

  if (!responseText) {
    return null;
  }

  try {
    return JSON.parse(responseText);
  } catch {
    return responseText;
  }
}