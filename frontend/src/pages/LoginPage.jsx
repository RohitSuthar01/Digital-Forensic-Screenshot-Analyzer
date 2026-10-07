import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const LoginPage = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [creds, setCreds] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = e => setCreds({ ...creds, [e.target.name]: e.target.value });

  const handleSubmit = async e => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await login(creds);
      navigate('/dashboard');
    } catch (err) {
      const msg = err.response?.data?.message || (typeof err.response?.data === 'string' ? err.response?.data : null) || 'Login failed. Check your credentials.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-logo">
          <div className="logo-icon">🔍</div>
          <span style={{ fontWeight: 700, fontSize: 16, background: 'linear-gradient(135deg,#00E5FF,#7C4DFF)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            DFSA Platform
          </span>
        </div>

        <h1 className="auth-title">Welcome back</h1>
        <p className="auth-sub">Sign in to your investigator account</p>

        {error && <div className="alert alert-error">⚠ {error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="username">Username</label>
            <input
              id="username" name="username" type="text" required
              className="form-input" placeholder="Enter your username"
              value={creds.username} onChange={handleChange}
            />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="password">Password</label>
            <input
              id="password" name="password" type="password" required
              className="form-input" placeholder="Enter your password"
              value={creds.password} onChange={handleChange}
            />
          </div>
          <button type="submit" disabled={loading} className="btn btn-primary w-full" style={{ width: '100%', justifyContent: 'center' }}>
            {loading ? '⏳ Signing in...' : '🔐 Sign In'}
          </button>
        </form>

        <div style={{ marginTop: 16, padding: '10px 14px', background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border)', borderRadius: 8, fontSize: 12, color: 'var(--text-secondary)' }}>
          <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>Quick Login: </span>
          <span style={{ display: 'inline-flex', gap: 6, marginLeft: 6 }}>
            <button type="button" className="btn btn-secondary" style={{ padding: '2px 8px', fontSize: 11, minHeight: 'auto' }} onClick={() => setCreds({ username: 'admin', password: 'Admin@123' })}>admin</button>
            <button type="button" className="btn btn-secondary" style={{ padding: '2px 8px', fontSize: 11, minHeight: 'auto' }} onClick={() => setCreds({ username: 'investigator1', password: 'Invest@123' })}>investigator1</button>
          </span>
        </div>

        <p className="auth-footer">
          Don't have an account? <Link to="/register" className="auth-link">Register here</Link>
        </p>
        <p className="auth-footer" style={{ marginTop: 4 }}>
          <Link to="/" className="auth-link" style={{ fontSize: 13, color: 'var(--text-muted)' }}>← Back to Home</Link>
        </p>
      </div>
    </div>
  );
};

export default LoginPage;