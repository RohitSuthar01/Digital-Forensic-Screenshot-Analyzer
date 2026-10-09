import React, { useRef, useState } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { Float, Environment } from '@react-three/drei';
import * as THREE from 'three';

function ScanningPanel() {
  const meshRef = useRef();
  const scannerRef = useRef();

  useFrame((state) => {
    // Rotate panel slowly
    meshRef.current.rotation.y = Math.sin(state.clock.elapsedTime * 0.5) * 0.2;
    meshRef.current.rotation.x = Math.sin(state.clock.elapsedTime * 0.3) * 0.1;
    
    // Move scanner up and down
    scannerRef.current.position.y = Math.sin(state.clock.elapsedTime * 2) * 1.5;
  });

  return (
    <Float speed={2} rotationIntensity={0.5} floatIntensity={1}>
      <group ref={meshRef}>
        {/* Main Panel Background */}
        <mesh position={[0, 0, 0]}>
          <planeGeometry args={[4, 3]} />
          <meshStandardMaterial color="#0A1128" roughness={0.2} metalness={0.8} transparent opacity={0.8} />
        </mesh>
        
        {/* Glowing border / Grid placeholder */}
        <lineSegments>
          <edgesGeometry args={[new THREE.PlaneGeometry(4, 3)]} />
          <lineBasicMaterial color="#00E5FF" linewidth={2} />
        </lineSegments>

        {/* Floating Data Nodes */}
        <mesh position={[-1.2, 0.8, 0.2]}>
          <boxGeometry args={[0.5, 0.2, 0.1]} />
          <meshStandardMaterial color="#7C4DFF" emissive="#7C4DFF" emissiveIntensity={0.5} />
        </mesh>
        <mesh position={[1.0, -0.5, 0.2]}>
          <boxGeometry args={[0.8, 0.3, 0.1]} />
          <meshStandardMaterial color="#00E5FF" emissive="#00E5FF" emissiveIntensity={0.8} />
        </mesh>

        {/* Scanner Beam */}
        <mesh ref={scannerRef} position={[0, 0, 0.3]}>
          <planeGeometry args={[4.2, 0.05]} />
          <meshBasicMaterial color="#00E5FF" transparent opacity={0.8} />
        </mesh>
      </group>
    </Float>
  );
}

export default function HeroScene() {
  return (
    <div style={{ height: '400px', width: '100%', position: 'relative', zIndex: 1, marginTop: '2rem' }}>
      <Canvas camera={{ position: [0, 0, 5], fov: 45 }} gl={{ antialias: true, alpha: true }}>
        <ambientLight intensity={0.5} />
        <directionalLight position={[10, 10, 10]} intensity={1} />
        <directionalLight position={[-10, -10, -10]} intensity={0.5} color="#7C4DFF" />
        
        <React.Suspense fallback={null}>
          <ScanningPanel />
          <Environment preset="city" />
        </React.Suspense>
      </Canvas>
    </div>
  );
}
