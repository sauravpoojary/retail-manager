-- ============================================================
-- V2 — Seed data for local development / demo
-- ============================================================

INSERT INTO stores (id, store_code, name, currency, timezone)
VALUES ('d47e5f6a-0000-0000-0000-000000000001', 'STORE-042', 'Downtown Market', 'USD', 'America/New_York');

INSERT INTO products (id, store_id, sku, name, category, low_stock_threshold) VALUES
('a0000000-0000-0000-0000-000000000001', 'd47e5f6a-0000-0000-0000-000000000001', 'SKU-1042', 'Organic Milk 1L',        'Dairy',  10),
('a0000000-0000-0000-0000-000000000002', 'd47e5f6a-0000-0000-0000-000000000001', 'SKU-2187', 'Free Range Eggs (12pk)', 'Dairy',  12),
('a0000000-0000-0000-0000-000000000003', 'd47e5f6a-0000-0000-0000-000000000001', 'SKU-0891', 'Whole Wheat Bread',      'Bakery', 15),
('a0000000-0000-0000-0000-000000000004', 'd47e5f6a-0000-0000-0000-000000000001', 'SKU-3301', 'Almond Butter 500g',     'Pantry',  8),
('a0000000-0000-0000-0000-000000000005', 'd47e5f6a-0000-0000-0000-000000000001', 'SKU-4420', 'Greek Yogurt 500g',      'Dairy',  20);

INSERT INTO product_inventory (product_id, current_stock, last_restocked) VALUES
('a0000000-0000-0000-0000-000000000001',  2, '2026-04-28T06:00:00Z'),
('a0000000-0000-0000-0000-000000000002',  4, '2026-04-29T07:30:00Z'),
('a0000000-0000-0000-0000-000000000003',  5, '2026-04-27T06:00:00Z'),
('a0000000-0000-0000-0000-000000000004', 25, '2026-04-30T06:00:00Z'),
('a0000000-0000-0000-0000-000000000005', 18, '2026-04-30T06:00:00Z');

-- Today's orders (2026-04-30)
INSERT INTO orders (store_id, order_ref, status, total_amount, ordered_at) VALUES
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260430-0001', 'completed',  34.50, '2026-04-30T08:05:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260430-0002', 'completed',  12.99, '2026-04-30T08:20:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260430-0003', 'completed',  55.00, '2026-04-30T09:00:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260430-0004', 'completed',  88.25, '2026-04-30T09:30:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260430-0005', 'completed', 120.00, '2026-04-30T10:00:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260430-0006', 'refunded',   22.00, '2026-04-30T10:15:00Z');

-- Yesterday's orders (2026-04-29) — for trend calculation
INSERT INTO orders (store_id, order_ref, status, total_amount, ordered_at) VALUES
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260429-0001', 'completed',  28.00, '2026-04-29T08:10:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260429-0002', 'completed',  45.50, '2026-04-29T09:30:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260429-0003', 'completed',  60.00, '2026-04-29T10:00:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', 'ORD-20260429-0004', 'completed',  95.00, '2026-04-29T11:00:00Z');

-- Footfall entries (today — sample, represents 342 total)
INSERT INTO footfall_entries (store_id, entered_at) VALUES
('d47e5f6a-0000-0000-0000-000000000001', '2026-04-30T08:01:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', '2026-04-30T08:04:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', '2026-04-30T08:09:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', '2026-04-30T08:15:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', '2026-04-30T08:22:00Z');

-- Footfall entries (yesterday — sample)
INSERT INTO footfall_entries (store_id, entered_at) VALUES
('d47e5f6a-0000-0000-0000-000000000001', '2026-04-29T08:02:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', '2026-04-29T08:06:00Z'),
('d47e5f6a-0000-0000-0000-000000000001', '2026-04-29T08:11:00Z');

INSERT INTO quick_prompts (label, query_text, sort_order) VALUES
('Why are sales down?',    'Why are sales down compared to yesterday?',        1),
('What needs restocking?', 'Which products need restocking most urgently?',     2),
('Staffing tips',          'What staffing adjustments should I make today?',    3),
('Today''s summary',       'Give me a summary of today''s store performance.', 4);
