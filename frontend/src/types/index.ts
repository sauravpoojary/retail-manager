// ─── API Envelope ────────────────────────────────────────────────────────────

export interface ApiError {
  code: string;
  message: string;
  fallback: boolean;
}

export interface ApiResponse<T> {
  status: 'success' | 'error';
  data: T | null;
  error: ApiError | null;
  timestamp: string;
}

// ─── KPI ─────────────────────────────────────────────────────────────────────

export interface DailySales {
  amount: number;
  currency: string;
  trendPercent: number;
  trendDirection: 'up' | 'down' | 'flat';
}

export interface Footfall {
  count: number;
  trendPercent: number;
  trendDirection: 'up' | 'down' | 'flat';
}

export interface KpiData {
  storeCode: string;
  date: string;
  dailySales: DailySales | null;
  footfall: Footfall | null;
  lowStockAlertCount: number | null;
  asOf: string;
}

// ─── Insights ────────────────────────────────────────────────────────────────

export type InsightCategory = 'sales' | 'footfall' | 'inventory' | 'staffing' | 'external';

export interface InsightReason {
  rank: number;
  description: string;
  category: InsightCategory;
}

export interface InsightData {
  storeCode: string;
  generatedAt: string;
  reasons: InsightReason[];
  isFallback: boolean;
}

// ─── Recommendations ─────────────────────────────────────────────────────────

export type RecommendationPriority = 'high' | 'medium' | 'low';
export type RecommendationCategory = 'promotion' | 'staffing' | 'restocking';

export interface Recommendation {
  id: string;
  priority: RecommendationPriority;
  category: RecommendationCategory;
  description: string;
  actionLabel: string;
}

export interface RecommendationData {
  storeCode: string;
  generatedAt: string;
  recommendations: Recommendation[];
  isFallback: boolean;
}

// ─── Inventory ───────────────────────────────────────────────────────────────

export type StockStatus = 'critical' | 'low';

export interface InventoryItem {
  sku: string;
  productName: string;
  category: string;
  currentStock: number;
  threshold: number;
  urgencyScore: number;
  status: StockStatus;
}

export interface InventoryData {
  storeCode: string;
  asOf: string;
  items: InventoryItem[];
}

// ─── Copilot ─────────────────────────────────────────────────────────────────

export type MessageRole = 'user' | 'assistant';

export interface CopilotMessage {
  id: string;
  role: MessageRole;
  content: string;
  createdAt: string;
  isFallback?: boolean;
}

export interface StoreContext {
  storeCode: string;
  date: string;
  dailySales: number;
  salesTrend: number;
  footfall: number;
  footfallTrend: number;
  lowStockCount: number;
  topLowStockItems: string[];
}

// ─── App State ───────────────────────────────────────────────────────────────

export type LoadStatus = 'idle' | 'loading' | 'success' | 'error';

export interface AppState {
  storeCode: string;
  storeName: string;
  kpis: KpiData | null;
  kpiStatus: LoadStatus;
  insights: InsightData | null;
  insightStatus: LoadStatus;
  recommendations: RecommendationData | null;
  recommendationStatus: LoadStatus;
  inventory: InventoryData | null;
  inventoryStatus: LoadStatus;
  copilotSessionId: string | null;
  copilotMessages: CopilotMessage[];
  copilotStatus: LoadStatus;
}
