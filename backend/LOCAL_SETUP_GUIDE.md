# 🛠️ CoreCompass — Local Development Setup Guide

> **Intha guide padichitu follow pannaa** — IntelliJ la full project run aagum, Docker la production ready aagum.

---

## 📋 Table of Contents

1. [What you need to install first](#1-prerequisites--install-list)
2. [Get the code](#2-get-the-code)
3. [Supabase free database setup](#3-supabase-database-setup-free)
4. [.env file — where to put it](#4-env-file---where-exactly-to-put-it)
5. [IntelliJ IDEA Setup (Recommended)](#5-intellij-idea-setup-step-by-step)
6. [Run locally — Maven (No Docker)](#6-run-locally-with-maven-no-docker)
7. [Run with Docker (Easiest)](#7-run-with-docker-easiest-method)
8. [Verify everything works](#8-verify-everything-is-running)
9. [Common errors & fixes](#9-common-errors--fixes)
10. [Day-to-day development workflow](#10-day-to-day-development-workflow)

---

## 1. Prerequisites — Install List

Install **all of these** before starting. Don't skip any.

### Java 21
```
Download: https://adoptium.net/temurin/releases/?version=21
→ Choose: Windows/Mac/Linux → JDK → x64 → .msi or .pkg
→ Install it (just click Next Next Finish)
```

Verify:
```bash
java -version
# Should show: openjdk version "21.x.x"
```

### Maven 3.9+
```
Download: https://maven.apache.org/download.cgi
→ Binary zip archive: apache-maven-3.9.x-bin.zip
```

**Windows setup:**
1. Extract to `C:\Program Files\Maven\`
2. Go to: System Properties → Environment Variables
3. New System Variable: `MAVEN_HOME` = `C:\Program Files\Maven\apache-maven-3.9.x`
4. Edit `PATH` → Add: `%MAVEN_HOME%\bin`

**Mac setup:**
```bash
brew install maven
```

**Linux setup:**
```bash
sudo apt install maven
```

Verify:
```bash
mvn -version
# Should show: Apache Maven 3.9.x
```

### Docker Desktop
```
Download: https://docker.com/products/docker-desktop
→ Install for your OS
→ Open Docker Desktop and wait until it says "Running" (green icon)
```

Verify:
```bash
docker --version
# Docker version 24.x.x

docker compose version
# Docker Compose version v2.x.x
```

### IntelliJ IDEA (Community Edition is free)
```
Download: https://www.jetbrains.com/idea/download
→ Community Edition = FREE ✅
→ Ultimate = Paid (not needed)
```

### Git
```
Download: https://git-scm.com/downloads
```

---

## 2. Get the Code

### If you have the ZIP (corecompass-backend-FINAL.zip):
```bash
# Extract the ZIP
unzip corecompass-backend-FINAL.zip

# You'll get this structure:
corecompass-final/
└── backend/
    ├── pom.xml           ← Parent POM (THIS IS IMPORTANT)
    ├── docker-compose.yml
    ├── .env.example
    ├── eureka-server/
    ├── api-gateway/
    ├── auth-service/
    ├── core-service/
    ├── fitness-service/
    ├── finance-service/
    ├── habits-service/
    └── report-service/
```

### If you're using Git:
```bash
git clone https://github.com/yourname/corecompass.git
cd corecompass
```

---

## 3. Supabase Database Setup (Free)

**This is your free PostgreSQL database. Setup takes 5 minutes.**

### Step 1 — Create account
Go to: https://supabase.com → Sign up (free)

### Step 2 — Create project
1. Click **"New Project"**
2. Name: `corecompass`
3. Database Password: Choose a strong password, **WRITE IT DOWN**
4. Region: Choose closest to you (Singapore for India)
5. Click **"Create new project"** → Wait ~2 minutes

### Step 3 — Get connection string
1. In your Supabase project → Click **"Settings"** (gear icon, left sidebar)
2. Click **"Database"**
3. Scroll down to **"Connection string"**
4. Click **"URI"** tab
5. Copy the string — looks like:
   ```
   postgresql://postgres:[YOUR-PASSWORD]@db.abcdefgh.supabase.co:5432/postgres
   ```

### Step 4 — Convert to JDBC format
Add `jdbc:` prefix to convert it:
```
# Supabase gives you:
postgresql://postgres:MyPassword123@db.abcdefgh.supabase.co:5432/postgres

# You need (add "jdbc:" at the start):
jdbc:postgresql://db.abcdefgh.supabase.co:5432/postgres
```
> Note: Username and password go separately in the .env file, not in the URL.

### Step 5 — Enable UUID extension
1. In Supabase → Click **"SQL Editor"** (left sidebar)
2. Paste and run:
```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```
3. Click **"Run"** → Should say "Success"

---

## 4. .env File — Where Exactly to Put It

### 📍 Location: `backend/.env`

```
corecompass-final/
└── backend/
    ├── .env              ← CREATE THIS FILE HERE ✅
    ├── .env.example      ← Copy this as starting point
    ├── pom.xml
    ├── docker-compose.yml
    ├── eureka-server/
    └── ...
```

### Step 1 — Copy the example file
```bash
# Navigate to backend folder
cd corecompass-final/backend

# Copy example to real .env
cp .env.example .env
```

### Step 2 — Edit the .env file

Open `.env` in any text editor (Notepad, VS Code, etc.) and fill in your values:

```env
# ================================================================
# CoreCompass — Environment Variables
# FILL IN ALL VALUES BEFORE RUNNING
# ================================================================

# ── EUREKA (leave these as default for local dev) ──────────────
EUREKA_USERNAME=admin
EUREKA_PASSWORD=corecompass2026

# ── JWT SECRET (CHANGE THIS!) ──────────────────────────────────
# Generate one: openssl rand -base64 64
# Or use any 64+ character random string
JWT_SECRET=MyVeryLongAndSecureSecretKeyThatIsAtLeast64CharactersLongForJWT!

# ── DATABASE (from Supabase) ───────────────────────────────────
DATABASE_URL=jdbc:postgresql://db.abcdefgh.supabase.co:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=YourSupabasePassword123

# ── GOOGLE OAUTH2 (optional - needed only for Google login) ────
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

# ── GOOGLE CALENDAR (optional - keep false for now) ────────────
GOOGLE_CALENDAR_ENABLED=false

# ── FRONTEND URL ───────────────────────────────────────────────
FRONTEND_URL=http://localhost:5173
```

### ⚠️ IMPORTANT Rules about .env:
- **NEVER commit .env to Git** — it's in `.gitignore` already
- The `.env` file is only for your machine
- Each developer on the team has their own `.env`
- Production has its own `.env` on the server

---

## 5. IntelliJ IDEA Setup (Step by Step)

### Step 1 — Open the project
1. Open IntelliJ IDEA
2. Click **"Open"** (NOT "New Project")
3. Navigate to your `corecompass-final/backend` folder
4. **Select the `pom.xml` file** (the parent one in `backend/`)
5. Click **"Open as Project"**

> ⚠️ VERY IMPORTANT: Open the **parent** `pom.xml` in the `backend/` folder, NOT inside any individual service folder.

### Step 2 — Trust the project
IntelliJ will ask "Trust and Open Maven Project?" → Click **"Trust Project"**

### Step 3 — Wait for Maven to download dependencies
You'll see a progress bar at the bottom of IntelliJ.
- This downloads all JAR files (~500MB first time)
- Wait until it says **"Sync finished"** or the progress bar disappears
- **Do NOT skip this step** — can take 5-10 minutes depending on internet

### Step 4 — Set Java SDK
1. Go to: **File → Project Structure** (or `Ctrl+Alt+Shift+S`)
2. Under **Project**:
   - SDK: Choose **21 (Eclipse Temurin)** or **21 (Amazon Corretto)**
   - If not in list: Click **"Add SDK" → Download JDK → Version 21**
3. Click **"Apply" → "OK"**

### Step 5 — Verify Maven modules loaded
In IntelliJ, on the right side click **"Maven"** panel.
You should see all 8 modules:
```
corecompass-backend
  ├── eureka-server
  ├── api-gateway
  ├── auth-service
  ├── core-service
  ├── fitness-service
  ├── finance-service
  ├── habits-service
  └── report-service
```

If you don't see them:
1. Click the **refresh icon** in the Maven panel
2. Or: Right-click `pom.xml` → **"Add as Maven Project"**

### Step 6 — Set up environment variables in IntelliJ Run Configs

You need to add the environment variables from `.env` to each service's run configuration.

**Method A — Edit each Run Configuration manually:**

1. Go to: **Run → Edit Configurations**
2. Click **"+" → "Spring Boot"**
3. Name: `AuthService`
4. Main class: `com.corecompass.auth.AuthServiceApplication`
5. Module: `auth-service`
6. Under **"Environment variables"**, click the icon on the right side
7. Add:
   ```
   DATABASE_URL=jdbc:postgresql://db.abcdefgh.supabase.co:5432/postgres
   DATABASE_USERNAME=postgres
   DATABASE_PASSWORD=YourPassword
   JWT_SECRET=YourJWTSecret
   GOOGLE_CLIENT_ID=
   GOOGLE_CLIENT_SECRET=
   ```
8. Repeat for each service (core-service, fitness-service, etc.)

**Method B — Use .env plugin (EASIER, Recommended):**

1. Go to: **File → Settings → Plugins**
2. Search: **"EnvFile"** by Borys Pierov
3. Click **Install** → Restart IntelliJ
4. In Run Configuration → tab **"EnvFile"**
5. Enable EnvFile checkbox ✅
6. Click **"+"** → **".env file"**
7. Browse to your `backend/.env` file
8. Click **"Apply"**

Now all services will automatically load the `.env` file!

### Step 7 — Create Run Configurations for all 8 services

Create one Spring Boot run config per service:

| Config Name | Main Class | Module |
|---|---|---|
| `1-EurekaServer` | `com.corecompass.eureka.EurekaServerApplication` | `eureka-server` |
| `2-ApiGateway` | `com.corecompass.gateway.ApiGatewayApplication` | `api-gateway` |
| `3-AuthService` | `com.corecompass.auth.AuthServiceApplication` | `auth-service` |
| `4-CoreService` | `com.corecompass.core.CoreServiceApplication` | `core-service` |
| `5-FitnessService` | `com.corecompass.fitness.FitnessServiceApplication` | `fitness-service` |
| `6-FinanceService` | `com.corecompass.finance.FinanceServiceApplication` | `finance-service` |
| `7-HabitsService` | `com.corecompass.habits.HabitsServiceApplication` | `habits-service` |
| `8-ReportService` | `com.corecompass.report.ReportServiceApplication` | `report-service` |

**Name them with numbers (1-, 2-, 3-...) so they appear in order in the dropdown!**

### Step 8 — Run order in IntelliJ

**ALWAYS start in this order. Each service must start BEFORE the next:**

```
Start 1-EurekaServer → Wait 15 seconds → 
Start 2-ApiGateway  → Wait 10 seconds →
Start 3-AuthService → Wait 10 seconds →
Start 4-CoreService → ... and so on
```

### Step 9 — How to run in IntelliJ

1. Open the Run Configurations dropdown (top right)
2. Select `1-EurekaServer`
3. Click the Green ▶️ Play button
4. Watch the console at the bottom — wait for:
   ```
   Started EurekaServerApplication in 8.x seconds
   ```
5. Then switch to `2-ApiGateway` and run it
6. Continue for all services

**Tip:** Use **"Run Dashboard"** (Run → Show Run Dashboard) to see all running services at once!

---

## 6. Run Locally with Maven (No Docker, No IntelliJ)

Use this if you want to run from terminal only.

### Step 1 — Build all modules
```bash
cd corecompass-final/backend
mvn clean install -DskipTests
```

### Step 2 — Export environment variables

**On Mac/Linux:**
```bash
# Create a script: start-env.sh
export DATABASE_URL="jdbc:postgresql://db.abcdefgh.supabase.co:5432/postgres"
export DATABASE_USERNAME="postgres"
export DATABASE_PASSWORD="YourPassword123"
export JWT_SECRET="YourVeryLongJWTSecretKeyAtLeast64CharactersLong!!"
export GOOGLE_CLIENT_ID=""
export GOOGLE_CLIENT_SECRET=""
export FRONTEND_URL="http://localhost:5173"
export EUREKA_URL="http://admin:corecompass2026@localhost:8761/eureka/"

# Load the variables
source start-env.sh
```

**On Windows (Command Prompt):**
```cmd
set DATABASE_URL=jdbc:postgresql://db.abcdefgh.supabase.co:5432/postgres
set DATABASE_USERNAME=postgres
set DATABASE_PASSWORD=YourPassword123
set JWT_SECRET=YourVeryLongJWTSecretKeyAtLeast64CharactersLong!!
```

**On Windows (PowerShell):**
```powershell
$env:DATABASE_URL="jdbc:postgresql://db.abcdefgh.supabase.co:5432/postgres"
$env:DATABASE_USERNAME="postgres"
$env:DATABASE_PASSWORD="YourPassword123"
$env:JWT_SECRET="YourVeryLongJWTSecretKeyAtLeast64CharactersLong!!"
```

### Step 3 — Open 8 terminals, run each service

**Terminal 1:**
```bash
cd corecompass-final/backend/eureka-server
mvn spring-boot:run
# Wait for "Started EurekaServerApplication"
```

**Terminal 2:**
```bash
cd corecompass-final/backend/api-gateway
mvn spring-boot:run
# Wait for "Started ApiGatewayApplication"
```

**Terminal 3:**
```bash
cd corecompass-final/backend/auth-service
mvn spring-boot:run
```

**Terminal 4:**
```bash
cd corecompass-final/backend/core-service
mvn spring-boot:run
```

**Terminal 5:**
```bash
cd corecompass-final/backend/fitness-service
mvn spring-boot:run
```

**Terminal 6:**
```bash
cd corecompass-final/backend/finance-service
mvn spring-boot:run
```

**Terminal 7:**
```bash
cd corecompass-final/backend/habits-service
mvn spring-boot:run
```

**Terminal 8:**
```bash
cd corecompass-final/backend/report-service
mvn spring-boot:run
```

---

## 7. Run with Docker (Easiest Method)

**Use this when you want everything running fast without configuring IntelliJ.**

### Step 1 — Make sure Docker Desktop is running
Open Docker Desktop app → Wait for green "Running" status.

### Step 2 — Make sure .env is filled
```bash
cd corecompass-final/backend
cat .env
# Verify DATABASE_URL, DATABASE_PASSWORD, JWT_SECRET are filled in
```

### Step 3 — Build and start everything
```bash
cd corecompass-final/backend
docker compose up --build
```

**What this does:**
- Builds Docker image for each service (compiles Java inside Docker)
- Starts all 8 containers
- They automatically register with Eureka
- Flyway runs all SQL migrations

**First time: takes 8-12 minutes** (downloads base images, compiles Java)  
**After that: takes 2-3 minutes**

### Step 4 — Watch the logs
You'll see all 8 services logging in the terminal.

**Look for these success messages:**
```
corecompass-eureka   | Started EurekaServerApplication
corecompass-gateway  | Started ApiGatewayApplication
corecompass-auth     | Started AuthServiceApplication
corecompass-core     | Started CoreServiceApplication
corecompass-fitness  | Started FitnessServiceApplication
corecompass-finance  | Started FinanceServiceApplication
corecompass-habits   | Started HabitsServiceApplication
corecompass-report   | Started ReportServiceApplication
```

### Useful Docker commands:
```bash
# Run in background (detached mode)
docker compose up --build -d

# See what's running
docker compose ps

# Watch logs of specific service
docker compose logs -f auth-service

# Watch logs of all services
docker compose logs -f

# Stop everything
docker compose down

# Stop and delete all data (fresh start)
docker compose down -v

# Restart one service (after you change code)
docker compose restart auth-service

# Rebuild one service after code change
docker compose up --build auth-service
```

### Docker Troubleshooting:
```bash
# See which ports are being used
docker compose ps

# If a service keeps restarting
docker compose logs auth-service
# Read the error message

# Check if .env is loaded
docker compose config | grep DATABASE_URL
```

---

## 8. Verify Everything is Running

### Step 1 — Check Eureka Dashboard
Open browser: **http://localhost:8761**

Login: `admin` / `corecompass2026`

You should see all 8 services registered:
```
Application         AMIs    Availability Zones    Status
AUTH-SERVICE        n/a     (1)                   UP (1)
CORE-SERVICE        n/a     (1)                   UP (1)
FITNESS-SERVICE     n/a     (1)                   UP (1)
FINANCE-SERVICE     n/a     (1)                   UP (1)
HABITS-SERVICE      n/a     (1)                   UP (1)
REPORT-SERVICE      n/a     (1)                   UP (1)
API-GATEWAY         n/a     (1)                   UP (1)
```

### Step 2 — Health checks
Open these in browser or run in terminal:
```bash
curl http://localhost:8080/actuator/health   # Gateway
curl http://localhost:8081/actuator/health   # Auth
curl http://localhost:8082/actuator/health   # Core
curl http://localhost:8083/actuator/health   # Fitness
curl http://localhost:8084/actuator/health   # Finance
curl http://localhost:8085/actuator/health   # Habits
curl http://localhost:8086/actuator/health   # Report
```

Each should return: `{"status":"UP"}`

### Step 3 — Test with a real API call
```bash
# Register a test user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test1234!","name":"Test User"}'
```

Expected response:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": "uuid-here",
      "email": "test@test.com",
      "name": "Test User"
    }
  }
}
```

If you get this → **Everything is working!** 🎉

### Step 4 — Test protected endpoint
```bash
# Copy the accessToken from the register response
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# Get dashboard (should return empty data, no 401)
curl http://localhost:8080/api/v1/dashboard \
  -H "Authorization: Bearer $TOKEN"
```

---

## 9. Common Errors & Fixes

### ❌ "Connection refused" or "Unable to connect to Eureka"
**Problem:** Services started before Eureka was ready.

**Fix:**
```bash
# Stop everything
docker compose down
# Start ONLY Eureka first, wait 20 seconds, then start all
docker compose up eureka-server
# Wait 20 seconds...
docker compose up
```

Or in IntelliJ: Stop all services, run only EurekaServer first, wait for "Started" message, then run others.

---

### ❌ "Flyway: Migration failed" or "Could not connect to DB"
**Problem:** DATABASE_URL or password is wrong.

**Fix:**
1. Test your DB connection directly:
```bash
# Mac/Linux - install psql if needed: brew install libpq
psql "postgresql://postgres:YourPassword@db.xxxx.supabase.co:5432/postgres" -c "\l"
```
2. Check `.env` — make sure DATABASE_URL starts with `jdbc:`
```
CORRECT:   jdbc:postgresql://db.xxxx.supabase.co:5432/postgres
WRONG:     postgresql://db.xxxx.supabase.co:5432/postgres
```
3. In Supabase → Settings → Database → Check if your IP is allowed (Connection Pooling section)

---

### ❌ "JWT signature mismatch" or "Invalid token"
**Problem:** Different JWT_SECRET values across services.

**Fix:**
All services must use the EXACT SAME `JWT_SECRET`.
In Docker, this comes from your single `.env` file → already correct.
In IntelliJ, make sure every run configuration has the SAME JWT_SECRET value.

---

### ❌ "Port 8080 already in use"
**Problem:** Something else is using port 8080 (maybe a previous run).

**Fix:**
```bash
# Mac/Linux - find what's using port 8080
lsof -i :8080
kill -9 <PID from above>

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Docker
docker compose down
# Then try again
```

---

### ❌ "OutOfMemoryError" or containers keep crashing
**Problem:** Not enough RAM. 8 services × 256MB = ~2GB needed.

**Fix:**
1. In Docker Desktop → Settings → Resources → Memory: Set to **4GB minimum**
2. Or run only the services you need:
```bash
# Run only auth + core (skip fitness, finance etc.)
docker compose up eureka-server api-gateway auth-service core-service
```

---

### ❌ "Cannot resolve symbol" errors in IntelliJ
**Problem:** Maven dependencies not downloaded yet.

**Fix:**
1. Right-click `pom.xml` → **Maven → Reload project**
2. Wait for download to finish
3. If still broken: **File → Invalidate Caches → Invalidate and Restart**

---

### ❌ Supabase "project paused" error
**Problem:** Supabase free tier pauses after 1 week of no usage.

**Fix:**
1. Go to https://supabase.com → your project
2. Click **"Restore project"** button
3. Wait 2-3 minutes
4. Try again

---

### ❌ Google OAuth2 "redirect_uri_mismatch"
**Problem:** Redirect URI not added in Google Console.

**Fix:**
1. Go to https://console.cloud.google.com
2. APIs & Services → Credentials → your OAuth 2.0 Client
3. Under "Authorized redirect URIs" add:
   ```
   http://localhost:8081/login/oauth2/code/google
   ```
4. Save and wait 5 minutes

---

## 10. Day-to-Day Development Workflow

### When you change code in a service:

**With Docker:**
```bash
# Rebuild only the changed service
docker compose up --build auth-service

# Check logs
docker compose logs -f auth-service
```

**With IntelliJ:**
1. Make your changes
2. Stop the service (red square button)
3. Run it again (green play button)
4. IntelliJ hot-reload works for some changes (HTML/config), not for Java class changes

### Enable Spring Boot DevTools (Hot Reload):
Add this to any service's `pom.xml` in `<dependencies>`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

Then in IntelliJ: **Settings → Build, Execution → Compiler → "Build project automatically"** ✅

### Running tests:
```bash
# All tests
cd backend && mvn test

# One service
cd backend/auth-service && mvn test

# Skip tests (faster build)
cd backend && mvn clean install -DskipTests
```

### Checking logs in Docker:
```bash
# All services live
docker compose logs -f

# One service
docker compose logs -f finance-service

# Last 100 lines
docker compose logs --tail=100 auth-service

# Filter for errors
docker compose logs finance-service 2>&1 | grep ERROR
```

---

## 🗂️ Quick Reference Card

| What | Command                                                   |
|---|-----------------------------------------------------------|
| Start all (Docker) | `docker compose up --build`                               |
| Start in background | `docker compose up -d --build`                            |
| Stop all | `docker compose down`                                     |
| See running containers | `docker compose ps`                                       |
| See logs | `docker compose logs -f`                                  |
| Rebuild one service | `docker compose up --build auth-service`                  |
| Fresh start (delete data) | `docker compose down -v` then `docker compose up --build` |
| Build without running | `mvn clean install -DskipTests`                           |
| Run tests | `mvn test`                                                |
| Eureka dashboard | http://localhost:8761 (admin/corecompass2026)             |
| API Base URL | http://localhost:8080/api/v1                              |
| Auth health | http://localhost:8081/actuator/health                     |
| Core health | http://localhost:8082/actuator/health                     |

---

## 📁 .env File Full Template

```env
# ================================================================
# CoreCompass .env — Copy this to backend/.env and fill in values
# ================================================================

# ── EUREKA (leave default for local) ──────────────────────────
EUREKA_USERNAME=admin
EUREKA_PASSWORD=corecompass2026

# ── JWT SECRET ─────────────────────────────────────────────────
# Generate with: openssl rand -base64 64
# Must be 64+ characters
JWT_SECRET=REPLACE_ME_WITH_64_CHAR_RANDOM_STRING_openssl_rand_base64_64

JWT_ACCESS_EXPIRY=900       # 15 minutes in seconds
JWT_REFRESH_EXPIRY=604800   # 7 days in seconds

# ── SUPABASE DATABASE ──────────────────────────────────────────
DATABASE_URL=jdbc:postgresql://db.YOUR_PROJECT_ID.supabase.co:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=YOUR_SUPABASE_DB_PASSWORD

# ── GOOGLE OAUTH2 ──────────────────────────────────────────────
# Get from: console.cloud.google.com
# Leave empty if you don't need Google login yet
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

# ── GOOGLE CALENDAR ────────────────────────────────────────────
# Set to true only if you have service account credentials set up
GOOGLE_CALENDAR_ENABLED=false

# ── FRONTEND ───────────────────────────────────────────────────
# React dev server URL (Vite default)
FRONTEND_URL=http://localhost:5173
```

---

*Intha guide follow pannaa local development easy-aa irukkum. Any issue vantaa error message paste pannu — fix pannuvom!* 🚀
