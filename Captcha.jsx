import React, { useState, useEffect } from 'react'

export default function Captcha({ onValidate }) {
  const [a, setA] = useState(1)
  const [b, setB] = useState(8)
  const [ans, setAns] = useState('')

  const refresh = () => {
    setA(Math.floor(Math.random() * 15) + 1)
    setB(Math.floor(Math.random() * 15) + 1)
    setAns('')
    onValidate(false)
  }

  useEffect(() => { refresh() }, [])

  const handleChange = (e) => {
    const v = e.target.value
    setAns(v)
    onValidate(parseInt(v) === a + b)
  }

  return (
    <div style={{
      display:'flex', alignItems:'center', gap:12,
      background:'rgba(255,255,255,0.05)', border:'1px solid rgba(255,255,255,0.08)',
      borderRadius:10, padding:'12px 16px'
    }}>
      <div style={{fontFamily:'Syne,sans-serif', fontSize:20, fontWeight:700, color:'#00d4e8'}}>
        {a} <span style={{color:'rgba(232,244,248,0.45)'}}>+</span> {b} <span style={{color:'rgba(232,244,248,0.45)'}}>=</span>
      </div>
      <input
        type="number"
        value={ans}
        onChange={handleChange}
        placeholder="?"
        style={{
          width:72, textAlign:'center',
          background:'rgba(255,255,255,0.08)', border:'1px solid rgba(255,255,255,0.08)',
          borderRadius:8, padding:'8px', color:'#e8f4f8', fontSize:16, fontWeight:700,
          outline:'none', fontFamily:'Syne,sans-serif',
          transition:'border-color .2s'
        }}
        onFocus={e => e.target.style.borderColor='#00d4e8'}
        onBlur={e => e.target.style.borderColor='rgba(255,255,255,0.08)'}
      />
      <button
        onClick={refresh}
        type="button"
        title="Refresh"
        style={{background:'none', border:'none', color:'rgba(232,244,248,0.2)', cursor:'pointer', padding:4, lineHeight:0, transition:'color .2s'}}
        onMouseOver={e => e.currentTarget.style.color='#00d4e8'}
        onMouseOut={e => e.currentTarget.style.color='rgba(232,244,248,0.2)'}
      >
        <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24">
          <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
        </svg>
      </button>
    </div>
  )
}
