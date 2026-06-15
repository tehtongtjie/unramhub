import React, { useState, useEffect } from "react";
import { supabase } from "../../lib/supabase"; // Sesuaikan path menuju config supabase Anda

export default function PetugasPanel() {
  const [petugasList, setPetugasList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Jalankan fungsi penarikan data saat komponen dimuat
  useEffect(() => {
    fetchPetugas();
  }, []);

  const fetchPetugas = async () => {
    try {
      setLoading(true);
      setError(null);

      // KUNCI BARU: Ambil data petugas lewat tabel relasi 'officer_categories' 
      // Supaya kita bisa dapetin nama spesialisasi tugasnya dari tabel 'categories'
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
        // Filter inner join untuk mastiin yang ketarik hanya yang rolenya 'officer'
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
  };

  return (
    <div className="content-panel">
      <div className="panel-header">
        <h3>Daftar Personil Tim Keamanan & Divisi Tugas</h3>
        <button className="refresh-btn" onClick={fetchPetugas} disabled={loading}>
          {loading ? "Memuat..." : "Refresh"}
        </button>
      </div>

      {loading ? (
        <div className="loading-skeleton">Mengambil data petugas dari database...</div>
      ) : error ? (
        <div className="status-message error">{error}</div>
      ) : petugasList.length === 0 ? (
        <div className="status-message">Belum ada data petugas terdaftar.</div>
      ) : (
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>NIP / ID Petugas</th>
                <th>Nama Petugas</th>
                <th>Email</th>
                <th>Kategori / Spesialisasi</th>
                <th>Status Akun</th>
                <th style={{ textAlign: "center" }}>Aksi</th>
              </tr>
            </thead>
            <tbody>
              {petugasList.map((item) => {
                // Karena data user bersarang di dalam objek hasil join, kita pecah variabelnya
                const p = item.users; 
                const kategoriNama = item.categories?.name || "Belum Diatur";

                // Antisipasi jaga-jaga kalau data user-nya bermasalah/kosong
                if (!p) return null;

                return (
                  <tr key={item.id}>
                    <td className="font-medium">{p.nim_nip}</td>
                    <td>{p.name}</td>
                    <td>{p.email}</td>
                    
                    {/* KOLOM BARU: Menampilkan Divisi Keahlian Petugas */}
                    <td className="category-text" style={{ fontWeight: "600", color: "#475569" }}>
                      {kategoriNama}
                    </td>

                    <td>
                      <span 
                        className="badge" 
                        style={{ 
                          backgroundColor: p.is_active ? "#dcfce7" : "#fee2e2", 
                          color: p.is_active ? "#166534" : "#991b1b" 
                        }}
                      >
                        {p.is_active ? "Aktif" : "Nonaktif"}
                      </span>
                    </td>
                    <td style={{ textAlign: "center" }}>
                      <button 
                        className="action-btn-secondary" 
                        onClick={() => alert(`Pengaturan divisi & akun untuk ${p.name}`)}
                      >
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