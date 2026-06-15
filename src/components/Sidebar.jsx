import React from "react";
import { 
  FaHome, 
  FaFileAlt, 
  FaSignOutAlt, 
  FaUserShield,
  FaBullhorn,       // Ikon Pengumuman
  FaQuestionCircle, // Ikon FAQ
  FaCog             // Ikon Pengaturan
} from "react-icons/fa";
import "./Sidebar.css";

// Array menu yang sudah diperluas sesuai kebutuhan rilis UnramHUB
const MENU_ITEMS = [
  { id: "dashboard", label: "Dashboard", icon: <FaHome /> },
  { id: "reports", label: "Semua Laporan", icon: <FaFileAlt /> },
  { id: "petugas", label: "Petugas", icon: <FaUserShield /> },
  { id: "announcements", label: "Pengumuman", icon: <FaBullhorn /> },
  { id: "faq", label: "Pusat FAQ", icon: <FaQuestionCircle /> },
  { id: "settings", label: "Pengaturan", icon: <FaCog /> },
];

export default function Sidebar({ activePage, setActivePage, onLogout }) {
  const handleLogout = () => {
    if (window.confirm("Apakah Anda yakin ingin keluar dari UnramHUB?")) {
      onLogout();
    }
  };

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="sidebar-logo">U</div>
        <div className="sidebar-brand-text">
          <h2>UnramHUB</h2>
          <p>Admin Panel</p>
        </div>
      </div>

      <nav className="sidebar-menu">
        {MENU_ITEMS.map((item) => {
          const isActive = activePage === item.id;
          return (
            <button
              key={item.id}
              className={`menu-item ${isActive ? "active" : ""}`}
              onClick={() => setActivePage(item.id)}
            >
              <span className="menu-icon">{item.icon}</span>
              <span className="menu-label">{item.label}</span>
            </button>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <button className="logout-btn" onClick={handleLogout}>
          <FaSignOutAlt />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
}