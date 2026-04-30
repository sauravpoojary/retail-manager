import React from 'react';
import { AlertCircle } from 'lucide-react';

interface FallbackBoxProps {
  message?: string;
}

export function FallbackBox({ message = 'AI service is temporarily unavailable. Please try again later.' }: FallbackBoxProps) {
  return (
    <div style={styles.box} role="status">
      <AlertCircle size={16} color="#C27803" style={{ flexShrink: 0 }} />
      <span style={styles.text}>{message}</span>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  box: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: 8,
    background: '#FFFBEB',
    border: '1px solid #FDE68A',
    borderRadius: 8,
    padding: '10px 12px',
  },
  text: {
    fontSize: 13,
    color: '#92400E',
    lineHeight: 1.5,
  },
};
