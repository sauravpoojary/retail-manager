import React from 'react';
import { Menu, RefreshCw } from 'lucide-react';
import { useApp } from '../context/AppContext';

export function Header() {
  const { state, refreshAll } = useApp();
  const isRefreshing = state.kpiStatus === 'loading' || state.inventoryStatus === 'loading';

  const lastUpdated = state.kpis?.asOf
    ? new Date(state.kpis.asOf).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    : null;

  return (
    <header style={styles.header}>
      <div style={styles.left}>
        <button style={styles.menuBtn} aria-label="Open menu">
          <Menu size={20} color="#fff" />
        </button>
        <div>
          <div style={styles.title}>Store Copilot</div>
          <div style={styles.subtitle}>
            Store ID: {state.storeCode} &bull; {state.storeName}
          </div>
        </div>
      </div>

      <div style={styles.right}>
        <button
          style={styles.refreshBtn}
          onClick={refreshAll}
          disabled={isRefreshing}
          aria-label="Refresh dashboard"
        >
          <RefreshCw
            size={16}
            color="#fff"
            style={{ animation: isRefreshing ? 'spin 1s linear infinite' : 'none' }}
          />
          <span style={styles.refreshLabel}>Refresh</span>
        </button>
        {lastUpdated && (
          <div style={styles.lastUpdated}>Last updated: {lastUpdated}</div>
        )}
      </div>

      <style>{`
        @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
      `}</style>
    </header>
  );
}

const styles: Record<string, React.CSSProperties> = {
  header: {
    position: 'sticky',
    top: 0,
    zIndex: 100,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '0 16px',
    height: 64,
    background: '#1A56DB',
    boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
  },
  left: {
    display: 'flex',
    alignItems: 'center',
    gap: 12,
  },
  menuBtn: {
    background: 'none',
    border: 'none',
    cursor: 'pointer',
    padding: 8,
    borderRadius: 8,
    display: 'flex',
    alignItems: 'center',
  },
  title: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 700,
    lineHeight: 1.2,
  },
  subtitle: {
    color: 'rgba(255,255,255,0.75)',
    fontSize: 12,
  },
  right: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-end',
    gap: 2,
  },
  refreshBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: 6,
    background: 'rgba(255,255,255,0.15)',
    border: '1px solid rgba(255,255,255,0.3)',
    borderRadius: 8,
    padding: '6px 12px',
    cursor: 'pointer',
    color: '#fff',
  },
  refreshLabel: {
    color: '#fff',
    fontSize: 13,
    fontWeight: 500,
  },
  lastUpdated: {
    color: 'rgba(255,255,255,0.7)',
    fontSize: 11,
  },
};
