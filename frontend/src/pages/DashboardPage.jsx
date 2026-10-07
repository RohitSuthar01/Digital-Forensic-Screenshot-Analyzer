import React, { useEffect, useState } from 'react';
import axios from 'axios';
import Sidebar from '../components/Sidebar';
import { Link } from 'react-router-dom';
import { BarChart, Bar, LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';

const PIE_COLORS = { AUTHENTIC: '#00E676', SUSPICIOUS: '#FFD740', LIKELY_TAMPERED: '#FF5252', UNKNOWN: '#94A3B8' };

const DashboardPage = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    axios.get('/api/dashboard', { withCredentials: true })
      .then(r => setStats(r.data))
      .catch(() => setError('Failed to load dashboard data.'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Dashboard</h1>
            <p className="page-subtitle">Overview of forensic analysis activity</p>
          </div>
          <Link to="/cases" className="btn btn-primary btn-sm">+ New Case</Link>
        </div>
        <div className="page-body">
          {loading && <div className="loading-screen"><div className="spinner" /><span>Loading statistics…</span></div>}
          {error && <div className="alert alert-error">{error}</div>}
          {stats && (
            <>
              {/* Stat Cards */}
              <div className="card-grid card-grid-4" style={{ marginBottom: 28 }}>
                <StatCard icon="📁" color="#00E5FF" label="Total Cases" value={stats.totalCases ?? 0} />
                <StatCard icon="🖼" color="#7C4DFF" label="Screenshots" value={stats.totalScreenshots ?? 0} />
                <StatCard icon="⚠" color="#FF5252" label="Tampered" value={stats.tamperedDetected ?? 0} />
                <StatCard icon="⏳" color="#FFD740" label="Pending" value={stats.pendingAnalyses ?? 0} />
              </div>

              <div className="card-grid card-grid-2" style={{ marginBottom: 28 }}>
                {/* Uploads over time */}
                <div className="card">
                  <h3 style={{ marginBottom: 20, fontSize: 15, fontWeight: 700 }}>📈 Uploads (Last 7 Days)</h3>
                  {stats.uploadsOverTime?.length ? (
                    <ResponsiveContainer width="100%" height={200}>
                      <LineChart data={stats.uploadsOverTime}>
                        <XAxis dataKey="date" tick={{ fill: '#94A3B8', fontSize: 11 }} />
                        <YAxis tick={{ fill: '#94A3B8', fontSize: 11 }} />
                        <Tooltip contentStyle={{ background: '#1a2235', border: '1px solid rgba(255,255,255,0.08)', borderRadius: 8 }} />
                        <Line type="monotone" dataKey="count" stroke="#00E5FF" strokeWidth={2} dot={{ r: 4, fill: '#00E5FF' }} />
                      </LineChart>
                    </ResponsiveContainer>
                  ) : <EmptyChart />}
                </div>
                {/* Verdict distribution */}
                <div className="card">
                  <h3 style={{ marginBottom: 20, fontSize: 15, fontWeight: 700 }}>🎯 Verdict Distribution</h3>
                  {stats.verdictDistribution?.length ? (
                    <ResponsiveContainer width="100%" height={200}>
                      <PieChart>
                        <Pie data={stats.verdictDistribution} dataKey="value" nameKey="label" cx="50%" cy="50%" outerRadius={75} label={({ label, percent }) => `${label} ${(percent * 100).toFixed(0)}%`}>
                          {stats.verdictDistribution.map((e, i) => <Cell key={i} fill={PIE_COLORS[e.label] || '#94A3B8'} />)}
                        </Pie>
                        <Tooltip contentStyle={{ background: '#1a2235', border: '1px solid rgba(255,255,255,0.08)', borderRadius: 8 }} />
                      </PieChart>
                    </ResponsiveContainer>
                  ) : <EmptyChart />}
                </div>
              </div>

              {/* Recent Activity */}
              {stats.recentActivity?.length > 0 && (
                <div className="card">
                  <h3 style={{ marginBottom: 16, fontSize: 15, fontWeight: 700 }}>🕐 Recent Activity</h3>
                  <div className="table-wrap">
                    <table>
                      <thead><tr><th>Action</th><th>User</th><th>Time</th></tr></thead>
                      <tbody>
                        {stats.recentActivity.slice(0, 10).map((a, i) => (
                          <tr key={i}>
                            <td>{a.action}</td>
                            <td>{a.username}</td>
                            <td className="text-muted text-sm">{new Date(a.timestamp).toLocaleString()}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

const StatCard = ({ icon, color, label, value }) => (
  <div className="stat-card">
    <div className="stat-icon" style={{ background: `${color}22`, color }}>
      {icon}
    </div>
    <div>
      <div className="stat-value" style={{ color }}>{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  </div>
);

const EmptyChart = () => (
  <div style={{ height: 200, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)', fontSize: 14 }}>
    No data yet
  </div>
);

export default DashboardPage;
