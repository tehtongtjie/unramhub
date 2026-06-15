import React, { useState, useEffect } from "react";
import { supabase } from "../../lib/supabase"; // Sesuaikan path config Supabase Anda

export default function ReportsPanel() {
  const [allReports, setAllReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // State baru untuk manajemen Modal Detail dan Riwayat Pengerjaan
  const [selectedReport, setSelectedReport] = useState(null);
  const [taskLogs, setTaskLogs] = useState([]);
  const [logsLoading, setLogsLoading] = useState(false);

  useEffect(() => {
    fetchAllReports();
  }, []);

  // Ambil riwayat log pengerjaan setiap kali admin membuka modal detail laporan
  useEffect(() => {
    if (selectedReport) {
      fetchTaskLogs(selectedReport.id);
    } else {
      setTaskLogs([]); // Bersihkan log saat modal ditutup
    }
  }, [selectedReport]);

  const fetchAllReports = async () => {
    try {
      setLoading(true);
      setError(null);

      const { data, error: fetchErr } = await supabase
        .from("reports")
        .select(`
          id,
          title,
          status,
          created_at,
          categories ( name ),
          pelapor:users!reports_user_id_fkey ( name, nim_nip, role ),
          petugas:users!reports_assigned_to_fkey ( name, nim_nip )
        `)
        .order("created_at", { ascending: false });

      if (fetchErr) throw fetchErr;
      setAllReports(data || []);
    } catch (err) {
      console.error("Gagal memuat arsip laporan:", err);
      setError("Gagal mengambil data arsip aduan dari database.");
    } finally {
      setLoading(false);
    }
  };

  // Ambil data riwayat perubahan status dari tabel task_logs
  const fetchTaskLogs = async (reportId) => {
    try {
      setLogsLoading(true);
      const { data, error: logErr } = await supabase
        .from("task_logs")
        .select(`
          id,
          old_status,
          new_status,
          notes,
          created_at,
          pendaftar:users!task_logs_changed_by_fkey ( name )
        `)
        .eq("report_id", reportId)
        .order("created_at", { ascending: true }); // Mengurutkan dari riwayat terlama ke terbaru

      if (logErr) throw logErr;
      setTaskLogs(data || []);
    } catch (err) {
      console.error("Gagal memuat log pengerjaan tugas:", err);
    } finally {
      setLogsLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "-";
    return new Date(dateString).toLocaleDateString("id-ID", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    }) + " WITA";
  };

  const handleExport = (type) => {
    alert(`Fitur Ekspor dokumen ke format ${type} untuk ${allReports.length} item laporan sedang diproses!`);
  };

  // Helper pemetaan badge status agar rapi dan seragam
  const getStatusDetails = (status) => {
    switch (status) {
      case "assigned":
        return { label: "Diserahkan", bg: "#e0f2fe", text: "#0369a1" };
      case "processing":
        return { label: "Diproses", bg: "#dbeafe", text: "#1e40af" };
      case "completed":
        return { label: "Selesai", bg: "#dcfce7", text: "#166534" };
      default:
        return { label: "Menunggu", bg: "#fef3c7", text: "#92400e" };
    }
  };

  return (
    <div className="content-panel">
      {/* Header Halaman Arsip */}
      <div className="panel-header" style={{ display: "flex", justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: "1rem" }}>
        <div>
          <h3>Database Log Seluruh Aduan</h3>
          <p className="description-text" style={{ margin: "4px 0 0 0" }}>
            Total arsip dokumen di sistem UnramHUB: <strong>{allReports.length} Item</strong>
          </p>
        </div>
        
        <div style={{ display: "flex", gap: "8px" }}>
          <button className="action-btn-secondary" onClick={() => handleExport("Excel")} disabled={allReports.length === 0}>
            Export Excel
          </button>
          <button className="action-btn" style={{ backgroundColor: "#ef4444", color: "#fff" }} onClick={() => handleExport("PDF")} disabled={allReports.length === 0}>
            Cetak PDF
          </button>
        </div>
      </div>

      <hr style={{ margin: "1.5rem 0", borderColor: "#f1f5f9" }} />

      {/* Kondisi Tampilan Data */}
      {loading ? (
        <div className="loading-skeleton">Memuat seluruh berkas arsip UnramHUB...</div>
      ) : error ? (
        <div className="status-message error">{error}</div>
      ) : allReports.length === 0 ? (
        <div className="status-message">Belum ada riwayat aduan yang masuk ke database.</div>
      ) : (
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>Tanggal Masuk</th>
                <th>Judul Aduan</th>
                <th>Pelapor (Civitas)</th>
                <th>Kategori</th>
                <th>Penanggung Jawab</th>
                <th style={{ textAlign: "center" }}>Status</th>
                <th style={{ textAlign: "center" }}>Aksi</th>
              </tr>
            </thead>
            <tbody>
              {allReports.map((report) => {
                const statusInfo = getStatusDetails(report.status);

                return (
                  <tr key={report.id}>
                    <td>{formatDate(report.created_at).split(",")}</td>
                    <td className="font-medium">{report.title}</td>
                    <td>{report.pelapor?.name || "Civitas Anonim"}</td>
                    <td className="category-text">{report.categories?.name || "Lainnya"}</td>
                    <td style={{ color: report.petugas?.name ? "#0f172a" : "#94a3b8", fontWeight: report.petugas?.name ? "500" : "400" }}>
                      {report.petugas?.name || "Belum Ditunjuk"}
                    </td>
                    <td style={{ textAlign: "center" }}>
                      <span className="badge" style={{ backgroundColor: statusInfo.bg, color: statusInfo.text }}>
                        {statusInfo.label}
                      </span>
                    </td>
                    <td style={{ textAlign: "center" }}>
                      <button 
                        className="action-btn"
                        style={{ padding: "6px 12px", fontSize: "12px", backgroundColor: "#0f172a", color: "#fff" }}
                        onClick={() => setSelectedReport(report)}
                      >
                        Buka Detail
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* ============================================================
          MODAL DETAIL LAPORAN & PROGRESS TRACKING (LOG TASK)
          ============================================================ */}
      {selectedReport && (
        <div className="modal-overlay" style={modalOverlayStyle}>
          <div className="modal-box" style={modalBoxStyle}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "12px" }}>
              <h4 style={{ margin: 0 }}>Detail Berkas Dokumen Aduan</h4>
              <span className="badge" style={{ backgroundColor: getStatusDetails(selectedReport.status).bg, color: getStatusDetails(selectedReport.status).text }}>
                {getStatusDetails(selectedReport.status).label}
              </span>
            </div>
            <hr style={{ margin: "12px 0", borderColor: "#cbd5e1" }} />

            {/* Sub-Panel Informasi Laporan */}
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "12px", fontSize: "13px", marginBottom: "20px", backgroundColor: "#f8fafc", padding: "12px", borderRadius: "6px" }}>
              <div style={{ gridColumn: "1 / -1" }}>
                <strong style={{ color: "#64748b" }}>Judul Laporan:</strong>
                <p style={{ margin: "4px 0 0 0", fontSize: "14px", fontWeight: "600", color: "#0f172a" }}>{selectedReport.title}</p>
              </div>
              <div>
                <strong style={{ color: "#64748b" }}>Kategori:</strong>
                <p style={{ margin: "4px 0 0 0" }}>{selectedReport.categories?.name || "Lainnya"}</p>
              </div>
              <div>
                <strong style={{ color: "#64748b" }}>Waktu Masuk:</strong>
                <p style={{ margin: "4px 0 0 0" }}>{formatDate(selectedReport.created_at)}</p>
              </div>
              <div>
                <strong style={{ color: "#64748b" }}>Nama Pelapor:</strong>
                <p style={{ margin: "4px 0 0 0" }}>{selectedReport.pelapor?.name || "Anonim"} ({selectedReport.pelapor?.nim_nip || "-"})</p>
              </div>
              <div>
                <strong style={{ color: "#64748b" }}>Petugas Lapangan:</strong>
                <p style={{ margin: "4px 0 0 0", fontWeight: "500", color: selectedReport.petugas?.name ? "#1e40af" : "#64748b" }}>
                  {selectedReport.petugas?.name || "Belum Ada"}
                </p>
              </div>
            </div>

            {/* Sub-Panel Histori Perubahan Log Tugas */}
            <h5 style={{ margin: "0 0 10px 0", fontSize: "14px" }}>Riwayat Pengerjaan & Tindak Lanjut</h5>
            
            {logsLoading ? (
              <p style={{ fontSize: "13px", color: "#64748b" }}>Memuat riwayat pengerjaan tugas...</p>
            ) : taskLogs.length === 0 ? (
              <p style={{ fontSize: "13px", color: "#94a3b8", italic: "true", backgroundColor: "#f1f5f9", padding: "10px", borderRadius: "6px" }}>
                Belum ada log pengerjaan untuk berkas ini. Status saat ini masih pending.
              </p>
            ) : (
              <div style={{ maxHeight: "200px", overflowY: "auto", borderLeft: "2px solid #e2e8f0", paddingLeft: "14px", marginLeft: "6px" }}>
                {taskLogs.map((log) => (
                  <div key={log.id} style={{ marginBottom: "14px", position: "relative" }}>
                    {/* Penunjuk Titik Timeline */}
                    <div style={{ position: "absolute", left: "-20px", top: "4px", width: "10px", height: "10px", borderRadius: "50%", backgroundColor: "#2563eb" }}></div>
                    
                    <span style={{ fontSize: "11px", fontWeight: "600", color: "#64748b" }}>
                      {formatDate(log.created_at)}
                    </span>
                    <p style={{ margin: "2px 0", fontSize: "13px", fontWeight: "500", color: "#0f172a" }}>
                      Status berubah dari <span style={{ textDecoration: "line-through", color: "#ef4444" }}>{getStatusDetails(log.old_status).label}</span> ke <span style={{ color: "#166534" }}>{getStatusDetails(log.new_status).label}</span>
                    </p>
                    {log.notes && (
                      <p style={{ margin: "2px 0 0 0", fontSize: "12px", color: "#475569", backgroundColor: "#f1f5f9", padding: "6px", borderRadius: "4px" }}>
                        💬 <strong>Catatan:</strong> {log.notes}
                      </p>
                    )}
                    <span style={{ fontSize: "11px", color: "#94a3b8", display: "block", marginTop: "2px" }}>
                      Oleh: {log.pendaftar?.name || "Sistem"}
                    </span>
                  </div>
                ))}
              </div>
            )}

            {/* Tombol Aksi Keluar */}
            <div style={{ display: "flex", justifyContent: "flex-end", marginTop: "20px" }}>
              <button 
                type="button" 
                style={{ padding: "8px 20px", borderRadius: "6px", border: "1px solid #cbd5e1", backgroundColor: "#fff", fontWeight: "500", cursor: "pointer" }}
                onClick={() => setSelectedReport(null)}
              >
                Tutup Dokumen
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// Layout CSS bantuan untuk Tampilan Modal Detail 
const modalOverlayStyle = {
  position: "fixed", top: 0, left: 0, width: "100%", height: "100%",
  backgroundColor: "rgba(15, 23, 42, 0.5)", display: "flex", justifyContent: "center", alignItems: "center", zIndex: 999
};
const modalBoxStyle = {
  backgroundColor: "#fff", padding: "24px", borderRadius: "10px", width: "550px", maxWidth: "92%", boxShadow: "0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)"
};