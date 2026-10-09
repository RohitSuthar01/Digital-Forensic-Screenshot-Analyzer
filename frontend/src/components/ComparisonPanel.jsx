import React, { useEffect, useState } from 'react';
import axios from 'axios';
import ImageInspector from './ImageInspector';

export default function ComparisonPanel({ targetId }) {
  const [caseScreenshots, setCaseScreenshots] = useState([]);
  const [targetEvidence, setTargetEvidence] = useState(null);
  const [referenceId, setReferenceId] = useState('');
  const [comparisonResult, setComparisonResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Fetch the target screenshot to get its caseId, then fetch case screenshots
  useEffect(() => {
    axios.get(`/api/screenshots/${targetId}`, { withCredentials: true })
      .then(res => {
        setTargetEvidence(res.data);
        const caseId = res.data.caseId;
        return axios.get(`/api/screenshots/case/${caseId}`, { withCredentials: true });
      })
      .then(res => {
        // Filter out the target itself from the references list
        setCaseScreenshots(res.data.filter(s => s.id !== parseInt(targetId)));
      })
      .catch(err => {
        console.error('Failed to load screenshots for comparison', err);
      });
      
    // Also try to load existing comparison result
    loadExistingComparison();
  }, [targetId]);

  const loadExistingComparison = () => {
    axios.get(`/api/screenshots/${targetId}/comparison`, { withCredentials: true })
      .then(res => {
        const saved = res.data;
        if (String(saved.targetScreenshot?.id) !== String(targetId) || !saved.referenceScreenshot?.id) {
          setComparisonResult(null);
          return;
        }
        setReferenceId(String(saved.referenceScreenshot.id));
        setComparisonResult(saved);
      })
      .catch(() => setComparisonResult(null)); // Not found is fine
  };

  const selectedReference = caseScreenshots.find(s => String(s.id) === String(referenceId));

  const handleCompare = () => {
    if (!referenceId) return;
    setLoading(true);
    setError('');
    axios.post(`/api/screenshots/${targetId}/compare?referenceId=${referenceId}`, null, { withCredentials: true })
      .then(res => setComparisonResult(res.data))
      .catch(err => {
          const resData = err.response?.data;
          const msg = typeof resData === 'string' ? resData : (resData?.message || 'Comparison failed');
          setError(msg);
      })
      .finally(() => setLoading(false));
  };

  return (
    <div className="card" style={{ marginBottom: 24, border: '1px solid var(--accent-blue)' }}>
      <h3 style={{ fontSize: 16, fontWeight: 700, marginBottom: 12, color: 'var(--accent-blue)' }}>⚖️ Reference-Image Comparison</h3>
      <p style={{ fontSize: 13, color: 'var(--text-secondary)', marginBottom: 16 }}>
        Select a reference screenshot from this same case to compare against this target screenshot.
        This will detect visual differences and changes in the decoded image content.
      </p>
      
      <div style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
        <select 
          className="form-input" 
          value={referenceId} 
          onChange={e => {
            setReferenceId(e.target.value);
            if (comparisonResult && String(comparisonResult.referenceScreenshot?.id) !== e.target.value) {
              setComparisonResult(null);
            }
          }}
          style={{ maxWidth: 400, color: '#1e293b', backgroundColor: '#f8fafc', fontWeight: 500 }}
        >
          <option value="">-- Select Reference Screenshot --</option>
          {caseScreenshots.map(s => (
            <option key={s.id} value={s.id}>#{s.id} - {s.originalFilename} ({new Date(s.uploadedAt).toLocaleString()})</option>
          ))}
        </select>
        <button 
          className="btn btn-primary" 
          onClick={handleCompare} 
          disabled={!referenceId || loading}
        >
          {loading ? 'Comparing...' : 'Run Comparison'}
        </button>
      </div>

      {referenceId && selectedReference && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit,minmax(260px,1fr))', gap: 12, marginBottom: 16, fontSize: 12 }}>
          {[{ label: `Target #${targetId}`, evidence: targetEvidence }, { label: `Reference #${referenceId}`, evidence: selectedReference }].map(item => (
            <div key={item.label} style={{ padding: 12, background: 'rgba(255,255,255,0.03)', borderRadius: 8 }}>
              <strong>{item.label} file fingerprints</strong>
              <div style={{ marginTop: 6 }}>MD5: <code style={{ wordBreak: 'break-all' }}>{item.evidence?.md5 || 'Unavailable'}</code></div>
              <div style={{ marginTop: 4 }}>SHA-256: <code style={{ wordBreak: 'break-all' }}>{item.evidence?.sha256 || 'Unavailable'}</code></div>
            </div>
          ))}
          <div style={{ gridColumn: '1 / -1', color: 'var(--text-muted)' }}>
            Matching SHA-256 fingerprints mean the stored files are byte-for-byte identical. Different hashes only mean the file bytes differ; the pixel comparison below checks visual content separately.
          </div>
        </div>
      )}

      {error && <div className="alert alert-error" style={{ marginBottom: 16 }}>{error}</div>}

      {comparisonResult && (
        <div style={{ padding: 16, background: 'rgba(255,255,255,0.03)', borderRadius: 8 }}>
          <h4 style={{ marginBottom: 12, color: comparisonResult.visualDifferenceDetected ? 'var(--accent-red)' : 'var(--accent-green)' }}>
            {comparisonResult.visualDifferenceDetected ? '🚨 Difference detected relative to the selected reference' : '✅ No visual differences detected'}
          </h4>
          
          <div style={{ fontSize: 13, marginBottom: 16 }}>
            <strong>Findings:</strong> {comparisonResult.changedRegionsDetails}<br/>
            <strong>Method:</strong> {comparisonResult.methodVersion}<br/>
            <strong>Limitations:</strong> {comparisonResult.limitations}
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
            <div>
              <h5 style={{ marginBottom: 8, fontSize: 13, color: 'var(--text-muted)' }}>Target Screenshot (Current: #{targetId})</h5>
              <ImageInspector 
                originalSrc={`/api/screenshots/${targetId}/file`} 
                elaSrc={comparisonResult.differenceMapFileName ? `/api/screenshots/${targetId}/comparison-image` : null}
                overlayLabel="Show Difference Map"
              />
            </div>
            <div>
              <h5 style={{ marginBottom: 8, fontSize: 13, color: 'var(--text-muted)' }}>Reference Screenshot (#{comparisonResult.referenceScreenshot?.id})</h5>
              <ImageInspector 
                originalSrc={`/api/screenshots/${comparisonResult.referenceScreenshot?.id}/file`} 
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
