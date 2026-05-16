# Locked In For The Career Center

Locked In For The Career Center is a full-stack Career Center event and volunteer coordination app. It supports account registration and login, role-based event actions, event viewing, volunteer/admin event registration, withdrawal, and admin event creation/deletion.

The current implementation keeps the required stack:

- Frontend: Vite, React, JavaScript
- Backend: Java, Spring Boot
- Database: MongoDB
- Local orchestration: Docker Compose

## Demo URLs

- API: https://lockedinforthecareercenter.onrender.com
- FRONTEND: https://j-j-o-s-h.github.io/LockedInForTheCareerCenter/

## Example API Calls (Direct)

- https://lockedinforthecareercenter.onrender.com/api/events
- https://lockedinforthecareercenter.onrender.com/api/api/auth/me

## Local Setup

### Prerequisites

- Docker Desktop or another Docker Compose compatible runtime
- Optional for non-Docker development:
  - Java 17
  - Maven
  - Node.js 20+
  - MongoDB

### Run With Docker Compose

From the repository root:

```bash
docker compose up --build
```

Then open:

- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api`
- Health endpoint: `http://localhost:8080/api/health`
- MongoDB: `localhost:27017`

To stop:

```bash
docker compose down
```

To reset local MongoDB seed data:

```bash
docker compose down -v
docker compose up --build
```

## Seeded Demo Data

Seed data is inserted only when the relevant MongoDB collection is empty.

Demo users:

| Role | Email | Password |
| --- | --- | --- |
| `VOLUNTEER` | `returninguser@example.com` | `Password123!` |
| `ADMIN` | `admin@example.com` | `Admin123!` |

Demo events:

- Spring Career Fair
- Resume Review Workshop
- Employer Networking Night

## API Summary

Authentication:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

Health:

- `GET /api/health`

Events:

- `GET /api/events`
- `POST /api/events` requires `ADMIN`
- `DELETE /api/events/{eventId}` requires `ADMIN`

Registrations:

- `POST /api/events/{eventId}/registrations` requires `VOLUNTEER` or `ADMIN`
- `DELETE /api/events/{eventId}/registrations/me` requires `VOLUNTEER` or `ADMIN`
- `GET /api/users/me/registrations` requires authentication

## Testing And Validation

Backend tests:

```bash
docker run --rm -v ${PWD}:/app -w /app/backend maven:3.9.6-eclipse-temurin-17 mvn test
```
