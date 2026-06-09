import {
    BarChart3,
    BookOpen,
    Boxes,
    CreditCard,
    Home,
    Library,
    LogOut,
    RefreshCcw,
    RotateCcw,
    Search,
    UserRound,
    UsersRound
} from "lucide-react";
import { Navigate, NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { isReaderUser } from "../utils/authRole";

const menu = [
    { to: "/", label: "Tổng quan", icon: Home },
    { to: "/books", label: "Đầu sách", icon: BookOpen },
    { to: "/book-copies", label: "Cuốn sách", icon: Boxes },
    { to: "/readers", label: "Độc giả", icon: UsersRound },
    { to: "/loans", label: "Mượn sách", icon: RefreshCcw },
    { to: "/returns", label: "Trả sách", icon: RotateCcw },
    { to: "/payments", label: "Thu tiền", icon: CreditCard },
    { to: "/reports", label: "Báo cáo", icon: BarChart3 }
];

export default function AppLayout() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    if (isReaderUser(user)) {
        return <Navigate to="/reader" replace />;
    }

    function handleLogout() {
        logout();
        navigate("/login");
    }

    return (
        <div className="app-shell">
            <aside className="sidebar">
                <div className="brand">
                    <div className="brand-icon">
                        <Library size={24} />
                    </div>
                    <div>
                        <div className="brand-title">LibraDesk</div>
                        <div className="brand-subtitle">Library Manager</div>
                    </div>
                </div>

                <nav className="nav-menu">
                    {menu.map((item) => {
                        const Icon = item.icon;

                        return (
                            <NavLink key={item.to} to={item.to} end={item.to === "/"}>
                                <Icon size={18} />
                                <span>{item.label}</span>
                            </NavLink>
                        );
                    })}
                </nav>

                <div className="sidebar-user">
                    <div className="avatar">
                        <UserRound size={20} />
                    </div>
                    <div className="user-meta">
                        <b>{user?.tenDangNhap || "user"}</b>
                        <span>{user?.tenVaiTro || "ROLE"}</span>
                    </div>
                </div>
            </aside>

            <main className="workspace">
                <header className="topbar">
                    <div className="search-box">
                        <Search size={18} />
                        <input placeholder="Tìm sách, độc giả, phiếu mượn..." />
                    </div>

                    <div className="topbar-actions">
                        <div className="user-chip">
                            {user?.maNhanVien || user?.maDocGia || user?.maTaiKhoan}
                        </div>
                        <button className="ghost-button" onClick={handleLogout}>
                            <LogOut size={18} />
                            Đăng xuất
                        </button>
                    </div>
                </header>

                <section className="page-container">
                    <Outlet />
                </section>
            </main>
        </div>
    );
}
