import { useEffect, useState } from "react";
import { supabase } from "../../lib/supabase";

export default function PetugasPanel() {
  const [petugasList, setPetugasList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedPetugas, setSelectedPetugas] = useState(null);
  const [emailDraft, setEmailDraft] = useState("");
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState("");
  const [saveSuccess, setSaveSuccess] = useState("");

  async function fetchPetugas() {
    try {
      setLoading(true);
      setError(null);

      const { data, error: fetchError } = await supabase
        .from("officer_categories")
        .select(`
          id,
          user_id,
          category_id,
          users!inner (
            id,
            nim_nip,
            name,
            email,
            is_active,
            role
          ),
          categories (
            name
          )
        `)
        .eq("users.role", "officer")
        .order("id", { ascending: true });

      if (fetchError) throw fetchError;
      setPetugasList(data || []);
    } catch (err) {
      console.error("Gagal mengambil data petugas:", err);
      setError("Gagal memuat data petugas dari server.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchPetugas();
  }, []);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        handleCloseModal();
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  const handleOpenModal = (item) => {
    const user = item?.users;
    if (!user) return;

    setSelectedPetugas(item);
    setEmailDraft(user.email || "");
    setSaveError("");
    setSaveSuccess("");
  };

  const handleCloseModal = () => {
    setSelectedPetugas(null);
    setEmailDraft("");
    setSaving(false);
    setSaveError("");
    setSaveSuccess("");
  };

  const handleSaveEmail = async (e) => {
    e.preventDefault();
    if (!selectedPetugas?.users?.id) return;

    const nextEmail = emailDraft.trim();
    if (!nextEmail) {
      setSaveError("Email tidak boleh kosong.");
      return;
    }

    setSaving(true);
    setSaveError("");
    setSaveSuccess("");

    try {
      const { error: updateError } = await supabase
        .from("users")
        .update({ email: nextEmail })
        .eq("id", selectedPetugas.users.id);

      if (updateError) throw updateError;

      setPetugasList((prev) =>
        prev.map((item) =>
          item.users?.id === selectedPetugas.users.id
            ? {
                ...item,
                users: {
                  ...item.users,
                  email: nextEmail
                }
              }
            : item
        )
      );

      setSaveSuccess("Email petugas berhasil diperbarui.");
      setSelectedPetugas((current) =>
        current
          ? {
              ...current,
              users: {
                ...current.users,
                email: nextEmail
              }
            }
          : current
      );
    } catch (err) {
      console.error("Gagal menyimpan email petugas:", err);
      setSaveError("Gagal menyimpan perubahan email.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="content-panel dashboard-section">
      <div className="panel-header">
        <div>
          <h3>Daftar Personil Tim Keamanan</h3>
          <p className="description-text">Data petugas dan spesialisasi tugas yang terhubung ke sistem.</p>
        </div>
        <button className="ui-btn ui-btn--ghost" onClick={fetchPetugas} disabled={loading}>
          {loading ? "Memuat..." : "Refresh"}
        </button>
      </div>

      {loading ? (
        <div className="loading-skeleton">Mengambil data petugas dari database...</div>
      ) : error ? (
        <div className="status-message">{error}</div>
      ) : petugasList.length === 0 ? (
        <div className="status-message">Belum ada data petugas terdaftar.</div>
      ) : (
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>NIP / ID</th>
                <th>Nama</th>
                <th>Email</th>
                <th>Kategori</th>
                <th>Status Akun</th>
                <th style={{ textAlign: "center" }}>Aksi</th>
              </tr>
            </thead>
            <tbody>
              {petugasList.map((item) => {
                const p = item.users;
                const kategoriNama = item.categories?.name || "Belum Diatur";

                if (!p) return null;

                return (
                  <tr key={item.id}>
                    <td className="font-medium">{p.nim_nip}</td>
                    <td>{p.name}</td>
                    <td className="category-text">{p.email}</td>
                    <td className="category-text">{kategoriNama}</td>
                    <td>
                      <span className="badge">{p.is_active ? "Aktif" : "Nonaktif"}</span>
                    </td>
                    <td style={{ textAlign: "center" }}>
                      <button className="ui-btn ui-btn--ghost" onClick={() => handleOpenModal(item)}>
                        Atur Akun
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {selectedPetugas && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content" style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
              <div className="panel-header" style={{ marginBottom: 0 }}>
                <div>
                  <h4 style={{ margin: 0 }}>Atur Akun Petugas</h4>
                  <p className="description-text" style={{ marginTop: "4px" }}>
                    Ubah hanya email untuk akun petugas terpilih.
                  </p>
                </div>
              </div>

              <div className="panel-grid-2 align-start" style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px" }}>
                <div className="ui-card" style={{ padding: "14px 16px" }}>
                  <div className="ui-label">Nama Petugas</div>
                  <strong>{selectedPetugas.users?.name || "-"}</strong>
                </div>
                <div className="ui-card" style={{ padding: "14px 16px" }}>
                  <div className="ui-label">NIP / ID</div>
                  <strong>{selectedPetugas.users?.nim_nip || "-"}</strong>
                </div>
                <div className="ui-card" style={{ padding: "14px 16px" }}>
                  <div className="ui-label">Kategori</div>
                  <strong>{selectedPetugas.categories?.name || "Belum Diatur"}</strong>
                </div>
                <div className="ui-card" style={{ padding: "14px 16px" }}>
                  <div className="ui-label">Status Akun</div>
                  <strong>{selectedPetugas.users?.is_active ? "Aktif" : "Nonaktif"}</strong>
                </div>
              </div>

              <form onSubmit={handleSaveEmail} style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
                <div>
                  <label className="ui-label" htmlFor="petugas-email">
                    Email Petugas
                  </label>
                  <input
                    id="petugas-email"
                    type="email"
                    className="ui-input"
                    value={emailDraft}
                    onChange={(e) => setEmailDraft(e.target.value)}
                    placeholder="Masukkan email baru"
                    required
                  />
                </div>

                {saveError && (
                  <div style={{ background: "#fef2f2", border: "1px solid #fca5a5", color: "#ef4444", padding: "10px 12px", fontSize: "13px", borderRadius: "12px", textAlign: "center" }}>
                    {saveError}
                  </div>
                )}
                {saveSuccess && (
                  <div style={{ padding: "12px 14px", background: "#ecfeff", color: "#155e75", borderRadius: "12px", textAlign: "center" }}>
                    {saveSuccess}
                  </div>
                )}

                <div className="toolbar" style={{ display: "flex", justifyContent: "flex-end", gap: "10px", marginTop: "6px" }}>
                  <button type="button" className="ui-btn ui-btn--ghost" onClick={handleCloseModal} disabled={saving}>
                    Batal
                  </button>
                  <button type="submit" className="ui-btn ui-btn--solid" disabled={saving}>
                    {saving ? "Menyimpan..." : "Simpan Perubahan"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
