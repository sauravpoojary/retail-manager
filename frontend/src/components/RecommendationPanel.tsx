import React from 'react';
import { Target, Tag, Users, Package, ChevronRight } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { PanelCard } from './ui/PanelCard';
import { SkeletonLines } from './ui/Skeleton';
import { FallbackBox } from './ui/FallbackBox';
import type { RecommendationCategory, RecommendationPriority } from '../types';

const PRIORITY_COLORS: Record<RecommendationPriority, { bg: string; text: string }> = {
  high:   { bg: '#FEE2E2', text: '#C81E1E' },
  medium: { bg: '#FEF3C7', text: '#C27803' },
  low:    { bg: '#DCFCE7', text: '#057A55' },
};

const CATEGORY_ICONS: Record<RecommendationCategory, React.ReactNode> = {
  promotion:  <Tag size={18} color="#C27803" />,
  staffing:   <Users size={18} color="#1A56DB" />,
  restocking: <Package size={18} color="#057A55" />,
};

const CATEGORY_BG: Record<RecommendationCategory, string> = {
  promotion:  '#FEF3C7',
  staffing:   '#DBEAFE',
  restocking: '#DCFCE7',
};

export function RecommendationPanel() {
  const { state, loadRecommendations } = useApp();
  const { recommendations, recommendationStatus } = state;

  const action = (
    <button
      style={styles.recBtn}
      onClick={loadRecommendations}
      disabled={recommendationStatus === 'loading'}
    >
      <Target size={14} />
      {recommendationStatus === 'loading' ? 'Loading...' : 'Get Recommendations'}
    </button>
  );

  return (
    <PanelCard
      icon={<Target size={18} color="#1A56DB" />}
      title="Recommendations"
      subtitle="Actionable steps to improve performance"
      action={action}
      collapsible
    >
      {recommendationStatus === 'loading' && <SkeletonLines count={3} />}

      {recommendationStatus === 'error' && (
        <FallbackBox message="Recommendations temporarily unavailable. Please try again." />
      )}

      {recommendationStatus === 'success' && recommendations && (
        <>
          {recommendations.isFallback && (
            <FallbackBox message="Showing default recommendations. AI service is temporarily unavailable." />
          )}
          <div style={styles.list}>
            {recommendations.recommendations.map((rec) => {
              const priority = PRIORITY_COLORS[rec.priority];
              const catBg = CATEGORY_BG[rec.category];
              return (
                <div key={rec.id} style={styles.item}>
                  <div style={{ ...styles.catIcon, background: catBg }}>
                    {CATEGORY_ICONS[rec.category]}
                  </div>
                  <div style={styles.itemContent}>
                    <div style={styles.itemTitle}>{rec.description}</div>
                    <div style={styles.itemSub}>{rec.actionLabel}</div>
                  </div>
                  <span style={{ ...styles.priorityBadge, background: priority.bg, color: priority.text }}>
                    {rec.priority.toUpperCase()}
                  </span>
                  <ChevronRight size={16} color="#9CA3AF" />
                </div>
              );
            })}
          </div>
        </>
      )}

      {recommendationStatus === 'idle' && (
        <p style={styles.hint}>Click "Get Recommendations" for AI-powered action suggestions.</p>
      )}
    </PanelCard>
  );
}

const styles: Record<string, React.CSSProperties> = {
  recBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: 6,
    background: '#fff',
    color: '#1A56DB',
    border: '1.5px solid #1A56DB',
    borderRadius: 8,
    padding: '7px 14px',
    fontSize: 13,
    fontWeight: 500,
    cursor: 'pointer',
    whiteSpace: 'nowrap',
  },
  list: {
    display: 'flex',
    flexDirection: 'column',
    gap: 8,
  },
  item: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    padding: '10px 12px',
    borderRadius: 10,
    border: '1px solid #F3F4F6',
    background: '#FAFAFA',
    cursor: 'pointer',
  },
  catIcon: {
    width: 38,
    height: 38,
    borderRadius: 10,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  itemContent: {
    flex: 1,
    minWidth: 0,
  },
  itemTitle: {
    fontSize: 13,
    fontWeight: 500,
    color: '#111827',
    lineHeight: 1.4,
  },
  itemSub: {
    fontSize: 11,
    color: '#6B7280',
    marginTop: 2,
  },
  priorityBadge: {
    fontSize: 11,
    fontWeight: 700,
    borderRadius: 20,
    padding: '3px 8px',
    whiteSpace: 'nowrap',
    flexShrink: 0,
  },
  hint: {
    fontSize: 13,
    color: '#9CA3AF',
    textAlign: 'center',
    padding: '8px 0',
    margin: 0,
  },
};
