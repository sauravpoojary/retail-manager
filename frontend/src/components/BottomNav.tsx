import React from 'react';
import { Home, FileText, Bell, CheckSquare, MoreHorizontal } from 'lucide-react';

const NAV_ITEMS = [
  { icon: Home,          label: 'Dashboard', active: true  },
  { icon: FileText,      label: 'Reports',   active: false },
  { icon: Bell,          label: 'Alerts',    active: false, badge: 3 },
  { icon: CheckSquare,   label: 'Tasks',     active: false },
  { icon: MoreHorizontal,label: 'More',      active: false },
];

export function BottomNav() {
  return (
    <nav style={styles.nav} aria-label="Main navigation">
      {NAV_ITEMS.map(({ icon: Icon, label, active, badge }) => (
        <button key={label} style={styles.item} aria-current={active ? 'page' : undefined}>
          <div style={styles.iconWrap}>
            <Icon size={22} color={active ? '#1A56DB' : '#6B7280'} />
            {badge != null && <span style={styles.badge}>{badge}</span>}
          </div>
          <span style={{ ...styles.label, color: active ? '#1A56DB' : '#6B7280' }}>{label}</span>
        </button>
      ))}
    </nav>
  );
}

const styles: Record<string, React.CSSProperties> = {
  nav: {
    position: 'sticky',
    bottom: 0,
    display: 'flex',
    background: '#fff',
    borderTop: '1px solid #E5E7EB',
    padding: '8px 0 env(safe-area-inset-bottom)',
    zIndex: 100,
  },
  item: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: 3,
    background: 'none',
    border: 'none',
    cursor: 'pointer',
    padding: '4px 0',
    minHeight: 44,
  },
  iconWrap: {
    position: 'relative',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  badge: {
    position: 'absolute',
    top: -4,
    right: -8,
    background: '#C81E1E',
    color: '#fff',
    fontSize: 10,
    fontWeight: 700,
    borderRadius: 10,
    padding: '1px 5px',
    minWidth: 16,
    textAlign: 'center',
  },
  label: {
    fontSize: 11,
    fontWeight: 500,
  },
};
