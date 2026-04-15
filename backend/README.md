# 🧭 CoreCompass — Personal Life OS

> **One app. Every life metric. Total clarity.**
> 
> A zero-cost, cloud-native Personal Life OS that unifies fitness tracking, financial management, habit building, goal setting, and productivity — built on a microservices architecture.

---

## 🏗️ Architecture Overview

```
React PWA (Vercel — Free)
         │
         │ HTTPS
         ▼
┌─────────────────────────────────────────────┐
│  API Gateway  :8080                         │
│  JWT Validation · Rate Limiting · CORS      │
└──────┬──────┬──────┬──────┬──────┬──────────┘
       │      │      │      │      │
  ┌────▼──┐ ┌─▼───┐ ┌▼────┐ ┌▼─────┐ ┌▼──────┐ ┌▼──────┐
  │ Auth  │ │Core │ │Fit  │ │Fin   │ │Habits│ │Report│
  │ :8081 │ │:8082│ │:8083│ │:8084 │ │:8085 │ │:8086 │
  └───────┘ └─────┘ └─────┘ └──────┘ └──────┘ └──────┘
         │      │      │      │      │      │
         └──────┴──────┴──────┴──────┴──────┘
                          │
                ┌─────────▼──────────┐
                │  Eureka  :8761      │
                │  Service Discovery  │
                └────────────────────┘
                          │
                ┌─────────▼──────────┐
                │  PostgreSQL         │
                │  Supabase Free Tier │
                │  (6 isolated schemas)│
                └────────────────────┘
```

### Services at a Glance

| Service | Port | Owns |
|---|---|---|
| **Eureka Server** | 8761 | Service registry & discovery |
| **API Gateway** | 8080 | JWT validation, routing, rate limiting, CORS |
| **Auth Service** | 8081 | Users, JWT, Google OAuth2, refresh tokens |
| **Core Service** | 8082 | Goals, Todos, Activities, Dashboard, Notifications |
| **Fitness Service** | 8083 | Workouts, Cardio, Nutrition, Body Metrics, Sleep, Mood |
| **Finance Service** | 8084 | Expenses, Budgets, Income, Savings, Debts, Investments |
| **Habits Service** | 8085 | Habits, Check-ins, Streaks, Stacks, Routines |
| **Report Service** | 8086 | Weekly/Monthly reports, AI-style insights (cron) |

---

## ✅ Prerequisites

| Tool | Version | Install |
|---|---|---|
| Java | 21 (Eclipse Temurin) | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org |
| Docker | 24+ | https://docker.com |
| Docker Compose | v2 | Included with Docker Desktop |
| Git | any | https://git-scm.com |

Verify installations:
```bash
java -version        # openjdk 21...
mvn -version         # Apache Maven 3.9...
docker --version     # Docker version 24...
docker compose version  # Docker Compose version v2...
```

---

## ⚡ Quickstart (Docker — Recommended)

### 1. Clone the repo
```bash
git clone https://github.com/yourname/corecompass.git
cd corecompass
```

### 2. Set up Supabase (free PostgreSQL)
1. Go to https://supabase.com → Create new project
2. Go to **Settings → Database → Connection string → URI**
3. Copy the connection string — it looks like:
   ```
   postgresql://postgres:[YOUR-PASSWORD]@db.xxxx.supabase.co:5432/postgres
   ```

### 3. Configure environment variables
```bash
cd backend
cp .env.example .env
```

Edit `.env`:
```env
# REQUIRED — change all of these!
DATABASE_URL=jdbc:postgresql://db.xxxx.supabase.co:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-supabase-db-password

JWT_SECRET=replace-this-with-64-char-random-string-minimum-256-bits

# Optional for Google login
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret

# Optional for Google Calendar sync  
GOOGLE_CALENDAR_ENABLED=false
```

> **Generate a strong JWT secret:**
> ```bash
> openssl rand -base64 64
> ```

### 4. Start all 8 services
```bash
cd backend
docker compose up --build
```

First build takes ~5 minutes (downloads images, compiles Java). After that it's fast.

Watch for this log line in each service — it means it's ready:
```
Started AuthServiceApplication in 12.3 seconds
```

### 5. Verify everything is running
```bash
# All 8 services should show "UP"
curl http://localhost:8080/actuator/health
curl http://localhost:8761/actuator/health

# Eureka dashboard (shows all registered services)
open http://localhost:8761
# Login: admin / corecompass2026
```

---

## 🛠️ Option B — Run with Maven (Local Dev, No Docker)

Run each service in a separate terminal. **Start in this exact order.**

### Terminal 1 — Eureka Server
```bash
cd backend/eureka-server
mvn spring-boot:run
# Wait for: "Started EurekaServerApplication"
# Dashboard → http://localhost:8761
```

### Terminal 2 — API Gateway
```bash
cd backend/api-gateway
export JWT_SECRET="your-jwt-secret-here"
mvn spring-boot:run
# Wait for: "Started ApiGatewayApplication"
```

### Terminal 3 — Auth Service
```bash
cd backend/auth-service
export DATABASE_URL="jdbc:postgresql://db.xxxx.supabase.co:5432/postgres"
export DATABASE_USERNAME="postgres"
export DATABASE_PASSWORD="your-password"
export JWT_SECRET="your-jwt-secret-here"
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
mvn spring-boot:run
```

### Terminal 4 — Core Service
```bash
cd backend/core-service
export DATABASE_URL="jdbc:postgresql://db.xxxx.supabase.co:5432/postgres"
export DATABASE_USERNAME="postgres"
export DATABASE_PASSWORD="your-password"
mvn spring-boot:run
```

### Terminals 5, 6, 7, 8 — Remaining services
Repeat the same pattern for `fitness-service`, `finance-service`, `habits-service`, `report-service`.

> **Tip:** Create a shell script to start all services:
> ```bash
> # start-all.sh
> source .env
> cd eureka-server  && mvn spring-boot:run &
> sleep 15
> cd ../api-gateway  && mvn spring-boot:run &
> sleep 10
> cd ../auth-service && mvn spring-boot:run &
> cd ../core-service && mvn spring-boot:run &
> # ... etc
> ```

---

## 🔑 Environment Variables Reference

| Variable | Required | Default                                               | Description |
|---|---|-------------------------------------------------------|---|
| `DATABASE_URL` | ✅ | —                                                     | Supabase JDBC URL |
| `DATABASE_USERNAME` | ✅ | `postgres`                                            | DB username |
| `DATABASE_PASSWORD` | ✅ | —                                                     | DB password |
| `JWT_SECRET` | ✅ | —                                                     | Min 64 chars / 256 bits |
| `JWT_ACCESS_EXPIRY` | ❌ | `900`                                                 | Access token TTL (seconds) |
| `JWT_REFRESH_EXPIRY` | ❌ | `604800`                                              | Refresh token TTL (7 days) |
| `GOOGLE_CLIENT_ID` | ❌ | —                                                     | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | ❌ | —                                                     | Google OAuth2 client secret |
| `GOOGLE_CALENDAR_ENABLED` | ❌ | `false`                                               | Enable Google Calendar sync |
| `FRONTEND_URL` | ❌ | `http://localhost:5173`                               | Frontend URL for OAuth2 redirect |
| `EUREKA_URL` | ❌ | `http://admin:corecompass2026@localhost:8761/eureka/` | Eureka URL |
| `EUREKA_USERNAME` | ❌ | `admin`                                               | Eureka dashboard username |
| `EUREKA_PASSWORD` | ❌ | `corecompass2026`                                     | Eureka dashboard password |

---

## 🗄️ Database Setup

**Flyway auto-runs migrations on startup.** You don't need to create tables manually.

Migration order (runs automatically):
```
V1 — auth_schema        (users, refresh_tokens)
V2 — core_schema        (goals, todos, activities, notifications)
V3 — fitness_schema     (cardio, workouts, meals, sleep, mood, hydration)
V4 — finance_schema     (expenses, income, budgets, savings, debts, investments)
V5 — habits_schema      (habits, check-ins, stacks, routines)
V6 — report_schema      (weekly_reports, monthly_reports)
V7 — type_registry      (notifications, user_preferences)
V8 — indexes            (performance indexes per LLD)
```

If migration fails (e.g., Supabase connection issue), check:
```bash
# Test your DB connection
psql "postgresql://postgres:PASSWORD@db.xxxx.supabase.co:5432/postgres" -c "SELECT version();"
```

---

## 📡 API Quick Reference

**Base URL:** `http://localhost:8080/api/v1`

All protected endpoints require: `Authorization: Bearer <access_token>`

### Auth
```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Password123!","name":"Test User"}'

# Login → get access token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"Password123!"}'

# Get profile
curl http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Refresh token (from HttpOnly cookie)
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -c cookies.txt -b cookies.txt
```

### Goals (Core Service)
```bash
TOKEN="your-access-token"

# List goal categories (Type Registry)
curl http://localhost:8080/api/v1/goals/types -H "Authorization: Bearer $TOKEN"

# Create a goal
curl -X POST http://localhost:8080/api/v1/goals \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Run 5K","categoryTypeId":"uuid","targetDate":"2026-12-31"}'

# List goals
curl "http://localhost:8080/api/v1/goals?status=ACTIVE&page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Create todo under a goal
curl -X POST http://localhost:8080/api/v1/goals/{goalId}/todos \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Run 2K today","dueDate":"2026-06-15","dueTime":"07:00"}'

# Toggle todo complete (auto-updates goal progress%)
curl -X PATCH http://localhost:8080/api/v1/goals/{goalId}/todos/{todoId}/done \
  -H "Authorization: Bearer $TOKEN"
```

### Fitness
```bash
# Log cardio
curl -X POST http://localhost:8080/api/v1/fitness/cardio \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"cardioType":"Running","durationMinutes":30,"distanceKm":5.2,"caloriesBurned":320}'

# Log workout with exercise sets
curl -X POST http://localhost:8080/api/v1/fitness/workouts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "workoutName":"Push Day",
    "sessionDate":"2026-06-15",
    "durationMinutes":60,
    "exerciseSets":[
      {"exerciseName":"Bench Press","setNumber":1,"reps":10,"weightKg":60},
      {"exerciseName":"Bench Press","setNumber":2,"reps":8,"weightKg":65}
    ]
  }'

# Log sleep
curl -X POST http://localhost:8080/api/v1/fitness/sleep \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bedTime":"22:30","wakeTime":"06:30","qualityRating":4}'

# Log hydration
curl -X POST http://localhost:8080/api/v1/fitness/hydration \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amountMl":500,"targetMl":2500}'

# Get streaks
curl http://localhost:8080/api/v1/fitness/streaks -H "Authorization: Bearer $TOKEN"
```

### Finance
```bash
# Add expense
curl -X POST http://localhost:8080/api/v1/finance/expenses \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":250.00,"categoryId":"uuid","date":"2026-06-15","merchant":"Swiggy","note":"Biryani"}'

# Add income
curl -X POST http://localhost:8080/api/v1/finance/income \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":75000,"sourceType":"SALARY","date":"2026-06-01"}'

# Get financial health score
curl http://localhost:8080/api/v1/finance/health-score -H "Authorization: Bearer $TOKEN"

# Get net worth
curl http://localhost:8080/api/v1/finance/net-worth -H "Authorization: Bearer $TOKEN"

# Set budget
curl -X PUT http://localhost:8080/api/v1/finance/budgets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[{"categoryId":"uuid","budgetAmount":5000}]'

# Add investment
curl -X POST http://localhost:8080/api/v1/finance/investments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"investmentTypeId":"uuid","name":"Nifty 50 SIP","investedAmount":5000,"purchaseDate":"2026-01-01"}'

# Debt payoff strategy (Avalanche vs Snowball)
curl http://localhost:8080/api/v1/finance/debts/payoff-strategy -H "Authorization: Bearer $TOKEN"

# Spending patterns
curl http://localhost:8080/api/v1/finance/analytics/spending-patterns -H "Authorization: Bearer $TOKEN"
```

### Habits
```bash
# Create a habit
curl -X POST http://localhost:8080/api/v1/habits \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"Drink 8 glasses of water",
    "trackingType":"QUANTITY",
    "frequencyPattern":"DAILY",
    "targetValue":8,
    "targetUnit":"glasses",
    "cue":"After waking up",
    "reward":"Feel energized all day"
  }'

# Get dashboard (all habits with today check-in status)
curl http://localhost:8080/api/v1/habits/dashboard -H "Authorization: Bearer $TOKEN"

# Log a check-in
curl -X POST http://localhost:8080/api/v1/habits/{habitId}/check-in \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"value":8,"mood":"GREAT","note":"Felt great today!"}'

# Create habit stack
curl -X POST http://localhost:8080/api/v1/habit-stacks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Morning Stack","habitIds":["uuid1","uuid2","uuid3"]}'

# Create routine group
curl -X POST http://localhost:8080/api/v1/routines \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Morning Routine","timeOfDay":"MORNING","habitIds":["uuid1","uuid2"]}'
```

### Reports
```bash
# Get weekly reports (last 12)
curl http://localhost:8080/api/v1/reports/weekly -H "Authorization: Bearer $TOKEN"

# Get latest report
curl http://localhost:8080/api/v1/reports/weekly/latest -H "Authorization: Bearer $TOKEN"

# Manually trigger report (for testing)
curl -X POST http://localhost:8080/api/v1/reports/generate -H "Authorization: Bearer $TOKEN"

# Dashboard (unified: goals + fitness + finance + habits)
curl http://localhost:8080/api/v1/dashboard -H "Authorization: Bearer $TOKEN"
```

---

## 📊 Health Check Endpoints

```bash
# Check all services
curl http://localhost:8761/actuator/health    # Eureka
curl http://localhost:8080/actuator/health    # Gateway
curl http://localhost:8081/actuator/health    # Auth
curl http://localhost:8082/actuator/health    # Core
curl http://localhost:8083/actuator/health    # Fitness
curl http://localhost:8084/actuator/health    # Finance
curl http://localhost:8085/actuator/health    # Habits
curl http://localhost:8086/actuator/health    # Report
```

Expected response: `{"status":"UP"}`

---

## 🚀 Production Deployment (Zero Cost)

### Stack
| Component | Service | Cost |
|---|---|---|
| Backend (8 services) | Oracle Cloud Free Tier | $0 |
| Database (PostgreSQL) | Supabase Free | $0 |
| Frontend (React PWA) | Vercel Free | $0 |
| Service Registry | Self-hosted on Oracle | $0 |

### Oracle Cloud Free Tier Setup

Oracle Always-Free gives you **2 AMD VMs** (1 OCPU, 1GB RAM each).

**VM 1** → Eureka + API Gateway + Auth + Core  
**VM 2** → Fitness + Finance + Habits + Report

```bash
# On each Oracle VM
sudo apt update && sudo apt install -y docker.io docker-compose-plugin git

# Clone repo
git clone https://github.com/yourname/corecompass.git
cd corecompass/backend

# Set secrets
cp .env.example .env
nano .env   # Fill in DATABASE_URL, JWT_SECRET

# Allow ports in Oracle's firewall (iptables + Security List in console)
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8080 -j ACCEPT
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 8761 -j ACCEPT
sudo iptables-save | sudo tee /etc/iptables/rules.v4

# Start services
docker compose up -d

# View logs
docker compose logs -f
```

Also open ports in Oracle Cloud Console:
- VCN → Security Lists → Ingress Rules → Add TCP 8080, 8761

### Supabase Free Tier

1. Create project at https://supabase.com
2. Go to Settings → Database
3. Copy **Connection String (URI)** — convert to JDBC format:
   ```
   # Supabase gives you:
   postgresql://postgres:PASSWORD@db.xxxx.supabase.co:5432/postgres

   # Convert to JDBC (add jdbc: prefix):
   jdbc:postgresql://db.xxxx.supabase.co:5432/postgres
   ```
4. In Supabase Dashboard → SQL Editor → Run this to enable UUID extension:
   ```sql
   CREATE EXTENSION IF NOT EXISTS "pgcrypto";
   ```

**Free tier limits:** 500MB storage, 2 CPU, unlimited API requests. Plenty for early users.

### Vercel (Frontend — Phase 6)
```bash
# In frontend directory
npm install -g vercel
vercel --prod

# Set environment variables in Vercel dashboard:
VITE_API_BASE_URL=https://your-oracle-vm-ip:8080/api/v1
VITE_GOOGLE_CLIENT_ID=your-google-client-id
```

### Google OAuth2 Setup
1. Go to https://console.cloud.google.com
2. Create project → APIs & Services → Credentials
3. Create OAuth 2.0 Client ID (Web application)
4. Add Authorized redirect URIs:
   ```
   http://localhost:8081/login/oauth2/code/google
   https://your-domain.com/login/oauth2/code/google
   ```
5. Copy Client ID and Client Secret → add to `.env`

### Google Calendar Setup (Optional)
1. Enable Google Calendar API in Cloud Console
2. Create Service Account → download JSON credentials
3. In your `.env`:
   ```env
   GOOGLE_CALENDAR_ENABLED=true
   GOOGLE_APPLICATION_CREDENTIALS=/path/to/credentials.json
   ```

---

## 🧪 Testing

### Run unit tests
```bash
# All services
cd backend && mvn test

# Single service
cd backend/auth-service && mvn test

# Specific test class
cd backend/auth-service && mvn test -Dtest=AuthServiceTest
```

### Run a full flow test
```bash
# 1. Register
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test1234!","name":"Test User"}')
echo $RESPONSE | python3 -m json.tool

# 2. Extract token
TOKEN=$(echo $RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

# 3. Get goal categories
curl http://localhost:8080/api/v1/goals/types -H "Authorization: Bearer $TOKEN" | python3 -m json.tool

# 4. Get dashboard
curl http://localhost:8080/api/v1/dashboard -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

---

## 📁 Project Structure

```
corecompass/
├── backend/
│   ├── pom.xml                         ← Parent Maven POM (all 8 modules)
│   ├── docker-compose.yml              ← Start all 8 services
│   ├── .env.example                    ← Copy to .env and fill in values
│   │
│   ├── eureka-server/                  ← Service registry :8761
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/.../EurekaServerApplication.java
│   │       ├── java/.../EurekaSecurityConfig.java
│   │       └── resources/application.yml
│   │
│   ├── api-gateway/                    ← All traffic enters here :8080
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/.../filter/JwtAuthFilter.java       ← JWT validation
│   │       ├── java/.../filter/RequestLoggingFilter.java
│   │       ├── java/.../config/RateLimiterConfig.java
│   │       ├── java/.../config/CircuitBreakerConfig2.java
│   │       ├── java/.../config/FallbackController.java  ← 503 on circuit open
│   │       └── resources/application.yml               ← All routes defined here
│   │
│   ├── auth-service/                   ← JWT, OAuth2, users :8081
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/.../controller/AuthController.java
│   │       ├── java/.../service/AuthService.java
│   │       ├── java/.../security/JwtService.java
│   │       ├── java/.../config/SecurityConfig.java
│   │       ├── java/.../config/OAuth2SuccessHandler.java
│   │       ├── java/.../entity/{User,RefreshToken}Entity.java
│   │       └── resources/db/migration/V1__create_auth_schema.sql
│   │
│   ├── core-service/                   ← Goals, Todos, Dashboard :8082
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/.../controller/Controllers.java     ← Goal + Todo + Dashboard
│   │       ├── java/.../service/GoalService.java
│   │       ├── java/.../service/DashboardService.java
│   │       ├── java/.../service/GoogleCalendarService.java  ← @Async sync
│   │       ├── java/.../client/FeignClients.java        ← Calls fitness, finance, habits
│   │       └── resources/db/migration/V2,V7,V8__*.sql
│   │
│   ├── fitness-service/                ← Workouts, Cardio, Nutrition :8083
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/.../controller/FitnessController.java
│   │       ├── java/.../service/FitnessService.java
│   │       ├── java/.../entity/FitnessEntities.java     ← All 7 entity types
│   │       └── resources/db/migration/V3__create_fitness_schema.sql
│   │
│   ├── finance-service/                ← Expenses, Income, Investments :8084
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/.../controller/FinanceController.java
│   │       ├── java/.../service/FinanceService.java     ← Avalanche/Snowball, health score
│   │       ├── java/.../entity/FinanceEntities.java     ← Expense, Income, Budget, Debt, Investment
│   │       └── resources/db/migration/V4__create_finance_schema.sql
│   │
│   ├── habits-service/                 ← Habits, Stacks, Routines :8085
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/.../controller/HabitsController.java
│   │       ├── java/.../service/HabitsService.java     ← Atomic Habits, streaks
│   │       ├── java/.../entity/HabitEntities.java      ← Habit, Checkin, Stack, Routine
│   │       └── resources/db/migration/V5__create_habits_schema.sql
│   │
│   └── report-service/                 ← Weekly cron, insights :8086
│       ├── Dockerfile
│       ├── pom.xml
│       └── src/main/
│           ├── java/.../scheduler/ReportScheduler.java  ← Mon 08:00 IST cron
│           ├── java/.../client/ReportFeignClients.java  ← Fetches from all services
│           ├── java/.../service/ReportService.java
│           ├── java/.../controller/ReportController.java
│           └── resources/db/migration/V6__create_report_schema.sql
│
└── frontend/                           ← React PWA (Phase 6 — coming soon)
    └── ...
```

---

## 🔥 Common Issues & Fixes

### Services won't start — "Connection refused to Eureka"
```bash
# Start Eureka FIRST, wait 15s, then start others
docker compose up eureka-server
sleep 15
docker compose up
```

### "Flyway migration failed"
```bash
# Check DB connection
docker compose logs auth-service | grep "flyway\|ERROR"

# If schema conflict, reset (dev only!)
# In Supabase SQL editor:
# DROP SCHEMA IF EXISTS auth_schema CASCADE;
# Then restart: docker compose restart auth-service
```

### "JWT signature invalid" errors
```bash
# ALL services must use the SAME JWT_SECRET value
# Check your .env has one consistent value
grep JWT_SECRET .env
```

### Port already in use
```bash
# Find and kill the process
lsof -i :8080 | grep LISTEN
kill -9 <PID>

# Or use Docker cleanup
docker compose down
docker compose up --build
```

### Out of memory (Oracle Free Tier 1GB RAM)
```bash
# Each service uses 256MB max (configured in docker-compose)
# If all 8 services don't fit on 1 VM, split across 2 VMs:
# VM1: eureka, gateway, auth, core (docker-compose-vm1.yml)
# VM2: fitness, finance, habits, report (docker-compose-vm2.yml)
```

### Supabase connection timeout
```bash
# Supabase free tier pauses after 1 week of inactivity
# Go to https://supabase.com → your project → "Restore project"
# Or set up a cron to ping the DB every 6 days
```

---

## 📅 Weekly Report Cron

The Report Service automatically generates weekly reports:
- **Schedule:** Every Monday at 08:00 IST (Asia/Kolkata timezone)
- **What it does:** Fetches data from all services via Feign → builds insights → saves to DB
- **Manual trigger (for testing):**
  ```bash
  curl -X POST http://localhost:8080/api/v1/reports/generate \
    -H "Authorization: Bearer $TOKEN"
  ```

---

## 🔐 Security Notes

1. **Never commit `.env`** — it's in `.gitignore`
2. **Change JWT_SECRET** before deploying — generate with: `openssl rand -base64 64`
3. **Change Eureka password** in production — set `EUREKA_PASSWORD` in `.env`
4. Access tokens expire in **15 minutes** — frontend must refresh using the HttpOnly cookie
5. Refresh tokens expire in **7 days** and rotate on every use
6. All services run stateless — no session, no server-side state

---

## 📈 Upgrade Path (Future)

| Feature | What to add |
|---|---|
| Redis cache | Add `spring-boot-starter-data-redis`, cache dashboard responses |
| Kafka events | Replace `@Async` with Kafka topics for cross-service events |
| Kubernetes | Each service already has a Dockerfile — just add k8s manifests |
| AI insights | Uncomment Claude/OpenAI API call in `ReportScheduler.buildInsights()` |
| Push notifications | Add Firebase FCM, wire to `notification_schema` |
| CSV/PDF export | Add iText or Apache POI to finance-service |

---

## 📜 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Inter-service calls | OpenFeign + Resilience4j Circuit Breaker |
| Database | PostgreSQL 15 via Supabase |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Auth | JWT (jjwt 0.12.5) + Google OAuth2 |
| Build | Maven 3.9 (multi-module) |
| Containers | Docker + Docker Compose |
| Frontend | React 18 + Vite + Tailwind (Phase 6) |

---

*Built with ❤️ — CoreCompass Personal Life OS v1.0*
