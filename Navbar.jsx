import React from 'react'
import { Link, useLocation } from 'react-router-dom'

export default function Navbar() {
  const { pathname } = useLocation()
  return (
    <nav style={{
      position:'fixed', top:0, left:0, right:0, zIndex:100, height:62,
      display:'flex', alignItems:'center', justifyContent:'space-between',
      padding:'0 28px',
      background:'rgba(7,17,29,0.85)', backdropFilter:'blur(20px)',
      borderBottom:'1px solid rgba(255,255,255,0.08)'
    }}>
      <Link to="/" style={{display:'flex',alignItems:'center',gap:10,textDecoration:'none'}}>
        <div style={{
          width:32, height:32, borderRadius:8,
          background:'rgba(0,212,232,0.15)', border:'1px solid rgba(0,212,232,0.3)',
          display:'flex', alignItems:'center', justifyContent:'center'
        }}>
          <svg width="16" height="16" fill="#00d4e8" viewBox="0 0 24 24">
            <path d="M12 3L1 9l11 6 9-4.91V17h2V9L12 3zM5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82z"/>
          </svg>
        </div>
        <span style={{fontFamily:'Syne,sans-serif', fontWeight:700, fontSize:15}}>
          <span style={{color:'#00d4e8'}}>ENTC</span> Results
        </span>
      </Link>

      <div style={{display:'flex', gap:6}}>
        <NavBtn to="/" label="Home" active={pathname==='/'} />
        <NavBtn to="/check-result" label="Check Result" active={pathname==='/check-result'} />
      </div>
    </nav>
  )
}

function NavBtn({ to, label, active }) {
  return (
    <Link to={to} style={{
      padding:'7px 18px', borderRadius:8, fontSize:13, fontWeight:500,
      border: active ? '1px solid #00d4e8' : '1px solid rgba(255,255,255,0.08)',
      background: active ? '#00d4e8' : 'transparent',
      color: active ? '#07111d' : 'rgba(232,244,248,0.45)',
      textDecoration:'none', cursor:'pointer', transition:'all .2s',
      fontFamily:'DM Sans,sans-serif', fontWeight: active ? 600 : 500
    }}>
      {label}
    </Link>
  )
}
