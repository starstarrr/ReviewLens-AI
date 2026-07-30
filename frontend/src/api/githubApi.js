const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "";

async function readErrorResponse(
  response,
  fallbackMessage
) {
  const responseText =
    await response.text();

  if (!responseText) {
    return `${fallbackMessage}: ${response.status}`;
  }

  try {
    const errorData =
      JSON.parse(responseText);

    return (
      errorData.message ||
      errorData.error ||
      responseText
    );
  } catch {
    return responseText;
  }
}

export async function getCurrentUser() {
  const response = await fetch(
    `${API_BASE_URL}/api/me`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    const errorMessage =
      await readErrorResponse(
        response,
        "User is not authenticated"
      );

    throw new Error(errorMessage);
  }

  return response.json();
}

export async function getRepositories() {
  const response = await fetch(
    `${API_BASE_URL}/github/repositories`,
    {
      method: "GET",
      credentials: "include",
    }
  );

  if (!response.ok) {
    const errorMessage =
      await readErrorResponse(
        response,
        "Failed to load repositories"
      );

    throw new Error(errorMessage);
  }

  const repositories =
    await response.json();

  return Array.isArray(repositories)
    ? repositories
    : [];
}

export function redirectToGitHubLogin() {
  window.location.assign(
    `${API_BASE_URL}/oauth2/authorization/github`
  );
}