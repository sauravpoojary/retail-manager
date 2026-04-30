# API Design — Retail Store Manager Copilot

## Overview

REST API contracts for the Retail Store Manager Copilot. All public endpoints are versioned under `/api/v1/` and consumed by the React.js frontend. Internal endpoints under `/internal/prompt/` handle backend-to-Prompt Service communication.

**Identifier convention**: All external API identifiers use `store_code` (e.g. `"STORE-042"`) as the store reference, not the internal UUID. All other entity IDs (sessions, messages) are UUIDs.

All responses use a standard envelope. Timestamps follow ISO 8601 (UTC).

---

## Standard Response Envelope

```json
{
  "status": "success",
  "data": { },
  "error": null,
  "timestamp": "2026-04-30T09:15:00Z"
}
```

Error response:

```json
{
  "status": "error",
  "data": null,
  "error": {
    "code": "BEDROCK_UNAVAILABLE",
    "message": "AI service is temporarily unavailable",
    "fallback": true
  },
  "timestamp": "2026-04-30T09:15:00Z"
}
```

---

## Public API Endpoints

### 1. GET `/api/v1/kpis`

Returns current store KPIs computed at runtime from `orders`, `footfall_entries`, and `product_inventory`.

**UI Mapping**: KPI Cards (Daily Sales, Footfall, Low Stock Alerts)

**DB sources**: `orders` (sales + trend), `footfall_entries` (footfall + trend), `products` + `product_inventory` (low-stock count), `stores` (currency)

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `storeCode` | string | Yes | Store code, e.g. `STORE-042` |

#### Response — 200 OK

```json
{
  "status": "success",
  "data": {
    "storeCode": "STORE-042",
    "date": "2026-04-30",
    "dailySales": {
      "amount": 12450.75,
      "currency": "USD",
      "trendPercent": 8.2,
      "trendDirection": "up"
    },
    "footfall": {
      "count": 342,
      "trendPercent": -3.1,
      "trendDirection": "down"
    },
    "lowStockAlertCount": 3,
    "asOf": "2026-04-30T09:15:00Z"
  },
  "error": null,
  "timestamp": "2026-04-30T09:15:00Z"
}
```

> `trendPercent` is signed: positive = up, negative = down. `trendDirection` is derived: `> 0` → `"up"`, `< 0` → `"down"`, `= 0` → `"flat"`. `asOf` is the server timestamp at query time.

#### Response — 200 OK (Data Unavailable)

Individual KPI objects are `null` when their source table has no data for the day:

```json
{
  "status": "success",
  "data": {
    "storeCode": "STORE-042",
    "date": "2026-04-30",
    "dailySales": null,
    "footfall": null,
    "lowStockAlertCount": null,
    "asOf": "2026-04-30T09:15:00Z"
  },
  "error": null,
  "timestamp": "2026-04-30T09:15:00Z"
}
```

#### Response — 404 Not Found

```json
{
  "status": "error",
  "data": null,
  "error": {
    "code": "STORE_NOT_FOUND",
    "message": "Store with code STORE-042 not found",
    "fallback": false
  },
  "timestamp": "2026-04-30T09:15:00Z"
}
```

---

### 2. POST `/api/v1/insights`

Triggers AI-driven sales diagnosis. Returns top reasons for sales fluctuations.

**UI Mapping**: AI Insights Panel

**DB sources**: reads nothing (context passed in body); no write to DB (AI responses are not persisted)

#### Request Body

```json
{
  "storeCode": "STORE-042",
  "date": "2026-04-30",
  "dailySales": 12450.75,
  "salesTrend": 8.2,
  "footfall": 342,
  "footfallTrend": -3.1,
  "lowStockCount": 3,
  "topLowStockItems": ["Organic Milk 1L", "Whole Wheat Bread", "Free Range Eggs"]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `storeCode` | string | Yes | Store code |
| `date` | string (ISO date) | Yes | Date for analysis |
| `dailySales` | number | Yes | Today's completed sales total |
| `salesTrend` | number | Yes | Signed % change vs previous day |
| `footfall` | number | Yes | Customer entry count today |
| `footfallTrend` | number | Yes | Signed % change vs previous day |
| `lowStockCount` | number | Yes | Number of products below threshold |
| `topLowStockItems` | string[] | Yes | Up to 3 product names with lowest stock |

#### Response — 200 OK

```json
{
  "status": "success",
  "data": {
    "storeCode": "STORE-042",
    "generatedAt": "2026-04-30T09:15:02Z",
    "reasons": [
      {
        "rank": 1,
        "description": "Morning foot traffic dropped 15% due to road construction on Main Street diverting commuters.",
        "category": "external"
      },
      {
        "rank": 2,
        "description": "Three high-demand dairy products are critically low, causing missed basket additions.",
        "category": "inventory"
      },
      {
        "rank": 3,
        "description": "Checkout wait times increased to 8 minutes during lunch rush due to understaffing.",
        "category": "staffing"
      },
      {
        "rank": 4,
        "description": "Competitor launched a 20% off promotion on household essentials this week.",
        "category": "external"
      },
      {
        "rank": 5,
        "description": "Afternoon footfall recovered but average basket size is 12% lower than last week.",
        "category": "sales"
      }
    ],
    "isFallback": false
  },
  "error": null,
  "timestamp": "2026-04-30T09:15:02Z"
}
```

`category` values: `"sales"` | `"footfall"` | `"inventory"` | `"staffing"` | `"external"`

#### Response — 200 OK (Fallback — Bedrock Unavailable)

```json
{
  "status": "success",
  "data": {
    "storeCode": "STORE-042",
    "generatedAt": "2026-04-30T09:15:02Z",
    "reasons": [
      {
        "rank": 1,
        "description": "AI insights are temporarily unavailable. Common factors affecting sales include inventory gaps, staffing levels, and local events.",
        "category": "sales"
      }
    ],
    "isFallback": true
  },
  "error": null,
  "timestamp": "2026-04-30T09:15:02Z"
}
```

#### Response — 400 Bad Request

```json
{
  "status": "error",
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Missing required fields: dailySales, footfall",
    "fallback": false
  },
  "timestamp": "2026-04-30T09:15:02Z"
}
```

---

### 3. POST `/api/v1/recommendations`

Generates 2–3 prioritized corrective action recommendations.

**UI Mapping**: Recommendations Panel

**DB sources**: reads nothing; no write to DB

#### Request Body

Same structure as `/api/v1/insights`:

```json
{
  "storeCode": "STORE-042",
  "date": "2026-04-30",
  "dailySales": 12450.75,
  "salesTrend": 8.2,
  "footfall": 342,
  "footfallTrend": -3.1,
  "lowStockCount": 3,
  "topLowStockItems": ["Organic Milk 1L", "Whole Wheat Bread", "Free Range Eggs"]
}
```

#### Response — 200 OK

```json
{
  "status": "success",
  "data": {
    "storeCode": "STORE-042",
    "generatedAt": "2026-04-30T09:16:00Z",
    "recommendations": [
      {
        "id": "a3f1c2d4-0001-0000-0000-000000000001",
        "priority": "high",
        "category": "restocking",
        "description": "Immediately restock Organic Milk 1L and Free Range Eggs — these are top-selling items with only 2 and 4 units remaining respectively.",
        "actionLabel": "Restock Now"
      },
      {
        "id": "a3f1c2d4-0001-0000-0000-000000000002",
        "priority": "high",
        "category": "staffing",
        "description": "Reallocate one team member from stocking to checkout between 11:30 AM–1:30 PM to reduce wait times during lunch rush.",
        "actionLabel": "Adjust Schedule"
      },
      {
        "id": "a3f1c2d4-0001-0000-0000-000000000003",
        "priority": "medium",
        "category": "promotion",
        "description": "Launch a 'Buy 2 Get 1' promotion on aisle 3 household essentials to counter competitor pricing.",
        "actionLabel": "Start Promotion"
      }
    ],
    "isFallback": false
  },
  "error": null,
  "timestamp": "2026-04-30T09:16:00Z"
}
```

`priority` values: `"high"` | `"medium"` | `"low"`
`category` values: `"promotion"` | `"staffing"` | `"restocking"`

#### Response — 200 OK (Fallback)

```json
{
  "status": "success",
  "data": {
    "storeCode": "STORE-042",
    "generatedAt": "2026-04-30T09:16:00Z",
    "recommendations": [
      {
        "id": "a3f1c2d4-0002-0000-0000-000000000001",
        "priority": "medium",
        "category": "restocking",
        "description": "Review and restock any items below threshold levels.",
        "actionLabel": "Check Inventory"
      },
      {
        "id": "a3f1c2d4-0002-0000-0000-000000000002",
        "priority": "medium",
        "category": "staffing",
        "description": "Ensure adequate checkout coverage during peak hours (11 AM–2 PM).",
        "actionLabel": "Review Schedule"
      }
    ],
    "isFallback": true
  },
  "error": null,
  "timestamp": "2026-04-30T09:16:00Z"
}
```

---

### 4. GET `/api/v1/inventory`

Returns low-stock products sorted by urgency, computed from `products` + `product_inventory`.

**UI Mapping**: Inventory Panel

**DB sources**: `products` (sku, name, category, low_stock_threshold), `product_inventory` (current_stock, updated_at)

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `storeCode` | string | Yes | Store code, e.g. `STORE-042` |

#### Response — 200 OK (Low-Stock Items Present)

```json
{
  "status": "success",
  "data": {
    "storeCode": "STORE-042",
    "asOf": "2026-04-30T09:00:00Z",
    "items": [
      {
        "sku": "SKU-1042",
        "productName": "Organic Milk 1L",
        "category": "Dairy",
        "currentStock": 2,
        "threshold": 10,
        "urgencyScore": 5.0,
        "status": "critical"
      },
      {
        "sku": "SKU-2187",
        "productName": "Free Range Eggs (12pk)",
        "category": "Dairy",
        "currentStock": 4,
        "threshold": 12,
        "urgencyScore": 3.0,
        "status": "critical"
      },
      {
        "sku": "SKU-0891",
        "productName": "Whole Wheat Bread",
        "category": "Bakery",
        "currentStock": 5,
        "threshold": 15,
        "urgencyScore": 3.0,
        "status": "low"
      }
    ]
  },
  "error": null,
  "timestamp": "2026-04-30T09:15:00Z"
}
```

> `urgencyScore` = `threshold / currentStock` (higher = more urgent). `status`: `"critical"` when `currentStock < threshold * 0.3`, otherwise `"low"`. `asOf` = `MAX(product_inventory.updated_at)` across returned items.

#### Response — 200 OK (All Stock Adequate)

```json
{
  "status": "success",
  "data": {
    "storeCode": "STORE-042",
    "asOf": "2026-04-30T09:00:00Z",
    "items": []
  },
  "error": null,
  "timestamp": "2026-04-30T09:15:00Z"
}
```

---

### 5. POST `/api/v1/copilot/sessions`

Creates a new copilot session for a store. Returns the `sessionId` (UUID) the frontend uses for all subsequent messages.

**UI Mapping**: Called once when the Copilot Panel mounts

**DB write**: inserts into `copilot_sessions`

#### Request Body

```json
{
  "storeCode": "STORE-042"
}
```

#### Response — 201 Created

```json
{
  "status": "success",
  "data": {
    "sessionId": "f7a3c1e2-4b56-7890-abcd-ef1234567890",
    "storeCode": "STORE-042",
    "startedAt": "2026-04-30T09:10:00Z"
  },
  "error": null,
  "timestamp": "2026-04-30T09:10:00Z"
}
```

---

### 6. POST `/api/v1/copilot/query`

Sends a natural language query to the copilot. Persists both the user message and assistant reply to `copilot_messages`.

**UI Mapping**: Copilot Panel (chat input, quick-prompt chips)

**DB read**: `copilot_sessions` (validate session exists), `copilot_messages` (load history if not sent by client)
**DB write**: inserts two rows into `copilot_messages` (user message + assistant reply)

#### Request Body

```json
{
  "sessionId": "f7a3c1e2-4b56-7890-abcd-ef1234567890",
  "query": "Why are sales down compared to yesterday?",
  "storeContext": {
    "storeCode": "STORE-042",
    "date": "2026-04-30",
    "dailySales": 12450.75,
    "salesTrend": 8.2,
    "footfall": 342,
    "footfallTrend": -3.1,
    "lowStockCount": 3,
    "topLowStockItems": ["Organic Milk 1L", "Whole Wheat Bread", "Free Range Eggs"]
  },
  "conversationHistory": [
    {
      "id": "c1a2b3d4-0000-0000-0000-000000000001",
      "role": "user",
      "content": "Give me today's summary",
      "createdAt": "2026-04-30T09:10:00Z"
    },
    {
      "id": "c1a2b3d4-0000-0000-0000-000000000002",
      "role": "assistant",
      "content": "Today's sales are $12,450 (up 8.2% from yesterday). Footfall is 342 visitors, slightly down 3.1%. You have 3 low-stock alerts requiring attention.",
      "createdAt": "2026-04-30T09:10:02Z"
    }
  ]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `sessionId` | UUID | Yes | From `POST /api/v1/copilot/sessions` |
| `query` | string | Yes | User's question. Must not be whitespace-only. |
| `storeContext` | object | Yes | Current store metrics for prompt context |
| `conversationHistory` | array | Yes | Prior messages in this session (can be `[]`) |

#### Response — 200 OK

```json
{
  "status": "success",
  "data": {
    "id": "c1a2b3d4-0000-0000-0000-000000000003",
    "role": "assistant",
    "content": "Sales are actually up 8.2% today compared to yesterday. However, footfall is down 3.1%, which suggests customers are spending more per visit. The drop in foot traffic is likely due to the road construction on Main Street. Your average basket size has increased, which is a positive sign.",
    "createdAt": "2026-04-30T09:15:03Z",
    "isFallback": false
  },
  "error": null,
  "timestamp": "2026-04-30T09:15:03Z"
}
```

#### Response — 200 OK (Fallback — AI Unavailable)

```json
{
  "status": "success",
  "data": {
    "id": "c1a2b3d4-0000-0000-0000-000000000004",
    "role": "assistant",
    "content": "I'm sorry, the AI service is temporarily unavailable. Please try again in a few moments. In the meantime, you can review your KPI dashboard for the latest store metrics.",
    "createdAt": "2026-04-30T09:15:03Z",
    "isFallback": true
  },
  "error": null,
  "timestamp": "2026-04-30T09:15:03Z"
}
```

#### Response — 400 (Whitespace Query)

```json
{
  "status": "error",
  "data": null,
  "error": {
    "code": "INVALID_QUERY",
    "message": "Query must contain non-whitespace characters",
    "fallback": false
  },
  "timestamp": "2026-04-30T09:15:03Z"
}
```

#### Response — 404 (Session Not Found)

```json
{
  "status": "error",
  "data": null,
  "error": {
    "code": "SESSION_NOT_FOUND",
    "message": "Copilot session f7a3c1e2-4b56-7890-abcd-ef1234567890 not found",
    "fallback": false
  },
  "timestamp": "2026-04-30T09:15:03Z"
}
```

---

## Internal API Endpoints (Backend → Prompt Service)

Not exposed to the frontend. No DB interaction — pure prompt construction and Bedrock dispatch.

### 7. POST `/internal/prompt/insights`

```json
{
  "storeContext": {
    "storeCode": "STORE-042",
    "date": "2026-04-30",
    "dailySales": 12450.75,
    "salesTrend": 8.2,
    "footfall": 342,
    "footfallTrend": -3.1,
    "lowStockCount": 3,
    "topLowStockItems": ["Organic Milk 1L", "Whole Wheat Bread", "Free Range Eggs"]
  },
  "maxReasons": 5
}
```

**Response — 200 OK**

```json
{
  "reasons": [
    {
      "rank": 1,
      "description": "Morning foot traffic dropped 15% due to road construction on Main Street.",
      "category": "external"
    },
    {
      "rank": 2,
      "description": "Three high-demand dairy products are critically low.",
      "category": "inventory"
    }
  ],
  "isFallback": false,
  "modelId": "anthropic.claude-3-sonnet",
  "latencyMs": 1842
}
```

---

### 8. POST `/internal/prompt/recommendations`

```json
{
  "storeContext": {
    "storeCode": "STORE-042",
    "date": "2026-04-30",
    "dailySales": 12450.75,
    "salesTrend": 8.2,
    "footfall": 342,
    "footfallTrend": -3.1,
    "lowStockCount": 3,
    "topLowStockItems": ["Organic Milk 1L", "Whole Wheat Bread", "Free Range Eggs"]
  },
  "minRecommendations": 2,
  "maxRecommendations": 3
}
```

**Response — 200 OK**

```json
{
  "recommendations": [
    {
      "id": "a3f1c2d4-0001-0000-0000-000000000001",
      "priority": "high",
      "category": "restocking",
      "description": "Immediately restock Organic Milk 1L and Free Range Eggs.",
      "actionLabel": "Restock Now"
    },
    {
      "id": "a3f1c2d4-0001-0000-0000-000000000002",
      "priority": "high",
      "category": "staffing",
      "description": "Reallocate staff to checkout during lunch rush.",
      "actionLabel": "Adjust Schedule"
    }
  ],
  "isFallback": false,
  "modelId": "anthropic.claude-3-sonnet",
  "latencyMs": 2103
}
```

---

### 9. POST `/internal/prompt/query`

```json
{
  "storeContext": {
    "storeCode": "STORE-042",
    "date": "2026-04-30",
    "dailySales": 12450.75,
    "salesTrend": 8.2,
    "footfall": 342,
    "footfallTrend": -3.1,
    "lowStockCount": 3,
    "topLowStockItems": ["Organic Milk 1L", "Whole Wheat Bread", "Free Range Eggs"]
  },
  "query": "Why are sales down compared to yesterday?",
  "conversationHistory": [
    { "role": "user",      "content": "Give me today's summary" },
    { "role": "assistant", "content": "Today's sales are $12,450 (up 8.2% from yesterday)..." }
  ]
}
```

**Response — 200 OK**

```json
{
  "content": "Sales are actually up 8.2% today compared to yesterday. However, footfall is down 3.1%...",
  "isFallback": false,
  "modelId": "anthropic.claude-3-sonnet",
  "latencyMs": 1567
}
```

---

## Error Codes Reference

| Code | HTTP Status | Description | Frontend Handling |
|------|-------------|-------------|-------------------|
| `VALIDATION_ERROR` | 400 | Missing or invalid request fields | Show field-level error message |
| `INVALID_QUERY` | 400 | Whitespace-only copilot query | Show "Please enter a question" hint |
| `STORE_NOT_FOUND` | 404 | Store code not recognized | Show "Store not found" message |
| `SESSION_NOT_FOUND` | 404 | Copilot session UUID not found | Re-create session and retry |
| `BEDROCK_UNAVAILABLE` | 503 | Bedrock service unreachable | Show fallback content (amber info box) |
| `BEDROCK_TIMEOUT` | 504 | Bedrock response exceeded 5s | Show timeout fallback message |
| `PARSE_ERROR` | 500 | AI response could not be parsed | Show fallback content |
| `INTERNAL_ERROR` | 500 | Unexpected server error | Show "Something went wrong" + retry button |

---

## Request/Response Headers

| Header | Direction | Required | Description |
|--------|-----------|----------|-------------|
| `Content-Type: application/json` | Request | Yes (POST) | Body format |
| `Accept: application/json` | Request | Yes | Expected response format |
| `X-Request-Id` | Both | No | UUID for tracing; echoed in response |
| `X-Response-Time` | Response | — | Server processing time in ms |

---

## Timeouts

| Endpoint | Timeout | Reason |
|----------|---------|--------|
| `GET /api/v1/kpis` | 3s | Requirement: render within 3s |
| `GET /api/v1/inventory` | 3s | DB query only |
| `POST /api/v1/copilot/sessions` | 2s | DB insert only |
| `POST /api/v1/insights` | 5s | Bedrock round-trip |
| `POST /api/v1/recommendations` | 5s | Bedrock round-trip |
| `POST /api/v1/copilot/query` | 5s | Bedrock round-trip |

Frontend uses `AbortController` with these timeouts. On timeout, AI endpoints return fallback content.

---

## Endpoint-to-DB Table Mapping

| Endpoint | Tables Read | Tables Written |
|----------|-------------|----------------|
| `GET /api/v1/kpis` | `stores`, `orders`, `footfall_entries`, `products`, `product_inventory` | — |
| `POST /api/v1/insights` | — | — |
| `POST /api/v1/recommendations` | — | — |
| `GET /api/v1/inventory` | `stores`, `products`, `product_inventory` | — |
| `POST /api/v1/copilot/sessions` | `stores` | `copilot_sessions` |
| `POST /api/v1/copilot/query` | `copilot_sessions` | `copilot_messages` (×2) |
