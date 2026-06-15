import React, { useState, useEffect } from "react";
import { supabase } from "../../lib/supabase"; // Sesuaikan path config Supabase Anda

export default function Overview({ 
  stats, 
  categories, 
  selectedCategory, 
  setSelectedCategory, 
  loading, 
  error, 
  filteredReports, 
  statusStyles 
}) {
  // State untuk manajemen Modal Penugasan
  const [selectedReport, setSelectedReport] = useState(null);
  const [availableOfficers, setAvailableOfficers] = useState([]);
  const [chosenOfficerId, setChosenOfficerId] = useState("");
  const [assignLoading, setAssignLoading] = useState(false);

  // Ambil daftar petugas yang sesuai dengan kategori laporan yang sedang diklik
  useEffect(() => {
    if (selectedReport) {
      fetchOfficersByCategory(selectedReport.category_id);
    }
  }, [selectedReport]);

  const fetchOfficersByCategory = async (categoryId) => {
    try {
      let officers = [];
      console.log("Memulai fetch untuk Category ID:", categoryId); 

      // JIKA KATEGORI "LAINNYA" (ID: 5)
      if (String(categoryId) === "5") {
        // Ambil semua user yang rolenya officer langsung dari tabel users
        const { data, error: fetchErr } = await supabase
          .from("users")
          .select("id, name, nim_nip, role")
          .eq("role", "officer");

        if (fetchErr) throw fetchErr;

        // Saring agar Satgas PPKS (ID: 10) tidak masuk ke kategori umum
        officers = data ? data.filter(user => String(user.id) !== "10") : [];

      } else {
        // JIKA KATEGORI SPESIFIK (1, 2, 3, 4)
        const { data, error: fetchErr } = await supabase
          .from("officer_categories")
          .select(`
            user_id,
            users!inner ( id, name, nim_nip )
          `)
          .eq("category_id", categoryId);

        if (fetchErr) throw fetchErr;
        
        // Pastikan data relasi users ada dan tidak null
        officers = data ? data.filter(item => item.users !== null).map(item => item.users) : [];
      }

      console.log("Petugas yang berhasil dimuat:", officers); 
      setAvailableOfficers(officers);
      
      // FIX AMAN: Set default value indeks ke-0 dikonversi ke String agar sinkron dengan HTML select
      if (officers && officers.length > 0 && officers?.id) {
        setChosenOfficerId(String(officers.id)); 
      } else {
        setChosenOfficerId(""); 
      }

    } catch (err) {
      console.error("🚨 Error fatal saat memuat petugas:", err);
      setAvailableOfficers([]);
      setChosenOfficerId("");
    }
  };

  // Fungsi saat Admin menekan tombol "Simpan Penugasan"
  const handleAssignSubmit = async (e) => {
    e.preventDefault();
    if (!chosenOfficerId || !selectedReport) {
      alert("Silakan pilih petugas terlebih dahulu.");
      return;
    }

    try {
      setAssignLoading(true);

      // 1. Update status laporan di tabel 'reports' menjadi 'assigned'
      const { error: reportErr } = await supabase
        .from("reports")
        .update({
          status: "assigned",
          assigned_to: chosenOfficerId
        })
        .eq("id", selectedReport.id);

      if (reportErr) throw reportErr;

      // 2. Tulis riwayat pengerjaan awal ke tabel 'task_logs'
      const { error: logErr } = await supabase
        .from("task_logs")
        .insert([{
          report_id: selectedReport.id,
          changed_by: 1, // Merujuk ke ID Admin utama di database (Prof. Dian)
          old_status: "pending",
          new_status: "assigned",
          notes: "Laporan telah diverifikasi oleh Admin Panel UnramHUB dan diteruskan ke petugas lapangan terkait."
        }]);

      if (logErr) throw logErr;

      alert("Laporan aduan berhasil ditugaskan!");
      setSelectedReport(null); // Tutup modal
      window.location.reload(); // Refresh halaman agar widget dan tabel sinkron otomatis
    } catch (err) {
      console.error("Gagal melakukan delegasi tugas:", err);
      alert("Terjadi kesalahan saat memproses penugasan.");
    } finally {
      setAssignLoading(false);
    }
  };

  return (
    <>
      {/* Widget Angka Ringkasan */}
      <div className="stats-grid">
        <div className="stat-card total"><h3>{stats.total}</h3><p>Total Aduan</p></div>
        <div className="stat-card pending"><h3>{stats.pending}</h3><p>Menunggu</p></div>
        <div className="stat-card processing"><h3>{stats.processing}</h3><p>Diproses</p></div>
        <div className="stat-card completed"><h3>{stats.completed}</h3><p>Selesai</p></div>
      </div>

      {/* Panel Utama Tabel */}
      <div className="content-panel">
        <div className="filter-tabs">
          {categories.map(cat => (
            <button
              key={cat.id}
              className={`tab-btn ${selectedCategory === cat.id ? "active" : ""}`}
              onClick={() => setSelectedCategory(cat.id)}
            >
              {cat.label}
            </button>
          ))}
        </div>

        {loading ? (
          <div className="loading-skeleton">Mengambil data aduan dari database...</div>
        ) : error ? (
          <div className="status-message error">{error}</div>
        ) : filteredReports.length === 0 ? (
          <div className="status-message">Tidak ada laporan di kategori ini.</div>
        ) : (
          <div className="table-responsive">
            <table className="table">
              <thead>
                <tr>
                  <th>Judul Laporan</th>
                  <th>Kategori</th>
                  <th>Status</th>
                  <th style={{ textAlign: "center" }}>Aksi</th>
                </tr>
              </thead>
              <tbody>
                {filteredReports.map((report) => {
                  const statusInfo = statusStyles[report.status] || statusStyles.default;
                  
                  return (
                    <tr key={report.id}>
                      <td className="font-medium">{report.title}</td>
                      <td className="category-text">
                        {report.categories?.name || "Lainnya"}
                      </td>
                      <td>
                        <span className="badge" style={statusInfo}>
                          {statusInfo.label}
                        </span>
                      </td>
                      <td style={{ textAlign: "center" }}>
                        {report.status === "pending" ? (
                          <button 
                            className="action-btn" 
                            style={{ backgroundColor: "#2563eb", color: "#fff" }}
                            onClick={() => setSelectedReport(report)}
                          >
                            Tugaskan
                          </button>
                        ) : (
                          <button 
                            className="action-btn" 
                            onClick={() => alert(`Detail ID Laporan: ${report.id}`)}
                          >
                            Detail
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* MODAL INTERAKTIF PENUGASAN */}
      {selectedReport && (
        <div className="modal-overlay" style={modalOverlayStyle}>
          <div className="modal-box" style={modalBoxStyle}>
            <h4>Form Delegasi Petugas UnramHUB</h4>
            <hr style={{ margin: "12px 0", borderColor: "#f1f5f9" }} />
            
            <p style={{ fontSize: "14px", margin: "4px 0" }}><strong>Judul Aduan:</strong> {selectedReport.title}</p>
            <p style={{ fontSize: "14px", color: "#64748b", marginBottom: "16px" }}>
              <strong>Kategori Terkait:</strong> {selectedReport.categories?.name || "Lainnya"}
            </p>

            <form onSubmit={handleAssignSubmit}>
              <div style={{ marginBottom: "16px" }}>
                <label style={{ display: "block", fontSize: "14px", fontWeight: "600", marginBottom: "6px" }}>
                  Pilih Personil yang Bertanggung Jawab:
                </label>
                
                {availableOfficers.length === 0 ? (
                  <p style={{ color: "#ef4444", fontSize: "13px", fontWeight: "500" }}>
                    ⚠️ Tidak ada petugas lapangan yang tersedia untuk kategori ini!
                  </p>
                ) : (
                  /* FIX SELECT VALUE BINDING & ONCHANGE */
                  <select 
                    style={selectStyle}
                    value={chosenOfficerId}
                    onChange={(e) => setChosenOfficerId(e.target.value)}
                    required
                  >
                    <option value="" disabled>-- Pilih Personil Lapangan --</option>
                    {availableOfficers.map(officer => (
                      <option key={officer.id} value={String(officer.id)}>
                        {officer.name} ({officer.nim_nip})
                      </option>
                    ))}
                  </select>
                )}
              </div>

              <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px" }}>
                <button 
                  type="button" 
                  style={{ padding: "8px 16px", borderRadius: "6px", border: "1px solid #cbd5e1", backgroundColor: "#fff", cursor: "pointer" }}
                  onClick={() => setSelectedReport(null)}
                >
                  Batal
                </button>
                
                {/* FIX VALIDASI KONDISI SUBMIT BUTTON */}
                <button 
                  type="submit" 
                  style={{ 
                    padding: "8px 16px", 
                    borderRadius: "6px", 
                    border: "none", 
                    backgroundColor: (assignLoading || availableOfficers.length === 0 || !chosenOfficerId) ? "#cbd5e1" : "#2563eb", 
                    color: "#fff", 
                    cursor: (assignLoading || availableOfficers.length === 0 || !chosenOfficerId) ? "not-allowed" : "pointer" 
                  }}
                  disabled={assignLoading || availableOfficers.length === 0 || !chosenOfficerId}
                >
                  {assignLoading 
                    ? "Memproses..." 
                    : availableOfficers.length === 0 
                      ? "Petugas Kosong" 
                      : !chosenOfficerId 
                        ? "Pilih Petugas Dulu" 
                        : "Simpan Penugasan"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}

// Inline CSS Styles bantuan untuk Modal 
const modalOverlayStyle = {
  position: "fixed", top: 0, left: 0, width: "100%", height: "100%",
  backgroundColor: "rgba(0,0,0,0.4)", display: "flex", justifyContent: "center", alignItems: "center", zIndex: 999
};
const modalBoxStyle = {
  backgroundColor: "#fff", padding: "24px", borderRadius: "8px", width: "450px", maxWidth: "90%", boxShadow: "0 10px 15px -3px rgba(0,0,0,0.1)"
};
const selectStyle = {
  width: "100%", padding: "10px", borderRadius: "6px", border: "1px solid #cbd5e1", fontSize: "14px", backgroundColor: "#f8fafc"
};