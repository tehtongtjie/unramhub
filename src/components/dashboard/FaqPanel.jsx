import { useEffect, useState } from "react";
import { supabase } from "../../lib/supabase";

export default function FaqPanel() {
  const [faqs, setFaqs] = useState([]);
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");
  const [loading, setLoading] = useState(false);

  async function fetchFaqs() {
    const { data } = await supabase.from("faqs").select("*").order("created_at", { ascending: true });
    setFaqs(data || []);
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchFaqs();
  }, []);

  const handleAddFaq = async (e) => {
    e.preventDefault();
    if (!question || !answer) return alert("Isi pertanyaan dan jawaban!");

    try {
      setLoading(true);
      const { error } = await supabase.from("faqs").insert([{ question, answer }]);
      if (error) throw error;

      alert("FAQ baru berhasil ditambahkan.");
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
    <div className="content-panel dashboard-section">
      <div className="panel-header">
        <div>
          <h3>Kelola Konten Bantuan FAQ</h3>
          <p className="description-text">Perbarui daftar panduan solusi mandiri agar laporan duplikat bisa ditekan.</p>
        </div>
      </div>

      <div className="panel-grid-2 align-start">
        <form onSubmit={handleAddFaq} className="panel-stack">
          <h4 style={{ margin: 0 }}>Tambah Item FAQ</h4>

          <div className="field-group">
            <label className="ui-label">Pertanyaan Umum</label>
            <input
              type="text"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              className="ui-input"
              placeholder="Misal: Bagaimana cara melacak status aduan?"
            />
          </div>

          <div className="field-group">
            <label className="ui-label">Jawaban / Solusi</label>
            <textarea
              rows="4"
              value={answer}
              onChange={(e) => setAnswer(e.target.value)}
              className="ui-textarea"
              placeholder="Tulis penjelasan penyelesaian lengkap..."
            />
          </div>

          <button type="submit" className="ui-btn ui-btn--solid" disabled={loading} style={{ width: "fit-content" }}>
            {loading ? "Menyimpan..." : "Simpan FAQ"}
          </button>
        </form>

        <div className="panel-stack">
          <h4 style={{ margin: 0 }}>Daftar Pertanyaan Terpublish</h4>
          <div className="panel-stack" style={{ maxHeight: 420, overflowY: "auto", paddingRight: 4 }}>
            {faqs.map((item) => (
              <div key={item.id} className="ui-card" style={{ padding: 20, paddingRight: 86, position: "relative" }}>
                <h5 style={{ margin: "0 0 10px" }}>Q: {item.question}</h5>
                <p className="description-text" style={{ margin: 0 }}>A: {item.answer}</p>
                <button
                  onClick={() => handleDelete(item.id)}
                  className="ui-btn ui-btn--ghost"
                  style={{ position: "absolute", top: 16, right: 16, padding: "0.55rem 0.8rem" }}
                >
                  Hapus
                </button>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
