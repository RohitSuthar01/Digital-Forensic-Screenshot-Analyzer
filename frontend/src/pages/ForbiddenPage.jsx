import React from 'react';
import { Link } from 'react-router-dom';

const ForbiddenPage = () => (
  <div style={{
    minHeight: '100vh', display: 'flex', flexDirection: 'column',
    alignItems: 'center', justifyContent: 'center',
    background: 'var(--bg-primary)', color: 'var(--text-primary)',
    gap: 16, textAlign: 'center', padding: 24,
  }}>
    <div style={{ fontSize: 80 }}>🚫</div>
    <h1 style={{ fontSize: 56, fontWeight: 800, margin: 0, color: 'var(--accent-red)' }}>
      403
    </h1>
    <p style={{ fontSize: 20, fontWeight: 600, margin: 0 }}>Access Forbidden</p>
    <p style={{ color: 'var(--text-muted)', maxWidth: 360, margin: 0 }}>
      You don't have permission to view this page. Admin access required.
    </p>
    <Link to="/dashboard" className="btn btn-primary" style={{ marginTop: 8 }}>← Back to Dashboard</Link>
  </div>
);

export default ForbiddenPage;
