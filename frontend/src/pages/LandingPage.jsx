import React from 'react';
import { Link } from 'react-router-dom';
import HeroScene from '../components/HeroScene';

const features = [
  { icon: '🔬', title: 'Error Level Analysis', desc: 'Visualize JPEG recompression differences for human review; ELA alone cannot prove editing.' },
  { icon: '📄', title: 'EXIF Metadata', desc: 'Extract hidden metadata including camera info, GPS, and editing software signatures.' },
  { icon: '🔤', title: 'OCR Evidence', desc: 'Automatically extract URLs, emails, phone numbers, and IPs from screenshot text.' },
  { icon: '🏛️', title: 'Evidence Integrity', desc: 'MD5 and SHA-256 file fingerprints with application audit records.' },
  { icon: '📊', title: 'Forensic Review', desc: 'Review metadata, OCR, LSB screening, and comparison indicators with their limitations.' },
  { icon: '📑', title: 'PDF Reports', desc: 'Generate downloadable reports containing analysis findings and evidence details.' },
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

    <div className="landing-hero" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
      <div className="hero-badge">⚡ Digital Forensics Suite</div>
      <h1 className="hero-title">
        Analyze Screenshots<br />
        <span className="gradient-text">Like a Detective</span>
      </h1>
      <p className="hero-sub">
        A screenshot evidence-review tool for investigators.
        Examine forensic indicators, compare files, and record findings for human review.
      </p>
      
      {/* 3D Hero Visualization */}
      <HeroScene />

      <div className="hero-actions" style={{ marginTop: '2rem' }}>
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
