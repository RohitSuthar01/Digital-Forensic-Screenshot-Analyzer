import React, { useEffect, useState } from 'react';
import axios from 'axios';
import Sidebar from '../components/Sidebar';

const AdminUsersPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');

  const load = () => {
    setLoading(true);
    axios.get('/api/admin/users', { withCredentials: true })
      .then(r => setUsers(r.data))
      .catch(() => setError('Failed to load users. Ensure you have admin access.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const toggleEnable = async (id, enabled) => {
    try {
      await axios.put(`/api/admin/users/${id}/${enabled ? 'disable' : 'enable'}`, {}, { withCredentials: true });
      load();
    } catch {
      alert('Failed to update user status.');
    }
  };

  const filtered = users.filter(u =>
    u.username?.toLowerCase().includes(search.toLowerCase()) ||
    u.email?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">User Management</h1>
            <p className="page-subtitle">Manage all registered investigators</p>
          </div>
        </div>
        <div className="page-body">
          <div className="search-bar" style={{ marginBottom: 20, maxWidth: 400 }}>
            <span className="search-icon">🔍</span>
            <input className="form-input" placeholder="Search by username or email…"
              value={search} onChange={e => setSearch(e.target.value)} />
          </div>

          {loading && <div className="loading-screen"><div className="spinner" /><span>Loading users…</span></div>}
          {error && <div className="alert alert-error">{error}</div>}

          {!loading && !error && (
            <div className="card">
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th><th>Username</th><th>Email</th><th>Role</th>
                      <th>Status</th><th>Created</th><th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.length === 0 ? (
                      <tr><td colSpan={7} style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 40 }}>
                        No users found.
                      </td></tr>
                    ) : filtered.map(u => (
                      <tr key={u.id}>
                        <td className="text-muted text-sm">{u.id}</td>
                        <td style={{ fontWeight: 600 }}>{u.username}</td>
                        <td className="text-sm">{u.email}</td>
                        <td><span className={`badge ${u.role === 'ADMIN' ? 'badge-purple' : 'badge-cyan'}`}>{u.role}</span></td>
                        <td><span className={`badge ${u.enabled ? 'badge-green' : 'badge-red'}`}>{u.enabled ? 'Active' : 'Disabled'}</span></td>
                        <td className="text-sm text-muted">{u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}</td>
                        <td>
                          <button
                            className={`btn btn-sm ${u.enabled ? 'btn-secondary' : 'btn-primary'}`}
                            onClick={() => toggleEnable(u.id, u.enabled)}
                          >
                            {u.enabled ? '🚫 Disable' : '✅ Enable'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminUsersPage;
