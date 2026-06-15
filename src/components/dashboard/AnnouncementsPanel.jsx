import React, { useState, useEffect } from "react";
import { supabase } from "../../lib/supabase";

export default function AnnouncementsPanel() {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [target, setTarget] = useState("Semua");
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchAnnouncements();
  }, []);

  const fetchAnnouncements = async () => {
    const { data } = await supabase
      .from("announcements")
      .select("*")
      .order("created_at", { ascending: false });
    setList(data || []);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title || !content) return alert("Semua kolom wajib diisi!");

    try {
      setLoading(true);
      const { error } = await supabase
        .from("announcements")
        .insert([{ title, content, target_audience: target }]);

      if (error) throw error;

      alert("Pengumuman berhasil dikirim dan di-broadcast!");
      setTitle("");
      setContent("");
      fetchAnnouncements();
    } catch (err) {
      console.error(err);
      alert("Gagal mengirim pengumuman.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="content-panel">
      <div className="panel-header">
        <h3>Broadcast Pengumuman Baru</h3>
        <p className="description-text">Kirim info penting seputar kampus langsung ke banner aplikasi mobile civitas secara real-time.</p>
      </div>

      {/* Form Input */}
      <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "1rem", maxWidth: "600px", marginBottom: "2.5rem" }}>
        <div>
          <label style={{ display: "block", fontSize: "0.9rem", fontWeight: "600", marginBottom: "4px" }}>Judul Pengumuman</label>
          <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} style={{ width: "100%", padding: "10px", borderRadius: "6px", border: "1px solid #cbd5e1" }} placeholder="Contoh: Pemeliharaan Sistem Jaringan Kampus" />
        </div>
        
        <div>
          <label style={{ display: "block", fontSize: "0.9rem", fontWeight: "600", marginBottom: "4px" }}>Target Fakultas / Audiens</label>
          <select value={target} onChange={(e) => setTarget(e.target.value)} style={{ width: "100%", padding: "10px", borderRadius: "6px", border: "1px solid #cbd5e1", backgroundColor: "#fff" }}>
            <option value="Semua">Semua Civitas Akdemika</option>
            <option value="Fakultas Teknik">Fakultas Teknik (FT)</option>
            <option value="Fakultas Kedokteran">Fakultas Kedokteran (FK)</option>
            <option value="Fakultas Hukum">Fakultas Hukum (FH)</option>
          </select>
        </div>

        <div>
          <label style={{ display: "block", fontSize: "0.9rem", fontWeight: "600", marginBottom: "4px" }}>Isi Pengumuman</label>
          <textarea rows="4" value={content} onChange={(e) => setContent(e.target.value)} style={{ width: "100%", padding: "10px", borderRadius: "6px", border: "1px solid #cbd5e1", fontFamily: "inherit" }} placeholder="Tulis rincian informasi di sini..."></textarea>
        </div>

        <button type="submit" className="action-btn" style={{ backgroundColor: "#2563eb", color: "#fff", alignSelf: "flex-start", padding: "10px 20px" }} disabled={loading}>
          {loading ? "Mengirim..." : "Kirim Broadcast"}
        </button>
      </form>

      {/* Daftar Riwayat */}
      <h4>Riwayat Pengumuman Aktif</h4>
      <div className="table-responsive" style={{ marginTop: "1rem" }}>
        <table className="table">
          <thead>
            <tr>
              <th>Judul</th>
              <th>Target</th>
              <th>Tanggal Kirim</th>
            </tr>
          </thead>
          <tbody>
            {list.map(item => (
              <tr key={item.id}>
                <td className="font-medium">{item.title}</td>
                <td><span className="badge" style={{ backgroundColor: "#f1f5f9", color: "#475569" }}>{item.target_audience}</span></td>
                <td>{new Date(item.created_at).toLocaleDateString("id-ID")}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}