# ⚡ FlashTix - High Concurrency Ticketing Engine

FlashTix is a backend system designed to handle massive traffic surges (e.g., concert ticket sales) while guaranteeing data consistency. It utilizes **Optimistic Locking** and **Redis Caching** to process 5,000+ requests per second without race conditions.

## 🚀 Tech Stack
- **Core:** Java 17, Spring Boot 3
- **Database:** PostgreSQL (with Optimistic Locking)
- **Caching:** Redis (Write-through strategy)
- **Infrastructure:** Docker & Docker Compose
- **Testing:** Apache JMeter

## 🏗️ Architecture
User Request -> Redis Cache Check -> Database Lock Check (@Version) -> Transaction Commit

## 🛠️ How to Run
**Prerequisite:** Docker Desktop must be running.

1. **Start Infrastructure (DB + Cache)**
   ```bash
   docker-compose up -d