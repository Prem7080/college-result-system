import React, { useState } from 'react'
import { changePassword } from '../services/api'

export default function ChangePasswordModal({ onClose }) {
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState('')

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const handleSubmit = async () => {
    setError('')
    if (!form.currentPassword) { setError('Enter your current password'); return }
    if (!form.newPassword) { setError('Enter a new password'); return }
    if (form.newPassword.length < 6) { setError('New password must be at least 6 characters'); return }
    if (form.newPassword !== form.confirmPassword) { setError('New password and confirm password do not match'); return }
    if (form.newPassword === form.currentPassword) { setError('New password must be different from current password'); return }

    setLoading(true)
    try {
      await changePassword(form)
      setSuccess(true)
      setTimeout(onClose, 2500)
    } catch (e) {
      setError(e.response?.data?.message || e.message || 'Failed to change password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
      style={{
        position:'fixed', inset:0, background:'rgba(0,0,0,0.65)',
        backdropFilter:'blur(6px)', zIndex:200,
        display:'flex', alignItems:'center', justifyContent:'center', padding:24
      }}
    >
      <div style={{
        background:'#0c1a28', border:'1px solid rgba(255,255,255,0.08)',
        borderRadius:14, padding:32, width:'100%', maxWidth:420,
        position:'relative', animation:'fadeUp .3s ease'
      }}>
        <button
          onClick={onClose}
          style={{
            position:'absolute', top:16, right:16, background:'none', border:'none',
            color:'rgba(232,244,248,0.4)', cursor:'pointer', fontSize:20, lineHeight:1
          }}
        >✕</button>

        <h3 style={{fontFamily:'Syne,sans-serif', fontSize:18, fontWeight:700, marginBottom:4, display:'flex', alignItems:'center', gap:8}}>
          <svg width="18" height="18" fill="#00d4e8" viewBox="0 0 24 24">
            <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
          </svg>
          Change Password
        </h3>
        <p style={{color:'rgba(232,244,248,0.45)', fontSize:13, marginBottom:24}}>Update your login password below.</p>

        {success ? (
          <div style={{
            background:'rgba(34,197,94,0.1)', border:'1px solid rgba(34,197,94,0.3)',
            borderRadius:10, padding:'16px 20px', textAlign:'center',
            color:'#4ade80', fontSize:15, fontWeight:600
          }}>
            Password changed successfully
          </div>
        ) : (
          <>
            {error && (
              <div style={{
                background:'rgba(239,68,68,0.1)', border:'1px solid rgba(239,68,68,0.25)',
                borderRadius:8, padding:'10px 14px', color:'#f87171',
                fontSize:13, marginBottom:16
              }}>
                {error}
              </div>
            )}

            <Field label="Current Password" type="password"
              value={form.currentPassword} onChange={v => set('currentPassword', v)}
              placeholder="Enter current password" />
            <Field label="New Password" type="password"
              value={form.newPassword} onChange={v => set('newPassword', v)}
              placeholder="Enter new password" />
            <Field label="Confirm New Password" type="password"
              value={form.confirmPassword} onChange={v => set('confirmPassword', v)}
              placeholder="Re-enter new password" />

            <button
              onClick={handleSubmit}
              disabled={loading}
              style={{
                width:'100%', padding:14, borderRadius:10, background:'#00d4e8',
                color:'#07111d', border:'none', fontFamily:'Syne,sans-serif',
                fontWeight:700, fontSize:15, cursor: loading ? 'not-allowed' : 'pointer',
                opacity: loading ? 0.7 : 1, display:'flex', alignItems:'center',
                justifyContent:'center', gap:8, transition:'all .2s',
                boxShadow:'0 0 24px rgba(0,212,232,0.2)', marginTop:8
              }}
            >
              <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24">
                <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
              </svg>
              {loading ? 'Updating...' : 'Update Password'}
            </button>
          </>
        )}
      </div>
    </div>
  )
}

function Field({ label, type, value, onChange, placeholder }) {
  return (
    <div style={{marginBottom:16}}>
      <label style={{
        display:'block', fontSize:10, fontWeight:700,
        letterSpacing:'0.15em', textTransform:'uppercase',
        color:'#00d4e8', marginBottom:8
      }}>{label}</label>
      <input
        type={type}
        value={value}
        onChange={e => onChange(e.target.value)}
        placeholder={placeholder}
        style={{
          width:'100%', background:'rgba(255,255,255,0.05)',
          border:'1px solid rgba(255,255,255,0.08)', borderRadius:10,
          padding:'12px 16px', color:'#e8f4f8', fontFamily:'DM Sans,sans-serif',
          fontSize:14, outline:'none', transition:'border-color .2s'
        }}
        onFocus={e => e.target.style.borderColor='#00d4e8'}
        onBlur={e => e.target.style.borderColor='rgba(255,255,255,0.08)'}
      />
    </div>
  )
}
