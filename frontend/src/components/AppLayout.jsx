import {
    BarChart3,
    BookOpen,
    Boxes,
    Home,
    Library,
    LogOut,
    MessageSquareWarning,
    ReceiptText,
    RefreshCcw,
    RotateCcw,
    Settings,
    UserRound,
    UsersRound
} from "lucide-react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { normalizeRole } from "../utils/roleUtils";

const adminMenu = [
    { to: "/dashboard", label: "Tổng quan", icon: Home },
    { to: "/admin/librarians", label: "Quản lý thủ thư", icon: UsersRound },
    { to: "/admin/rules", label: "Quy định hệ thống", icon: Settings },
    { to: "/admin/reports", label: "Báo cáo", icon: BarChart3 },
    { to: "/admin/comments", label: "Kiểm duyệt bình luận", icon: MessageSquareWarning }
];

const staffMenu = [
    { to: "/dashboard", label: "Tổng quan", icon: Home },
    { to: "/staff/loans", label: "Mượn sách", icon: RefreshCcw },
    { to: "/staff/returns", label: "Trả sách", icon: RotateCcw },
    { to: "/staff/payments", label: "Thu tiền phạt", icon: ReceiptText },
    { to: "/readers", label: "Độc giả", icon: UsersRound },
    { to: "/books", label: "Đầu sách", icon: BookOpen },
    { to: "/book-copies", label: "Cuốn sách", icon: Boxes }
];

export default function AppLayout() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const role = normalizeRole(user);
    const visibleMenu = role === "ADMIN"
        ? adminMenu
        : role === "STAFF"
            ? staffMenu
            : [];

    function handleLogout() {
        logout();
        navigate("/login", { replace: true });
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
                        <div className="brand-subtitle">
                            {role === "ADMIN" ? "Quản trị viên" : role === "STAFF" ? "Thủ thư" : "Library Manager"}
                        </div>
                    </div>
                </div>

                <nav className="nav-menu">
                    {visibleMenu.map((item) => {
                        const Icon = item.icon;

                        return (
                            <NavLink key={item.to} to={item.to} end={item.to === "/dashboard"}>
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
                        <b>{user?.tenDangNhap || user?.username || "user"}</b>
                        <span>{user?.tenVaiTro || user?.maVaiTro || role}</span>
                    </div>
                </div>

                <button className="ghost-button" onClick={handleLogout}>
                    <LogOut size={18} />
                    Đăng xuất
                </button>
            </aside>

            <main className="workspace">
                <header className="topbar">
                    <div className="user-chip">
                        {user?.maNhanVien || user?.maDocGia || user?.maTaiKhoan}
                    </div>
                </header>

                <section className="page-container">
                    <Outlet />
                </section>
            </main>
        </div>
    );
}
