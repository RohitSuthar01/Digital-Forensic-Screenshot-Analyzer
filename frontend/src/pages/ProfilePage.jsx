import React, { useState } from 'react';
import axios from 'axios';
import Sidebar from '../components/Sidebar';
import { useAuth } from '../context/AuthContext';

const ProfilePage = () => {
  const { user } = useAuth();
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value });

  const handlePasswordChange = async e => {
    e.preventDefault();
    if (form.newPassword !== form.confirmPassword) {
      setError('New passwords do not match.');
      return;
    }
    setLoading(true); setError(''); setMsg('');
    try {
      await axios.post('/api/auth/change-password', {
        currentPassword: form.currentPassword,
        newPassword: form.newPassword,
      }, { withCredentials: true });
      setMsg('Password changed successfully!');
      setForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data || 'Failed to change password.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Profile</h1>
            <p className="page-subtitle">Manage your account settings</p>
          </div>
        </div>
        <div className="page-body" style={{ maxWidth: 600 }}>
          {/* User Info Card */}
          <div className="card" style={{ marginBottom: 24 }}>
            <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 20 }}>👤 Account Information</h3>
            <div style={{ display: 'grid', gap: 14 }}>
              {[
                { label: 'Username', value: user?.username },
                { label: 'Role', value: user?.role },
              ].map(({ label, value }) => (
                <div key={label} style={{ display: 'flex', gap: 16, padding: '10px 14px', background: 'rgba(255,255,255,0.03)', borderRadius: 8 }}>
                  <span style={{ fontSize: 13, color: 'var(--text-muted)', minWidth: 110 }}>{label}</span>
                  <span style={{ fontSize: 14, fontWeight: 600 }}>{value || '—'}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Change Password Card */}
          <div className="card">
            <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 20 }}>🔑 Change Password</h3>
            {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>⚠ {error}</div>}
            {msg && <div className="alert alert-success" style={{ marginBottom: 16 }}>✓ {msg}</div>}
            <form onSubmit={handlePasswordChange}>
              <div className="form-group">
                <label className="form-label">Current Password</label>
                <input name="currentPassword" type="password" required className="form-input"
                  placeholder="Enter current password" value={form.currentPassword} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label className="form-label">New Password</label>
                <input name="newPassword" type="password" required minLength={8} className="form-input"
                  placeholder="Min 8 characters" value={form.newPassword} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label className="form-label">Confirm New Password</label>
                <input name="confirmPassword" type="password" required className="form-input"
                  placeholder="Repeat new password" value={form.confirmPassword} onChange={handleChange} />
              </div>
              <button type="submit" className="btn btn-primary" disabled={loading} style={{ width: '100%', justifyContent: 'center' }}>
                {loading ? '⏳ Changing…' : '🔒 Change Password'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
