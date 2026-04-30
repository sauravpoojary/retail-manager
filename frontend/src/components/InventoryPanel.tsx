import React from 'react';
import { Package, CheckCircle, ChevronRight } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { PanelCard } from './ui/PanelCard';
import { SkeletonLines } from './ui/Skeleton';

export function InventoryPanel() {
  const { state } = useApp();
  const { inventory, inventoryStatus } = state;
  const items = inventory?.items ?? [];

  const action = (
    <button style={styles.viewAllBtn}>
      View All <ChevronRight size={14} />
    </button>
  );

  return (
    <PanelCard
      icon={<Package size={18} color="#1A56DB" />}
      title="Inventory Alerts"
      subtitle="Low stock items that need attention"
      action={items.length > 0 ? action : undefined}
    >
      {inventoryStatus === 'loading' && <SkeletonLines count={3} />}

      {inventoryStatus === 'success' && items.length === 0 && (
        <div style={styles.adequate}>
          <CheckCircle size={20} color="#057A55" />
          <span style={styles.adequateText}>All stock levels are adequate</span>
        </div>
      )}

      {inventoryStatus === 'success' && items.length > 0 && (
        <>
          <table style={styles.table}>
            <thead>
              <tr>
                <th style={styles.th}>Product</th>
                <th style={{ ...styles.th, textAlign: 'center' }}>Current Stock</th>
                <th style={{ ...styles.th, textAlign: 'center' }}>Threshold</th>
                <th style={{ ...styles.th, textAlign: 'center' }}>Status</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.sku} style={styles.row}>
                  <td style={styles.td}>
                    <div style={styles.productName}>{item.productName}</div>
                    <div style={styles.sku}>{item.sku}</div>
                  </td>
                  <td style={{ ...styles.td, textAlign: 'center' }}>
                    <span style={{
                      ...styles.stockNum,
                      color: item.status === 'critical' ? '#C81E1E' : '#C27803',
                    }}>
                      {item.currentStock}
                    </span>
                  </td>
                  <td style={{ ...styles.td, textAlign: 'center', color: '#6B7280' }}>
                    {item.threshold}
                  </td>
                  <td style={{ ...styles.td, textAlign: 'center' }}>
                    <StatusBadge status={item.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div style={styles.footer}>
            <CheckCircle size={14} color="#057A55" />
            <span style={styles.footerText}>
              Showing {items.length} of {items.length} low stock items
            </span>
            <span style={styles.sortedBy}>Sorted by urgency ⓘ</span>
          </div>
        </>
      )}
    </PanelCard>
  );
}

function StatusBadge({ status }: { status: 'critical' | 'low' }) {
  const isCritical = status === 'critical';
  return (
    <span style={{
      fontSize: 11,
      fontWeight: 700,
      borderRadius: 20,
      padding: '3px 10px',
      background: isCritical ? '#FEE2E2' : '#FEF3C7',
      color: isCritical ? '#C81E1E' : '#C27803',
    }}>
      {status.toUpperCase()}
    </span>
  );
}

const styles: Record<string, React.CSSProperties> = {
  adequate: {
    display: 'flex',
    alignItems: 'center',
    gap: 8,
    padding: '12px 0',
  },
  adequateText: {
    fontSize: 14,
    color: '#057A55',
    fontWeight: 500,
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
  },
  th: {
    fontSize: 12,
    color: '#6B7280',
    fontWeight: 500,
    padding: '6px 8px',
    textAlign: 'left',
    borderBottom: '1px solid #F3F4F6',
  },
  row: {
    borderBottom: '1px solid #F9FAFB',
  },
  td: {
    padding: '10px 8px',
    fontSize: 13,
    color: '#374151',
    verticalAlign: 'middle',
  },
  productName: {
    fontWeight: 500,
    color: '#111827',
    fontSize: 13,
  },
  sku: {
    fontSize: 11,
    color: '#9CA3AF',
    marginTop: 2,
  },
  stockNum: {
    fontWeight: 700,
    fontSize: 15,
  },
  footer: {
    display: 'flex',
    alignItems: 'center',
    gap: 6,
    marginTop: 10,
    paddingTop: 10,
    borderTop: '1px solid #F3F4F6',
  },
  footerText: {
    fontSize: 12,
    color: '#6B7280',
    flex: 1,
  },
  sortedBy: {
    fontSize: 11,
    color: '#9CA3AF',
  },
  viewAllBtn: {
    display: 'flex',
    alignItems: 'center',
    gap: 4,
    background: 'none',
    border: 'none',
    color: '#1A56DB',
    fontSize: 13,
    fontWeight: 500,
    cursor: 'pointer',
    padding: '4px 0',
  },
};
