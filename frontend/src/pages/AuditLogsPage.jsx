import React, { useEffect, useState } from 'react';
import axios from 'axios';
import Sidebar from '../components/Sidebar';

const AuditLogsPage = () => {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    axios.get('/api/admin/audit-logs', { withCredentials: true })
      .then(r => setLogs(r.data.content || r.data))
      .catch(() => setError('Failed to load audit logs. Ensure you have admin access.'))
      .finally(() => setLoading(false));
  }, []);

  const filtered = logs.filter(l =>
    l.action?.toLowerCase().includes(search.toLowerCase()) ||
    l.username?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Audit Logs</h1>
            <p className="page-subtitle">System-wide activity and security events</p>
          </div>
        </div>
        <div className="page-body">
          <div className="search-bar" style={{ marginBottom: 20, maxWidth: 400 }}>
            <span className="search-icon">🔍</span>
            <input className="form-input" placeholder="Search by action or username…"
              value={search} onChange={e => setSearch(e.target.value)} />
          </div>

          {loading && <div className="loading-screen"><div className="spinner" /><span>Loading audit logs…</span></div>}
          {error && <div className="alert alert-error">{error}</div>}

          {!loading && !error && (
            <div className="card">
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Action</th><th>User</th><th>IP Address</th><th>Details</th><th>Timestamp</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.length === 0 ? (
                      <tr><td colSpan={5} style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 40 }}>
                        No audit logs found.
                      </td></tr>
                    ) : filtered.map((l, i) => (
                      <tr key={i}>
                        <td><span className="badge badge-cyan">{l.action}</span></td>
                        <td style={{ fontWeight: 600 }}>{l.username || '—'}</td>
                        <td style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 12, color: 'var(--text-muted)' }}>{l.ipAddress || '—'}</td>
                        <td style={{ fontSize: 13, color: 'var(--text-secondary)', maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis' }}>{l.details || '—'}</td>
                        <td className="text-sm text-muted">{l.timestamp ? new Date(l.timestamp).toLocaleString() : '—'}</td>
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

export default AuditLogsPage;
