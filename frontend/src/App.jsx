import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import CasesPage from './pages/CasesPage';
import CaseDetailPage from './pages/CaseDetailPage';
import UploadPage from './pages/UploadPage';
import AnalysisResultPage from './pages/AnalysisResultPage';
import ProfilePage from './pages/ProfilePage';
import AdminUsersPage from './pages/AdminUsersPage';
import AuditLogsPage from './pages/AuditLogsPage';
import NotFoundPage from './pages/NotFoundPage';
import ForbiddenPage from './pages/ForbiddenPage';
import { useAuth } from './context/AuthContext';

function App() {
  const { user, loading } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <Router>
      <Routes>
        {/* Public routes */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Protected routes */}
        <Route
          path="/dashboard"
          element={
            user ? <DashboardPage /> : <Navigate to="/login" replace />
          }
        />
        <Route
          path="/cases"
          element={
            user ? <CasesPage /> : <Navigate to="/login" replace />
          }
        />
        <Route
          path="/cases/:id"
          element={
            user ? <CaseDetailPage /> : <Navigate to="/login" replace />
          }
        />
        <Route
          path="/upload/:caseId"
          element={
            user ? <UploadPage /> : <Navigate to="/login" replace />
          }
        />
        <Route
          path="/analysis/:screenshotId"
          element={
            user ? <AnalysisResultPage /> : <Navigate to="/login" replace />
          }
        />
        <Route
          path="/profile"
          element={
            user ? <ProfilePage /> : <Navigate to="/login" replace />
          }
        />
        <Route
          path="/admin/users"
          element={
            user && user.role === 'ADMIN' ? (
              <AdminUsersPage />
            ) : (
              <Navigate to="/forbidden" replace />
            )
          }
        />
        <Route
          path="/admin/audit-logs"
          element={
            user && user.role === 'ADMIN' ? (
              <AuditLogsPage />
            ) : (
              <Navigate to="/forbidden" replace />
            )
          }
        />
        <Route path="/forbidden" element={<ForbiddenPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Router>
  );
}

export default App;