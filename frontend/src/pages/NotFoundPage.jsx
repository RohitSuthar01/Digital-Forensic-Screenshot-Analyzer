import React from 'react';
import { Link } from 'react-router-dom';

const NotFoundPage = () => (
  <div style={{
    minHeight: '100vh', display: 'flex', flexDirection: 'column',
    alignItems: 'center', justifyContent: 'center',
    background: 'var(--bg-primary)', color: 'var(--text-primary)',
    gap: 16, textAlign: 'center', padding: 24,
  }}>
    <div style={{ fontSize: 80 }}>🔍</div>
    <h1 style={{ fontSize: 56, fontWeight: 800, margin: 0, background: 'linear-gradient(135deg,#00E5FF,#7C4DFF)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
      404
    </h1>
    <p style={{ fontSize: 20, fontWeight: 600, margin: 0 }}>Page Not Found</p>
    <p style={{ color: 'var(--text-muted)', maxWidth: 360, margin: 0 }}>
      The page you're looking for doesn't exist or has been moved.
    </p>
    <Link to="/" className="btn btn-primary" style={{ marginTop: 8 }}>← Go Home</Link>
  </div>
);

export default NotFoundPage;
