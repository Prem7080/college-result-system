import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './hooks/useAuth'
import HomePage          from './pages/HomePage'
import CheckResultPage   from './pages/CheckResultPage'
import ResultDisplayPage from './pages/ResultDisplayPage'
import TeacherLoginPage  from './pages/TeacherLoginPage'
import AdminDashboard    from './pages/AdminDashboard'

/** Logged-in teachers cannot see the login form — go to dashboard. */
function GuestTeacherRoute({ children }) {
  const { isAuthenticated } = useAuth()
  if (isAuthenticated) {
    return <Navigate to="/teacher/dashboard" replace />
  }
  return <>{children}</>
}

/** Teacher admin — must be logged in or go to login. */
function ProtectedTeacherRoute({ children }) {
  const { isAuthenticated } = useAuth()
  if (!isAuthenticated) {
    return <Navigate to="/teacher/login" replace />
  }
  return <>{children}</>
}

/** /teacher → dashboard if session exists, otherwise login. */
function TeacherIndexRedirect() {
  const { isAuthenticated } = useAuth()
  return <Navigate to={isAuthenticated ? '/teacher/dashboard' : '/teacher/login'} replace />
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        {/* ── Student (public) ── */}
        <Route path="/"             element={<HomePage />} />
        <Route path="/check-result" element={<CheckResultPage />} />
        <Route path="/result"       element={<ResultDisplayPage />} />

        {/* ── Teacher (login vs dashboard by session) ── */}
        <Route path="/teacher" element={<TeacherIndexRedirect />} />
        <Route
          path="/teacher/login"
          element={
            <section aria-labelledby="teacher-login-title" style={{ minHeight: '100%' }}>
              <GuestTeacherRoute>
                <TeacherLoginPage />
              </GuestTeacherRoute>
            </section>
          }
        />
        <Route
          path="/teacher/dashboard"
          element={
            <section aria-label="Teacher admin dashboard" style={{ minHeight: '100%' }}>
              <ProtectedTeacherRoute>
                <AdminDashboard />
              </ProtectedTeacherRoute>
            </section>
          }
        />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  )
}
