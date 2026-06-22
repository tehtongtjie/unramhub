import { useEffect, useState } from "react";
import { supabase } from "../../lib/supabase";

export default function ReportsPanel() {
  const [allReports, setAllReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedReport, setSelectedReport] = useState(null);
  const [taskLogs, setTaskLogs] = useState([]);
  const [logsLoading, setLogsLoading] = useState(false);

  async function fetchAllReports() {
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
  }

  async function fetchTaskLogs(reportId) {
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
        .order("created_at", { ascending: true });

      if (logErr) throw logErr;
      setTaskLogs(data || []);
    } catch (err) {
      console.error("Gagal memuat log pengerjaan tugas:", err);
    } finally {
      setLogsLoading(false);
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchAllReports();
  }, []);

  useEffect(() => {
    if (selectedReport) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      fetchTaskLogs(selectedReport.id);
    }
  }, [selectedReport]);

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

      {selectedReport && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="modal-content">
              <div className="panel-header">
                <div>
                  <h4>Detail Berkas Aduan</h4>
                  <p className="description-text">{getStatusLabel(selectedReport.status)}</p>
                </div>
              </div>

              <div className="panel-grid-2 detail-grid">
                <div className="ui-card" style={{ padding: 16 }}>
                  <div className="ui-label">Judul Laporan</div>
                  <strong>{selectedReport.title}</strong>
                </div>
                <div className="ui-card" style={{ padding: 16 }}>
                  <div className="ui-label">Kategori</div>
                  <strong>{selectedReport.categories?.name || "Lainnya"}</strong>
                </div>
                <div className="ui-card" style={{ padding: 16 }}>
                  <div className="ui-label">Waktu Masuk</div>
                  <strong>{formatDate(selectedReport.created_at)}</strong>
                </div>
                <div className="ui-card" style={{ padding: 16 }}>
                  <div className="ui-label">Pelapor</div>
                  <strong>{selectedReport.pelapor?.name || "Anonim"}</strong>
                </div>
              </div>

              <h5 style={{ margin: "0 0 10px" }}>Riwayat Pengerjaan</h5>

              {logsLoading ? (
                <p className="field-note">Memuat riwayat pengerjaan tugas...</p>
              ) : taskLogs.length === 0 ? (
                <p className="field-note">Belum ada log pengerjaan untuk berkas ini.</p>
              ) : (
                <div className="panel-stack timeline-list">
                  {taskLogs.map((log) => (
                    <div key={log.id} className="ui-card" style={{ padding: 16 }}>
                      <div className="field-note">{formatDate(log.created_at)}</div>
                    <strong style={{ display: "block", margin: "6px 0" }}>
                      {getStatusLabel(log.old_status)} → {getStatusLabel(log.new_status)}
                    </strong>
                      {log.notes && <p className="description-text" style={{ marginTop: 0 }}>{log.notes}</p>}
                      <div className="field-note">Oleh: {log.pendaftar?.name || "Sistem"}</div>
                    </div>
                  ))}
                </div>
              )}

              <div className="toolbar" style={{ justifyContent: "flex-end", marginTop: 20 }}>
                <button type="button" className="ui-btn ui-btn--ghost" onClick={handleCloseDetail}>
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
