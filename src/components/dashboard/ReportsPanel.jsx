import React, { useEffect, useState } from "react";
import { supabase } from "../../lib/supabase";

export default function ReportsPanel() {
  const [allReports, setAllReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedReport, setSelectedReport] = useState(null);
  const [taskLogs, setTaskLogs] = useState([]);
  const [logsLoading, setLogsLoading] = useState(false);

  const exportFileBaseName = `laporan-unramhub-${new Date().toISOString().slice(0, 10)}`;

  const escapeHtml = (value) =>
    String(value ?? "").replace(/[&<>"']/g, (char) => {
      switch (char) {
        case "&":
          return "&amp;";
        case "<":
          return "&lt;";
        case ">":
          return "&gt;";
        case '"':
          return "&quot;";
        case "'":
          return "&#39;";
        default:
          return char;
      }
    });

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

  const buildExportRows = () =>
    allReports
      .map(
        (report, index) => `
          <tr>
            <td>${index + 1}</td>
            <td>${escapeHtml(formatDate(report.created_at))}</td>
            <td>${escapeHtml(report.title)}</td>
            <td>${escapeHtml(report.pelapor?.name || "Civitas Anonim")}</td>
            <td>${escapeHtml(report.categories?.name || "Lainnya")}</td>
            <td>${escapeHtml(report.petugas?.name || "Belum Ditunjuk")}</td>
            <td>${escapeHtml(getStatusLabel(report.status))}</td>
          </tr>
        `
      )
      .join("");

  const handleExportExcel = () => {
    const excelMarkup = `
      <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel">
        <head>
          <meta charset="utf-8" />
          <style>
            body { font-family: Arial, sans-serif; }
            h1 { font-size: 18px; margin-bottom: 6px; }
            p { margin-top: 0; color: #555; }
            table { border-collapse: collapse; width: 100%; }
            th, td { border: 1px solid #d1d5db; padding: 8px 10px; text-align: left; vertical-align: top; }
            th { background: #f3f4f6; }
          </style>
        </head>
        <body>
          <h1>Data Laporan UnramHUB</h1>
          <p>Diekspor pada ${escapeHtml(new Date().toLocaleString("id-ID"))}</p>
          <table>
            <thead>
              <tr>
                <th>No</th>
                <th>Tanggal Masuk</th>
                <th>Judul Aduan</th>
                <th>Pelapor</th>
                <th>Kategori</th>
                <th>Penanggung Jawab</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              ${buildExportRows()}
            </tbody>
          </table>
        </body>
      </html>
    `;

    const blob = new Blob([excelMarkup], { type: "application/vnd.ms-excel;charset=utf-8;" });
    const downloadUrl = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = downloadUrl;
    link.download = `${exportFileBaseName}.xls`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    setTimeout(() => URL.revokeObjectURL(downloadUrl), 1000);
  };

  const handlePrintPdf = () => {
    const printWindow = window.open("", "_blank", "width=1200,height=800");

    if (!printWindow) {
      window.alert("Browser memblokir jendela cetak. Izinkan pop-up untuk mengekspor PDF.");
      return;
    }

    printWindow.document.open();
    printWindow.document.write(`
      <!doctype html>
      <html>
        <head>
          <meta charset="utf-8" />
          <title>Data Laporan UnramHUB</title>
          <style>
            @page { size: landscape; margin: 14mm; }
            body {
              font-family: Arial, sans-serif;
              color: #111827;
              margin: 0;
              padding: 0;
            }
            .page {
              padding: 0;
            }
            h1 {
              margin: 0 0 6px;
              font-size: 18px;
            }
            .meta {
              margin: 0 0 18px;
              color: #4b5563;
              font-size: 12px;
            }
            table {
              width: 100%;
              border-collapse: collapse;
              font-size: 11px;
            }
            th, td {
              border: 1px solid #d1d5db;
              padding: 8px 10px;
              text-align: left;
              vertical-align: top;
            }
            th {
              background: #f3f4f6;
            }
          </style>
        </head>
        <body>
          <div class="page">
            <h1>Data Laporan UnramHUB</h1>
            <p class="meta">Diekspor pada ${escapeHtml(new Date().toLocaleString("id-ID"))}</p>
            <table>
              <thead>
                <tr>
                  <th>No</th>
                  <th>Tanggal Masuk</th>
                  <th>Judul Aduan</th>
                  <th>Pelapor</th>
                  <th>Kategori</th>
                  <th>Penanggung Jawab</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                ${buildExportRows()}
              </tbody>
            </table>
          </div>
        </body>
      </html>
    `);
    printWindow.document.close();
    printWindow.focus();
    setTimeout(() => {
      printWindow.print();
      printWindow.close();
    }, 250);
  };

  const handleExport = (type) => {
    if (!allReports.length) return;

    if (type === "Excel") {
      handleExportExcel();
      return;
    }

    if (type === "PDF") {
      handlePrintPdf();
    }
  };

  const handleCloseDetail = () => {
    setSelectedReport(null);
    setTaskLogs([]);
  };

  // =======================================================
  // FUNGSI UTAMA: RESOLVER URL GAMBAR DINAMIS & ANTI CACHE
  // =======================================================
  const getPublicImageUrl = (filePath) => {
    if (!filePath) return "https://placehold.co/150x150?text=Tanpa+Foto";

    const pathStr = String(filePath).trim();
    const cacheBuster = `?t=${new Date(selectedReport?.created_at || Date.now()).getTime()}`;

    if (pathStr.startsWith("http://") || pathStr.startsWith("https://")) {
      const cleanUrl = pathStr.split("?")[0];
      return `${cleanUrl}${cacheBuster}`;
    }

    const storageMarker = "storage/v1/object/public/";
    if (pathStr.includes(storageMarker)) {
      const storagePath = pathStr.split(storageMarker)[1] || "";
      const [bucket, ...rest] = storagePath.split("/").filter(Boolean);
      if (bucket && rest.length > 0) {
        const publicUrl = supabase.storage.from(bucket).getPublicUrl(rest.join("/")).data.publicUrl;
        return `${publicUrl}${cacheBuster}`;
      }
    }

    const targetBucket = selectedReport?.report_media?.some((media) => media.file_path === filePath)
      ? "report-media"
      : "task-evidence";
    const basePublicUrl = supabase.storage.from(targetBucket).getPublicUrl(pathStr).data.publicUrl;

    return `${basePublicUrl}${cacheBuster}`;
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
              overflowY: "auto",
              display: "flex",
              flexDirection: "column",
              boxShadow: "0 20px 25px -5px rgba(0,0,0,0.1)",
              margin: "0 auto"
            }}
          >
            <div className="modal-content" style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
              <div className="panel-header" style={{ flexShrink: 0, marginBottom: 0 }}>
                <div>
                  <h4 style={{ margin: 0 }}>Detail Berkas Aduan</h4>
                  <p className="description-text" style={{ marginTop: "4px" }}>
                    Status: <strong>{getStatusLabel(selectedReport.status)}</strong>
                  </p>
                </div>
              </div>

              <div className="modal-body" style={{ flex: 1, paddingRight: "8px" }}>
                <div
                  className="panel-grid-2 detail-grid"
                  style={{ marginBottom: "20px", display: "grid", gridTemplateColumns: "1fr 1fr", gap: "12px" }}
                >
                  <div className="ui-card" style={{ padding: 12, border: "1px solid #e2e8f0", borderRadius: "6px" }}>
                    <div className="ui-label" style={{ fontSize: "12px", color: "#64748b" }}>
                      Judul Laporan
                    </div>
                    <strong>{selectedReport.title}</strong>
                  </div>
                  <div className="ui-card" style={{ padding: 12, border: "1px solid #e2e8f0", borderRadius: "6px" }}>
                    <div className="ui-label" style={{ fontSize: "12px", color: "#64748b" }}>
                      Kategori
                    </div>
                    <strong>{selectedReport.categories?.name || "Lainnya"}</strong>
                  </div>
                  <div className="ui-card" style={{ padding: 12, border: "1px solid #e2e8f0", borderRadius: "6px" }}>
                    <div className="ui-label" style={{ fontSize: "12px", color: "#64748b" }}>
                      Waktu Masuk
                    </div>
                    <strong>{formatDate(selectedReport.created_at)}</strong>
                  </div>
                  <div className="ui-card" style={{ padding: 12, border: "1px solid #e2e8f0", borderRadius: "6px" }}>
                    <div className="ui-label" style={{ fontSize: "12px", color: "#64748b" }}>
                      Pelapor
                    </div>
                    <strong>
                      {selectedReport.pelapor?.name || "Anonim"} ({selectedReport.pelapor?.nim_nip || "-"})
                    </strong>
                  </div>
                </div>

                <div className="ui-card" style={{ padding: 16, marginBottom: "20px", border: "1px solid #e2e8f0", borderRadius: "6px" }}>
                  <div className="ui-label" style={{ fontSize: "12px", color: "#64748b" }}>
                    Deskripsi / Isi Laporan
                  </div>
                  <p className="description-text" style={{ marginTop: "6px", color: "#1e293b", lineHeight: "1.5" }}>
                    {selectedReport.description || "Tidak ada deskripsi rincian."}
                  </p>
                </div>

                <div className="ui-card" style={{ padding: 16, marginBottom: "20px", border: "1px solid #e2e8f0", borderRadius: "6px" }}>
                  <div className="ui-label" style={{ marginBottom: "10px", fontSize: "13px", fontWeight: "600" }}>
                    Foto Bukti Lampiran (Civitas)
                  </div>
                  {selectedReport.report_media && selectedReport.report_media.length > 0 ? (
                    <div style={{ display: "flex", gap: "12px", flexWrap: "wrap" }}>
                      {selectedReport.report_media.map((media) => (
                        <div key={media.id || media.file_path}>
                          {media.file_type === "image" ? (
                            <img
                              src={getPublicImageUrl(media.file_path)}
                              alt="Bukti Awal Kampus"
                              style={{
                                width: "160px",
                                height: "160px",
                                objectFit: "cover",
                                borderRadius: "8px",
                                border: "1px solid #cbd5e1",
                                cursor: "pointer",
                                display: "block"
                              }}
                              onClick={() => window.open(getPublicImageUrl(media.file_path), "_blank")}
                            />
                          ) : (
                            <div
                              style={{
                                width: "140px",
                                height: "140px",
                                backgroundColor: "#f1f5f9",
                                borderRadius: "8px",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                                fontSize: "12px"
                              }}
                            >
                              Video Bukti
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="field-note" style={{ margin: 0, color: "#64748b", fontStyle: "italic" }}>
                      Pelapor tidak melampirkan foto bukti awal.
                    </p>
                  )}
                </div>

                <h5 style={{ margin: "24px 0 10px 0", borderBottom: "2px solid #f1f5f9", paddingBottom: "6px" }}>
                  Lini Masa Pengerjaan Lapangan
                </h5>

                {logsLoading ? (
                  <p className="field-note">Memuat riwayat pengerjaan tugas...</p>
                ) : taskLogs.length === 0 ? (
                  <p className="field-note" style={{ color: "#64748b", fontStyle: "italic" }}>
                    Belum ada log progres pengerjaan untuk berkas ini.
                  </p>
                ) : (
                  <div className="ui-card" style={{ padding: 16, border: "1px solid #e2e8f0", borderRadius: "8px", backgroundColor: "#f8fafc" }}>
                    <div className="panel-stack" style={{ display: "flex", flexDirection: "column", gap: "12px" }}>
                      {taskLogs.map((log) => (
                        <div
                          key={log.id}
                          className="ui-card"
                          style={{
                            padding: 14,
                            borderLeft: "4px solid #3b82f6",
                            backgroundColor: "#ffffff",
                            borderRadius: "0 8px 8px 0",
                            borderTop: "1px solid #e2e8f0",
                            borderRight: "1px solid #e2e8f0",
                            borderBottom: "1px solid #e2e8f0"
                          }}
                        >
                          <div style={{ display: "flex", justifyContent: "space-between", flexWrap: "wrap", gap: "8px", fontSize: "0.85rem", marginBottom: "10px" }}>
                            <div className="field-note" style={{ color: "#64748b" }}>
                              {formatDate(log.created_at)}
                            </div>
                            <div className="field-note" style={{ fontWeight: "600" }}>
                              Oleh: {log.pendaftar?.name || "Sistem"}
                            </div>
                          </div>

                          <div style={{ display: "grid", gap: "10px" }}>
                            <div style={{ padding: "10px 12px", border: "1px solid #dbeafe", borderRadius: "8px", backgroundColor: "#eff6ff" }}>
                              <div className="ui-label" style={{ fontSize: "11px", color: "#64748b", marginBottom: "4px" }}>
                                Status Pengerjaan
                              </div>
                              <strong style={{ display: "block", color: "#1e3a8a" }}>
                                {getStatusLabel(log.old_status)} -&gt; {getStatusLabel(log.new_status)}
                              </strong>
                            </div>

                            <div style={{ padding: "10px 12px", border: "1px solid #e2e8f0", borderRadius: "8px", backgroundColor: "#ffffff" }}>
                              <div className="ui-label" style={{ fontSize: "11px", color: "#64748b", marginBottom: "4px" }}>
                                Keterangan
                              </div>
                              {log.notes ? (
                                <p className="description-text" style={{ margin: 0, color: "#334155", fontSize: "14px" }}>
                                  {log.notes}
                                </p>
                              ) : (
                                <p className="field-note" style={{ margin: 0, fontStyle: "italic" }}>
                                  Tidak ada keterangan tambahan.
                                </p>
                              )}
                            </div>

                            {log.photo_path && (
                              <div style={{ padding: "10px 12px", border: "1px solid #e2e8f0", borderRadius: "8px", backgroundColor: "#ffffff" }}>
                                <div className="ui-label" style={{ fontSize: "11px", color: "#64748b", marginBottom: "8px" }}>
                                  Lampiran Progres Petugas
                                </div>
                                <img
                                  src={getPublicImageUrl(log.photo_path)}
                                  alt="Bukti Lapangan Petugas"
                                  style={{
                                    maxWidth: "240px",
                                    maxHeight: "180px",
                                    objectFit: "contain",
                                    borderRadius: "6px",
                                    border: "1px solid #cbd5e1",
                                    cursor: "pointer",
                                    display: "block"
                                  }}
                                  onClick={() => window.open(getPublicImageUrl(log.photo_path), "_blank")}
                                />
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>

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
