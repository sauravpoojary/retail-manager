import {
  createContext,
  useContext,
  useReducer,
  useCallback,
  useEffect,
  type ReactNode,
} from 'react';
import type { AppState, CopilotMessage, StoreContext } from '../types';
import * as api from '../api/client';

// ─── Constants ───────────────────────────────────────────────────────────────

const STORE_CODE = 'STORE-042';
const STORE_NAME = 'Downtown Branch';

// ─── Actions ─────────────────────────────────────────────────────────────────

type Action =
  | { type: 'KPI_LOADING' }
  | { type: 'KPI_SUCCESS'; payload: AppState['kpis'] }
  | { type: 'KPI_ERROR' }
  | { type: 'INSIGHT_LOADING' }
  | { type: 'INSIGHT_SUCCESS'; payload: AppState['insights'] }
  | { type: 'INSIGHT_ERROR' }
  | { type: 'REC_LOADING' }
  | { type: 'REC_SUCCESS'; payload: AppState['recommendations'] }
  | { type: 'REC_ERROR' }
  | { type: 'INV_LOADING' }
  | { type: 'INV_SUCCESS'; payload: AppState['inventory'] }
  | { type: 'INV_ERROR' }
  | { type: 'SESSION_SET'; payload: string }
  | { type: 'COPILOT_LOADING' }
  | { type: 'COPILOT_MESSAGE'; payload: CopilotMessage }
  | { type: 'COPILOT_ERROR' };

// ─── Initial state ───────────────────────────────────────────────────────────

const initial: AppState = {
  storeCode: STORE_CODE,
  storeName: STORE_NAME,
  kpis: null,
  kpiStatus: 'idle',
  insights: null,
  insightStatus: 'idle',
  recommendations: null,
  recommendationStatus: 'idle',
  inventory: null,
  inventoryStatus: 'idle',
  copilotSessionId: null,
  copilotMessages: [],
  copilotStatus: 'idle',
};

// ─── Reducer ─────────────────────────────────────────────────────────────────

function reducer(state: AppState, action: Action): AppState {
  switch (action.type) {
    case 'KPI_LOADING':      return { ...state, kpiStatus: 'loading' };
    case 'KPI_SUCCESS':      return { ...state, kpiStatus: 'success', kpis: action.payload };
    case 'KPI_ERROR':        return { ...state, kpiStatus: 'error' };
    case 'INSIGHT_LOADING':  return { ...state, insightStatus: 'loading' };
    case 'INSIGHT_SUCCESS':  return { ...state, insightStatus: 'success', insights: action.payload };
    case 'INSIGHT_ERROR':    return { ...state, insightStatus: 'error' };
    case 'REC_LOADING':      return { ...state, recommendationStatus: 'loading' };
    case 'REC_SUCCESS':      return { ...state, recommendationStatus: 'success', recommendations: action.payload };
    case 'REC_ERROR':        return { ...state, recommendationStatus: 'error' };
    case 'INV_LOADING':      return { ...state, inventoryStatus: 'loading' };
    case 'INV_SUCCESS':      return { ...state, inventoryStatus: 'success', inventory: action.payload };
    case 'INV_ERROR':        return { ...state, inventoryStatus: 'error' };
    case 'SESSION_SET':      return { ...state, copilotSessionId: action.payload };
    case 'COPILOT_LOADING':  return { ...state, copilotStatus: 'loading' };
    case 'COPILOT_MESSAGE':  return {
      ...state,
      copilotStatus: 'idle',
      copilotMessages: [...state.copilotMessages, action.payload],
    };
    case 'COPILOT_ERROR':    return { ...state, copilotStatus: 'error' };
    default:                 return state;
  }
}

// ─── Context ─────────────────────────────────────────────────────────────────

interface AppContextValue {
  state: AppState;
  loadKpis: () => Promise<void>;
  loadInsights: () => Promise<void>;
  loadRecommendations: () => Promise<void>;
  loadInventory: () => Promise<void>;
  refreshAll: () => Promise<void>;
  sendMessage: (query: string) => Promise<void>;
}

const AppContext = createContext<AppContextValue | null>(null);

// ─── Provider ────────────────────────────────────────────────────────────────

export function AppProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(reducer, initial);

  // Build store context from current KPI state for AI calls
  const buildStoreContext = useCallback((): StoreContext => {
    const kpis = state.kpis;
    const inventory = state.inventory;
    const topItems = inventory?.items.slice(0, 3).map((i) => i.productName) ?? [];
    return {
      storeCode: STORE_CODE,
      date: kpis?.date ?? new Date().toISOString().split('T')[0],
      dailySales: kpis?.dailySales?.amount ?? 0,
      salesTrend: kpis?.dailySales?.trendPercent ?? 0,
      footfall: kpis?.footfall?.count ?? 0,
      footfallTrend: kpis?.footfall?.trendPercent ?? 0,
      lowStockCount: kpis?.lowStockAlertCount ?? 0,
      topLowStockItems: topItems,
    };
  }, [state.kpis, state.inventory]);

  const loadKpis = useCallback(async () => {
    dispatch({ type: 'KPI_LOADING' });
    try {
      const res = await api.getKpis(STORE_CODE);
      if (res.status === 'success' && res.data) {
        dispatch({ type: 'KPI_SUCCESS', payload: res.data });
      } else {
        dispatch({ type: 'KPI_ERROR' });
      }
    } catch {
      dispatch({ type: 'KPI_ERROR' });
    }
  }, []);

  const loadInventory = useCallback(async () => {
    dispatch({ type: 'INV_LOADING' });
    try {
      const res = await api.getInventory(STORE_CODE);
      if (res.status === 'success' && res.data) {
        dispatch({ type: 'INV_SUCCESS', payload: res.data });
      } else {
        dispatch({ type: 'INV_ERROR' });
      }
    } catch {
      dispatch({ type: 'INV_ERROR' });
    }
  }, []);

  const loadInsights = useCallback(async () => {
    dispatch({ type: 'INSIGHT_LOADING' });
    try {
      const res = await api.getInsights(buildStoreContext());
      if (res.status === 'success' && res.data) {
        dispatch({ type: 'INSIGHT_SUCCESS', payload: res.data });
      } else {
        dispatch({ type: 'INSIGHT_ERROR' });
      }
    } catch {
      dispatch({ type: 'INSIGHT_ERROR' });
    }
  }, [buildStoreContext]);

  const loadRecommendations = useCallback(async () => {
    dispatch({ type: 'REC_LOADING' });
    try {
      const res = await api.getRecommendations(buildStoreContext());
      if (res.status === 'success' && res.data) {
        dispatch({ type: 'REC_SUCCESS', payload: res.data });
      } else {
        dispatch({ type: 'REC_ERROR' });
      }
    } catch {
      dispatch({ type: 'REC_ERROR' });
    }
  }, [buildStoreContext]);

  const refreshAll = useCallback(async () => {
    await Promise.all([loadKpis(), loadInventory()]);
  }, [loadKpis, loadInventory]);

  // Initialise session on mount
  useEffect(() => {
    api.createCopilotSession(STORE_CODE).then((res) => {
      if (res.status === 'success' && res.data) {
        dispatch({ type: 'SESSION_SET', payload: res.data.sessionId });
      }
    }).catch(() => {/* session creation failed — will retry on first query */});
  }, []);

  // Load KPIs and inventory on mount
  useEffect(() => {
    loadKpis();
    loadInventory();
  }, [loadKpis, loadInventory]);

  const sendMessage = useCallback(async (query: string) => {
    if (!query.trim()) return;

    // Optimistically add user message
    const userMsg: CopilotMessage = {
      id: crypto.randomUUID(),
      role: 'user',
      content: query,
      createdAt: new Date().toISOString(),
    };
    dispatch({ type: 'COPILOT_MESSAGE', payload: userMsg });
    dispatch({ type: 'COPILOT_LOADING' });

    // Ensure we have a session
    let sessionId = state.copilotSessionId;
    if (!sessionId) {
      try {
        const res = await api.createCopilotSession(STORE_CODE);
        if (res.status === 'success' && res.data) {
          sessionId = res.data.sessionId;
          dispatch({ type: 'SESSION_SET', payload: sessionId });
        }
      } catch {
        dispatch({ type: 'COPILOT_ERROR' });
        return;
      }
    }

    try {
      const res = await api.sendCopilotQuery({
        sessionId: sessionId!,
        query,
        storeContext: buildStoreContext(),
        conversationHistory: state.copilotMessages,
      });

      if (res.status === 'success' && res.data) {
        const assistantMsg: CopilotMessage = {
          id: res.data.id,
          role: 'assistant',
          content: res.data.content,
          createdAt: res.data.createdAt,
          isFallback: res.data.isFallback,
        };
        dispatch({ type: 'COPILOT_MESSAGE', payload: assistantMsg });
      } else {
        dispatch({ type: 'COPILOT_ERROR' });
      }
    } catch {
      dispatch({ type: 'COPILOT_ERROR' });
    }
  }, [state.copilotSessionId, state.copilotMessages, buildStoreContext]);

  return (
    <AppContext.Provider value={{
      state,
      loadKpis,
      loadInsights,
      loadRecommendations,
      loadInventory,
      refreshAll,
      sendMessage,
    }}>
      {children}
    </AppContext.Provider>
  );
}

export function useApp(): AppContextValue {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error('useApp must be used within AppProvider');
  return ctx;
}
