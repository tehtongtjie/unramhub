import React, { useState, useEffect } from "react";
import { supabase } from "../../lib/supabase";

export default function SettingsPanel() {
  const [maxSize, setMaxSize] = useState("5");
  const [isMaintenance, setIsMaintenance] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchSettings();
  }, []);

  const fetchSettings = async () => {
    const { data } = await supabase.from("system_settings").select("*");
    if (data) {
      const sizeRow = data.find(r => r.key === "max_upload_size_mb");
      const maintRow = data.find(r => r.key === "maintenance_mode");
      
      if (sizeRow) setMaxSize(sizeRow.value);
      if (maintRow) setIsMaintenance(maintRow.value === "true");
    }
  };

  const handleSaveSettings = async () => {
    try {
      setLoading(true);

      // JIKA DI MASA DEPAN TAKTIK RPC / MOBILE VALIDATION SUDAH SIAP, AKTIFKAN KEMBALI KODE INI:
      // await supabase
      //   .from("system_settings")
      //   .update({ value: String(maxSize) })
      //   .eq("key", "max_upload_size_mb");

      // Update status operasional mode maintenance aplikasi (Hanya ini yang dieksekusi karena sudah berfungsi)
      await supabase
        .from("system_settings")
        .update({ value: String(isMaintenance) })
        .eq("key", "maintenance_mode");

      alert("Konfigurasi parameter sistem yang aktif berhasil disimpan!");
    } catch (err) {
      console.error(err);
      alert("Gagal merubah konfigurasi.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="content-panel">
      <div className="panel-header">
        <h3>Konfigurasi Sistem Utama</h3>
        <p className="description-text">Atur batasan keamanan API dan hak kontrol runtime aplikasi seluler mahasiswa UnramHUB dari jarak jauh.</p>
      </div>

      <div style={{ maxWidth: "500px", display: "flex", flexDirection: "column", gap: "1.5rem", marginTop: "1.5rem" }}>
        
        {/* Opsi 1: Batas File Bukti (DI-DISABLE KARENA BELUM TERHUBUNG KE STRUKTUR BUCKET) */}
        <div style={{ 
          backgroundColor: "#f1f5f9", 
          padding: "16px", 
          borderRadius: "8px", 
          border: "1px solid #cbd5e1",
          opacity: 0.65,
          position: "relative"
        }}>
          {/* Badge Indikator Belum Berfungsi */}
          <span style={{
            position: "absolute",
            top: "12px",
            right: "12px",
            backgroundColor: "#94a3b8",
            color: "#fff",
            fontSize: "11px",
            fontWeight: "600",
            padding: "2px 8px",
            borderRadius: "4px"
          }}>
            Belum Berfungsi
          </span>

          <label style={{ display: "block", fontWeight: "600", fontSize: "0.95rem", marginBottom: "6px", color: "#64748b" }}>
            Batas Maksimal Upload File Bukti
          </label>
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <input 
              type="number" 
              value={maxSize} 
              disabled={true} // Dikunci agar tidak membingungkan Admin
              style={{ 
                width: "80px", 
                padding: "8px", 
                borderRadius: "6px", 
                border: "1px solid #cbd5e1", 
                backgroundColor: "#e2e8f0", 
                color: "#64748b",
                cursor: "not-allowed" 
              }} 
            />
            <span style={{ fontSize: "0.9rem", color: "#64748b", fontWeight: "500" }}>Megabytes (MB)</span>
          </div>
          <p style={{ margin: "6px 0 0 0", fontSize: "0.8rem", color: "#94a3b8" }}>
            Fitur ini memerlukan konfigurasi RPC Storage Bucket di Supabase agar dapat membatasi ukuran berkas secara riil.
          </p>
        </div>

        {/* Opsi 2: Saklar Maintenance Switch (TETAP AKTIF BERFUNGSI) */}
        <div style={{ backgroundColor: "#f8fafc", padding: "16px", borderRadius: "8px", border: "1px solid #e2e8f0" }}>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <div>
              <label style={{ display: "block", fontWeight: "600", fontSize: "0.95rem" }}>Mode Perawatan Sistem (Maintenance)</label>
              <p style={{ margin: "4px 0 0 0", fontSize: "0.8rem", color: "#64748b" }}>Mengunci fungsi pengiriman aduan baru jika sistem pusat Unram sedang diperbaiki.</p>
            </div>
            <input 
              type="checkbox" 
              checked={isMaintenance}
              onChange={(e) => setIsMaintenance(e.target.checked)}
              style={{ width: "22px", height: "22px", cursor: "pointer" }}
            />
          </div>
        </div>

        <button 
          onClick={handleSaveSettings} 
          className="action-btn" 
          style={{ backgroundColor: "#2563eb", color: "#fff", alignSelf: "flex-start", padding: "10px 24px" }}
          disabled={loading}
        >
          {loading ? "Menyimpan..." : "Simpan Perubahan Sistem"}
        </button>
      </div>
    </div>
  );
}