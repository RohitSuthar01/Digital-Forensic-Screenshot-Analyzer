import React, { useEffect, useState } from 'react';
import api from '../api/api';
import { useParams, Link } from 'react-router-dom';
import Sidebar from '../components/Sidebar';

const statusBadge = { PENDING: 'badge-amber', PROCESSING: 'badge-cyan', COMPLETED: 'badge-green', FAILED: 'badge-red' };
const verdictBadge = { AUTHENTIC: 'badge-green', SUSPICIOUS: 'badge-amber', LIKELY_TAMPERED: 'badge-red', INCONCLUSIVE: 'badge-blue' };

const CaseDetailPage = () => {
  const { id } = useParams();
  const [caseData, setCaseData] = useState(null);
  const [screenshots, setScreenshots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expandedResults, setExpandedResults] = useState({});

  const load = () => {
    Promise.all([
      api.get(`/cases/${id}`),
      api.get(`/cases/${id}/screenshots`)
    ])
      .then(([caseRes, ssRes]) => {
        setCaseData(caseRes.data);
        setScreenshots(ssRes.data.content || ssRes.data);
      })
      .catch(() => setError('Failed to load case details.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => { 
    load(); 
    
    // Polling mechanism for processing screenshots
    const interval = setInterval(() => {
      setScreenshots(currentScreenshots => {
        const needsUpdate = currentScreenshots.some(ss => ss.status === 'PROCESSING' || ss.status === 'PENDING' && ss.triggered);
        if (needsUpdate) {
          load();
        }
        return currentScreenshots;
      });
    }, 2000);

    return () => clearInterval(interval);
  }, [id]);

  const triggerAnalysis = async (ssId) => {
    try {
      setScreenshots(current => current.map(ss => ss.id === ssId ? { ...ss, triggered: true, status: 'PROCESSING' } : ss));
      await api.post(`/screenshots/${ssId}/analyze`);
      // The polling interval will now pick this up and refresh until it's COMPLETED or FAILED
    } catch { 
      alert('Failed to trigger analysis.'); 
      setScreenshots(current => current.map(ss => ss.id === ssId ? { ...ss, triggered: false, status: 'PENDING' } : ss));
    }
  };

  const downloadReport = (ssId) => {
    window.open(`/api/reports/screenshot/${ssId}`, '_blank');
  };

  const toggleInlineResult = async (ssId) => {
    if (expandedResults[ssId]) {
      setExpandedResults(prev => {
        const next = { ...prev };
        delete next[ssId];
        return next;
      });
      return;
    }
    
    setExpandedResults(prev => ({ ...prev, [ssId]: { loading: true } }));
    try {
      const res = await api.get(`/screenshots/${ssId}/analysis`);
      setExpandedResults(prev => ({ ...prev, [ssId]: { loading: false, data: res.data } }));
    } catch (err) {
      setExpandedResults(prev => ({ ...prev, [ssId]: { loading: false, error: 'Failed to load results' } }));
    }
  };

  if (loading) return (
    <div className="app-layout"><Sidebar />
      <div className="main-content"><div className="loading-screen" style={{ minHeight: '100vh' }}>
        <div className="spinner" /><span>Loading case…</span>
      </div></div>
    </div>
  );

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <div className="page-header">
          <div>
            <div className="flex items-center gap-3" style={{ marginBottom: 4 }}>
              <Link to="/cases" style={{ color: 'var(--text-muted)', textDecoration: 'none', fontSize: 14 }}>← Cases</Link>
            </div>
            <h1 className="page-title">{caseData?.title || 'Case Details'}</h1>
            <p className="page-subtitle">
              <code style={{ color: 'var(--accent-cyan)', fontSize: 12 }}>{caseData?.caseNumber}</code>
              &nbsp;·&nbsp;{caseData?.status} · {caseData?.priority} Priority
            </p>
          </div>
          <Link to={`/upload/${id}`} className="btn btn-primary">📤 Upload Screenshot</Link>
        </div>

        <div className="page-body">
          {error && <div className="alert alert-error">{error}</div>}

          {caseData?.description && (
            <div className="card" style={{ marginBottom: 24 }}>
              <h3 style={{ fontSize: 14, fontWeight: 600, marginBottom: 8, color: 'var(--text-secondary)' }}>DESCRIPTION</h3>
              <p style={{ fontSize: 14, lineHeight: 1.7 }}>{caseData.description}</p>
            </div>
          )}

          <div className="card">
            <div className="flex justify-between items-center" style={{ marginBottom: 16 }}>
              <h3 style={{ fontSize: 16, fontWeight: 700 }}>🖼 Screenshots ({screenshots.length})</h3>
            </div>
            {screenshots.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '40px 0', color: 'var(--text-muted)' }}>
                <p style={{ fontSize: 32, marginBottom: 12 }}>📂</p>
                <p>No screenshots uploaded yet.</p>
                <Link to={`/upload/${id}`} className="btn btn-primary" style={{ marginTop: 16, display: 'inline-flex' }}>Upload First Screenshot</Link>
              </div>
            ) : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Filename</th><th>Status</th><th>Verdict</th><th>Score</th>
                      <th>Size</th><th>Uploaded</th><th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {screenshots.map(ss => (
                      <React.Fragment key={ss.id}>
                        <tr>
                          <td style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 13 }}>
                            {ss.originalFilename}
                            {ss.status === 'FAILED' && ss.errorMessage && <div style={{color: 'var(--accent-red)', fontSize: 11, marginTop: 4}}>{ss.errorMessage}</div>}
                          </td>
                          <td><span className={`badge ${statusBadge[ss.status] || 'badge-gray'}`}>{ss.status}</span></td>
                          <td>{ss.verdict ? <span className={`badge ${verdictBadge[ss.verdict] || 'badge-gray'}`}>{ss.verdict}</span> : <span className="text-muted text-sm">—</span>}</td>
                          <td>{ss.authenticityScore != null ? <strong style={{ color: ss.authenticityScore >= 70 ? 'var(--accent-green)' : ss.authenticityScore >= 40 ? 'var(--accent-amber)' : 'var(--accent-red)' }}>{ss.authenticityScore}</strong> : '—'}</td>
                          <td className="text-muted text-sm">{ss.fileSize ? `${(ss.fileSize / 1024).toFixed(1)} KB` : '—'}</td>
                          <td className="text-muted text-sm">{new Date(ss.uploadedAt).toLocaleDateString()}</td>
                          <td>
                            <div style={{ display: 'flex', gap: 6 }}>
                              {ss.status === 'PENDING' && <button className="btn btn-secondary btn-sm" onClick={() => triggerAnalysis(ss.id)}>▶ Analyze</button>}
                              {ss.status === 'FAILED' && <button className="btn btn-secondary btn-sm" onClick={() => triggerAnalysis(ss.id)}>↻ Retry</button>}
                              {ss.status === 'COMPLETED' && <button className="btn btn-primary btn-sm" onClick={() => toggleInlineResult(ss.id)}>{expandedResults[ss.id] ? 'Hide Results' : 'Show Results'}</button>}
                              {ss.status === 'COMPLETED' && <button className="btn btn-secondary btn-sm" onClick={() => downloadReport(ss.id)}>📑 PDF</button>}
                            </div>
                          </td>
                        </tr>
                        {expandedResults[ss.id] && (
                          <tr>
                            <td colSpan="7" style={{ padding: 16, background: 'rgba(0,0,0,0.1)' }}>
                              {expandedResults[ss.id].loading ? (
                                <div style={{ textAlign: 'center', color: 'var(--text-muted)' }}>Loading results...</div>
                              ) : expandedResults[ss.id].error ? (
                                <div className="alert alert-error">{expandedResults[ss.id].error}</div>
                              ) : (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                                    <div style={{ display: 'flex', gap: 24, alignItems: 'center' }}>
                                      <div style={{ flex: 1, padding: 16, background: 'var(--bg-card)', borderRadius: 8 }}>
                                        <h4 style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 8, textTransform: 'uppercase' }}>Verdict Explanation</h4>
                                        <p style={{ fontSize: 14 }}>{expandedResults[ss.id].data.verdictExplanation}</p>
                                      </div>
                                      <div style={{ flex: 1, padding: 16, background: 'var(--bg-card)', borderRadius: 8 }}>
                                        <h4 style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 8, textTransform: 'uppercase' }}>Original</h4>
                                        <img src={`/api/screenshots/${ss.id}/file`} alt="Original" style={{ maxWidth: '100%', borderRadius: 4, maxHeight: 120, objectFit: 'contain' }} onError={(e) => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'block'; }} />
                                        <div style={{ display: 'none', color: 'var(--text-muted)', fontSize: 12 }}>Image unavailable</div>
                                      </div>
                                      {expandedResults[ss.id].data.elaImageFileName && (
                                        <div style={{ flex: 1, padding: 16, background: 'var(--bg-card)', borderRadius: 8 }}>
                                          <h4 style={{ fontSize: 13, color: 'var(--text-muted)', marginBottom: 8, textTransform: 'uppercase' }}>ELA Visualization</h4>
                                          <p style={{ fontSize: 12, color: 'var(--text-secondary)', marginBottom: 8, lineHeight: 1.4 }}>
                                            <strong>Legend:</strong> Brighter pixels = larger recompression differences. Do not confirm tampering on this alone.
                                          </p>
                                          <img src={`/api/screenshots/${ss.id}/ela-image`} alt="ELA" style={{ maxWidth: '100%', borderRadius: 4, maxHeight: 120, objectFit: 'contain' }} onError={(e) => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'block'; }} />
                                          <div style={{ display: 'none', color: 'var(--text-muted)', fontSize: 12 }}>ELA unavailable or non-JPEG</div>
                                        </div>
                                      )}
                                    </div>
                                  <div style={{ display: 'flex', gap: 8 }}>
                                    <Link to={`/analysis/${ss.id}`} className="btn btn-secondary btn-sm">View Full Details</Link>
                                  </div>
                                </div>
                              )}
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CaseDetailPage;
