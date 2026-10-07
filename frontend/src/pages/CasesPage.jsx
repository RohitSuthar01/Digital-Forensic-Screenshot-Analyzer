import React, { useEffect, useState } from 'react';
import axios from 'axios';
import Sidebar from '../components/Sidebar';
import { Link } from 'react-router-dom';

const priorityBadge = { HIGH: 'badge-red', MEDIUM: 'badge-amber', LOW: 'badge-cyan', CRITICAL: 'badge-purple' };
const statusBadge = { OPEN: 'badge-green', IN_PROGRESS: 'badge-cyan', CLOSED: 'badge-gray', ARCHIVED: 'badge-gray' };

const CasesPage = () => {
  const [cases, setCases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ title: '', description: '', priority: 'MEDIUM' });
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState('');

  const load = () => {
    setLoading(true);
    axios.get('/api/cases', { withCredentials: true })
      .then(r => setCases(r.data.content || r.data))
      .catch(() => setError('Failed to load cases.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, []);

  const filtered = cases.filter(c =>
    c.title?.toLowerCase().includes(search.toLowerCase()) ||
    c.caseNumber?.toLowerCase().includes(search.toLowerCase())
  );

  const handleCreate = async e => {
    e.preventDefault();
    setCreating(true); setCreateError('');
    try {
      await axios.post('/api/cases', form, { withCredentials: true });
      setShowModal(false);
      setForm({ title: '', description: '', priority: 'MEDIUM' });
      load();
    } catch (err) {
      setCreateError(err.response?.data?.message || 'Failed to create case.');
    } finally { setCreating(false); }
  };

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Cases</h1>
            <p className="page-subtitle">Manage your forensic investigation cases</p>
          </div>
          <button className="btn btn-primary btn-sm" onClick={() => setShowModal(true)}>+ New Case</button>
        </div>

        <div className="page-body">
          {/* Search */}
          <div className="search-bar" style={{ marginBottom: 20, maxWidth: 400 }}>
            <span className="search-icon">🔍</span>
            <input className="form-input" placeholder="Search by title or case number…" value={search} onChange={e => setSearch(e.target.value)} />
          </div>

          {loading && <div className="loading-screen"><div className="spinner" /><span>Loading cases…</span></div>}
          {error && <div className="alert alert-error">{error}</div>}

          {!loading && !error && (
            <div className="card">
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Case #</th><th>Title</th><th>Priority</th><th>Status</th>
                      <th>Screenshots</th><th>Created</th><th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.length === 0 ? (
                      <tr><td colSpan={7} style={{ textAlign: 'center', color: 'var(--text-muted)', padding: 40 }}>
                        No cases found. Create your first case!
                      </td></tr>
                    ) : filtered.map(c => (
                      <tr key={c.id}>
                        <td><code style={{ fontSize: 12, color: 'var(--accent-cyan)' }}>{c.caseNumber}</code></td>
                        <td><Link to={`/cases/${c.id}`} style={{ color: 'var(--text-primary)', textDecoration: 'none', fontWeight: 600 }}>{c.title}</Link></td>
                        <td><span className={`badge ${priorityBadge[c.priority] || 'badge-gray'}`}>{c.priority}</span></td>
                        <td><span className={`badge ${statusBadge[c.status] || 'badge-gray'}`}>{c.status}</span></td>
                        <td style={{ color: 'var(--text-secondary)' }}>{c.screenshotCount ?? 0}</td>
                        <td className="text-sm text-muted">{c.createdAt ? new Date(c.createdAt).toLocaleDateString() : '-'}</td>
                        <td>
                          <div style={{ display: 'flex', gap: 8 }}>
                            <Link to={`/cases/${c.id}`} className="btn btn-secondary btn-sm">View</Link>
                            <Link to={`/upload/${c.id}`} className="btn btn-primary btn-sm">Upload</Link>
                          </div>
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

      {/* Create Case Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <span className="modal-title">Create New Case</span>
              <button className="modal-close" onClick={() => setShowModal(false)}>✕</button>
            </div>
            {createError && <div className="alert alert-error">{createError}</div>}
            <form onSubmit={handleCreate}>
              <div className="form-group">
                <label className="form-label">Case Title *</label>
                <input className="form-input" required placeholder="e.g. Social Media Fraud Investigation" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} />
              </div>
              <div className="form-group">
                <label className="form-label">Description</label>
                <textarea className="form-textarea" placeholder="Brief description of the case…" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
              </div>
              <div className="form-group">
                <label className="form-label">Priority</label>
                <select className="form-select form-input" value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}>
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={creating}>
                  {creating ? '⏳ Creating…' : '✅ Create Case'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default CasesPage;
