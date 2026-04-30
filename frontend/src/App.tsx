import React from 'react';
import { AppProvider } from './context/AppContext';
import { Header } from './components/Header';
import { KpiStrip } from './components/KpiStrip';
import { InsightPanel } from './components/InsightPanel';
import { RecommendationPanel } from './components/RecommendationPanel';
import { InventoryPanel } from './components/InventoryPanel';
import { CopilotPanel } from './components/CopilotPanel';
import { BottomNav } from './components/BottomNav';

function Dashboard() {
  return (
    <div style={styles.root}>
      <Header />

      <main style={styles.main}>
        {/* KPI strip — always full width */}
        <KpiStrip />

        {/* Two-column layout on desktop, single column on mobile */}
        <div style={styles.grid}>
          {/* Left column */}
          <div style={styles.leftCol}>
            <InsightPanel />
            <RecommendationPanel />
            <InventoryPanel />
          </div>

          {/* Right column — Copilot */}
          <div style={styles.rightCol}>
            <div style={styles.copilotSticky}>
              <CopilotPanel />
            </div>
          </div>
        </div>
      </main>

      <BottomNav />

      <style>{`
        *, *::before, *::after { box-sizing: border-box; }
        body {
          margin: 0;
          font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
          background: #F9FAFB;
          color: #111827;
          -webkit-font-smoothing: antialiased;
        }
        ::-webkit-scrollbar { width: 4px; height: 4px; }
        ::-webkit-scrollbar-track { background: transparent; }
        ::-webkit-scrollbar-thumb { background: #D1D5DB; border-radius: 4px; }

        /* Responsive grid */
        @media (min-width: 1024px) {
          .dashboard-grid {
            grid-template-columns: 60fr 40fr !important;
          }
          .copilot-sticky {
            position: sticky !important;
            top: 80px !important;
          }
        }
      `}</style>
    </div>
  );
}

export default function App() {
  return (
    <AppProvider>
      <Dashboard />
    </AppProvider>
  );
}

const styles: Record<string, React.CSSProperties> = {
  root: {
    minHeight: '100dvh',
    display: 'flex',
    flexDirection: 'column',
    background: '#F9FAFB',
  },
  main: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    gap: 12,
    padding: '0 0 16px',
    maxWidth: 1280,
    width: '100%',
    margin: '0 auto',
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: '1fr',
    gap: 12,
    padding: '0 16px',
  },
  leftCol: {
    display: 'flex',
    flexDirection: 'column',
    gap: 12,
  },
  rightCol: {
    display: 'flex',
    flexDirection: 'column',
  },
  copilotSticky: {
    // becomes sticky on desktop via CSS class
  },
};
