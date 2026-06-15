import React, { useState, useEffect } from "react";
import { supabase } from "../../lib/supabase";

export default function FaqPanel() {
  const [faqs, setFaqs] = useState([]);
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchFaqs();
  }, []);

  const fetchFaqs = async () => {
    const { data } = await supabase.from("faqs").select("*").order("created_at", { ascending: true });
    setFaqs(data || []);
  };

  const handleAddFaq = async (e) => {
    e.preventDefault();
    if (!question || !answer) return alert("Isi pertanyaan dan jawaban!");

    try {
      setLoading(true);
      const { error } = await supabase.from("faqs").insert([{ question, answer }]);
      if (error) throw error;

      alert("FAQ Baru Berhasil Ditambahkan!");
      setQuestion("");
      setAnswer("");
      fetchFaqs();
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm("Hapus FAQ ini?")) {
      await supabase.from("faqs").delete().eq("id", id);
      fetchFaqs();
    }
  };

  return (
    <div className="content-panel">
      <div className="panel-header">
        <h3>Kelola Konten Bantuan FAQ</h3>
        <p className="description-text">Perbarui daftar panduan solusi mandiri aduan agar menekan angka penumpukan duplikasi laporan masuk.</p>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "2rem", marginTop: "1.5rem" }}>
        {/* Kiri: Form Input */}
        <form onSubmit={handleAddFaq} style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
          <h4>+ Tambah Item FAQ</h4>
          <div>
            <label style={{ display: "block", fontSize: "0.85rem", fontWeight: "600", marginBottom: "4px" }}>Pertanyaan Umum</label>
            <input type="text" value={question} onChange={(e) => setQuestion(e.target.value)} style={{ width: "100%", padding: "10px", borderRadius: "6px", border: "1px solid #cbd5e1" }} placeholder="Misal: Bagaimana cara melacak status aduan?" />
          </div>
          <div>
            <label style={{ display: "block", fontSize: "0.85rem", fontWeight: "600", marginBottom: "4px" }}>Jawaban / Solusi</label>
            <textarea rows="4" value={answer} onChange={(e) => setAnswer(e.target.value)} style={{ width: "100%", padding: "10px", borderRadius: "6px", border: "1px solid #cbd5e1", fontFamily: "inherit" }} placeholder="Tulis penjelasan penyelesaian lengkap..."></textarea>
          </div>
          <button type="submit" className="action-btn" style={{ backgroundColor: "#10b981", color: "#fff", alignSelf: "flex-start" }} disabled={loading}>
            {loading ? "Menyimpan..." : "Simpan FAQ"}
          </button>
        </form>

        {/* Kanan: List FAQ dari DB */}
        <div>
          <h4>Daftar Pertanyaan Terpublish</h4>
          <div style={{ marginTop: "1rem", display: "flex", flexDirection: "column", gap: "12px", maxHeight: "400px", overflowY: "auto" }}>
            {faqs.map(item => (
              <div key={item.id} style={{ padding: "14px", borderRadius: "8px", border: "1px solid #e2e8f0", backgroundColor: "#f8fafc", position: "relative" }}>
                <h5 style={{ margin: "0 0 6px 0", color: "#1e293b", paddingRight: "40px" }}>Q: {item.question}</h5>
                <p style={{ margin: 0, fontSize: "0.85rem", color: "#64748b" }}>A: {item.answer}</p>
                <button onClick={() => handleDelete(item.id)} style={{ position: "absolute", top: "10px", right: "10px", background: "none", border: "none", color: "#ef4444", cursor: "pointer", fontSize: "0.85rem", fontWeight: "600" }}>Hapus</button>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}