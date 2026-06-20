import { useEffect, useState } from "react";
import { supabase } from "../../lib/supabase";

export default function AnnouncementsPanel() {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [target, setTarget] = useState("Semua");
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(false);

  async function fetchAnnouncements() {
    const { data } = await supabase
      .from("announcements")
      .select("*")
      .order("created_at", { ascending: false });
    setList(data || []);
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchAnnouncements();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!title || !content) return alert("Semua kolom wajib diisi!");

    try {
      setLoading(true);
      const { error } = await supabase
        .from("announcements")
        .insert([{ title, content, target_audience: target }]);

      if (error) throw error;

      alert("Pengumuman berhasil dikirim.");
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
    <div className="content-panel dashboard-section">
      <div className="panel-header">
        <div>
          <h3>Broadcast Pengumuman Baru</h3>
          <p className="description-text">Kirim informasi penting ke civitas dalam satu alur yang rapi.</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="panel-stack" style={{ maxWidth: 640, marginBottom: 28 }}>
        <div className="field-group">
          <label className="ui-label">Judul Pengumuman</label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="ui-input"
            placeholder="Contoh: Pemeliharaan Sistem Jaringan Kampus"
          />
        </div>

        <div className="field-group">
          <label className="ui-label">Target Audiens</label>
          <div className="select-shell">
            <select value={target} onChange={(e) => setTarget(e.target.value)} className="ui-select">
              <option value="Semua">Semua Civitas Akademika</option>
              <option value="Fakultas Teknik">Fakultas Teknik (FT)</option>
              <option value="Fakultas Kedokteran">Fakultas Kedokteran (FK)</option>
              <option value="Fakultas Hukum">Fakultas Hukum (FH)</option>
            </select>
          </div>
        </div>

        <div className="field-group">
          <label className="ui-label">Isi Pengumuman</label>
          <textarea
            rows="4"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            className="ui-textarea"
            placeholder="Tulis rincian informasi di sini..."
          />
        </div>

        <button type="submit" className="ui-btn ui-btn--solid" disabled={loading} style={{ width: "fit-content" }}>
          {loading ? "Mengirim..." : "Kirim Broadcast"}
        </button>
      </form>

      <h4 style={{ margin: "0 0 12px" }}>Riwayat Pengumuman Aktif</h4>
      <div className="table-responsive">
        <table className="table">
          <thead>
            <tr>
              <th>Judul</th>
              <th>Target</th>
              <th>Tanggal Kirim</th>
            </tr>
          </thead>
          <tbody>
            {list.map((item) => (
              <tr key={item.id}>
                <td className="font-medium">{item.title}</td>
                <td>
                  <span className="badge">{item.target_audience}</span>
                </td>
                <td className="category-text">{new Date(item.created_at).toLocaleDateString("id-ID")}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
