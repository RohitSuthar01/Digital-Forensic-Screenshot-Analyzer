import React from 'react';
import { Link } from 'react-router-dom';

const features = [
  { icon: '🔬', title: 'Error Level Analysis', desc: 'Detect JPEG compression artifacts to identify edited regions in images.' },
  { icon: '📄', title: 'EXIF Metadata', desc: 'Extract hidden metadata including camera info, GPS, and editing software signatures.' },
  { icon: '🔤', title: 'OCR Evidence', desc: 'Automatically extract URLs, emails, phone numbers, and IPs from screenshot text.' },
  { icon: '🏛️', title: 'Chain of Custody', desc: 'Cryptographic hash verification (MD5 + SHA-256) and full audit trail logging.' },
  { icon: '📊', title: 'Authenticity Score', desc: 'AI-driven 0–100 score with AUTHENTIC / SUSPICIOUS / LIKELY_TAMPERED verdict.' },
  { icon: '📑', title: 'PDF Reports', desc: 'Generate court-ready forensic PDF reports with all findings and evidence.' },
];

const LandingPage = () => (
  <div className="landing">
    <nav className="landing-nav">
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <div className="logo-icon" style={{
          width: 36, height: 36, borderRadius: 10, background: 'linear-gradient(135deg,#00E5FF,#7C4DFF)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 18
        }}>🔍</div>
        <span style={{ fontWeight: 700, fontSize: 15, background: 'linear-gradient(135deg,#00E5FF,#7C4DFF)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
          DFSA Platform
        </span>
      </div>
      <div style={{ display: 'flex', gap: 12 }}>
        <Link to="/login" className="btn btn-secondary btn-sm">Log In</Link>
        <Link to="/register" className="btn btn-primary btn-sm">Get Started</Link>
      </div>
    </nav>

    <div className="landing-hero">
      <div className="hero-badge">⚡ Digital Forensics Suite</div>
      <h1 className="hero-title">
        Analyze Screenshots<br />
        <span className="gradient-text">Like a Detective</span>
      </h1>
      <p className="hero-sub">
        Professional-grade screenshot forensics for cyber-crime investigators.
        Detect tampering, extract evidence, and maintain unbreakable chain of custody.
      </p>
      <div className="hero-actions">
        <Link to="/register" className="btn btn-primary btn-lg">Start Analyzing →</Link>
        <Link to="/login" className="btn btn-secondary btn-lg">Sign In</Link>
      </div>

      <div className="hero-features">
        {features.map(f => (
          <div key={f.title} className="feature-card">
            <span className="feature-icon">{f.icon}</span>
            <div className="feature-title">{f.title}</div>
            <div className="feature-desc">{f.desc}</div>
          </div>
        ))}
      </div>
    </div>
  </div>
);

export default LandingPage;