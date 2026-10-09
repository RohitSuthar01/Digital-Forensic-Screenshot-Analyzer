import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useParams, Link } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import ImageInspector from '../components/ImageInspector';
import ComparisonPanel from '../components/ComparisonPanel';

const verdictConfig = {
  AUTHENTIC: { color: 'var(--accent-green)', icon: '✅', label: 'Authentic' },
  SUSPICIOUS: { color: 'var(--accent-amber)', icon: '⚠️', label: 'Suspicious' },
  LIKELY_TAMPERED: { color: 'var(--accent-red)', icon: '🚨', label: 'Likely Tampered' },
  INCONCLUSIVE: { color: '#94A3B8', icon: '❓', label: 'Inconclusive / Needs Review' },
};

const AnalysisResultPage = () => {
  const { screenshotId } = useParams();
  const [result, setResult] = useState(null);
  const [screenshot, setScreenshot] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    axios.get(`/api/screenshots/${screenshotId}/analysis`, { withCredentials: true })
      .then(r => setResult(r.data))
      .catch(() => setError('Failed to load analysis result.'))
      .finally(() => setLoading(false));
    axios.get(`/api/screenshots/${screenshotId}`, { withCredentials: true })
      .then(r => setScreenshot(r.data))
      .catch(() => setScreenshot(null));
  }, [screenshotId]);

  const score = result?.authenticityScore ?? 0;
  const verdict = result?.verdict;
  const vc = verdictConfig[verdict] || { color: 'var(--text-muted)', icon: '❓', label: 'Unknown' };
  const scorePct = (score / 100) * 360;

  if (loading) return (
    <div className="app-layout"><Sidebar />
      <div className="main-content"><div className="loading-screen" style={{ minHeight: '100vh' }}>
        <div className="spinner" /><span>Loading analysis results…</span>
      </div></div>
    </div>
  );

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <div className="page-header">
          <div>
            <h1 className="page-title">Analysis Results</h1>
            <p className="page-subtitle">Forensic analysis for screenshot #{screenshotId}</p>
          </div>
          <button className="btn btn-secondary btn-sm" onClick={() => window.open(`/api/reports/screenshot/${screenshotId}`, '_blank')}>
            📑 Download PDF Report
          </button>
        </div>

        <div className="page-body">
          {error && <div className="alert alert-error">{error}</div>}

          {result && (
            <>
              {/* Top row: Verdict + Score */}
              <div className="card-grid card-grid-3" style={{ marginBottom: 24 }}>
                {/* Verdict Card */}
                <div className="card" style={{ gridColumn: 'span 2', display: 'flex', alignItems: 'center', gap: 24, background: `${vc.color}11`, borderColor: `${vc.color}44` }}>
                  <span style={{ fontSize: 56 }}>{vc.icon}</span>
                  <div>
                    <div style={{ fontSize: 13, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: 1, marginBottom: 4 }}>Forensic Verdict</div>
                    <div style={{ fontSize: 32, fontWeight: 800, color: vc.color }}>{vc.label}</div>
                    <div style={{ fontSize: 14, color: 'var(--text-secondary)', marginTop: 8, maxWidth: 500 }}>{result.verdictExplanation}</div>
                  </div>
                </div>

                {/* Score Ring */}
                <div className="card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 12 }}>
                  <div style={{ fontSize: 13, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: 1 }}>Authenticity Score</div>
                  <div style={{
                    width: 110, height: 110, borderRadius: '50%',
                    background: `conic-gradient(${vc.color} ${scorePct}deg, var(--bg-secondary) 0deg)`,
                    display: 'flex', alignItems: 'center', justifyContent: 'center', position: 'relative'
                  }}>
                    <div style={{ position: 'absolute', inset: 10, borderRadius: '50%', background: 'var(--bg-card)', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                      <span style={{ fontSize: 28, fontWeight: 800, color: vc.color }}>{score}</span>
                      <span style={{ fontSize: 10, color: 'var(--text-muted)' }}>/100</span>
                    </div>
                  </div>
                </div>
              </div>

              <ComparisonPanel targetId={screenshotId} />

              <ImageInspector 
                originalSrc={`/api/screenshots/${screenshotId}/file`} 
                elaSrc={result.elaImageFileName ? `/api/screenshots/${screenshotId}/ela-image` : null} 
              />

              <div className="card" style={{ marginBottom: 24 }}>
                <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 12 }}>Evidence integrity and module status</h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(220px,1fr))', gap: 10, fontSize: 13 }}>
                  <div><strong>SHA-256:</strong> <code style={{ wordBreak: 'break-all' }}>{screenshot?.sha256 || 'Unavailable'}</code></div>
                  <div><strong>MD5:</strong> <code style={{ wordBreak: 'break-all' }}>{screenshot?.md5 || 'Unavailable'}</code></div>
                  <div><strong>ELA:</strong> {result.elaImageFileName ? 'Completed (visual review only)' : (result.tamperHeuristicsJson?.moduleStatuses?.ela === 'NOT_APPLICABLE' ? 'Not applicable to this image format' : 'Unavailable; see module status')}</div>
                  <div><strong>Metadata:</strong> {result.metadataJson?.metadataStatus || 'Unavailable'}</div>
                </div>
                {result.tamperHeuristicsJson?.moduleStatuses && (
                  <div style={{ marginTop: 14, display: 'flex', flexWrap: 'wrap', gap: 8 }}>
                    {Object.entries(result.tamperHeuristicsJson.moduleStatuses).map(([name, status]) => (
                      <span key={name} className="badge badge-cyan">{name}: {status}</span>
                    ))}
                  </div>
                )}
                {result.tamperHeuristicsJson?.limitation && (
                  <p style={{ margin: '10px 0 0', color: 'var(--text-secondary)', fontSize: 12 }}>{result.tamperHeuristicsJson.limitation}</p>
                )}
                <p style={{ margin: '14px 0 0', color: 'var(--text-muted)', fontSize: 12 }}>
                  The score and metadata are screening indicators. They cannot prove that an image is authentic or identify the original without a trusted reference.
                </p>
              </div>

              {/* Metadata */}
              {result.metadataJson && Object.keys(result.metadataJson).length > 0 && (
                <div className="card" style={{ marginBottom: 24 }}>
                  <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16 }}>📄 Extracted image metadata</h3>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(280px,1fr))', gap: 8 }}>
                    {Object.entries(result.metadataJson).map(([k, v]) => (
                      <div key={k} style={{ display: 'flex', gap: 12, padding: '8px 12px', background: 'rgba(255,255,255,0.03)', borderRadius: 8 }}>
                        <span style={{ fontSize: 12, color: 'var(--text-muted)', minWidth: 100 }}>{k}</span>
                        <span style={{ fontSize: 13, fontFamily: 'JetBrains Mono, monospace', wordBreak: 'break-all' }}>{typeof v === 'object' ? JSON.stringify(v) : String(v)}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* OCR Text */}
              {result.ocrText && (
                <div className="card" style={{ marginBottom: 24 }}>
                  <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 12 }}>🔤 Extracted Text (OCR)</h3>
                  <pre style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 13, color: 'var(--text-secondary)', whiteSpace: 'pre-wrap', wordBreak: 'break-word', background: 'rgba(0,0,0,0.2)', padding: 16, borderRadius: 8, maxHeight: 300, overflow: 'auto' }}>
                    {result.ocrText}
                  </pre>
                </div>
              )}

              {/* Extracted Evidence */}
              {result.extractedEvidence?.length > 0 && (
                <div className="card">
                  <h3 style={{ fontSize: 15, fontWeight: 700, marginBottom: 16 }}>🔗 Extracted Evidence</h3>
                  <div className="table-wrap">
                    <table>
                      <thead><tr><th>Type</th><th>Value</th></tr></thead>
                      <tbody>
                        {result.extractedEvidence.map((e, i) => (
                          <tr key={i}>
                            <td><span className="badge badge-cyan">{e.evidenceType}</span></td>
                            <td style={{ fontFamily: 'JetBrains Mono, monospace', fontSize: 13 }}>{e.evidenceValue}</td>
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

export default AnalysisResultPage;
