# AI-Deskhelp MVP Architecture

```mermaid
flowchart LR
    U[User] --> UI[Chat UI]
    UI --> API[Backend API]
    API --> DB[(Database)]
    API -->|question| RAG[RAG Service]
    RAG --> FAQ[FAQ Markdown Files]
    RAG -->|embed & chunk| VS[(Vector Store)]
    RAG -->|retrieve context| VS
    RAG --> LLM[LLM]
    LLM -->|answer + confidence| API
    API -->|AI reply| UI
    API -->|low confidence / no match| TKT[Create Ticket]
    TKT --> DB
    TKT --> HS[Human Support]
    HS -->|human reply| API
    API -->|escalated answer| UI
```

## Components
- **Chat UI**: Web chat front-end handling user sessions and message display.
- **Backend API**: Modular monolith (e.g., FastAPI/Express/Spring) exposing chat and ticket endpoints, coordinating RAG + LLM, enforcing auth/rate limits, logging.
- **RAG Service**: Module that chunks FAQ markdown, creates embeddings, retrieves top-k context for each query, and calls the LLM with retrieved snippets.
- **FAQ Markdown Files**: Editable knowledge base; ingested at startup or on change to refresh embeddings.
- **Vector Store**: Lightweight local embedding index (e.g., SQLite/FAISS-like) used for similarity search.
- **LLM**: Hosted model API that generates answers using provided context and returns confidence/coverage signals.
- **Database**: Relational store for users, chats, messages, retrieval metadata, and tickets.
- **Human Support**: Simple agent/admin console to pick up escalated tickets and post replies.

## Request & Escalation Flow
- **Normal flow**: User asks → Chat UI → Backend API → RAG retrieves FAQ context → LLM generates answer with confidence → API stores message/logs in DB → response returned to Chat UI.
- **Escalation flow**: If retrieval sparse or LLM confidence low → API creates ticket in DB → Human Support console handles it → human reply sent via API → Chat UI shows escalated answer and links ticket status.

## Simple Request Flow

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend
    participant C as Controller
    participant S as Service
    participant R as Repository
    participant DB as Database
    participant JWT as security.jwt

    FE->>JWT: Send request with token
    JWT->>JWT: Validate token
    JWT-->>C: Allow authenticated request
    C->>S: Call service logic
    S->>R: Call repository
    R->>DB: Read / write data
    DB-->>R: Return result
    R-->>S: Return data
    S-->>C: Return response
    C-->>FE: Send response
```




![alt text](<mermaid-diagram (1)-1.png>)

## Architecture

The application follows a simple layered architecture:

- **Frontend / UI** sends requests
- **Controller** handles HTTP endpoints
- **Service** contains business logic
- **Repository** manages database access
- **Database** stores the data

This keeps the project simple, clean, and scalable without overengineering.
