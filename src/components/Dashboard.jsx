import { useEffect, useMemo, useState } from "react";
import { supabase } from "../lib/supabase";
import Sidebar from "./Sidebar";
import "./Dashboard.css";

import Overview from "./dashboard/Overview";
import ReportsPanel from "./dashboard/ReportsPanel";
import PetugasPanel from "./dashboard/PetugasPanel";
import AnnouncementsPanel from "./dashboard/AnnouncementsPanel";
import FaqPanel from "./dashboard/FaqPanel";
import SettingsPanel from "./dashboard/SettingsPanel";

const STATUS_STYLES = {
  completed: { label: "Selesai" },
  processing: { label: "Diproses" },
  assigned: { label: "Diserahkan" },
  pending: { label: "Menunggu" },
  default: { label: "Pending" }
};

const CATEGORIES = [
  { id: "all", label: "Semua" },
  { id: "1", label: "Kekerasan / Pelecehan" },
  { id: "2", label: "Kerusakan Fasilitas" },
  { id: "3", label: "Bencana / Darurat" },
  { id: "4", label: "Barang Hilang / Temuan" },
  { id: "5", label: "Lainnya" }
];

export default function Dashboard({ onLogout }) {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activePage, setActivePage] = useState("dashboard");
  const [selectedCategory, setSelectedCategory] = useState("all");

  async function fetchReports() {
    try {
      setLoading(true);
      setError(null);

      const { data, error: fetchError } = await supabase
        .from("reports")
        .select(`
          id,
          title,
          status,
          created_at,
          category_id,
          categories (
            name
          )
        `)
        .order("created_at", { ascending: false });

      if (fetchError) throw fetchError;
      setReports(data || []);
    } catch (err) {
      console.error("Supabase Fetch Error:", err);
      setError("Gagal memuat data laporan dari database.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    fetchReports();
  }, []);

  const filteredReports = useMemo(() => {
    if (selectedCategory === "all") return reports;
    return reports.filter((report) => String(report.category_id) === selectedCategory);
  }, [reports, selectedCategory]);

  const stats = useMemo(() => {
    return {
      total: reports.length,
      pending: reports.filter((r) => r.status === "pending").length,
      processing: reports.filter((r) => r.status === "processing" || r.status === "assigned").length,
      completed: reports.filter((r) => r.status === "completed").length
    };
  }, [reports]);

  return (
    <div className="dashboard-layout">
      <Sidebar activePage={activePage} setActivePage={setActivePage} onLogout={onLogout} />

      <main className="main-content">
        <header className="main-header">
          <div className="header-title">
            <span className="eyebrow">UnramHUB Admin</span>
            <h2>
              {activePage === "dashboard" && "Ringkasan Sistem"}
              {activePage === "reports" && "Arsip Semua Laporan"}
              {activePage === "petugas" && "Manajemen Petugas"}
              {activePage === "announcements" && "Broadcast Pengumuman"}
              {activePage === "faq" && "Kelola Pusat Bantuan FAQ"}
              {activePage === "settings" && "Konfigurasi Sistem Utama"}
            </h2>
            <p>Sistem informasi pengaduan keamanan dan fasilitas kampus UnramHUB.</p>
          </div>

          {activePage === "dashboard" && (
            <button className="refresh-btn ui-btn ui-btn--ghost" onClick={fetchReports} disabled={loading}>
              {loading ? "Memuat..." : "Refresh Data"}
            </button>
          )}
        </header>

        {activePage === "dashboard" && (
          <Overview
            stats={stats}
            categories={CATEGORIES}
            selectedCategory={selectedCategory}
            setSelectedCategory={setSelectedCategory}
            loading={loading}
            error={error}
            filteredReports={filteredReports}
            statusStyles={STATUS_STYLES}
          />
        )}

        {activePage === "reports" && <ReportsPanel />}
        {activePage === "petugas" && <PetugasPanel />}
        {activePage === "announcements" && <AnnouncementsPanel />}
        {activePage === "faq" && <FaqPanel />}
        {activePage === "settings" && <SettingsPanel />}
      </main>
    </div>
  );
}
