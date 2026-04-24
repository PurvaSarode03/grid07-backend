# Grid07 Backend - Spring Boot Microservice

A robust, high-performance Spring Boot microservice that acts as a central API gateway and guardrail system for managing bot interactions on a social media platform.

## Tech Stack
- Java 21
- Spring Boot 3.4.4
- PostgreSQL 15 (via Docker)
- Redis 7 (via Docker)
- Spring Data JPA / Hibernate
- Spring Data Redis

## Project Structure
src/main/java/com/purva/grid07/
├── entity/          → User, Bot, Post, Comment
├── repository/      → JPA repositories
├── service/         → PostService, RedisGuardrailService, NotificationService
├── controller/      → PostController
└── scheduler/       → NotificationSweeper (CRON job)

## Prerequisites
- Java 21
- Docker Desktop
- Maven

## How to Run

### 1. Start Docker containers
docker-compose up -d

### 2. Run the application
Run from IntelliJ with VM option: -Duser.timezone=UTC

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/posts | Create a new post |
| POST | /api/posts/{postId}/like | Like a post |
| POST | /api/posts/{postId}/comments | Add a comment |
| GET | /api/posts/{postId}/virality | Get virality score |

Add Bot Comment:
POST /api/posts/{postId}/comments?isBot=true&botId={botId}&postAuthorId={userId}

## Redis Guardrails

### How Thread Safety is Guaranteed for Atomic Locks

1. Horizontal Cap (Max 100 bot replies per post)
   Redis INCR command is atomic — reads and increments in a single operation. Even if 200 concurrent requests hit simultaneously, Redis processes them one at a time internally. This guarantees the counter stops at exactly 100, never 101.
   Key used: post:{id}:bot_count

2. Vertical Cap (Max depth 20)
   Simple integer check on depthLevel field before saving to database. If depthLevel > 20, request is rejected with HTTP 429.

3. Cooldown Cap (Bot cannot interact with same user within 10 minutes)
   Redis SET with TTL creates a key that auto-expires after 10 minutes. The EXISTS check is atomic so no race condition is possible.
   Key used: cooldown:bot_{id}:human_{id}

### Why Redis and not Java?
Using Java HashMap or static variables would fail under concurrent requests because multiple threads could read/write simultaneously. Redis operations are single-threaded and atomic, making it the perfect tool for distributed state management.

## Notification Engine
- Bot interactions trigger notifications
- If user was notified in last 15 minutes, notification is queued in Redis List
- If not, notification sent immediately and 15 minute cooldown is set
- CRON job runs every 5 minutes, sweeps all pending notifications, summarizes and logs them

## Postman Collection
Import postman_collection.json to test all endpoints.
