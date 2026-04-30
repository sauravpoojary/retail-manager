import React, { useState, type ReactNode } from 'react';
import { ChevronUp, ChevronDown } from 'lucide-react';

interface PanelCardProps {
  icon: ReactNode;
  title: string;
  subtitle?: string;
  action?: ReactNode;
  children: ReactNode;
  collapsible?: boolean;
  defaultOpen?: boolean;
}

export function PanelCard({
  icon,
  title,
  subtitle,
  action,
  children,
  collapsible = false,
  defaultOpen = true,
}: PanelCardProps) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <div style={styles.card}>
      <div style={styles.header}>
        <div style={styles.headerLeft}>
          <span style={styles.iconWrap}>{icon}</span>
          <div>
            <div style={styles.title}>{title}</div>
            {subtitle && <div style={styles.subtitle}>{subtitle}</div>}
          </div>
        </div>
        <div style={styles.headerRight}>
          {action}
          {collapsible && (
            <button
              style={styles.chevronBtn}
              onClick={() => setOpen((o) => !o)}
              aria-label={open ? 'Collapse' : 'Expand'}
            >
              {open ? <ChevronUp size={18} color="#6B7280" /> : <ChevronDown size={18} color="#6B7280" />}
            </button>
          )}
        </div>
      </div>
      {open && <div style={styles.body}>{children}</div>}
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  card: {
    background: '#fff',
    borderRadius: 12,
    border: '1px solid #E5E7EB',
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
    overflow: 'hidden',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: '14px 16px',
    borderBottom: '1px solid #F3F4F6',
  },
  headerLeft: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
  },
  iconWrap: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: 36,
    height: 36,
    borderRadius: 8,
    background: '#EFF6FF',
    flexShrink: 0,
  },
  title: {
    fontSize: 15,
    fontWeight: 600,
    color: '#111827',
  },
  subtitle: {
    fontSize: 12,
    color: '#6B7280',
    marginTop: 1,
  },
  headerRight: {
    display: 'flex',
    alignItems: 'center',
    gap: 8,
  },
  chevronBtn: {
    background: 'none',
    border: 'none',
    cursor: 'pointer',
    padding: 4,
    display: 'flex',
    alignItems: 'center',
  },
  body: {
    padding: '14px 16px',
  },
};
