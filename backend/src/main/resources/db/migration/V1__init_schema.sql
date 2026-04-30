-- ============================================================
-- V1 — Initial schema for Retail Store Manager Copilot
-- ============================================================

-- Enum types
CREATE TYPE order_status  AS ENUM ('completed', 'refunded', 'voided');
CREATE TYPE message_role  AS ENUM ('user', 'assistant');
CREATE TYPE stock_status  AS ENUM ('critical', 'low');

-- ------------------------------------------------------------
-- stores
-- ------------------------------------------------------------
CREATE TABLE stores (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_code  VARCHAR(20) NOT NULL UNIQUE,
    name        VARCHAR(200) NOT NULL,
    currency    VARCHAR(3)  NOT NULL DEFAULT 'USD',
    timezone    VARCHAR(50) NOT NULL DEFAULT 'UTC',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- products
-- ------------------------------------------------------------
CREATE TABLE products (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id            UUID         NOT NULL REFERENCES stores(id),
    sku                 VARCHAR(30)  NOT NULL,
    name                VARCHAR(200) NOT NULL,
    category            VARCHAR(100),
    low_stock_threshold INTEGER      NOT NULL DEFAULT 10,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (store_id, sku)
);

CREATE INDEX idx_products_store_id ON products(store_id);

-- ------------------------------------------------------------
-- product_inventory
-- ------------------------------------------------------------
CREATE TABLE product_inventory (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id      UUID        NOT NULL REFERENCES products(id) UNIQUE,
    current_stock   INTEGER     NOT NULL DEFAULT 0 CHECK (current_stock >= 0),
    last_restocked  TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ------------------------------------------------------------
-- orders
-- ------------------------------------------------------------
CREATE TABLE orders (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id      UUID         NOT NULL REFERENCES stores(id),
    order_ref     VARCHAR(50)  NOT NULL UNIQUE,
    status        order_status NOT NULL DEFAULT 'completed',
    total_amount  NUMERIC(12,2) NOT NULL,
    ordered_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_store_ordered_at ON orders(store_id, ordered_at DESC);

-- ------------------------------------------------------------
-- order_items
-- ------------------------------------------------------------
CREATE TABLE order_items (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id    UUID          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id  UUID          NOT NULL REFERENCES products(id),
    quantity    INTEGER       NOT NULL CHECK (quantity > 0),
    unit_price  NUMERIC(10,2) NOT NULL,
    line_total  NUMERIC(12,2) GENERATED ALWAYS AS (quantity * unit_price) STORED
);

CREATE INDEX idx_order_items_order_id   ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- ------------------------------------------------------------
-- footfall_entries
-- ------------------------------------------------------------
CREATE TABLE footfall_entries (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id    UUID        NOT NULL REFERENCES stores(id),
    entered_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_footfall_store_entered_at ON footfall_entries(store_id, entered_at DESC);

-- ------------------------------------------------------------
-- copilot_sessions
-- ------------------------------------------------------------
CREATE TABLE copilot_sessions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id        UUID        NOT NULL REFERENCES stores(id),
    started_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_active_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_copilot_sessions_store_id ON copilot_sessions(store_id);

-- ------------------------------------------------------------
-- copilot_messages
-- ------------------------------------------------------------
CREATE TABLE copilot_messages (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id  UUID         NOT NULL REFERENCES copilot_sessions(id) ON DELETE CASCADE,
    role        message_role NOT NULL,
    content     TEXT         NOT NULL,
    is_fallback BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_copilot_messages_session_id ON copilot_messages(session_id, created_at ASC);

-- ------------------------------------------------------------
-- quick_prompts
-- ------------------------------------------------------------
CREATE TABLE quick_prompts (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    label       VARCHAR(100) NOT NULL,
    query_text  TEXT         NOT NULL,
    sort_order  SMALLINT     NOT NULL DEFAULT 0,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
