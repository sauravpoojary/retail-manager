# Retail Store Manager Copilot

An AI-powered web application that gives retail store managers a single-screen command center for daily operations. It surfaces real-time KPIs, AI-generated diagnostic insights, prioritized corrective action recommendations, inventory alerts, and a conversational AI panel — all in one responsive interface.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Features](#features)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Running Locally with Docker](#running-locally-with-docker)
- [Backend Deep Dive](#backend-deep-dive)
- [AI / Bedrock Flow](#ai--bedrock-flow)
- [Frontend Deep Dive](#frontend-deep-dive)
- [Known Limitations & Future Work](#known-limitations--future-work)

---

## Overview

The Retail Store Manager Copilot is built for store managers who need to understand and act on store performance without digging through raw data. The system answers three questions at a glance:

- **How is the store performing today?** — KPI cards for daily sales, footfall, and low-stock alerts
- **Why is performance changing?** — AI-driven insight engine diagnosing sales fluctuations
- **What should I do about it?** — Prioritized corrective action recommendations

All AI interactions go through a dedicated Prompt Service layer that communicates with Amazon Bedrock (Claude). When Bedrock is unavailable, every AI endpoint returns a structured fallback response — the UI never breaks.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Browser (React SPA)                                        │
│  - Dashboard, KPI Cards, Insights, Recommendations,        │
│    Inventory, Copilot Chat                                  │
└────────────────────┬────────────────────────────────────────┘
                     │ REST /api/v1/*
┌────────────────────▼────────────────────────────────────────┐
│  Spring Boot Backend  (port 8080)                           │
│  - KPI Controller      → computes KPIs from raw DB tables  │
│  - Insight Controller  → delegates to Prompt Service       │
│  - Recommendation Ctrl → delegates to Prompt Service       │
│  - Inventory Controller→ queries products + inventory      │
│  - Copilot Controller  → session mgmt + message persist    │
└──────────┬──────────────────────────┬───────────────────────┘
           │ JDBC                     │ AWS SDK
┌──────────▼──────────┐   ┌───────────▼───────────────────────┐
│  PostgreSQL 16      │   │  Bedrock Integration              │
│  - stores           │   │  - PromptTemplates (prompt build) │
│  - products         │   │  - PromptServiceClient (invoke)   │
│  - product_inventory│   │  - BedrockResponseParser (parse)  │
│  - orders           │   │  - Fallback handler               │
│  - order_items      │   └──────────────┬────────────────────┘
│  - footfall_entries │                  │ HTTPS
│  - copilot_sessions │   ┌──────────────▼────────────────────┐
│  - copilot_messages │   │  Amazon Bedrock                   │
│  - quick_prompts    │   │  Claude Sonnet 4.6                │
└─────────────────────┘   └───────────────────────────────────┘
```

**Key design decisions:**

- KPIs are computed at runtime from raw `orders` and `footfall_entries` tables — no pre-aggregated KPI table
- AI responses are not persisted (stateless per request), except copilot chat messages which are stored in `copilot_messages`
- The Bedrock integration lives inside the backend — `PromptServiceClient` builds prompts, calls Claude, and parses responses. No separate microservice needed
- All API responses use a standard envelope: `{ status, data, error, timestamp }`

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, TypeScript, Vite 5, lucide-react |
| Backend | Spring Boot 3.2, Java 17, Spring Data JPA, Hibernate 6 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Containerization | Docker, Docker Compose |
| Frontend serving | Nginx (production), Vite dev server (development) |
| AI Integration | Amazon Bedrock — Claude Sonnet 4.6 (`us.anthropic.claude-sonnet-4-6`) |

---

## Project Structure

```
retail-manager/
├── docker-compose.yml              # Orchestrates postgres + backend + frontend
├── .gitignore
│
├── backend/
│   ├── Dockerfile                  # 2-stage: Maven build → JRE Alpine
│   ├── pom.xml                     # Spring Boot 3.2, Java 17, PostgreSQL, Flyway, Lombok
│   └── src/main/
│       ├── resources/
│       │   ├── application.yml     # Datasource, JPA, Flyway, Actuator config
│       │   └── db/migration/
│       │       ├── V1__init_schema.sql   # All tables, indexes, PostgreSQL enums
│       │       └── V2__seed_data.sql     # Demo store, products, orders, footfall
│       └── java/com/retail/copilot/
│           ├── CopilotApplication.java
│           ├── config/             # JPA configuration
│           ├── model/              # JPA entities + enums
│           ├── repository/         # Spring Data JPA repositories
│           ├── dao/                # Query logic layer (KpiDao, InventoryDao, CopilotDao)
│           ├── dto/                # Request/response DTOs per feature
│           ├── service/            # Business logic + PromptServiceClient
│           ├── controller/         # REST controllers
│           └── exception/          # Custom exceptions + GlobalExceptionHandler
│
├── frontend/
│   ├── Dockerfile                  # 2-stage: Node build → Nginx Alpine
│   ├── nginx.conf                  # Proxies /api/* → backend, SPA fallback
│   ├── vite.config.ts              # Dev proxy to localhost:8080
│   ├── tsconfig.json
│   ├── package.json                # React 18, TypeScript, lucide-react
│   └── src/
│       ├── main.tsx
│       ├── App.tsx                 # Root layout: header + 2-col grid + bottom nav
│       ├── types/index.ts          # All TypeScript interfaces matching API contracts
│       ├── api/client.ts           # Typed fetch wrappers with AbortSignal timeouts
│       ├── context/AppContext.tsx  # useReducer global state + all API actions
│       └── components/
│           ├── Header.tsx          # Blue header, store info, refresh button
│           ├── KpiStrip.tsx        # 3 KPI cards with trend indicators
│           ├── InsightPanel.tsx    # Collapsible, AI insight list
│           ├── RecommendationPanel.tsx  # Priority badges, category icons
│           ├── InventoryPanel.tsx  # Low-stock table with CRITICAL/LOW pills
│           ├── CopilotPanel.tsx    # Quick-prompt chips, chat bubbles, typing dots
│           ├── BottomNav.tsx       # Mobile navigation bar
│           └── ui/
│               ├── Skeleton.tsx    # Shimmer loading placeholders
│               ├── FallbackBox.tsx # Amber warning box for AI unavailability
│               └── PanelCard.tsx   # Reusable collapsible card wrapper
│
├── api-design.md                   # Full REST API contracts with example payloads
├── database-schema-design.md       # PostgreSQL schema with runtime KPI queries
├── design.md                       # UI/UX design spec, component hierarchy, color tokens
└── requirements.md                 # Functional requirements and acceptance criteria
```

---

## Features

### KPI Dashboard
- **Daily Sales** — sum of `completed` orders for today vs yesterday, with signed trend %
- **Footfall** — count of `footfall_entries` for today vs yesterday, with trend
- **Low Stock Alerts** — count of products where `current_stock < low_stock_threshold`
- All KPIs computed at runtime — no stale cached data
- Skeleton loaders during fetch, null-safe placeholders when data is unavailable

### AI Insights Panel
- On-demand analysis triggered by "Analyze Sales" button
- Sends current store context (sales, footfall, low-stock items) to the backend
- Returns up to 5 ranked reasons for sales fluctuations with categories: `sales`, `footfall`, `inventory`, `staffing`, `external`
- Fallback amber info box when AI is unavailable

### Recommendations Panel
- On-demand, returns 2–3 prioritized corrective actions
- Each recommendation has a priority (`high`/`medium`/`low`), category (`promotion`/`staffing`/`restocking`), description, and action label
- Color-coded priority badges, category icons
- Pre-defined fallback recommendations when AI is unavailable

### Inventory Alerts
- Always-visible panel showing all products below their `low_stock_threshold`
- Sorted by urgency score (`threshold / current_stock`) — most critical first
- Status pills: `CRITICAL` (stock < 30% of threshold) in red, `LOW` in amber
- Green confirmation message when all stock levels are adequate

### Conversational Copilot
- Session-based chat — creates a `copilot_session` on panel mount, persists all messages to `copilot_messages`
- Quick-prompt chips for common queries (sales, restocking, staffing, summary)
- Free-text input with Enter-to-send
- Typing indicator (animated dots) while awaiting AI response
- Fallback error bubble when AI is unavailable
- Whitespace-only queries rejected before reaching the backend

---

## Database Schema

9 tables, all KPIs computed at runtime:

| Table | Purpose |
|-------|---------|
| `stores` | Store registry — `store_code` is the external identifier |
| `products` | Product catalog with `low_stock_threshold` per product |
| `product_inventory` | Current stock level per product (1:1 with products) |
| `orders` | Sales transactions — source for daily sales KPI |
| `order_items` | Line items per order (quantity, unit price, computed line total) |
| `footfall_entries` | One row per customer entry — source for footfall KPI |
| `copilot_sessions` | One session per manager interaction window |
| `copilot_messages` | Chat messages within a session, ordered by `created_at` |
| `quick_prompts` | Configurable chip labels and query text for the Copilot Panel |

**PostgreSQL native enum types:** `order_status`, `message_role`, `stock_status`

Flyway manages schema migrations:
- `V1__init_schema.sql` — creates all tables, indexes, and enum types
- `V2__seed_data.sql` — seeds one demo store (`STORE-042`), 5 products, sample orders and footfall entries

---

## API Reference

All endpoints are under `/api/v1/`. Full contracts with example request/response payloads are in [`api-design.md`](./api-design.md).

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/kpis?storeCode=STORE-042` | Runtime-computed KPIs |
| `POST` | `/api/v1/insights` | AI sales diagnosis (up to 5 reasons) |
| `POST` | `/api/v1/recommendations` | AI corrective actions (2–3 items) |
| `GET` | `/api/v1/inventory?storeCode=STORE-042` | Low-stock products sorted by urgency |
| `POST` | `/api/v1/copilot/sessions` | Create a new chat session |
| `POST` | `/api/v1/copilot/query` | Send a message, get AI reply |
| `GET` | `/actuator/health` | Health check (used by Docker) |

**Standard response envelope:**
```json
{
  "status": "success",
  "data": { },
  "error": null,
  "timestamp": "2026-04-30T09:15:00Z"
}
```

---

## Running Locally with Docker

**Prerequisites:** Docker Desktop running, AWS account with Bedrock access

### 1. AWS Setup (one-time)

**Create an IAM user for local development:**
1. AWS Console → IAM → Users → Create user → name it `retail-copilot-dev`
2. Attach policy: `AmazonBedrockFullAccess`
3. Security credentials tab → Create access key → Local code → copy both values

**Enable the Claude model:**
1. AWS Console → Amazon Bedrock → Model catalog → search `Claude Haiku 4.5`
2. Click it → Submit use case details (fill in company name + "retail store management dashboard for internal testing")
3. Wait ~15 minutes for activation

### 2. Create your `.env` file

Copy `.env.example` to `.env` and fill in your credentials:

```bash
cp .env.example .env
```

```
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=your-access-key-id
AWS_SECRET_ACCESS_KEY=your-secret-access-key
BEDROCK_MODEL_ID=us.anthropic.claude-sonnet-4-6
```

> `.env` is gitignored — never commit real credentials.

### 3. Run

```bash
# Clone the repo
git clone https://github.com/sauravpoojary/retail-manager.git
cd retail-manager

# Build and start all services
docker compose up --build

# Or run in background
docker compose up --build -d
```

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080/api/v1 |
| Health check | http://localhost:8080/actuator/health |
| PostgreSQL | localhost:5432 (user: `copilot`, db: `copilot_db`) |

**Startup order:** PostgreSQL → Backend (waits for DB health) → Frontend

The first build takes a few minutes (Maven downloads dependencies, npm installs packages). Subsequent runs use Docker layer cache and are much faster.

```bash
# View logs
docker compose logs -f backend
docker compose logs -f frontend

# Stop everything
docker compose down

# Stop and wipe the database volume (fresh start)
docker compose down -v
```

---

## Backend Deep Dive

### Layer Structure

```
Controller → Service → DAO → Repository (Spring Data JPA)
                    ↘ JdbcClient (for complex native queries)
```

- **Controllers** — thin, handle HTTP mapping and validation only
- **Services** — business logic, fallback handling, trend computation
- **DAOs** — encapsulate query logic; `KpiDao` computes sales/footfall aggregates, `InventoryDao` uses `JdbcClient` for the joined low-stock query to avoid N+1
- **Repositories** — Spring Data JPA interfaces with custom `@Query` annotations

### PostgreSQL Enum Handling

PostgreSQL native enum types (`order_status`, `message_role`) require explicit type handling with Hibernate 6. The entities use `@JdbcTypeCode(SqlTypes.NAMED_ENUM)` alongside `@Enumerated(EnumType.STRING)` to ensure Hibernate binds parameters with the correct PostgreSQL type rather than `varchar`.

### KPI Computation

KPIs are computed at runtime using date-range queries scoped to the store's timezone:

```sql
-- Daily sales
SELECT COALESCE(SUM(total_amount), 0)
FROM orders
WHERE store_id = ? AND status = 'completed'
  AND ordered_at >= <today_start> AND ordered_at < <tomorrow_start>

-- Trend % = ((today - yesterday) / yesterday) * 100
```

### Bedrock Integration

AI features call Amazon Bedrock directly using the AWS SDK `BedrockRuntimeClient`. The integration is in three classes:

- **`BedrockConfig`** — creates the `BedrockRuntimeClient` bean using `DefaultCredentialsProvider` (reads from env vars, `~/.aws/credentials`, or EC2/ECS instance role automatically)
- **`PromptTemplates`** — structured prompt strings for each feature (insights, recommendations, copilot). All prompts instruct Claude to return strict JSON for reliable parsing
- **`BedrockResponseParser`** — parses Claude's JSON responses into typed DTOs. Handles markdown code fences, validates enum values, enforces item count limits (max 5 insights, max 3 recommendations)
- **`PromptServiceClient`** — orchestrates the full call: build prompt → invoke Bedrock → parse response → return DTO. Throws `RuntimeException` on failure so the calling service falls back gracefully

**Model:** Claude Sonnet 4.6 (`us.anthropic.claude-sonnet-4-6`) via cross-region inference profile. The `us.` prefix is required for all Claude models from 3.5 onwards.

---

## AI / Bedrock Flow

All three AI features (Insights, Recommendations, Copilot) share the same internal pipeline. Here is the complete flow for each.

### How a prompt is built and sent

Every AI call has two parts sent to Bedrock:

**System prompt** — tells Claude what role to play. Fixed per feature, never changes at runtime.

**User prompt** — built dynamically by injecting real store data (sales, footfall, low-stock items, conversation history) into a template string.

The combined payload sent to Bedrock looks like this:

```json
{
  "anthropic_version": "bedrock-2023-05-31",
  "max_tokens": 1024,
  "temperature": 0.7,
  "system": "<system prompt>",
  "messages": [
    { "role": "user", "content": "<user prompt with live store data>" }
  ]
}
```

---

### Feature 1 — AI Insights (`POST /api/v1/insights`)

**Trigger:** Manager clicks "Analyze Sales" in the UI. The frontend sends the current KPI values it already has on screen.

**System prompt:**
```
You are a retail analytics expert. Analyze store performance data and identify
the top reasons behind sales fluctuations. Always respond with valid JSON only —
no markdown, no explanation outside the JSON structure.
```

**User prompt (built from live KPI data):**
```
Store performance data for STORE-042 on 2026-05-01:
- Daily sales: $12,450.75 (8.2% vs yesterday)
- Customer footfall: 342 (-3.1% vs yesterday)
- Low stock alerts: 3 items
- Top low-stock products: Organic Milk 1L, Whole Wheat Bread, Free Range Eggs

Identify the top reasons for the current sales performance.
Return ONLY this JSON structure (no other text):
{
  "reasons": [
    { "rank": 1, "description": "...", "category": "sales|footfall|inventory|staffing|external" }
  ]
}
Return between 3 and 5 reasons. Each description must be one concise sentence.
```

**Claude returns:**
```json
{
  "reasons": [
    { "rank": 1, "description": "Footfall dropped 3.1% likely due to reduced morning commuter traffic.", "category": "footfall" },
    { "rank": 2, "description": "Three critically low dairy products caused missed basket additions.", "category": "inventory" },
    { "rank": 3, "description": "Sales per customer increased, offsetting the footfall decline.", "category": "sales" }
  ]
}
```

**Parser (`BedrockResponseParser.parseInsights`):**
- Strips markdown code fences if Claude wrapped the JSON in ` ```json ``` `
- Extracts the `reasons` array
- Maps each item to `InsightReasonDto`
- Sanitizes `category` to one of: `sales`, `footfall`, `inventory`, `staffing`, `external`
- Enforces max 5 items

**Response to frontend:**
```json
{
  "storeCode": "STORE-042",
  "generatedAt": "2026-05-01T09:15:02Z",
  "reasons": [ ... ],
  "isFallback": false
}
```

---

### Feature 2 — Recommendations (`POST /api/v1/recommendations`)

**Trigger:** Manager clicks "Get Recommendations". Same store context as insights.

**System prompt:**
```
You are a retail operations advisor. Generate prioritized corrective action
recommendations based on store performance data. Always respond with valid JSON
only — no markdown, no explanation outside the JSON structure.
```

**User prompt:**
```
Store performance data for STORE-042 on 2026-05-01:
- Daily sales: $12,450.75 (8.2% vs yesterday)
- Customer footfall: 342 (-3.1% vs yesterday)
- Low stock alerts: 3 items
- Top low-stock products: Organic Milk 1L, Whole Wheat Bread, Free Range Eggs

Generate 2 to 3 prioritized corrective action recommendations.
Return ONLY this JSON structure (no other text):
{
  "recommendations": [
    {
      "priority": "high|medium|low",
      "category": "promotion|staffing|restocking",
      "description": "...",
      "actionLabel": "short CTA (2-3 words)"
    }
  ]
}
Sort by priority descending. Each description must be one actionable sentence.
```

**Claude returns:**
```json
{
  "recommendations": [
    { "priority": "high", "category": "restocking", "description": "Immediately restock Organic Milk 1L — only 2 units remain against a threshold of 10.", "actionLabel": "Restock Now" },
    { "priority": "high", "category": "staffing",   "description": "Add one cashier during 11 AM–2 PM to reduce checkout wait times.", "actionLabel": "Adjust Schedule" },
    { "priority": "medium", "category": "promotion", "description": "Run a weekend dairy promotion to recover footfall lost to competitor pricing.", "actionLabel": "Start Promotion" }
  ]
}
```

**Parser (`BedrockResponseParser.parseRecommendations`):**
- Same JSON extraction as insights
- Maps each item to `RecommendationItemDto` with a generated UUID
- Sanitizes `priority` to `high`, `medium`, or `low`
- Sanitizes `category` to `promotion`, `staffing`, or `restocking`
- Enforces max 3 items

---

### Feature 3 — Copilot Chat (`POST /api/v1/copilot/query`)

**Trigger:** Manager types a question or clicks a quick-prompt chip.

**System prompt:**
```
You are a helpful retail store copilot assistant. Answer the store manager's
question concisely and actionably using the provided store context.
Keep responses under 150 words. Be direct and practical.
```

**User prompt (includes conversation history for context continuity):**
```
Current store context for STORE-042 on 2026-05-01:
- Daily sales: $12,450.75 (8.2% vs yesterday)
- Customer footfall: 342 (-3.1% vs yesterday)
- Low stock alerts: 3 items
- Top low-stock products: Organic Milk 1L, Whole Wheat Bread, Free Range Eggs

Recent conversation:
Manager: Give me a summary of today's store performance.
Copilot: Sales are up 8.2% at $12,450. Footfall is slightly down 3.1%...

Store manager asks: Which products need restocking most urgently?
```

The conversation history includes the **last 6 messages** from the session to keep Claude aware of what was already discussed, without exceeding token limits.

**Claude returns:** Plain conversational text (no JSON required for chat).
```
Organic Milk 1L is most urgent — only 2 units left against a threshold of 10 (urgency score: 5.0).
Free Range Eggs follow with 4 units vs threshold of 12. Whole Wheat Bread has 5 units vs 15.
Prioritise restocking dairy first as it drives the highest basket additions.
```

**Parser (`BedrockResponseParser.parseCopilotReply`):** Just trims whitespace — no JSON parsing needed for conversational responses.

---

### Fallback strategy

If Bedrock is unavailable (network error, invalid credentials, model access issue), `PromptServiceClient` throws a `RuntimeException`. The calling service catches it and returns a pre-defined fallback:

```
InsightService    → returns 1 generic reason with isFallback: true
RecommendationService → returns 2 generic recommendations with isFallback: true
CopilotService    → returns "AI service temporarily unavailable" message
```

The UI shows an amber info box for insights/recommendations, and a red-bordered chat bubble for the copilot — the app never crashes.

---

### Why store data comes from the frontend, not the DB

The frontend already has the KPI values on screen (fetched from `GET /api/v1/kpis`). Rather than the backend re-querying the DB for every AI call, the frontend sends the current values as the `StoreContextRequest` body. This keeps AI calls stateless and fast — no extra DB round-trip needed per AI request.

---

## Frontend Deep Dive

### State Management

Uses React Context + `useReducer` — no external state library. The `AppContext` holds all async state with `LoadStatus` (`idle | loading | success | error`) per feature area.

### API Client

`src/api/client.ts` wraps `fetch` with:
- `AbortSignal.timeout()` per endpoint (3s for KPIs/inventory, 5s for AI endpoints, 2s for session creation)
- Typed generics matching the API response envelope
- No third-party HTTP library

### Responsive Layout

- Mobile (< 1024px): single column, vertical stack
- Desktop (≥ 1024px): 60/40 two-column grid — left column has KPIs + AI panels, right column has the sticky Copilot panel
- Implemented via CSS Grid with a media query in `App.tsx`

### Fallback Strategy

Every AI panel (`InsightPanel`, `RecommendationPanel`, `CopilotPanel`) handles three states independently:
1. `loading` — skeleton shimmer animation
2. `isFallback: true` in response — amber `FallbackBox` component
3. Network/timeout error — same amber box with retry guidance

---

## Known Limitations & Future Work

- **Single store** — the seed data has one store (`STORE-042`). Multi-store support is schema-ready (all tables have `store_id`) but the frontend hardcodes `STORE-042`.
- **No authentication** — there is no user login or session management beyond the copilot chat session. Adding Spring Security with JWT is the natural next step.
- **Seed data footfall** — `V2__seed_data.sql` only inserts a few footfall rows. For realistic KPI trends, generate more rows covering multiple days.
- **No real-time updates** — KPIs refresh only on manual trigger. WebSocket or SSE polling could enable live updates.
- **`order_items` unused in KPIs** — the table exists for future basket analysis but the current KPI computation only uses `orders.total_amount`.
