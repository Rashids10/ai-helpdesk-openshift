# AI-Deskhelp

[![Project Status](https://img.shields.io/badge/status-MVP-blue)](#)
[![Tech Stack](https://img.shields.io/badge/stack-Angular%20%7C%20Spring%20Boot%20%7C%20PostgreSQL%20pgvector%20%7C%20Ollama%20%7C%20OpenShift-lightgrey)](#)

AI-Deskhelp is an AI-powered IT helpdesk MVP. Users can sign up, log in, create support tickets, view their ticket dashboard, and ask IT questions through a RAG assistant.

The RAG assistant reads a Markdown knowledge base, chunks it, creates embeddings with Ollama, and stores those embeddings persistently in PostgreSQL with pgvector.

## Features

- Angular frontend with login, signup, dashboard, ticket creation, and AI assistant pages.
- JWT-based authentication with Spring Security.
- Logged-in username is loaded from the backend and shown across the app.
- Ticket creation and ticket overview for the authenticated user.
- RAG endpoint for IT support questions.
- Persistent vector search with PostgreSQL + pgvector.
- One-time knowledge-base import through `KnowledgeBaseImporter`.
- OpenShift deployment files for frontend, backend, PostgreSQL/pgvector, Ollama, services, and routes.

## Tech Stack

<p align="center">
  <img src="https://cdn.simpleicons.org/angular/DD0031" alt="Angular" title="Angular" width="52" height="52" />
  <img src="https://cdn.simpleicons.org/typescript/3178C6" alt="TypeScript" title="TypeScript" width="52" height="52" />
  <img src="https://cdn.simpleicons.org/reactivex/B7178C" alt="RxJS" title="RxJS" width="52" height="52" />
  <img src="https://cdn.simpleicons.org/openjdk/FFFFFF" alt="Java" title="Java" width="52" height="52" />
  <img src="https://cdn.simpleicons.org/spring/6DB33F" alt="Spring Boot" title="Spring Boot" width="52" height="52" />
  <img src="https://cdn.simpleicons.org/postgresql/4169E1" alt="PostgreSQL and pgvector" title="PostgreSQL and pgvector" width="52" height="52" />
  <img src="https://cdn.simpleicons.org/ollama/FFFFFF" alt="Ollama" title="Ollama" width="52" height="52" />
  <img src="https://cdn.simpleicons.org/docker/2496ED" alt="Docker" title="Docker" width="52" height="52" />
  <img src="https://cdn.simpleicons.org/redhatopenshift/EE0000" alt="OpenShift" title="OpenShift" width="52" height="52" />
</p>

| Layer | Technology |
| --- | --- |
| Frontend | Angular 21, TypeScript, RxJS, Signals |
| Backend | Java 21, Spring Boot 3.5, Spring Security, Spring AI |
| Database | PostgreSQL 15 with pgvector |
| RAG / AI | Ollama, Spring AI `PgVectorStore` |
| Persistence | JPA, Flyway, PostgreSQL |
| Containerization | Docker |
| Platform | OpenShift |

## Architecture

```text
Browser
  |
  v
OpenShift Route
  |
  +-- /      -> Angular frontend
  +-- /api   -> Spring Boot REST API
  +-- /rag   -> Spring Boot RAG API
                 |
                 +-- PostgreSQL + pgvector
                 +-- Ollama
```

## RAG Flow

1. `KnowledgeBaseImporter` loads `backend/src/main/resources/docs/rag-knowledge-base.md`.
2. The file is split into Markdown sections.
3. Each section receives deterministic metadata including a content hash.
4. Spring AI creates embeddings through Ollama.
5. Chunks and embeddings are stored in `public.vector_store`.
6. Duplicate imports are prevented by checking document id and content hash.
7. `/rag/ask?query=...` searches pgvector and returns the best matching answer.

## Important Backend Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/auth/signup` | Create account |
| `POST` | `/api/auth/login` | Login and receive JWT |
| `GET` | `/api/auth/logged-in-username` | Return current username |
| `POST` | `/api/ticket/createTicket` | Create ticket |
| `GET` | `/api/ticket/my-tickets` | List current user's tickets |
| `POST` | `/rag/ask?query=...` | Ask RAG assistant |

## Local Development

### Prerequisites

- Java 21
- Node.js 22
- npm 10
- Docker
- PostgreSQL with pgvector or the provided pgvector image
- Ollama with the configured chat and embedding models

### Backend

The backend expects these environment variables:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=ai_deskhelp
export POSTGRES_USER=rashid
export DB_PASSWORD=your_password
export JWT_SECRET=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=
export JWT_EXPIRATION_MS=86400000
export OLLAMA_BASE_URL=http://localhost:11434
export OLLAMA_CHAT_MODEL=qwen2.5:0.5b
export OLLAMA_EMBEDDING_MODEL=nomic-embed-text
export SPRING_AI_VECTORSTORE_PGVECTOR_DIMENSIONS=768
```

Start backend:

```bash
cd backend
./mvnw spring-boot:run
```

Useful URLs:

```text
http://localhost:8089/swagger-ui/index.html
http://localhost:8089/v3/api-docs
```

### Frontend

```bash
cd ai-deskhelp-frontend
npm install
npm start
```

The frontend uses:

```ts
apiBaseUrl: '/api'
ragBaseUrl: '/rag'
```

In OpenShift these paths are routed through OpenShift Routes.

## OpenShift Deployment

Current image tags in the deployment manifests:

| Component | Image |
| --- | --- |
| Backend | `ghcr.io/rashids10/backend-ai-deskhelp:v8` |
| Frontend | `ghcr.io/rashids10/frontend-ai-deskhelp:v6` |
| Database | `ghcr.io/rashids10/ai-deskhelp-postgres-pgvector:v1` |

### Build and Push Backend

```bash
BACKEND_VERSION=v8

docker build --platform linux/amd64 \
  -t ghcr.io/rashids10/backend-ai-deskhelp:$BACKEND_VERSION \
  -f backend/backend-docker/Dockerfile .

docker push ghcr.io/rashids10/backend-ai-deskhelp:$BACKEND_VERSION
```

### Build and Push Frontend

```bash
FRONTEND_VERSION=v6

docker build --platform linux/amd64 \
  -t ghcr.io/rashids10/frontend-ai-deskhelp:$FRONTEND_VERSION \
  -f ai-deskhelp-frontend/frontend-Docker-file/Dockerfile .

docker push ghcr.io/rashids10/frontend-ai-deskhelp:$FRONTEND_VERSION
```

### Build and Push PostgreSQL pgvector Image

```bash
DB_VERSION=v1

docker build --platform linux/amd64 \
  -t ghcr.io/rashids10/ai-deskhelp-postgres-pgvector:$DB_VERSION \
  -f infra/postgres-pgvector/Dockerfile infra/postgres-pgvector

docker push ghcr.io/rashids10/ai-deskhelp-postgres-pgvector:$DB_VERSION
```

### Apply OpenShift Manifests

```bash
oc apply -f infra/openshift/db-deployment.yaml
oc apply -f infra/openshift/backend-deployment.yaml
oc apply -f infra/openshift/frontend-deployment.yaml
oc apply -f infra/openshift/ollama-deyployment.yaml
oc apply -f infra/openshift/route.yaml
```

Wait for rollouts:

```bash
oc rollout status deployment/ai-deskhelp-db
oc rollout status deployment/backend-spring-app
oc rollout status deployment/frontend-ai-deskhelp-app
```

## Verify Deployment

### Check Pods

```bash
oc get pods
```

### Check Routes

```bash
oc get routes
```

### Test RAG Through Route

Replace the host with the host from `oc get routes`:

```bash
curl -k -X POST \
  "https://<ROUTE_HOST>/rag/ask?query=Mein%20VPN%20funktioniert%20nicht"
```

### Check pgvector Table

```bash
oc exec -it deployment/ai-deskhelp-db -c ai-deskhelp-db -- \
  psql -U rashid -d ai_deskhelp -c "select count(*) from vector_store;"
```

Expected: the knowledge base chunks are stored in `vector_store`.

## Database Notes

Spring AI creates and uses the pgvector table:

```text
public.vector_store
```

Application tables such as users, tickets, and comments are managed through the app schema and Flyway/JPA configuration.

The PostgreSQL image must include the pgvector extension. This project uses:

```text
infra/postgres-pgvector/Dockerfile
```

## Troubleshooting

### `/api/auth/logged-in-username` returns 500

This endpoint must be authenticated. Login/signup are public, but `/api/auth/logged-in-username` needs a valid JWT. If the JWT filter skips all `/api/auth/**`, the principal is null and the endpoint fails. The current configuration only skips:

```text
/api/auth/login
/api/auth/signup
```

### RAG works in terminal but frontend stays on loading

The frontend uses Angular Signals for async state in Dashboard, Tickets, and AI Assistant. If the UI still looks stale after deployment, hard refresh the browser:

```text
Cmd + Shift + R
```

Also verify that OpenShift pulled the newest image tag and that `imagePullPolicy: Always` is set.

### pgvector dimension error

If the embedding model is `nomic-embed-text`, set:

```text
SPRING_AI_VECTORSTORE_PGVECTOR_DIMENSIONS=768
```

If the table was created with a wrong dimension, drop and recreate `vector_store` or recreate the database PVC if data is disposable.

## Project Structure

```text
ai-deskhelp-frontend/           Angular frontend
backend/                        Spring Boot backend
backend/src/main/resources/docs/rag-knowledge-base.md
infra/openshift/                OpenShift manifests
infra/postgres-pgvector/        PostgreSQL pgvector image
```

## Example Use Case

User asks:

```text
Mein VPN funktioniert nicht.
```

Flow:

```text
Frontend -> /rag/ask -> PgVectorStore similarity search -> Ollama -> answer returned to user
```

## Database Schema

![Database schema](AI-Deskhelp-Database-scheme.png)
