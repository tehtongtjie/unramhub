import { useState } from 'react';
import { supabase } from '../lib/supabase';
import './Login.css';

export default function Login({ onLoginSuccess }) {
  const [nim, setNim] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    if (e) e.preventDefault(); // Mencegah reload halaman baik dari form submit atau click event
    setLoading(true);
    setError('');

    try {
      // Mengambil data user berdasarkan tabel kustom 'users' di Supabase
      const { data: user, error: dbError } = await supabase
        .from('users')
        .select('*')
        .eq('nim_nip', nim.trim())
        .eq('password', password.trim()) // Catatan: Disarankan hash password pada produksi
        .single();

      if (dbError || !user) throw new Error('Nama akun atau kata sandi salah!');
      if (user.role !== 'admin' && user.role !== 'civitas') throw new Error('Akses ditolak. Anda bukan Admin.');

      // Menyimpan detail sesi lokal
      localStorage.setItem('user_role', user.role);
      localStorage.setItem('user_name', user.name);
      localStorage.setItem('user_id', user.id);
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
      {/* Overlay Gelap Transparan di Atas Gambar Background */}
      <div className="login-overlay"></div>
      
      <div className="login-card">
        {/* AREA ATAS KARTU (PADDING INTERNAL VIA CSS) */}
        <div className="login-body">
          <div className="login-header">
            {/* Memanggil logo unram yang ditaruh di folder public/logo_unram.png */}
            <img 
              src="/logo_unram.png" 
              alt="Logo Universitas Mataram" 
              className="login-logo" 
              onError={(e) => { e.target.src = "https://via.placeholder.com/150?text=Logo+Unram" }}
            />
            <hr className="login-divider" />
            <h2 className="login-title">Single Sign On</h2>
          </div>
          
          {error && <div className="error-box">{error}</div>}
          
          <form onSubmit={handleSubmit}>
            {/* INPUT 1: NAMA AKUN DENGAN BADGE IKON */}
            <div className="input-group-sso">
              <div className="input-badge">👤</div>
              <input 
                type="text" 
                className="login-input-sso" 
                placeholder="Nama akun"
                value={nim} 
                onChange={(e) => setNim(e.target.value)} 
                required 
              />
            </div>
            
            {/* INPUT 2: KATA SANDI PLAIN BOX */}
            <div className="input-group-sso no-badge">
              <input 
                type="password" 
                className="login-input-sso" 
                placeholder="Kata sandi"
                value={password} 
                onChange={(e) => setPassword(e.target.value)} 
                required 
              />
            </div>

            {/* LINK BANTUAN */}
            <div className="login-links">
              <span className="login-link">Lupa kata sandi?</span>
              <span className="login-link text-right">Bantuan</span>
            </div>
          </form>
        </div>

        {/* BUTTON LANJUT (Menempel Penuh Tanpa Jeda di Bagian Bawah Kartu) */}
        <button 
          onClick={handleSubmit}
          type="submit" 
          className="login-button-sso" 
          disabled={loading}
        >
          {loading ? 'Memvalidasi...' : 'Lanjut  ➔'}
        </button>
      </div>
      
      <div className="login-system-footer">
        <p>Universitas Mataram © 2026</p>
      </div>
    </div>
  );
}