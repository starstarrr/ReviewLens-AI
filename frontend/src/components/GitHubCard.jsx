function GitHubCard({
    user,
    loadingUser,
    onLogin,
  }) {
    return (
      <section className="card github-card">
        <h2>GitHub</h2>
  
        {loadingUser ? (
          <p className="muted-text">
            Checking login status...
          </p>
        ) : user ? (
          <div className="user-information">
            <p>
              Signed in as{" "}
              <strong>
                {user.login || user.name || "GitHub User"}
              </strong>
            </p>
  
            {user.avatarUrl && (
              <img
                className="github-avatar"
                src={user.avatarUrl}
                alt={`${user.login || "GitHub user"} avatar`}
              />
            )}
          </div>
        ) : (
          <div>
            <p className="muted-text">
              Sign in with GitHub to view and review your
              repositories.
            </p>
  
            <button
              type="button"
              onClick={onLogin}
            >
              Login with GitHub
            </button>
          </div>
        )}
      </section>
    );
  }
  
  export default GitHubCard;