import React from 'react';

interface SkeletonBoxProps {
  width?: number | string;
  height?: number | string;
  borderRadius?: number;
}

export function SkeletonBox({ width = '100%', height = 16, borderRadius = 6 }: SkeletonBoxProps) {
  return (
    <div
      style={{
        width,
        height,
        borderRadius,
        background: 'linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%)',
        backgroundSize: '200% 100%',
        animation: 'shimmer 1.4s infinite',
      }}
    >
      <style>{`
        @keyframes shimmer {
          0%   { background-position: 200% 0; }
          100% { background-position: -200% 0; }
        }
      `}</style>
    </div>
  );
}

export function SkeletonLines({ count = 3 }: { count?: number }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
      {Array.from({ length: count }).map((_, i) => (
        <SkeletonBox key={i} width={i === count - 1 ? '70%' : '100%'} height={14} />
      ))}
    </div>
  );
}
