import React, { useState } from 'react';
import { supabase } from '../lib/supabase';
import './Login.css';

export default function Login({ onLoginSuccess }) {
  const [nim, setNim] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const { data: user, error: dbError } = await supabase
        .from('users')
        .select('*')
        .eq('nim_nip', nim.trim())
        .eq('password', password.trim())
        .single();

      if (dbError || !user) throw new Error('NIM atau Password salah!');
      if (user.role !== 'admin' && user.role !== 'civitas') throw new Error('Akses ditolak.');

      localStorage.setItem('user_role', user.role);
      localStorage.setItem('user_name', user.name);
      localStorage.setItem('is_logged_in', 'true');
      
      onLoginSuccess();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          {/* Tambahkan logo Unram di sini jika sudah ada */}
          <h2 className="login-title">UnramHUB</h2>
          <p className="login-subtitle">Silakan login untuk mengakses portal admin</p>
        </div>
        
        {error && <div className="error-box">{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label className="input-label">NIM / NIP / Username</label>
            <input 
              type="text" 
              className="login-input" 
              placeholder="Masukkan identitas Anda"
              value={nim} 
              onChange={(e) => setNim(e.target.value)} 
              required 
            />
          </div>
          <div className="input-group">
            <label className="input-label">Password</label>
            <input 
              type="password" 
              className="login-input" 
              placeholder="••••••••"
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              required 
            />
          </div>
          <button type="submit" className="login-button" disabled={loading}>
            {loading ? 'Memvalidasi...' : 'Masuk ke Sistem'}
          </button>
        </form>

        <div className="login-footer">
          <p>© 2026 UnramHUB. Sistem Informasi Terpadu.</p>
        </div>
      </div>
    </div>
  );
}