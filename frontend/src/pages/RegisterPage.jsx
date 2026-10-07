import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const RegisterPage = () => {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', username: '', password: '', confirmPassword: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value });

  const strength = (pw) => {
    let s = 0;
    if (pw.length >= 8) s++;
    if (/[A-Z]/.test(pw)) s++;
    if (/[0-9]/.test(pw)) s++;
    if (/[^A-Za-z0-9]/.test(pw)) s++;
    return s;
  };
  const strengthLabel = ['', 'Weak', 'Fair', 'Good', 'Strong'];
  const strengthColor = ['', '#FF5252', '#FFD740', '#00E5FF', '#00E676'];
  const s = strength(form.password);

  const handleSubmit = async e => {
    e.preventDefault();
    if (form.password !== form.confirmPassword) { setError('Passwords do not match'); return; }
    if (s < 2) { setError('Password is too weak'); return; }
    setLoading(true); setError('');
    try {
      await register({
        firstName: form.firstName,
        lastName: form.lastName,
        email: form.email,
        username: form.username,
        password: form.password,
        confirmPassword: form.confirmPassword
      });
      setSuccess('Registration successful! Redirecting to login...');
      setTimeout(() => navigate('/login'), 1500);
    } catch (err) {
      const msg = err.response?.data?.message || (typeof err.response?.data === 'string' ? err.response?.data : null) || 'Registration failed. Please try again.';
      setError(msg);
    } finally { setLoading(false); }
  };

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ maxWidth: 520 }}>
        <div className="auth-logo">
          <div className="logo-icon">🔍</div>
          <span style={{ fontWeight: 700, fontSize: 16, background: 'linear-gradient(135deg,#00E5FF,#7C4DFF)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            DFSA Platform
          </span>
        </div>

        <h1 className="auth-title">Create account</h1>
        <p className="auth-sub">Register as a forensic investigator</p>

        {error && <div className="alert alert-error">⚠ {error}</div>}
        {success && <div className="alert alert-success">✓ {success}</div>}

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div className="form-group">
              <label className="form-label">First Name</label>
              <input name="firstName" type="text" required className="form-input" placeholder="John" value={form.firstName} onChange={handleChange} />
            </div>
            <div className="form-group">
              <label className="form-label">Last Name</label>
              <input name="lastName" type="text" required className="form-input" placeholder="Doe" value={form.lastName} onChange={handleChange} />
            </div>
          </div>
          <div className="form-group">
            <label className="form-label">Email Address</label>
            <input name="email" type="email" required className="form-input" placeholder="john.doe@agency.gov" value={form.email} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label className="form-label">Username</label>
            <input name="username" type="text" required minLength={3} className="form-input" placeholder="johndoe" value={form.username} onChange={handleChange} />
          </div>
          <div className="form-group">
            <label className="form-label">Password</label>
            <input name="password" type="password" required className="form-input" placeholder="Min 8 characters" value={form.password} onChange={handleChange} />
            {form.password && (
              <div style={{ marginTop: 8 }}>
                <div style={{ height: 4, borderRadius: 2, background: 'var(--border)', overflow: 'hidden' }}>
                  <div style={{ height: '100%', width: `${s * 25}%`, background: strengthColor[s], borderRadius: 2, transition: 'all 0.3s' }} />
                </div>
                <span style={{ fontSize: 12, color: strengthColor[s], marginTop: 4, display: 'block' }}>{strengthLabel[s]}</span>
              </div>
            )}
          </div>
          <div className="form-group">
            <label className="form-label">Confirm Password</label>
            <input name="confirmPassword" type="password" required className="form-input" placeholder="Repeat password" value={form.confirmPassword} onChange={handleChange} />
          </div>
          <button type="submit" disabled={loading} className="btn btn-primary" style={{ width: '100%', justifyContent: 'center' }}>
            {loading ? '⏳ Creating account...' : '✅ Create Account'}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account? <Link to="/login" className="auth-link">Sign in</Link>
        </p>
      </div>
    </div>
  );
};

export default RegisterPage;