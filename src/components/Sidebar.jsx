import {
  FaHome,
  FaFileAlt,
  FaSignOutAlt,
  FaUserShield,
  FaBullhorn,
  FaQuestionCircle,
  FaCog
} from "react-icons/fa";
import "./Sidebar.css";

const MENU_ITEMS = [
  { id: "dashboard", label: "Dashboard", icon: <FaHome /> },
  { id: "reports", label: "Semua Laporan", icon: <FaFileAlt /> },
  { id: "petugas", label: "Petugas", icon: <FaUserShield /> },
  { id: "announcements", label: "Pengumuman", icon: <FaBullhorn /> },
  { id: "faq", label: "Pusat FAQ", icon: <FaQuestionCircle /> },
  { id: "settings", label: "Pengaturan", icon: <FaCog /> }
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
        <div className="sidebar-logo">UH</div>
        <div className="sidebar-brand-text">
          <h2>UnramHUB</h2>
          <p>Admin Panel</p>
        </div>
      </div>

      <nav className="sidebar-menu" aria-label="Navigasi utama">
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
        <button className="logout-btn ui-btn ui-btn--ghost" onClick={handleLogout}>
          <FaSignOutAlt />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
}
