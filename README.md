# AI-Deskhelp

[![Project Status](https://img.shields.io/badge/status-MVP-blue)](#)
[![Tech Stack](https://img.shields.io/badge/stack-Angular%20%7C%20SpringBoot%20%7C%20PostgreSQL%20%7C%20Docker%20%7C%20Kubernetes%20%7C%20Ollama-lightgrey)](#)

AI-Deskhelp is a lightweight, AI-powered IT helpdesk that answers user questions from FAQ markdown files via RAG, and escalates unclear cases to human support.

## Features

- Chat UI for users to ask IT support questions.
- Retrieval-Augmented Generation over FAQ markdown files.
...

## Why this project?
- Practice a practical AI product: combines RAG + LLM + human-in-the-loop.
- Learn end-to-end delivery: frontend, backend orchestration, vector search, and persistence.
- Show real-world relevance: mirrors how support teams blend AI self-service with human escalation.

## Architecture Overview
See `docs/architecture.md` for the Mermaid diagram and detailed component notes. The design uses a modular monolith backend to keep the MVP simple and buildable.

## 🚀 Tech Stack

| Layer            | Technology                                |
| ---------------- | ----------------------------------------- |
| Frontend         | Angular 20, TypeScript                    |
| Backend          | Spring Boot 3, Spring Security, Spring AI |
| Database         | PostgreSQL                                |
| AI / RAG         | Ollama, Spring AI, Vector Store           |
| Containerization | Docker, Docker Compose                    |
| Orchestration    | Kubernetes (Minikube)                     |
| Networking       | NGINX, NGINX Ingress Controller           |
| CI/CD            | GitHub Actions *(planned)*                |

### Technologies Used

<p align="center">
  <img src="https://skillicons.dev/icons?i=angular,spring,postgres,docker,kubernetes,githubactions" />
</p>

### Architecture

```text
Browser
   │
   ▼
NGINX Ingress
   │
   ├── Angular Frontend
   │
   └── Spring Boot Backend
            │
            ├── PostgreSQL
            │
            └── Ollama (LLM)
```



## How It Works (happy path and fallback)
1. User asks a question in the Chat UI.
2. Backend API stores the message and calls the RAG module.
3. RAG retrieves top FAQ chunks from the vector store.
4. LLM generates an answer using the retrieved context and returns confidence.
5. If confident: API sends the AI reply to the user and logs it.


## Setup

```bash
git clone https://github.com/Rashids10/AI-Deskhelp.git && cd ai-helpdesk
```


## Start Spring Boot Backend

### Prerequisites
- Java 21


### Run the backend
```bash
cd backend
./mvnw spring-boot:run

```

##  Backend (example with FastAPI) -> please ignore  this step now
```bash

#still in progress ,so now please ignore  this step now
```

## Docker Starten

```bash
cd infra/docker-compose&&docker compose up
```

## Spring-boot-App starten

```bash

cd backend
./mvnw spring-boot:run

```

### API-Testen
```bash
http://localhost:8089/swagger-ui/index.html

```

## API-Docs

```bash
http://localhost:8089/v3/api-docs
```


```bash
# 3) Frontend
npm install
npm run dev
```

## Example Use Case
- Scenario: User asks, “How do I reset my VPN password?”
- Flow: Chat UI → Backend → RAG retrieves VPN FAQ snippet → LLM crafts answer → User sees steps. If no VPN entry exists, a ticket is opened for IT to respond.

## Future Improvements
- Add source citations and confidence scores in the UI.
- Implement feedback buttons (thumbs up/down) to improve responses.
- Auto-sync FAQ markdown from a repo or CMS.
- Add lightweight analytics (top queries, deflection rate).
- Role-based access for agents vs. end-users.




## Database-Scheme of the App


![alt text](AI-Deskhelp-Database-scheme.png)
