# Database Schema Design — Retail Store Manager Copilot

## Overview

Simplified PostgreSQL schema. KPIs (daily sales, footfall, low-stock count) are computed at runtime from raw tables. No pre-aggregated KPI table.

**Identifier convention**: `stores.store_code` (e.g. `STORE-042`) is the external-facing identifier used in all API requests/responses. Internal foreign keys use UUIDs.

**Core tables:**
- `stores` — store registry
- `products` — product catalog with stock thresholds
- `product_inventory` — current stock levels
- `orders` + `order_items` — sales transactions (KPI source for daily sales)
- `footfall_entries` — customer entry events (KPI source for footfall)
- `copilot_sessions` + `copilot_messages` — chat history
- `quick_prompts` — configurable copilot chips

---

## Entity Relationship Diagram

```
stores
  ├── products (1:N)
  │     └── product_inventory (1:1)
  ├── orders (1:N)
  │     └── order_items (1:N) ──▶ products
  ├── footfall_entries (1:N)
  └── copilot_sessions (1:N)
        └── copilot_messages (1:N)

quick_prompts  (standalone)
```

---

## Enum Types

```sql
CREATE TYPE order_status    AS ENUM ('completed', 'refunded', 'voided');
CREATE TYPE message_role    AS ENUM ('user', 'assistant');
CREATE TYPE stock_status    AS ENUM ('critical', 'low');
```

---

## Tables

### 1. `stores`

```sql
CREATE TABLE stores (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_code  VARCHAR(20) NOT NULL UNIQUE,   -- e.g. 'STORE-042'
    name        VARCHAR(200) NOT NULL,
    currency    VARCHAR(3)  NOT NULL DEFAULT 'USD',
    timezone    VARCHAR(50) NOT NULL DEFAULT 'UTC',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

**Example:**

| store_code | name | currency | timezone |
|-----------|------|----------|----------|
| STORE-042 | Downtown Market | USD | America/New_York |

---

### 2. `products`

```sql
CREATE TABLE products (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id            UUID        NOT NULL REFERENCES stores(id),
    sku                 VARCHAR(30) NOT NULL,
    name                VARCHAR(200) NOT NULL,
    category            VARCHAR(100),
    low_stock_threshold INTEGER     NOT NULL DEFAULT 10,
    is_active           BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE (store_id, sku)
);

CREATE INDEX idx_products_store_id ON products(store_id);
```

**Example:**

| sku | name | category | low_stock_threshold |
|-----|------|----------|---------------------|
| SKU-1042 | Organic Milk 1L | Dairy | 10 |
| SKU-2187 | Free Range Eggs (12pk) | Dairy | 12 |
| SKU-0891 | Whole Wheat Bread | Bakery | 15 |

---

### 3. `product_inventory`

Current stock level per product. One row per product, updated in place on restock/sale.

```sql
CREATE TABLE product_inventory (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID    NOT NULL REFERENCES products(id) UNIQUE,
    current_stock   INTEGER NOT NULL DEFAULT 0 CHECK (current_stock >= 0),
    last_restocked  TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

**Example:**

| product (sku) | current_stock | last_restocked |
|---------------|---------------|----------------|
| SKU-1042 | 2 | 2026-04-28T06:00:00Z |
| SKU-2187 | 4 | 2026-04-29T07:30:00Z |
| SKU-0891 | 5 | 2026-04-27T06:00:00Z |

---

### 4. `orders`

One row per sales transaction. Source for daily sales KPI.

```sql
CREATE TABLE orders (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID         NOT NULL REFERENCES stores(id),
    order_ref       VARCHAR(50)  NOT NULL UNIQUE,  -- e.g. 'ORD-20260430-0091'
    status          order_status NOT NULL DEFAULT 'completed',
    total_amount    NUMERIC(12,2) NOT NULL,
    ordered_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_store_ordered_at ON orders(store_id, ordered_at DESC);
```

**Example:**

| order_ref | status | total_amount | ordered_at |
|-----------|--------|-------------|-----------|
| ORD-20260430-0091 | completed | 34.50 | 2026-04-30T09:05:00Z |
| ORD-20260430-0092 | completed | 12.99 | 2026-04-30T09:07:00Z |
| ORD-20260430-0093 | refunded | 22.00 | 2026-04-30T09:15:00Z |

---

### 5. `order_items`

Line items within an order. Useful for basket analysis and per-product sales.

```sql
CREATE TABLE order_items (
    id          UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id    UUID           NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id  UUID           NOT NULL REFERENCES products(id),
    quantity    INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price  NUMERIC(10,2)  NOT NULL,
    line_total  NUMERIC(12,2)  GENERATED ALWAYS AS (quantity * unit_price) STORED
);

CREATE INDEX idx_order_items_order_id   ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
```

**Example:**

| order_ref | product sku | quantity | unit_price | line_total |
|-----------|-------------|----------|-----------|-----------|
| ORD-20260430-0091 | SKU-1042 | 2 | 1.99 | 3.98 |
| ORD-20260430-0091 | SKU-0891 | 1 | 3.49 | 3.49 |

---

### 6. `footfall_entries`

One row per customer entry event. Source for footfall KPI.

```sql
CREATE TABLE footfall_entries (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id    UUID        NOT NULL REFERENCES stores(id),
    entered_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_footfall_store_entered_at ON footfall_entries(store_id, entered_at DESC);
```

**Example:**

| store_code | entered_at |
|-----------|-----------|
| STORE-042 | 2026-04-30T09:01:00Z |
| STORE-042 | 2026-04-30T09:03:00Z |
| STORE-042 | 2026-04-30T09:04:00Z |

> For mock data, bulk-insert N rows for a given day to simulate footfall count.

---

### 7. `copilot_sessions`

One session per manager interaction window. Created via `POST /api/v1/copilot/sessions` — the returned `id` (UUID) is the `sessionId` used in all subsequent `POST /api/v1/copilot/query` calls.

```sql
CREATE TABLE copilot_sessions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID        NOT NULL REFERENCES stores(id),
    started_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_active_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_copilot_sessions_store_id ON copilot_sessions(store_id);
```

---

### 8. `copilot_messages`

Chat messages within a session, in order.

```sql
CREATE TABLE copilot_messages (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id  UUID         NOT NULL REFERENCES copilot_sessions(id) ON DELETE CASCADE,
    role        message_role NOT NULL,
    content     TEXT         NOT NULL,
    is_fallback BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_copilot_messages_session_id ON copilot_messages(session_id, created_at ASC);
```

**Example:**

| role | content | is_fallback |
|------|---------|-------------|
| user | Give me today's summary | false |
| assistant | Today's sales are $12,450 (up 8.2%)... | false |
| user | Why are sales down? | false |
| assistant | Sales are actually up 8.2% today... | false |

---

### 9. `quick_prompts`

Configurable chips shown in the Copilot Panel.

```sql
CREATE TABLE quick_prompts (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    label       VARCHAR(100) NOT NULL,    -- chip display text
    query_text  TEXT         NOT NULL,    -- full query sent to API
    sort_order  SMALLINT     NOT NULL DEFAULT 0,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
```

**Example:**

| label | query_text | sort_order |
|-------|-----------|-----------|
| Why are sales down? | Why are sales down compared to yesterday? | 1 |
| What needs restocking? | Which products need restocking most urgently? | 2 |
| Staffing tips | What staffing adjustments should I make today? | 3 |
| Today's summary | Give me a summary of today's store performance. | 4 |

---

## Runtime KPI Queries

These queries power the `GET /api/v1/kpis` endpoint. All computed on the fly.

### Daily Sales (today vs yesterday)

```sql
-- Today's total sales
SELECT COALESCE(SUM(total_amount), 0) AS today_sales
FROM orders
WHERE store_id  = :storeId
  AND status    = 'completed'
  AND ordered_at >= CURRENT_DATE AT TIME ZONE :timezone
  AND ordered_at <  CURRENT_DATE AT TIME ZONE :timezone + INTERVAL '1 day';

-- Yesterday's total (for trend %)
SELECT COALESCE(SUM(total_amount), 0) AS yesterday_sales
FROM orders
WHERE store_id  = :storeId
  AND status    = 'completed'
  AND ordered_at >= (CURRENT_DATE - 1) AT TIME ZONE :timezone
  AND ordered_at <  CURRENT_DATE AT TIME ZONE :timezone;

-- Trend %: ((today - yesterday) / yesterday) * 100
```

### Footfall (today vs yesterday)

```sql
-- Today's footfall
SELECT COUNT(*) AS today_footfall
FROM footfall_entries
WHERE store_id  = :storeId
  AND entered_at >= CURRENT_DATE AT TIME ZONE :timezone
  AND entered_at <  CURRENT_DATE AT TIME ZONE :timezone + INTERVAL '1 day';
```

### Low-Stock Count

```sql
SELECT COUNT(*) AS low_stock_count
FROM products p
JOIN product_inventory pi ON pi.product_id = p.id
WHERE p.store_id  = :storeId
  AND p.is_active = TRUE
  AND pi.current_stock < p.low_stock_threshold;
```

### Low-Stock Product List (Inventory Panel)

```sql
SELECT
    p.sku,
    p.name,
    pi.current_stock,
    p.low_stock_threshold                                          AS threshold,
    ROUND(p.low_stock_threshold::NUMERIC / NULLIF(pi.current_stock, 0), 2) AS urgency_score,
    CASE
        WHEN pi.current_stock < (p.low_stock_threshold * 0.3) THEN 'critical'
        ELSE 'low'
    END AS status
FROM products p
JOIN product_inventory pi ON pi.product_id = p.id
WHERE p.store_id  = :storeId
  AND p.is_active = TRUE
  AND pi.current_stock < p.low_stock_threshold
ORDER BY urgency_score DESC;
```

---

## Seed Data

```sql
-- Store
INSERT INTO stores (id, store_code, name, currency, timezone)
VALUES ('d47e5f6a-0000-0000-0000-000000000001', 'STORE-042', 'Downtown Market', 'USD', 'America/New_York');

-- Products
INSERT INTO products (id, store_id, sku, name, category, low_stock_threshold) VALUES
('prod-0000-0000-0000-000000000001', 'd47e5f6a-0000-0000-0000-000000000001', 'SKU-1042', 'Organic Milk 1L',        'Dairy',  10),
('prod-0000-0000-0000-000000000002', 'd47e5f6a-0000-0000-0000-000000000001', 'SKU-2187', 'Free Range Eggs (12pk)', 'Dairy',  12),
('prod-0000-0000-0000-000000000003', 'd47e5f6a-0000-0000-0000-000000000001', 'SKU-0891', 'Whole Wheat Bread',      'Bakery', 15),
('prod-0000-0000-0000-000000000004', 'd47e5f6a-0000-0000-0000-000000000001', 'SKU-3301', 'Almond Butter 500g',     'Pantry',  8),
('prod-0000-0000-0000-000000000005', 'd47e5f6a-0000-0000-0000-000000000001', 'SKU-4420', 'Greek Yogurt 500g',      'Dairy',  20);

-- Inventory
INSERT INTO product_inventory (product_id, current_stock, last_restocked) VALUES
('prod-0000-0000-0000-000000000001',  2, '2026-04-28T06:00:00Z'),
('prod-0000-0000-0000-000000000002',  4, '2026-04-29T07:30:00Z'),
('prod-0000-0000-0000-000000000003',  5, '2026-04-27T06:00:00Z'),
('prod-0000-0000-0000-000000000004', 25, '2026-04-30T06:00:00Z'),
('prod-0000-0000-0000-000000000005', 18, '2026-04-30T06:00:00Z');

-- Orders (today)
INSERT INTO orders (store_id, order_ref, status, total_amount, ordered_at) VALUES
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260430-0091', 'completed',  34.50, '2026-04-30T09:05:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260430-0092', 'completed',  12.99, '2026-04-30T09:07:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260430-0093', 'completed',  22.00, '2026-04-30T10:15:00Z');
-- (add more rows to reach ~$12,450 total for the day in a real seed)

-- Orders (yesterday — for trend calculation)
INSERT INTO orders (store_id, order_ref, status, total_amount, ordered_at) VALUES
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260429-0088', 'completed', 28.00, '2026-04-29T09:10:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260429-0089', 'completed', 45.50, '2026-04-29T10:30:00Z');

-- Footfall (today — 342 entries)
-- In practice, generate 342 rows. Example of a few:
INSERT INTO footfall_entries (store_id, entered_at) VALUES
('d47e5f6a-0000-0000-0000-000000000001', '2026-04-30T08:01:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', '2026-04-30T08:04:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', '2026-04-30T08:09:00Z');

-- Quick Prompts
INSERT INTO quick_prompts (label, query_text, sort_order) VALUES
('Why are sales down?',    'Why are sales down compared to yesterday?',        1),
('What needs restocking?', 'Which products need restocking most urgently?',     2),
('Staffing tips',          'What staffing adjustments should I make today?',    3),
('Today''s summary',       'Give me a summary of today''s store performance.', 4);
```

---

## Schema Summary

| Table | Rows represent | API endpoint |
|-------|---------------|--------------|
| `stores` | Retail locations | All endpoints (lookup by `store_code`) |
| `products` | Product catalog | `GET /api/v1/inventory`, `GET /api/v1/kpis` |
| `product_inventory` | Current stock per product | `GET /api/v1/inventory`, `GET /api/v1/kpis` |
| `orders` | Sales transactions | `GET /api/v1/kpis` (sales + trend) |
| `order_items` | Line items per order | Future: per-product sales breakdown |
| `footfall_entries` | Customer entry events | `GET /api/v1/kpis` (footfall + trend) |
| `copilot_sessions` | Chat sessions | `POST /api/v1/copilot/sessions` (create), `POST /api/v1/copilot/query` (validate) |
| `copilot_messages` | Chat messages | `POST /api/v1/copilot/query` (write x2) |
| `quick_prompts` | Copilot chip config | Hardcoded in frontend or fetched separately |
