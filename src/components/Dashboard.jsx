import React, { useState, useEffect, useMemo } from "react";
import { supabase } from "../lib/supabase";
import Sidebar from "./Sidebar";
import "./Dashboard.css";

// Import sub-komponen halaman utama & kontrol data
// SESUDAH
import Overview from "./dashboard/Overview";
import ReportsPanel from "./dashboard/ReportsPanel";
import PetugasPanel from "./dashboard/PetugasPanel";
import AnnouncementsPanel from "./dashboard/AnnouncementsPanel"; 
import FaqPanel from "./dashboard/FaqPanel";                     
import SettingsPanel from "./dashboard/SettingsPanel";      

// Disesuaikan dengan enum report_status di skema database Anda
const STATUS_STYLES = {
  completed: { backgroundColor: "#dcfce7", color: "#166534", label: "Selesai" },
  processing: { backgroundColor: "#dbeafe", color: "#1e40af", label: "Diproses" },
  assigned: { backgroundColor: "#e0f2fe", color: "#0369a1", label: "Diserahkan" },
  pending: { backgroundColor: "#fef3c7", color: "#92400e", label: "Menunggu" },
  default: { backgroundColor: "#f3f4f6", color: "#374151", label: "Pending" }
};

//-- Disamakan persis dengan isi data SEED Master Kategori SQL database Anda
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

  useEffect(() => {
    fetchReports();
  }, []);

  const fetchReports = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Mengambil data utama dengan teknik JOIN tabel categories
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
  };

  // Filter mencocokkan string category_id hasil bentukan tabel SQL
  const filteredReports = useMemo(() => {
    if (selectedCategory === "all") return reports;
    return reports.filter(report => String(report.category_id) === selectedCategory);
  }, [reports, selectedCategory]);

  const stats = useMemo(() => {
    return {
      total: reports.length,
      pending: reports.filter(r => r.status === "pending").length,
      processing: reports.filter(r => r.status === "processing" || r.status === "assigned").length,
      completed: reports.filter(r => r.status === "completed").length,
    };
  }, [reports]);

  return (
    <div className="dashboard-layout">
      {/* Sidebar mengontrol state navigasi menu admin panel */}
      <Sidebar activePage={activePage} setActivePage={setActivePage} onLogout={onLogout} />

      <main className="main-content">
        {/* ================= HEADER DINAMIS KONTEN ================= */}
        <header className="main-header">
          <div className="header-title">
            <h2>
              {activePage === "dashboard" && "Ringkasan Sistem"}
              {activePage === "reports" && "Arsip Semua Laporan"}
              {activePage === "petugas" && "Manajemen Petugas"}
              {activePage === "announcements" && "Broadcast Pengumuman"}
              {activePage === "faq" && "Kelola Pusat Bantuan FAQ"}
              {activePage === "settings" && "Konfigurasi Sistem Utama"}
            </h2>
            <p>Sistem Informasi Pengaduan Keamanan &amp; Fasilitas Kampus UnramHUB.</p>
          </div>
          {activePage === "dashboard" && (
            <button className="refresh-btn" onClick={fetchReports} disabled={loading}>
              {loading ? "Memuat..." : "Refresh Data"}
            </button>
          )}
        </header>

        {/* ================= ROUTING VIEW HALAMAN UTAMA ================= */}
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