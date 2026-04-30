import React from 'react';
import { DollarSign, Users, AlertTriangle, TrendingUp, TrendingDown, Minus } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { SkeletonBox } from './ui/Skeleton';

export function KpiStrip() {
  const { state } = useApp();
  const { kpis, kpiStatus } = state;

  return (
    <div style={styles.strip}>
      <KpiCard
        label="Daily Sales"
        icon={<DollarSign size={22} color="#057A55" />}
        iconBg="#DCFCE7"
        value={kpis?.dailySales ? `$${kpis.dailySales.amount.toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}` : null}
        trend={kpis?.dailySales ? { percent: kpis.dailySales.trendPercent, direction: kpis.dailySales.trendDirection } : null}
        trendLabel="vs yesterday"
        loading={kpiStatus === 'loading'}
        chartColor="#057A55"
      />
      <KpiCard
        label="Footfall"
        icon={<Users size={22} color="#1A56DB" />}
        iconBg="#DBEAFE"
        value={kpis?.footfall ? kpis.footfall.count.toLocaleString() : null}
        trend={kpis?.footfall ? { percent: kpis.footfall.trendPercent, direction: kpis.footfall.trendDirection } : null}
        trendLabel="vs yesterday"
        loading={kpiStatus === 'loading'}
        chartColor="#1A56DB"
      />
      <KpiCard
        label="Low Stock Alerts"
        icon={<AlertTriangle size={22} color="#C27803" />}
        iconBg="#FEF3C7"
        value={kpis?.lowStockAlertCount != null ? String(kpis.lowStockAlertCount) : null}
        trend={null}
        trendLabel="View details"
        trendIsLink
        loading={kpiStatus === 'loading'}
        chartColor="#C27803"
      />
    </div>
  );
}

interface TrendInfo {
  percent: number;
  direction: 'up' | 'down' | 'flat';
}

interface KpiCardProps {
  label: string;
  icon: React.ReactNode;
  iconBg: string;
  value: string | null;
  trend: TrendInfo | null;
  trendLabel: string;
  trendIsLink?: boolean;
  loading: boolean;
  chartColor: string;
}

function KpiCard({ label, icon, iconBg, value, trend, trendLabel, trendIsLink, loading }: KpiCardProps) {
  return (
    <div style={styles.card}>
      <div style={styles.cardTop}>
        <div style={{ ...styles.iconWrap, background: iconBg }}>{icon}</div>
        <div style={styles.cardMeta}>
          <div style={styles.cardLabel}>{label}</div>
          {loading ? (
            <SkeletonBox width={80} height={28} />
          ) : (
            <div style={styles.cardValue}>{value ?? '—'}</div>
          )}
        </div>
      </div>
      <div style={styles.cardBottom}>
        {loading ? (
          <SkeletonBox width={100} height={14} />
        ) : trend ? (
          <TrendBadge percent={trend.percent} direction={trend.direction} label={trendLabel} />
        ) : trendIsLink ? (
          <span style={styles.viewLink}>{trendLabel} &rsaquo;</span>
        ) : null}
      </div>
    </div>
  );
}

function TrendBadge({ percent, direction, label }: { percent: number; direction: string; label: string }) {
  const isUp = direction === 'up';
  const isDown = direction === 'down';
  const color = isUp ? '#057A55' : isDown ? '#C81E1E' : '#6B7280';
  const Icon = isUp ? TrendingUp : isDown ? TrendingDown : Minus;
  const sign = isUp ? '+' : '';

  return (
    <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
      <Icon size={13} color={color} />
      <span style={{ color, fontSize: 12, fontWeight: 600 }}>
        {sign}{Math.abs(percent).toFixed(1)}%
      </span>
      <span style={{ color: '#6B7280', fontSize: 12 }}>{label}</span>
    </span>
  );
}

const styles: Record<string, React.CSSProperties> = {
  strip: {
    display: 'grid',
    gridTemplateColumns: 'repeat(3, 1fr)',
    gap: 12,
    padding: '16px 16px 0',
  },
  card: {
    background: '#fff',
    borderRadius: 12,
    padding: '14px 16px',
    border: '1px solid #E5E7EB',
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
    display: 'flex',
    flexDirection: 'column',
    gap: 10,
  },
  cardTop: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: 10,
  },
  iconWrap: {
    width: 40,
    height: 40,
    borderRadius: 10,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  cardMeta: {
    flex: 1,
    minWidth: 0,
  },
  cardLabel: {
    fontSize: 12,
    color: '#6B7280',
    marginBottom: 2,
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  cardValue: {
    fontSize: 22,
    fontWeight: 700,
    color: '#111827',
    lineHeight: 1.2,
  },
  cardBottom: {
    minHeight: 18,
  },
  viewLink: {
    fontSize: 12,
    color: '#1A56DB',
    fontWeight: 500,
    cursor: 'pointer',
  },
};
