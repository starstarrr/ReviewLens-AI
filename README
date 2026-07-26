# ReviewLens

ReviewLens is an AI-powered code review platform that analyzes GitHub repositories and generates automated review reports using static analysis and Amazon Bedrock.

## Features

- Analyze public GitHub repositories
- Clone repositories automatically
- Perform static code analysis
- Generate AI-powered review reports
- Asynchronous analysis with Dispatcher/Worker architecture
- RESTful API built with Spring Boot
- PostgreSQL database with Flyway migrations
- Docker Compose support

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway
- Docker & Docker Compose
- Maven
- Amazon Bedrock
- GitHub API

## Getting Started

### Prerequisites

- Java 21
- Docker
- Docker Compose

### Run with Docker

```bash
docker compose up --build
```

The application will be available at:

```
http://localhost:8080
```

## Example API

Create a review:

```bash
curl -X POST http://localhost:8080/reviews \
  -H "Content-Type: application/json" \
  -d '{"repositoryUrl":"https://github.com/octocat/Hello-World"}'
```

Check review status:

```bash
curl http://localhost:8080/reviews/1
```

## Project Structure

```
src/main/java/com/reviewlens
├── controller
├── dispatcher
├── worker
├── service
├── repository
├── entity
├── dto
├── config
└── analysis
```

## Future Improvements

- GitHub OAuth
- Pull Request Review
- Amazon SQS
- Amazon S3
- Amazon ECS
- CloudWatch