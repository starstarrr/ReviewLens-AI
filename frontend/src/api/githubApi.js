const API_BASE_URL = "http://localhost:8080";

export async function getCurrentUser() {
  const response = await fetch(`${API_BASE_URL}/api/me`, {
    method: "GET",
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error("User is not authenticated.");
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
    throw new Error("Failed to load repositories.");
  }

  return response.json();
}

export function redirectToGitHubLogin() {
  window.location.href =
    `${API_BASE_URL}/oauth2/authorization/github`;
}