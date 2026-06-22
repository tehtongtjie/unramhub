import React, { useEffect, useState } from "react";
import { supabase } from "../../lib/supabase";

export default function ReportsPanel() {
  const [allReports, setAllReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedReport, setSelectedReport] = useState(null);
  const [taskLogs, setTaskLogs] = useState([]);
  const [logsLoading, setLogsLoading] = useState(false);

  // 1. Fetch seluruh laporan dengan query relasi yang rapi
  async function fetchAllReports(isMounted) {
    try {
      setLoading(true);
      setError(null);

      const { data, error: fetchErr } = await supabase
        .from("reports")
        .select(`
          id,
          title,
          description,
          status,
          created_at,
          category_id,
          categories ( name ),
          pelapor:users!reports_user_id_fkey ( name, nim_nip, role ),
          petugas:users!reports_assigned_to_fkey ( name, nim_nip ),
          report_media ( id, file_path, file_type )
        `)
        .order("created_at", { ascending: false });

      if (fetchErr) throw fetchErr;
      
      if (isMounted) {
        setAllReports(data || []);
      }
    } catch (err) {
      console.error("Gagal memuat arsip laporan:", err);
      if (isMounted) {
        setError("Gagal mengambil data arsip aduan dari database.");
      }
    } finally {
      if (isMounted) {
        setLoading(false);
      }
    }
  }

  // 2. Fetch log pengerjaan berdasarkan ID laporan secara spesifik
  async function fetchTaskLogs(reportId, isMounted) {
    try {
      setLogsLoading(true);
      const { data, error: logErr } = await supabase
        .from("task_logs")
        .select(`
          id,
          old_status,
          new_status,
          notes,
          photo_path,
          created_at,
          pendaftar:users!task_logs_changed_by_fkey ( name )
        `)
        .eq("report_id", reportId)
        .order("created_at", { ascending: true });

      if (logErr) throw logErr;
      
      if (isMounted) {
        setTaskLogs(data || []);
      }
    } catch (err) {
      console.error("Gagal memuat log pengerjaan tugas:", err);
    } finally {
      if (isMounted) {
        setLogsLoading(false);
      }
    }
  }

  useEffect(() => {
    let isMounted = true;
    fetchAllReports(isMounted);
    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let isMounted = true;
    if (selectedReport) {
      fetchTaskLogs(selectedReport.id, isMounted);
    }
    return () => {
      isMounted = false;
    };
  }, [selectedReport]);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        handleCloseDetail();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, []);

  const formatDate = (dateString) => {
    if (!dateString) return "-";
    return (
      new Date(dateString).toLocaleDateString("id-ID", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
      }) + " WITA"
    );
  };

  const handleExport = (type) => {
    alert(`Fitur ekspor ke ${type} untuk ${allReports.length} item sedang diproses.`);
  };

  const handleCloseDetail = () => {
    setSelectedReport(null);
    setTaskLogs([]);
  };

  const getStatusLabel = (status) => {
    switch (status) {
      case "assigned":
        return "Diserahkan";
      case "processing":
        return "Diproses";
      case "completed":
        return "Selesai";
      default:
        return "Menunggu";
    }
  };

  // =======================================================
  // FUNGSI UTAMA: RESOLVER URL GAMBAR DINAMIS & ANTI CACHE
  // =======================================================
  const getPublicImageUrl = (filePath) => {
    if (!filePath) return "https://placehold.co/150x150?text=Tanpa+Foto";

    const pathStr = String(filePath).trim();
    
    // Membuat cache buster unik berdasarkan waktu dibuatnya aduan agar tidak terkena cache browser
    const cacheBuster = `?t=${new Date(selectedReport?.created_at || Date.now()).getTime()}`;

    // 1. KONDISI JIKA DI DATABASE SUDAH BERUPA URL LENGKAP (HTTP/HTTPS)
    if (pathStr.startsWith("http://") || pathStr.startsWith("https://")) {
      const cleanUrl = pathStr.split("?"); // Bersihkan timestamp lama jika ada
      return cleanUrl + cacheBuster;
    }

    // 2. KONDISI JIKA BERUPA PATH RELATIF ATAU URL BAWAAN SUPABASE YANG AKAN DIBONGKAR
    let targetBucket = "task-evidence"; // Bucket default bawaan petugas
    let cleanPath = pathStr;

    // Jika string berisi pola URL storage publik Supabase, bongkar secara dinamis
    if (pathStr.includes("storage/v1/object/public/")) {
      const segments = pathStr.split("storage/v1/object/public/");
      if (segments.length > 1) {
        // FIX: Menggunakan segments yang bertipe String untuk di-split, bukan objek array segments
        const pathParts = segments.split("/");
        targetBucket = pathParts; // Bagian pertama otomatis menjadi nama BUCKET aslinya (e.g. task-evidence)
        cleanPath = pathParts.slice(1).join("/"); // Sisanya menjadi nama FILE asli
      }
    } else {
      // Jika hanya teks nama file mentah tanpa domain URL, cek apakah ini milik tabel report_media civitas
      if (selectedReport?.report_media?.some(media => media.file_path === filePath)) {
        // SESUAIKAN: Ubah "report-media" di bawah ini ke nama bucket file civitas Anda jika berbeda
        targetBucket = "report-media"; 
      }
    }

    // Buat ulang URL publik resmi yang bersih dari database berdasarkan bucket dinamis
    const basePublicUrl = supabase.storage.from(targetBucket).getPublicUrl(cleanPath).data.publicUrl;
    
    return basePublicUrl + cacheBuster;
  };

  return (
    <div className="content-panel dashboard-section">
      <div className="panel-header">
        <div>
          <h3>Database Log Seluruh Aduan</h3>
          <p className="description-text">
            Total arsip dokumen di sistem UnramHUB: <strong>{allReports.length} item</strong>
          </p>
        </div>

        <div className="toolbar">
          <button className="ui-btn ui-btn--ghost" onClick={() => handleExport("Excel")} disabled={allReports.length === 0}>
            Export Excel
          </button>
          <button className="ui-btn ui-btn--solid" onClick={() => handleExport("PDF")} disabled={allReports.length === 0}>
            Cetak PDF
          </button>
        </div>
      </div>

      {loading ? (
        <div className="loading-skeleton">Memuat seluruh berkas arsip UnramHUB...</div>
      ) : error ? (
        <div className="status-message">{error}</div>
      ) : allReports.length === 0 ? (
        <div className="status-message">Belum ada riwayat aduan yang masuk ke database.</div>
      ) : (
        <div className="table-responsive">
          <table className="table">
            <thead>
              <tr>
                <th>Tanggal Masuk</th>
                <th>Judul Aduan</th>
                <th>Pelapor</th>
                <th>Kategori</th>
                <th>Penanggung Jawab</th>
                <th style={{ textAlign: "center" }}>Status</th>
                <th style={{ textAlign: "center" }}>Aksi</th>
              </tr>
            </thead>
            <tbody>
              {allReports.map((report) => (
                <tr key={report.id}>
                  <td>{formatDate(report.created_at)}</td>
                  <td className="font-medium">{report.title}</td>
                  <td>{report.pelapor?.name || "Civitas Anonim"}</td>
                  <td className="category-text">{report.categories?.name || "Lainnya"}</td>
                  <td className="category-text">{report.petugas?.name || "Belum Ditunjuk"}</td>
                  <td style={{ textAlign: "center" }}>
                    <span className="badge">{getStatusLabel(report.status)}</span>
                  </td>
                  <td style={{ textAlign: "center" }}>
                    <button className="ui-btn ui-btn--ghost" onClick={() => setSelectedReport(report)}>
                      Buka Detail
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ==================== MODAL DETAIL BERKAS ADUAN ==================== */}
      {selectedReport && (
        <div 
          className="modal-overlay" 
          onClick={handleCloseDetail}
          style={{ 
            position: "fixed", 
            top: 0, 
            left: 0, 
            width: "100%", 
            height: "100%", 
            backgroundColor: "rgba(0, 0, 0, 0.5)", 
            display: "flex", 
            justifyContent: "center", 
            alignItems: "center", 
            zIndex: 1000, 
            padding: "20px 12px" 
          }}
        >
          <div 
            className="modal-box" 
            onClick={(e) => e.stopPropagation()}
            style={{ 
              backgroundColor: "#fff", 
              padding: "24px", 
              borderRadius: "8px", 
              width: "750px", 
              maxWidth: "100%", 
              maxHeight: "90vh",
              display: "flex",
              flexDirection: "column",
              boxShadow: "0 20px 25px -5px rgba(0,0,0,0.1)",
              margin: "0 auto" 
            }}
          >
            <div className="modal-content" style={{ display: "flex", flexDirection: "column", height: "100%", maxHeight: "100%", flex: 1, overflow: "hidden", gap: "16px" }}>
              <div className="panel-header" style={{ flexShrink: 0, marginBottom: 0 }}>
                <div>
                  <h4 style={{ margin: 0 }}>Detail Berkas Aduan</h4>
                  <p className="description-text" style={{ marginTop: "4px" }}>Status: <strong>{getStatusLabel(selectedReport.status)}</strong></p>
                </div>
              </div>

              {/* Scrollable Modal Body */}
              <div className="modal-body" style={{ overflowY: "auto", flex: 1, paddingRight: "8px" }}>
                {/* Grid Info Utama */}
                <div className="panel-grid-2 detail-grid" style={{ marginBottom: "20px", display: "grid", gridTemplateColumns: "1fr 1fr", gap: "12px" }}>
                  <div className="ui-card" style={{ padding: 12, border: "1px solid #e2e8f0", borderRadius: "6px" }}>
                    <div className="ui-label" style={{ fontSize: "12px", color: "#64748b" }}>Judul Laporan</div>
                    <strong>{selectedReport.title}</strong>
                  </div>
                  <div className="ui-card" style={{ padding: 12, border: "1px solid #e2e8f0", borderRadius: "6px" }}>
                    <div className="ui-label" style={{ fontSize: "12px", color: "#64748b" }}>Kategori</div>
                    <strong>{selectedReport.categories?.name || "Lainnya"}</strong>
                  </div>
                  <div className="ui-card" style={{ padding: 12, border: "1px solid #e2e8f0", borderRadius: "6px" }}>
                    <div className="ui-label" style={{ fontSize: "12px", color: "#64748b" }}>Waktu Masuk</div>
                    <strong>{formatDate(selectedReport.created_at)}</strong>
                  </div>
                  <div className="ui-card" style={{ padding: 12, border: "1px solid #e2e8f0", borderRadius: "6px" }}>
                    <div className="ui-label" style={{ fontSize: "12px", color: "#64748b" }}>Pelapor</div>
                    <strong>{selectedReport.pelapor?.name || "Anonim"} ({selectedReport.pelapor?.nim_nip || "-"})</strong>
                  </div>
                </div>

                {/* Deskripsi Laporan */}
                <div className="ui-card" style={{ padding: 16, marginBottom: "20px", border: "1px solid #e2e8f0", borderRadius: "6px" }}>
                  <div className="ui-label" style={{ fontSize: "12px", color: "#64748b" }}>Deskripsi / Isi Laporan</div>
                  <p className="description-text" style={{ marginTop: "6px", color: "#1e293b", lineHeight: "1.5" }}>{selectedReport.description || "Tidak ada deskripsi rincian."}</p>
                </div>

                {/* Foto Bukti Civitas */}
                <div className="ui-card" style={{ padding: 16, marginBottom: "20px", border: "1px solid #e2e8f0", borderRadius: "6px" }}>
                  <div className="ui-label" style={{ marginBottom: "10px", fontSize: "13px", fontWeight: "600" }}>Foto Bukti Lampiran (Civitas)</div>
                  {selectedReport.report_media && selectedReport.report_media.length > 0 ? (
                    <div style={{ display: "flex", gap: "12px", flexWrap: "wrap" }}>
                      {selectedReport.report_media.map((media) => (
                        <div key={media.id || media.file_path}>
                          {media.file_type === "image" ? (
                            <img
                              src={getPublicImageUrl(media.file_path)}
                              alt="Bukti Awal Kampus"
                              style={{ width: "160px", height: "160px", objectFit: "cover", borderRadius: "8px", border: "1px solid #cbd5e1", cursor: "pointer", display: "block" }}
                              onClick={() => window.open(getPublicImageUrl(media.file_path), "_blank")}
                            />
                          ) : (
                            <div style={{ width: "140px", height: "140px", backgroundColor: "#f1f5f9", borderRadius: "8px", display: "flex", alignItems: "center", justifyContent: "center", fontSize: "12px" }}>Video Bukti</div>
                          )}
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="field-note" style={{ margin: 0, color: "#64748b", fontStyle: "italic" }}>Pelapor tidak melampirkan foto bukti awal.</p>
                  )}
                </div>

                {/* Timeline Riwayat Progress */}
                <h5 style={{ margin: "24px 0 10px 0", borderBottom: "2px solid #f1f5f9", paddingBottom: "6px" }}>Lini Masa Pengerjaan Lapangan</h5>

                {logsLoading ? (
                  <p className="field-note">Memuat riwayat pengerjaan tugas...</p>
                ) : taskLogs.length === 0 ? (
                  <p className="field-note" style={{ color: "#64748b", fontStyle: "italic" }}>Belum ada log progres pengerjaan untuk berkas ini.</p>
                ) : (
                  <div className="panel-stack timeline-list" style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
                    {taskLogs.map((log) => (
                      <div key={log.id} className="ui-card" style={{ padding: 14, borderLeft: "4px solid #3b82f6", backgroundColor: "#f8fafc", borderRadius: "0 6px 6px 0", borderTop: "1px solid #e2e8f0", borderRight: "1px solid #e2e8f0", borderBottom: "1px solid #e2e8f0" }}>
                        <div style={{ display: "flex", justifyContent: "space-between", flexWrap: "wrap", fontSize: "0.85rem" }}>
                          <div className="field-note" style={{ color: "#64748b" }}>{formatDate(log.created_at)}</div>
                          <div className="field-note" style={{ fontWeight: "600" }}>Oleh: {log.pendaftar?.name || "Sistem"}</div>
                        </div>
                        
                        <strong style={{ display: "block", margin: "6px 0", color: "#1e3a8a" }}>
                          {getStatusLabel(log.old_status)} → {getStatusLabel(log.new_status)}
                        </strong>
                        
                        {log.notes && <p className="description-text" style={{ margin: "4px 0 8px 0", color: "#334155", fontSize: "14px" }}>{log.notes}</p>}
                        
                        {log.photo_path && (
                          <div style={{ marginTop: "10px" }}>
                            <div className="ui-label" style={{ fontSize: "11px", color: "#64748b", marginBottom: "4px" }}>Lampiran Progres Petugas:</div>
                            <img
                              src={getPublicImageUrl(log.photo_path)}
                              alt="Bukti Lapangan Petugas"
                              style={{ maxWidth: "240px", maxHeight: "180px", objectFit: "contain", borderRadius: "6px", border: "1px solid #cbd5e1", cursor: "pointer", display: "block" }}
                              onClick={() => window.open(getPublicImageUrl(log.photo_path), "_blank")}
                            />
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Ruang toolbar bawah */}
              <div className="toolbar" style={{ display: "flex", justifyContent: "flex-end", marginTop: 0, borderTop: "1px solid #f1f5f9", paddingTop: "16px", flexShrink: 0 }}>
                <button type="button" className="ui-btn ui-btn--ghost" onClick={handleCloseDetail} style={{ padding: "8px 20px", borderRadius: "6px", cursor: "pointer" }}>
                  Tutup Dokumen
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}