import React from 'react';
import { Sparkles, Clock } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { PanelCard } from './ui/PanelCard';
import { SkeletonLines } from './ui/Skeleton';
import { FallbackBox } from './ui/FallbackBox';

export function InsightPanel() {
  const { state, loadInsights } = useApp();
  const { insights, insightStatus } = state;

  const generatedIn = insights?.generatedAt
    ? ((Date.now() - new Date(insights.generatedAt).getTime()) / 1000).toFixed(1)
    : null;

  const action = (
    <button style={styles.analyzeBtn} onClick={loadInsights} disabled={insightStatus === 'loading'}>
      <Sparkles size={14} />
      {insightStatus === 'loading' ? 'Analyzing...' : 'Analyze Sales'}
    </button>
  );

  return (
    <PanelCard
      icon={<Sparkles size={18} color="#1A56DB" />}
      title="AI Insights"
      subtitle="Understand what's driving your sales"
      action={action}
      collapsible
    >
      {insightStatus === 'loading' && <SkeletonLines count={4} />}

      {insightStatus === 'error' && (
        <FallbackBox message="AI insights temporarily unavailable. Please try again later." />
      )}

      {insightStatus === 'success' && insights && (
        <>
          {insights.isFallback && (
            <FallbackBox message="AI insights are temporarily unavailable. Showing default guidance." />
          )}
          <ol style={styles.list}>
            {insights.reasons.map((r) => (
              <li key={r.rank} style={styles.item}>
                <span style={styles.rank}>{r.rank}</span>
                <span style={styles.desc}>{r.description}</span>
              </li>
            ))}
          </ol>
          {generatedIn && !insights.isFallback && (
            <div style={styles.generatedRow}>
              <Clock size={12} color="#9CA3AF" />
              <span style={styles.generatedText}>Generated in {generatedIn}s</span>
              <span style={styles.aiBadge}>AI Generated</span>
            </div>
          )}
        </>
      )}

      {insightStatus === 'idle' && (
        <p style={styles.hint}>Click "Analyze Sales" to get AI-powered insights about your store performance.</p>
      )}
    </PanelCard>
  );
}

const styles: Record<string, React.CSSProperties> = {
  analyzeBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: 6,
    background: '#1A56DB',
    color: '#fff',
    border: 'none',
    borderRadius: 8,
    padding: '7px 14px',
    fontSize: 13,
    fontWeight: 500,
    cursor: 'pointer',
    whiteSpace: 'nowrap',
  },
  list: {
    listStyle: 'none',
    padding: 0,
    margin: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: 10,
  },
  item: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: 10,
    padding: '8px 10px',
    borderRadius: 8,
    background: '#F9FAFB',
    borderLeft: '3px solid #1A56DB',
  },
  rank: {
    minWidth: 22,
    height: 22,
    borderRadius: 6,
    background: '#1A56DB',
    color: '#fff',
    fontSize: 12,
    fontWeight: 700,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  desc: {
    fontSize: 13,
    color: '#374151',
    lineHeight: 1.5,
  },
  generatedRow: {
    display: 'flex',
    alignItems: 'center',
    gap: 5,
    marginTop: 10,
  },
  generatedText: {
    fontSize: 11,
    color: '#9CA3AF',
    flex: 1,
  },
  aiBadge: {
    fontSize: 11,
    color: '#057A55',
    background: '#DCFCE7',
    borderRadius: 20,
    padding: '2px 8px',
    fontWeight: 500,
  },
  hint: {
    fontSize: 13,
    color: '#9CA3AF',
    textAlign: 'center',
    padding: '8px 0',
    margin: 0,
  },
};
