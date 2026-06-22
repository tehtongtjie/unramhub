import { useState } from "react";
import { supabase } from "../lib/supabase";
import "./Login.css";

export default function Login({ onLoginSuccess, logoSrc = "/public/logo-login.png" }) {
  const [nim, setNim] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [logoFailed, setLogoFailed] = useState(false);

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const { data: user, error: dbError } = await supabase
        .from("users")
        .select("*")
        .eq("nim_nip", nim.trim())
        .eq("password", password.trim())
        .single();

      if (dbError || !user) throw new Error("Nama akun atau kata sandi salah!");
      if (user.role !== "admin" && user.role !== "civitas") throw new Error("Akses ditolak. Anda bukan Admin.");

      localStorage.setItem("user_role", user.role);
      localStorage.setItem("user_name", user.name);
      localStorage.setItem("user_id", user.id);
      localStorage.setItem("is_logged_in", "true");

      onLoginSuccess();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-overlay"></div>

      <div className="login-card">
        <div className="login-body">
          <div className="login-header">
            <div className="login-logo-wrap">
              {!logoFailed ? (
                <img
                  src={logoSrc}
                  alt="Logo Universitas Mataram"
                  className="login-logo"
                  onError={() => setLogoFailed(true)}
                />
              ) : (
                <div className="login-logo-fallback">UH</div>
              )}
            </div>
            <hr className="login-divider" />
            <h2 className="login-title">Single Sign On</h2>
          </div>

          {error && <div className="error-box">{error}</div>}

          <form onSubmit={handleSubmit}>
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

            <div className="login-links">
              <span className="login-link">Lupa kata sandi?</span>
              <span className="login-link text-right">Bantuan</span>
            </div>

            <button type="submit" className="login-button-sso" disabled={loading}>
              {loading ? "Memvalidasi..." : "Lanjut →"}
            </button>
          </form>
        </div>
      </div>

      <div className="login-system-footer">
        <p>Universitas Mataram © 2026</p>
      </div>
    </div>
  );
}
