import { Library, LogOut, UserRound } from "lucide-react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/**
 * AppShell — khung dùng chung Admin / Staff / Reader.
 * Không đổi route/logic, chỉ chuẩn hoá Sidebar sáng + Topbar.
 */
export default function AppShell({
  role = "staff",
  brandTitle = "LibraDesk",
  brandSubtitle = "Library Manager",
  menuGroups = [],
  roleBadge = null,
  topbarTitle = "",
  topbarSubtitle = "",
  topbarActions = null,
  profilePath = "/settings#profile",
  profileTargetId = "settings-profile",
  userName = "Tài khoản",
  userSub = "Vai trò",
}) {
  const { logout } = useAuth();
  const navigate = useNavigate();

  function goProfile() {
    navigate(profilePath);
    window.setTimeout(() => {
      document.getElementById(profileTargetId)?.scrollIntoView({ behavior: "smooth", block: "start" });
    }, 50);
  }

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <div className={`app-shell role-${role}`}>
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">
            <Library size={24} />
          </div>
          <div>
            <div className="brand-title">{brandTitle}</div>
            <div className="brand-subtitle">{brandSubtitle}</div>
          </div>
          {roleBadge}
        </div>

        <nav className="nav-menu">
          {menuGroups.map((group) => (
            <div className="nav-group" key={group.label}>
              <span>{group.label}</span>
              {group.items.map((item) => {
                const Icon = item.icon;
                return (
                  <NavLink key={item.to} to={item.to} end={item.to === "/" || item.to === "/reader"}>
                    <Icon size={18} />
                    <span>{item.label}</span>
                  </NavLink>
                );
              })}
            </div>
          ))}
        </nav>

        <button type="button" className="sidebar-user sidebar-user-button" onClick={goProfile}>
          <div className="avatar">
            <UserRound size={20} />
          </div>
          <div className="user-meta">
            <b>{userName}</b>
            <span>{userSub}</span>
          </div>
        </button>
      </aside>

      <main className="workspace">
        <header className="app-topbar">
          <div className="app-topbar-title">
            {topbarTitle && <strong>{topbarTitle}</strong>}
            {topbarSubtitle && <span>{topbarSubtitle}</span>}
          </div>
          {topbarActions}
          <button type="button" className="topbar-logout" onClick={handleLogout} title="Đăng xuất">
            <LogOut size={16} />
            <span>Đăng xuất</span>
          </button>
        </header>

        <section className="page-container">
          <Outlet />
        </section>
      </main>
    </div>
  );
}
