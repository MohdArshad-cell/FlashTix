# ⚡ FlashTix – High-Performance Ticketing Engine

![CI/CD Pipeline](https://github.com/MohdArshad-cell/FlashTix-Backend/actions/workflows/maven.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green)
![React](https://img.shields.io/badge/Frontend-React%20%2B%20TypeScript-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Redis](https://img.shields.io/badge/Redis-Distributed%20Lock-red)
![Grafana](https://img.shields.io/badge/Observability-Grafana%20%2B%20Prometheus-orange)

**FlashTix** is a full-stack, high‑throughput ticketing system designed for flash‑sale‑style traffic while guaranteeing **strict data consistency** and **no double booking**.

The engine uses a **defense‑in‑depth concurrency strategy** so that, even under thousands of concurrent requests, **exactly one user** can successfully book a given seat and all other requests are rejected with a clear `409 Conflict`.

---

## 📊 Proof of Work: Real-Time Observability

FlashTix doesn’t just “run”; it exposes rich metrics so you can **see** the concurrency behavior in real time.

![Grafana Dashboard](assets/high-concurrency-dashboard.png)

| Metric | Result | Description |
| :--- | :--- | :--- |
| **Concurrency Load** | **5000 Threads** | Simulates thousands of users hitting `/book` at the same time. |
| **Success Rate** | **1 Booking** | Only one user obtains the ticket for a given ID. |
| **Rejection Rate** | **N‑1 Conflicts** | All other requests receive HTTP 409 (already booked / sold out). |
| **Data Integrity** | **100%** | No race conditions, lost updates, or duplicate bookings detected. |
| **Throughput** | **~900 req/s+** | Measured during `ApiLoadTest` against a running instance. |

---

## 🏗️ System Architecture: Defense-in-Depth

Booking requests flow through several layers to protect the database and maintain correctness.

![System Architecture](assets/architecture.png)

### 1. Redis Distributed Lock (Gatekeeper Layer)
* **Tech:** Redis with `SETNX` + expiry and atomic Lua scripts.
* **Role:** Per‑ticket mutex. Only one request per `ticketId` can proceed to the critical section at a time.
* **Details:**
    * Uses a **TTL** so locks auto‑expire if a node dies mid‑request.
    * Releases only if the same owner holds the lock (checked via Lua script).
    * Prevents thundering‑herd write load on Postgres.

### 2. Optimistic Locking with JPA (Database Guard)
* **Tech:** JPA `@Version` column on `Ticket`.
* **Role:** Final safety net at the database level.
* **Behavior:**
    * When two transactions try to update the same row, only the first commit succeeds.
    * Later commits see a stale version and fail with an optimistic locking exception, which is translated into HTTP `409 Conflict`.

### 3. Validation & Business Rules
* Checks ticket existence, current status (AVAILABLE / SOLD), and user constraints.
* Returns **meaningful HTTP errors**:
    * `404` if the ticket does not exist.
    * `409` if the ticket is already sold.
    * `500` for unexpected failures with a consistent JSON error envelope.

---

## 💻 Frontend: React Load Test Simulator

FlashTix includes a small React + TypeScript UI to visualize and experiment with concurrency.

**Features:**
* **Seat Grid:** Tickets are loaded from the backend. Colors reflect real‑time status (Green = Available, Red = Booked).
* **Load Test Simulator Panel:**
    * Input a Target Ticket ID and Number of Concurrent Users.
    * Fires concurrent requests to `/api/tickets/book`.
    * Summarizes results (Success vs Conflicts, Duration, Throughput).
* **Error Handling:** CORS configured for dev environment; Toasts display success/conflict messages.

**To run the frontend:**
```bash
cd frontend
npm install
npm start
# React dev server starts on http://localhost:3000

```

*Note: Ensure `.env` points to `REACT_APP_API_URL=http://localhost:8080/api*`

---

## ✅ CI/CD & Testing

FlashTix is wired with a CI pipeline that builds and tests the backend on every push.

**GitHub Actions:**

* Runs `mvn clean test` on Java 17.
* Fails fast if Spring context cannot load or concurrency invariants are violated.

**Backend Tests:**

1. **`BackendApplicationTests`**: Sanity check for context loading.
2. **`TicketConcurrencyTest`**:
* Boots Spring context.
* Fires thousands of concurrent booking calls in‑JVM.
* Asserts exactly one success and correct Ticket Version.


3. **`ApiLoadTest`**:
* Targets a running server over HTTP.
* Logs success/conflict counts matching Grafana behavior.



---

## 🚀 Tech Stack

### Language & Frameworks

* **Java 17**
* **Spring Boot 3.4** (Web, Data JPA, Validation, Actuator)
* **Spring Data Redis**

### Frontend

* **React** + **TypeScript** + **Axios**
* **Tailwind CSS**

### Data & Infrastructure

* **PostgreSQL 15** (HikariCP pool)
* **Redis** (Distributed locking & caching)
* **Docker** & **Docker Compose** (Infrastructure orchestration)

### Observability

* **Spring Boot Actuator** + **Micrometer**
* **Prometheus** (Metrics scraper)
* **Grafana** (Dashboards for Lock Contention & DB Pool)

---

## 🛠️ How to Run Locally

### 1. Start Infrastructure (Docker)

From the project root:

```bash
cd backend
docker compose up -d
# Starts Postgres, Redis, Prometheus, Grafana

```

### 2. Run the Backend

```bash
cd backend
mvn spring-boot:run
# Backend starts on http://localhost:8080

```

### 3. Run the Frontend

```bash
cd frontend
npm install
npm start
# Frontend starts on http://localhost:3000

```

---

## 🧪 Running Stress Tests

### 1. Seed the Database

Creates 100 fresh tickets (ID 1..100):

```bash
curl -X POST http://localhost:8080/api/tickets/seed

```

### 2. Backend Load Test (Code)

From the `backend` directory:

```bash
# API-level load test
mvn test -Dtest=ApiLoadTest

# Concurrency test using Spring context
mvn test -Dtest=TicketConcurrencyTest

```

### 3. Frontend Load Test (UI)

1. Open http://localhost:3000.
2. Use the **Load Test Simulator** panel.
3. Set a Ticket ID (e.g., `50`) and Users (e.g., `500`).
4. Click **Start Load Test** and watch the seat turn red!

---

## 👨‍💻 Author

**Mohd Arshad**

* Backend Engineer
* [LinkedIn Profile](https://www.google.com/search?q=%23)

```

```
