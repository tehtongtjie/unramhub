import { useEffect, useState } from "react";
import { supabase } from "../../lib/supabase";

export default function PetugasPanel() {
  const [petugasList, setPetugasList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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
                      <button className="ui-btn ui-btn--ghost" onClick={() => alert(`Pengaturan akun untuk ${p.name}`)}>
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
    </div>
  );
}
