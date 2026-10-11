import React, { useState, useRef } from 'react';
import { motion } from 'framer-motion';

export default function ImageInspector({ originalSrc, elaSrc, overlayLabel = 'Show ELA Overlay' }) {
  const [zoom, setZoom] = useState(1);
  const [showEla, setShowEla] = useState(false);
  const containerRef = useRef(null);

  const handleZoomIn = () => setZoom(prev => Math.min(prev + 0.5, 4));
  const handleZoomOut = () => setZoom(prev => Math.max(prev - 0.5, 1));
  const resetZoom = () => setZoom(1);

  return (
    <div className="card" style={{ marginBottom: 24, padding: 0, overflow: 'hidden' }}>
      <div style={{ padding: '16px 24px', borderBottom: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h3 style={{ fontSize: 15, fontWeight: 700, margin: 0 }}>🔍 Image Inspector (Zoom & Pan)</h3>
        <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
          {elaSrc && (
            <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer', fontSize: 13, marginRight: 16 }}>
              <input type="checkbox" checked={showEla} onChange={(e) => setShowEla(e.target.checked)} />
              {overlayLabel}
            </label>
          )}
          <button className="btn btn-secondary btn-sm" onClick={handleZoomOut} disabled={zoom <= 1}>-</button>
          <span style={{ fontSize: 13, width: 40, textAlign: 'center' }}>{Math.round(zoom * 100)}%</span>
          <button className="btn btn-secondary btn-sm" onClick={handleZoomIn} disabled={zoom >= 4}>+</button>
          <button className="btn btn-primary btn-sm" onClick={resetZoom}>Reset</button>
        </div>
      </div>
      
      <div 
        ref={containerRef}
        style={{ 
          height: 500, 
          background: '#050a15', 
          position: 'relative',
          overflow: 'hidden',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: zoom > 1 ? 'grab' : 'default'
        }}
      >
        <motion.div
          drag={zoom > 1}
          dragConstraints={containerRef}
          style={{ position: 'relative', scale: zoom }}
          animate={{ scale: zoom }}
          transition={{ type: 'spring', stiffness: 300, damping: 30 }}
        >
          {/* Original Image */}
          <img 
            src={originalSrc} 
            alt="Original" 
            draggable={false}
            style={{ 
              maxWidth: '100%', 
              maxHeight: 500, 
              objectFit: 'contain',
              display: 'block'
            }} 
            onError={(e) => { e.target.style.display = 'none'; }}
          />

          {/* ELA Overlay */}
          {elaSrc && (
            <motion.img 
              src={elaSrc} 
              alt="ELA Overlay" 
              draggable={false}
              initial={{ opacity: 0 }}
              animate={{ opacity: showEla ? 0.85 : 0 }}
              transition={{ duration: 0.3 }}
              style={{ 
                position: 'absolute',
                top: 0,
                left: 0,
                width: '100%',
                height: '100%',
                objectFit: 'contain',
                pointerEvents: 'none'
              }}
              onError={(e) => { e.target.style.display = 'none'; }}
            />
          )}
        </motion.div>

        {!elaSrc && showEla && (
           <div style={{ position: 'absolute', bottom: 16, background: 'rgba(0,0,0,0.8)', padding: '8px 16px', borderRadius: 8, fontSize: 12 }}>
             ELA Visualization not applicable or unavailable.
           </div>
        )}
      </div>
      <div style={{ padding: '12px 24px', background: 'var(--bg-secondary)', fontSize: 12, color: 'var(--text-secondary)' }}>
        <strong>Hint:</strong> Use the +/- buttons to zoom. When zoomed in, click and drag to pan the image. Toggle the ELA overlay to compare directly.
      </div>
    </div>
  );
}
