import { useEffect, useState } from "react";
import { FaCheckCircle, FaClock, FaFileAlt, FaHourglassHalf } from "react-icons/fa";
import { supabase } from "../../lib/supabase";

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
  const [selectedReport, setSelectedReport] = useState(null);
  const [availableOfficers, setAvailableOfficers] = useState([]);
  const [chosenOfficerId, setChosenOfficerId] = useState("");
  const [assignLoading, setAssignLoading] = useState(false);

  async function fetchOfficersByCategory(categoryId) {
    try {
      let officers = [];

      if (String(categoryId) === "5") {
        const { data, error: fetchErr } = await supabase
          .from("users")
          .select("id, name, nim_nip, role")
          .eq("role", "officer");

        if (fetchErr) throw fetchErr;
        officers = data ? data.filter((user) => String(user.id) !== "10") : [];
      } else {
        const { data, error: fetchErr } = await supabase
          .from("officer_categories")
          .select(`
            user_id,
            users!inner ( id, name, nim_nip )
          `)
          .eq("category_id", categoryId);

        if (fetchErr) throw fetchErr;
        officers = data ? data.filter((item) => item.users !== null).map((item) => item.users) : [];
      }

      setAvailableOfficers(officers);
      setChosenOfficerId(officers.length > 0 ? String(officers[0].id) : "");
    } catch (err) {
      console.error("Error saat memuat petugas:", err);
      setAvailableOfficers([]);
      setChosenOfficerId("");
    }
  }

  useEffect(() => {
    if (selectedReport) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      fetchOfficersByCategory(selectedReport.category_id);
    }
  }, [selectedReport]);

  const handleAssignSubmit = async (e) => {
    e.preventDefault();
    if (!chosenOfficerId || !selectedReport) {
      alert("Silakan pilih petugas terlebih dahulu.");
      return;
    }

    try {
      setAssignLoading(true);

      const { error: reportErr } = await supabase
        .from("reports")
        .update({
          status: "assigned",
          assigned_to: chosenOfficerId
        })
        .eq("id", selectedReport.id);

      if (reportErr) throw reportErr;

      const { error: logErr } = await supabase
        .from("task_logs")
        .insert([
          {
            report_id: selectedReport.id,
            changed_by: 1,
            old_status: "pending",
            new_status: "assigned",
            notes: "Laporan telah diverifikasi dan diteruskan ke petugas terkait."
          }
        ]);

      if (logErr) throw logErr;

      alert("Laporan aduan berhasil ditugaskan!");
      setSelectedReport(null);
      window.location.reload();
    } catch (err) {
      console.error("Gagal melakukan delegasi tugas:", err);
      alert("Terjadi kesalahan saat memproses penugasan.");
    } finally {
      setAssignLoading(false);
    }
  };

  const statCards = [
    { key: "total", label: "Total Aduan", value: stats.total, icon: FaFileAlt },
    { key: "pending", label: "Menunggu", value: stats.pending, icon: FaHourglassHalf },
    { key: "processing", label: "Diproses", value: stats.processing, icon: FaClock },
    { key: "completed", label: "Selesai", value: stats.completed, icon: FaCheckCircle }
  ];

  return (
    <>
      <div className="stats-grid">
        {statCards.map((item) => {
          const Icon = item.icon;

          return (
            <div key={item.key} className="stat-card">
              <div className="stat-card-top">
                <div>
                  <h3>{item.value}</h3>
                  <p className="stat-card-label">{item.label}</p>
                </div>
                <div className="stat-card-icon">
                  <Icon />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      <div className="content-panel dashboard-section">
        <div className="filter-tabs">
          {categories.map((cat) => (
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
          <div className="status-message">{error}</div>
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
                      <td className="category-text">{report.categories?.name || "Lainnya"}</td>
                      <td>
                        <span className="badge">{statusInfo.label}</span>
                      </td>
                      <td style={{ textAlign: "center" }}>
                        {report.status === "pending" ? (
                          <button className="ui-btn ui-btn--solid" onClick={() => setSelectedReport(report)}>
                            Tugaskan
                          </button>
                        ) : (
                          <button className="ui-btn ui-btn--ghost" onClick={() => alert(`Detail ID Laporan: ${report.id}`)}>
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

      {selectedReport && (
        <div className="modal-overlay">
          <div className="modal-box">
            <div className="panel-header">
              <div>
                <h4>Form Delegasi Petugas UnramHUB</h4>
                <p className="description-text">Pilih petugas yang akan menangani laporan ini.</p>
              </div>
              <button type="button" className="ui-btn ui-btn--ghost" onClick={() => setSelectedReport(null)}>
                Tutup
              </button>
            </div>

            <div className="panel-stack">
              <div className="field-group">
                <div className="ui-badge ui-badge--muted" style={{ width: "fit-content" }}>
                  {selectedReport.categories?.name || "Lainnya"}
                </div>
                <strong>{selectedReport.title}</strong>
              </div>

              <form onSubmit={handleAssignSubmit} className="panel-stack">
                <div className="field-group">
                  <label className="ui-label">Pilih Personil</label>
                  {availableOfficers.length === 0 ? (
                    <p className="field-note">Tidak ada petugas lapangan yang tersedia untuk kategori ini.</p>
                  ) : (
                    <select
                      className="ui-select"
                      value={chosenOfficerId}
                      onChange={(e) => setChosenOfficerId(e.target.value)}
                      required
                    >
                      <option value="" disabled>
                        -- Pilih Personil Lapangan --
                      </option>
                      {availableOfficers.map((officer) => (
                        <option key={officer.id} value={String(officer.id)}>
                          {officer.name} ({officer.nim_nip})
                        </option>
                      ))}
                    </select>
                  )}
                </div>

                <div className="toolbar" style={{ justifyContent: "flex-end" }}>
                  <button
                    type="button"
                    className="ui-btn ui-btn--ghost"
                    onClick={() => setSelectedReport(null)}
                  >
                    Batal
                  </button>
                  <button
                    type="submit"
                    className="ui-btn ui-btn--solid"
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
        </div>
      )}
    </>
  );
}
