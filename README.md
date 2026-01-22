# ⚡ FlashTix – High-Performance Ticketing Engine

![CI/CD Pipeline](https://github.com/MohdArshad-cell/FlashTix-Backend/actions/workflows/maven.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Redis](https://img.shields.io/badge/Redis-Distributed%20Lock-red)
![Grafana](https://img.shields.io/badge/Observability-Grafana%20%2B%20Prometheus-orange)

**FlashTix** is a pure backend concurrency engine designed to handle massive traffic spikes (e.g., flash sales) without data corruption.

Unlike typical CRUD apps, FlashTix implements a **Defense-in-Depth** locking strategy to guarantee that **exactly one user** secures a seat, even when 5,000+ concurrent requests hit the same record simultaneously.

---

## 🏗️ System Architecture

The system uses a multi-layered approach to protect the database from race conditions.

![System Architecture](assets/architecture.png)

### 1. The Gatekeeper: Redis Distributed Lock
* **Mechanism:** `SETNX` (Atomic Set-if-Not-Exists) with a 10-second TTL.
* **Role:** High-speed mutex. It rejects ~99% of conflicting traffic in-memory before it ever touches the database.
* **Resilience:** Locks auto-expire to prevent deadlocks if a service instance crashes.

### 2. The Safety Net: Optimistic Locking (PostgreSQL)
* **Mechanism:** JPA `@Version` column.
* **Role:** Final consistency check. If two requests somehow bypass Redis (e.g., lock expiry edge case), the database rejects the second commit with an `ObjectOptimisticLockingFailureException`.

---

## 📊 Proof of Work: Real-Time Observability

I integrated **Prometheus** and **Grafana** to visualize the system's behavior under stress.

![Grafana Dashboard](assets/high-concurrency-dashboard.png)

| Metric | Result | Description |
| :--- | :--- | :--- |
| **Concurrency Load** | **5,000 Threads** | Simulates a "Thundering Herd" on a single ticket ID. |
| **Success Rate** | **1 Booking** | Exactly one user receives HTTP 200 OK. |
| **Rejection Rate** | **4,999 Conflicts** | All other requests fail with HTTP 409 Conflict. |
| **Data Integrity** | **100%** | Zero lost updates or double bookings. |
| **Throughput** | **~900 req/s** | Sustained write throughput on local hardware. |

---

## 🚀 Tech Stack

* **Core:** Java 17, Spring Boot 3.4 (Web, Data JPA, Actuator)
* **Database:** PostgreSQL 15 (HikariCP Connection Pooling)
* **Caching & Locking:** Redis (Spring Data Redis)
* **Observability:** Prometheus, Grafana, Micrometer
* **Testing:** JUnit 5, Mockito, Testcontainers
* **Containerization:** Docker, Docker Compose

---

## 🛠️ How to Run

### 1. Start Infrastructure
Run the database and monitoring stack in the background:
```bash
cd backend
docker compose up -d
# Starts Postgres, Redis, Prometheus, Grafana

```

### 2. Start the Backend Server

```bash
cd backend
mvn spring-boot:run
# Server starts at http://localhost:8080

```

### 3. Test the API

**Step 1: Seed Data (Create 100 Tickets)**

```bash
curl -X POST http://localhost:8080/api/tickets/seed

```

**Step 2: Attempt to Book a Ticket**

```bash
curl -X POST "http://localhost:8080/api/tickets/book?ticketId=1&userId=101"

```

* **Response:** `200 OK` (Booking Successful) or `409 Conflict` (Already Booked).

---

## 🧪 Testing Strategy

The system is validated using **three layers of automated testing**:

1. **`TicketConcurrencyTest` (The Stress Test):**
* Uses a thread pool of **5,000 virtual users**.
* Hammers the `bookTicket()` method concurrently.
* **Asserts:** Exactly 1 success, 4999 failures, and correct final DB state.


2. **`ApiLoadTest`:**
* Runs against the live HTTP server to measure end-to-end latency and throughput.


3. **CI/CD Pipeline (GitHub Actions):**
* Automatically builds and runs tests on every commit.
* Uses a real PostgreSQL service container (no H2) to ensure production parity.



---

## 👨‍💻 Author

**Mohd Arshad**

* Backend Engineer | System Design Enthusiast
* [LinkedIn Profile](https://www.linkedin.com/in/mohd-arshad-156227314/) | [GitHub Profile](https://github.com/MohdArshad-cell)
