# 🚀 ReviewLens AI

<p align="center">

<h3 align="center">
Cloud-Native AI Code Review Platform
</h3>

<p align="center">
Analyze GitHub repositories with static analysis and AI-powered code review using Spring Boot, AWS, GitHub OAuth, and Amazon Bedrock.
</p>

<p align="center">

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-green)
![React](https://img.shields.io/badge/React-19-61DAFB)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-AWS%20RDS-blue)
![AWS](https://img.shields.io/badge/AWS-Bedrock%20|%20RDS%20|%20S3-FF9900)
![GitHub Actions](https://img.shields.io/badge/CI-GitHub%20Actions-success)
![Render](https://img.shields.io/badge/Deploy-Render-46E3B7)
![License](https://img.shields.io/badge/License-MIT-blue)

</p>

---

# 🌐 Live Demo

Frontend

```
https://YOUR_FRONTEND_URL.onrender.com
```

Backend API

```
https://YOUR_BACKEND_URL.onrender.com
```

---

# 📖 Overview

ReviewLens AI is a cloud-native code review platform that automatically analyzes GitHub repositories and generates AI-powered code review reports.

The platform combines traditional static analysis with Large Language Models to provide meaningful code quality feedback for developers.

### Core Features

- 🤖 AI-powered code review with Amazon Bedrock
- 🔍 Static code analysis
- 🔐 GitHub OAuth2 authentication
- ☁️ Cloud-native AWS integration
- ⚡ Asynchronous repository processing
- 📄 AI review report generation
- 💾 Report storage in Amazon S3

---

# 🏗 Architecture

```text
                     React Frontend
                    (Vite + TypeScript)
                             │
                             ▼
                  Spring Boot REST API
               Spring Security + OAuth2
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
         ▼                   ▼                   ▼
    GitHub API        PostgreSQL (AWS RDS)   Amazon S3
         │
         ▼
 Repository Cloning
         │
         ▼
 Static Code Analysis
         │
         ▼
 Amazon Bedrock
         │
         ▼
 AI Review Report
```

---

# ✨ Features

## 🤖 AI Review

- Amazon Bedrock integration
- AI-generated repository review
- Intelligent improvement suggestions
- Repository summary generation

## 🔍 Static Analysis

- Repository scanning
- Rule-based analysis
- Severity classification
- Code quality findings

## 🔐 Authentication

- GitHub OAuth Login
- Secure OAuth2 authentication
- GitHub Repository API integration

## ☁️ Cloud

- AWS RDS
- Amazon S3
- Amazon Bedrock
- Cloud deployment on Render

---

# 🛠 Tech Stack

| Category | Technologies |
|----------|--------------|
| Backend | Java 21, Spring Boot 4 |
| Frontend | React, TypeScript, Vite |
| Security | Spring Security, OAuth2, GitHub OAuth |
| Database | PostgreSQL (AWS RDS), Spring Data JPA, Hibernate, Flyway |
| AI | Amazon Bedrock |
| Cloud | AWS RDS, Amazon S3, Render |
| DevOps | GitHub Actions CI, Docker |
| Build | Maven |

---

# ⚡ Review Workflow

```text
GitHub Login
      │
      ▼
Select Repository
      │
      ▼
Clone Repository
      │
      ▼
Static Analysis
      │
      ▼
Generate Findings
      │
      ▼
Amazon Bedrock
      │
      ▼
Generate AI Review
      │
      ▼
Store Report in Amazon S3
```

---

# 📂 Project Structure

```text
src
└── main
    ├── java
    │   └── com.reviewlens
    │       ├── analysis
    │       ├── config
    │       ├── controller
    │       ├── dispatcher
    │       ├── dto
    │       ├── entity
    │       ├── repository
    │       ├── service
    │       └── worker
    └── resources
        └── db
            └── migration
```

---

# 📡 REST API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/reviews` | Create a new review |
| GET | `/reviews/{id}` | Review status |
| GET | `/reviews/{id}/findings` | Static analysis findings |
| GET | `/reviews/{id}/summary` | AI review summary |
| GET | `/github/repos/{owner}/{repo}` | GitHub repository metadata |

---

# 🚀 Getting Started

## Clone Repository

```bash
git clone https://github.com/starstarrr/ReviewLens-AI.git

cd ReviewLens-AI
```

## Configure Environment Variables

```properties
DB_URL=
DB_USERNAME=
DB_PASSWORD=

GITHUB_CLIENT_ID=
GITHUB_CLIENT_SECRET=

AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=

AWS_REGION=
AWS_S3_BUCKET_NAME=

BEDROCK_MODEL_ID=
```

## Run Backend

```bash
./mvnw spring-boot:run
```

## Run Frontend

```bash
cd frontend

npm install

npm run dev
```

Backend

```
http://localhost:8080
```

Frontend

```
http://localhost:5173
```

## AI Review Result

> Add screenshot

---

## GitHub Actions CI

> Add screenshot

---

# 🎯 Key Learning Outcomes

This project demonstrates practical experience with:

- Cloud-native application development
- Backend system design using Spring Boot
- RESTful API development
- GitHub OAuth2 authentication
- Amazon Bedrock integration
- AWS RDS & Amazon S3
- Database migration using Flyway
- Asynchronous background processing
- CI pipelines using GitHub Actions
- Cloud deployment with Render

---

# 🚧 Roadmap

- [ ] Pull Request Review
- [ ] Multi-language Static Analysis
- [ ] Amazon SQS Integration
- [ ] AWS ECS / Fargate Deployment
- [ ] CloudWatch Monitoring
- [ ] Redis Cache
- [ ] Repository History Dashboard
- [ ] Team Workspace
- [ ] AI Chat with Repository
- [ ] Multi-LLM Support

---

# 👨‍💻 Author

## Xingran Ma

**B.Sc. Computer Science**  
**The University of British Columbia (UBC)**

Interested in Backend Engineering, Cloud Computing, AI Infrastructure, Distributed Systems, and Applied AI.

GitHub: https://github.com/starstarrr

---

# ⭐ Support

If you found this project helpful, please consider giving it a ⭐ on GitHub.