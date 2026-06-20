import { useEffect, useState } from "react";
import { supabase } from "../../lib/supabase";

export default function SettingsPanel() {
  const [maxSize, setMaxSize] = useState("5");
  const [isMaintenance, setIsMaintenance] = useState(false);
  const [loading, setLoading] = useState(false);

  async function fetchSettings() {
    const { data } = await supabase.from("system_settings").select("*");
    if (data) {
      const sizeRow = data.find((r) => r.key === "max_upload_size_mb");
      const maintRow = data.find((r) => r.key === "maintenance_mode");

      if (sizeRow) setMaxSize(sizeRow.value);
      if (maintRow) setIsMaintenance(maintRow.value === "true");
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchSettings();
  }, []);

  const handleSaveSettings = async () => {
    try {
      setLoading(true);

      await supabase
        .from("system_settings")
        .update({ value: String(isMaintenance) })
        .eq("key", "maintenance_mode");

      alert("Konfigurasi sistem berhasil disimpan.");
    } catch (err) {
      console.error(err);
      alert("Gagal merubah konfigurasi.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="content-panel dashboard-section">
      <div className="panel-header">
        <div>
          <h3>Konfigurasi Sistem Utama</h3>
          <p className="description-text">Atur parameter runtime aplikasi dan mode perawatan sistem.</p>
        </div>
      </div>

      <div className="panel-stack" style={{ maxWidth: 560 }}>
        <div className="ui-card settings-card" style={{ opacity: 0.72 }}>
          <div className="panel-header" style={{ marginBottom: 12 }}>
            <div>
              <label className="ui-label">Batas Maksimal Upload File Bukti</label>
              <div className="field-note">Fitur ini belum aktif di backend, jadi tampil sebagai informasi saja.</div>
            </div>
            <span className="ui-badge ui-badge--muted" style={{ textAlign: "center", lineHeight: 1.05, minWidth: 78 }}>
              Belum
              <br />
              Aktif
            </span>
          </div>
          <div className="toolbar">
            <input type="number" value={maxSize} disabled className="ui-input" style={{ width: 120 }} />
            <span className="field-note" style={{ alignSelf: "center" }}>Megabytes (MB)</span>
          </div>
        </div>

        <div className="ui-card settings-card">
          <div className="settings-toggle-row">
            <div>
              <label className="ui-label">Mode Perawatan Sistem</label>
              <div className="field-note">Mengunci fungsi pengiriman aduan baru saat sistem dipelihara.</div>
            </div>
            <input
              type="checkbox"
              checked={isMaintenance}
              onChange={(e) => setIsMaintenance(e.target.checked)}
              style={{ width: 22, height: 22 }}
            />
          </div>
        </div>

        <button onClick={handleSaveSettings} className="ui-btn ui-btn--solid" disabled={loading} style={{ width: "fit-content" }}>
          {loading ? "Menyimpan..." : "Simpan Perubahan Sistem"}
        </button>
      </div>
    </div>
  );
}
