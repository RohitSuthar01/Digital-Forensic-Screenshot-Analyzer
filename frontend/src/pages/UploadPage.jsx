import React, { useState, useRef } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../api/api';
import Sidebar from '../components/Sidebar';

const UploadPage = () => {
  const { caseId } = useParams();
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  const [file, setFile] = useState(null);
  const [dragOver, setDragOver] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');
  const [progress, setProgress] = useState(0);

  const validateFile = (f) => {
    if (f.size > 10 * 1024 * 1024) { setError('File must be under 10 MB.'); return false; }
    return true;
  };

  const onFileSelect = (f) => {
    setError('');
    if (validateFile(f)) setFile(f);
  };

  const handleDrop = (e) => {
    e.preventDefault(); setDragOver(false);
    const f = e.dataTransfer.files[0];
    if (f) onFileSelect(f);
  };

  const handleUpload = async () => {
    if (!file) { setError('Please select a file.'); return; }
    setUploading(true); setError(''); setProgress(0);
    const formData = new FormData();
    formData.append('file', file);
    try {
      const res = await api.post(`/screenshots/upload/${caseId}`, formData, {
        withCredentials: true,
        onUploadProgress: (e) => setProgress(Math.round((e.loaded * 100) / e.total))
      });
      navigate(`/cases/${caseId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Upload failed. Please try again.');
    } finally { setUploading(false); }
  };

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-content">
        <div className="page-header">
          <div>
            <Link to={`/cases/${caseId}`} style={{ color: 'var(--text-muted)', textDecoration: 'none', fontSize: 14, display: 'block', marginBottom: 4 }}>← Back to Case</Link>
            <h1 className="page-title">Upload Screenshot</h1>
            <p className="page-subtitle">Upload an image for forensic analysis</p>
          </div>
        </div>
        <div className="page-body" style={{ maxWidth: 600 }}>
          {error && <div className="alert alert-error">{error}</div>}

          <div
            className={`dropzone${dragOver ? ' active' : ''}`}
            onClick={() => fileInputRef.current?.click()}
            onDragOver={e => { e.preventDefault(); setDragOver(true); }}
            onDragLeave={() => setDragOver(false)}
            onDrop={handleDrop}
          >
            <span className="dropzone-icon">{file ? '📎' : '☁️'}</span>
            {file ? (
              <>
                <div className="dropzone-title" style={{ color: 'var(--accent-cyan)' }}>{file.name}</div>
                <div className="dropzone-sub">{(file.size / 1024).toFixed(1)} KB · {file.type}</div>
              </>
            ) : (
              <>
                <div className="dropzone-title">Drop your screenshot here</div>
                <div className="dropzone-sub">or click to browse · PNG, JPG, BMP up to 10 MB (file content is validated on upload)</div>
              </>
            )}
          </div>
          <input ref={fileInputRef} type="file" accept="image/png,image/jpeg,image/bmp" style={{ display: 'none' }} onChange={e => e.target.files[0] && onFileSelect(e.target.files[0])} />

          {uploading && (
            <div style={{ margin: '20px 0' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8, fontSize: 13, color: 'var(--text-secondary)' }}>
                <span>Uploading…</span><span>{progress}%</span>
              </div>
              <div style={{ height: 6, background: 'var(--border)', borderRadius: 3, overflow: 'hidden' }}>
                <div style={{ height: '100%', width: `${progress}%`, background: 'linear-gradient(90deg,var(--accent-cyan),var(--accent-purple))', borderRadius: 3, transition: 'width 0.3s' }} />
              </div>
            </div>
          )}

          <div style={{ display: 'flex', gap: 12, marginTop: 24 }}>
            <Link to={`/cases/${caseId}`} className="btn btn-secondary">Cancel</Link>
            <button className="btn btn-primary" onClick={handleUpload} disabled={!file || uploading} style={{ flex: 1, justifyContent: 'center' }}>
              {uploading ? '⏳ Uploading…' : '📤 Upload Screenshot'}
            </button>
          </div>

          <div className="card" style={{ marginTop: 28 }}>
            <h3 style={{ fontSize: 14, fontWeight: 700, marginBottom: 12 }}>📋 What happens next?</h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {['The original file is retained and hashed (MD5 and SHA-256)','Supported metadata is extracted; missing metadata is not treated as tampering','JPEG ELA may be available as a visualization for human review','OCR may extract text and evidence when the OCR module is available','After upload, start analysis from the case page to see findings and limitations'].map((step, i) => (
                <div key={i} style={{ display: 'flex', gap: 12, fontSize: 14, alignItems: 'flex-start' }}>
                  <span style={{ color: 'var(--accent-cyan)', fontWeight: 700, fontSize: 12, marginTop: 2 }}>{i + 1}.</span>
                  <span style={{ color: 'var(--text-secondary)' }}>{step}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UploadPage;
