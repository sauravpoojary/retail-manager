import type {
  ApiResponse,
  KpiData,
  InsightData,
  RecommendationData,
  InventoryData,
  StoreContext,
  CopilotMessage,
} from '../types';

const BASE = (import.meta.env.VITE_API_BASE_URL ?? '') + '/api/v1';

// ─── Timeout helper ──────────────────────────────────────────────────────────

function withTimeout(ms: number): AbortSignal {
  return AbortSignal.timeout(ms);
}

async function request<T>(
  url: string,
  options: RequestInit = {},
  timeoutMs = 5000,
): Promise<ApiResponse<T>> {
  const res = await fetch(url, {
    ...options,
    signal: withTimeout(timeoutMs),
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...options.headers,
    },
  });
  return res.json() as Promise<ApiResponse<T>>;
}

// ─── KPI ─────────────────────────────────────────────────────────────────────

export function getKpis(storeCode: string): Promise<ApiResponse<KpiData>> {
  return request<KpiData>(
    `${BASE}/kpis?storeCode=${encodeURIComponent(storeCode)}`,
    {},
    3000,
  );
}

// ─── Insights ────────────────────────────────────────────────────────────────

export function getInsights(context: StoreContext): Promise<ApiResponse<InsightData>> {
  return request<InsightData>(`${BASE}/insights`, {
    method: 'POST',
    body: JSON.stringify(context),
  });
}

// ─── Recommendations ─────────────────────────────────────────────────────────

export function getRecommendations(context: StoreContext): Promise<ApiResponse<RecommendationData>> {
  return request<RecommendationData>(`${BASE}/recommendations`, {
    method: 'POST',
    body: JSON.stringify(context),
  });
}

// ─── Inventory ───────────────────────────────────────────────────────────────

export function getInventory(storeCode: string): Promise<ApiResponse<InventoryData>> {
  return request<InventoryData>(
    `${BASE}/inventory?storeCode=${encodeURIComponent(storeCode)}`,
    {},
    3000,
  );
}

// ─── Copilot ─────────────────────────────────────────────────────────────────

export interface CreateSessionResponse {
  sessionId: string;
  storeCode: string;
  startedAt: string;
}

export function createCopilotSession(
  storeCode: string,
): Promise<ApiResponse<CreateSessionResponse>> {
  return request<CreateSessionResponse>(
    `${BASE}/copilot/sessions`,
    { method: 'POST', body: JSON.stringify({ storeCode }) },
    2000,
  );
}

export interface CopilotQueryResponse {
  id: string;
  role: 'assistant';
  content: string;
  createdAt: string;
  isFallback: boolean;
}

export function sendCopilotQuery(payload: {
  sessionId: string;
  query: string;
  storeContext: StoreContext;
  conversationHistory: CopilotMessage[];
}): Promise<ApiResponse<CopilotQueryResponse>> {
  return request<CopilotQueryResponse>(`${BASE}/copilot/query`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
