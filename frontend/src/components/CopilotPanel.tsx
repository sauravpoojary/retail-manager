import React, { useState, useRef, useEffect } from 'react';
import { MessageCircle, Send, ThumbsUp, ThumbsDown, ShieldAlert } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { PanelCard } from './ui/PanelCard';

const QUICK_PROMPTS = [
  { label: 'Why are sales down?',    query: 'Why are sales down compared to yesterday?' },
  { label: 'What needs restocking?', query: 'Which products need restocking most urgently?' },
  { label: 'Staffing tips',          query: 'What staffing adjustments should I make today?' },
  { label: "Today's summary",        query: "Give me a summary of today's store performance." },
];

export function CopilotPanel() {
  const { state, sendMessage } = useApp();
  const { copilotMessages, copilotStatus } = state;
  const [input, setInput] = useState('');
  const [activeChip, setActiveChip] = useState<string | null>(null);
  const chatEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [copilotMessages, copilotStatus]);

  const handleSend = async (query?: string) => {
    const text = (query ?? input).trim();
    if (!text) return;
    setInput('');
    setActiveChip(query ?? null);
    await sendMessage(text);
    setActiveChip(null);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <PanelCard
      icon={<MessageCircle size={18} color="#1A56DB" />}
      title="Copilot"
      subtitle="Ask anything about your store"
      collapsible
    >
      {/* Quick Prompts */}
      <div style={styles.chipsRow}>
        <span style={styles.chipsLabel}>Quick Prompts</span>
        <div style={styles.chips}>
          {QUICK_PROMPTS.map((p) => (
            <button
              key={p.label}
              style={{
                ...styles.chip,
                ...(activeChip === p.query ? styles.chipActive : {}),
              }}
              onClick={() => handleSend(p.query)}
              disabled={copilotStatus === 'loading'}
            >
              {p.label}
            </button>
          ))}
        </div>
      </div>

      {/* Chat history */}
      <div style={styles.chatArea}>
        {copilotMessages.length === 0 && (
          <div style={styles.emptyChat}>
            <MessageCircle size={28} color="#D1D5DB" />
            <p style={styles.emptyChatText}>Ask a question or select a quick prompt above</p>
          </div>
        )}

        {copilotMessages.map((msg) => (
          <div
            key={msg.id}
            style={{
              ...styles.msgRow,
              justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start',
            }}
          >
            <div style={{
              ...styles.bubble,
              ...(msg.role === 'user' ? styles.userBubble : styles.assistantBubble),
              ...(msg.isFallback ? styles.fallbackBubble : {}),
            }}>
              <p style={styles.bubbleText}>{msg.content}</p>
              <div style={styles.bubbleMeta}>
                <span style={styles.timestamp}>
                  {new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </span>
                {msg.role === 'assistant' && !msg.isFallback && (
                  <span style={styles.feedbackRow}>
                    <ThumbsUp size={12} color="#9CA3AF" style={{ cursor: 'pointer' }} />
                    <ThumbsDown size={12} color="#9CA3AF" style={{ cursor: 'pointer' }} />
                  </span>
                )}
              </div>
            </div>
          </div>
        ))}

        {/* Loading dots */}
        {copilotStatus === 'loading' && (
          <div style={{ ...styles.msgRow, justifyContent: 'flex-start' }}>
            <div style={{ ...styles.bubble, ...styles.assistantBubble }}>
              <div style={styles.typingDots}>
                <span style={{ ...styles.dot, animationDelay: '0ms' }} />
                <span style={{ ...styles.dot, animationDelay: '160ms' }} />
                <span style={{ ...styles.dot, animationDelay: '320ms' }} />
              </div>
            </div>
          </div>
        )}

        {/* Error state */}
        {copilotStatus === 'error' && (
          <div style={{ ...styles.msgRow, justifyContent: 'flex-start' }}>
            <div style={{ ...styles.bubble, ...styles.errorBubble }}>
              <p style={styles.bubbleText}>AI service is temporarily unavailable. Please try again.</p>
            </div>
          </div>
        )}

        <div ref={chatEndRef} />
      </div>

      {/* Input */}
      <div style={styles.inputRow}>
        <input
          style={styles.input}
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Ask about your store..."
          disabled={copilotStatus === 'loading'}
          aria-label="Ask the copilot"
        />
        <button
          style={{
            ...styles.sendBtn,
            opacity: !input.trim() || copilotStatus === 'loading' ? 0.5 : 1,
            cursor: !input.trim() || copilotStatus === 'loading' ? 'not-allowed' : 'pointer',
          }}
          onClick={() => handleSend()}
          disabled={!input.trim() || copilotStatus === 'loading'}
          aria-label="Send message"
        >
          <Send size={18} color="#fff" />
        </button>
      </div>

      {/* Disclaimer */}
      <div style={styles.disclaimer}>
        <ShieldAlert size={12} color="#9CA3AF" />
        <span style={styles.disclaimerText}>AI responses may be inaccurate. Please verify important information.</span>
      </div>

      <style>{`
        @keyframes bounce {
          0%, 80%, 100% { transform: translateY(0); }
          40%            { transform: translateY(-6px); }
        }
      `}</style>
    </PanelCard>
  );
}

const styles: Record<string, React.CSSProperties> = {
  chipsRow: {
    marginBottom: 12,
  },
  chipsLabel: {
    fontSize: 11,
    fontWeight: 600,
    color: '#6B7280',
    textTransform: 'uppercase',
    letterSpacing: '0.05em',
    display: 'block',
    marginBottom: 6,
  },
  chips: {
    display: 'flex',
    gap: 6,
    flexWrap: 'wrap',
  },
  chip: {
    fontSize: 12,
    padding: '5px 12px',
    borderRadius: 20,
    border: '1.5px solid #1A56DB',
    background: '#fff',
    color: '#1A56DB',
    cursor: 'pointer',
    fontWeight: 500,
    whiteSpace: 'nowrap',
  },
  chipActive: {
    background: '#1A56DB',
    color: '#fff',
  },
  chatArea: {
    minHeight: 200,
    maxHeight: 340,
    overflowY: 'auto',
    display: 'flex',
    flexDirection: 'column',
    gap: 10,
    marginBottom: 12,
    paddingRight: 4,
  },
  emptyChat: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    padding: '32px 0',
    flex: 1,
  },
  emptyChatText: {
    fontSize: 13,
    color: '#9CA3AF',
    textAlign: 'center',
    margin: 0,
  },
  msgRow: {
    display: 'flex',
    width: '100%',
  },
  bubble: {
    maxWidth: '80%',
    borderRadius: 12,
    padding: '10px 14px',
  },
  userBubble: {
    background: '#1A56DB',
    borderBottomRightRadius: 4,
  },
  assistantBubble: {
    background: '#F9FAFB',
    border: '1px solid #E5E7EB',
    borderBottomLeftRadius: 4,
  },
  fallbackBubble: {
    border: '1px solid #FCA5A5',
    background: '#FFF5F5',
  },
  errorBubble: {
    border: '1px solid #FCA5A5',
    background: '#FFF5F5',
  },
  bubbleText: {
    fontSize: 13,
    lineHeight: 1.55,
    margin: 0,
    color: 'inherit',
    whiteSpace: 'pre-wrap',
  },
  bubbleMeta: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginTop: 6,
    gap: 8,
  },
  timestamp: {
    fontSize: 10,
    color: 'rgba(107,114,128,0.8)',
  },
  feedbackRow: {
    display: 'flex',
    gap: 6,
  },
  typingDots: {
    display: 'flex',
    gap: 5,
    alignItems: 'center',
    padding: '2px 0',
  },
  dot: {
    width: 8,
    height: 8,
    borderRadius: '50%',
    background: '#9CA3AF',
    display: 'inline-block',
    animation: 'bounce 1.2s infinite ease-in-out',
  },
  inputRow: {
    display: 'flex',
    gap: 8,
    alignItems: 'center',
  },
  input: {
    flex: 1,
    height: 44,
    borderRadius: 10,
    border: '1.5px solid #E5E7EB',
    padding: '0 14px',
    fontSize: 14,
    outline: 'none',
    color: '#111827',
    background: '#fff',
  },
  sendBtn: {
    width: 44,
    height: 44,
    borderRadius: 10,
    background: '#1A56DB',
    border: 'none',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  disclaimer: {
    display: 'flex',
    alignItems: 'center',
    gap: 5,
    marginTop: 8,
  },
  disclaimerText: {
    fontSize: 11,
    color: '#9CA3AF',
  },
};
