import {
    BarChart3,
    BookOpen,
    Boxes,
    CreditCard,
    Home,
    Library,
    MessageSquare,
    RefreshCcw,
    RotateCcw,
    Settings,
    ShieldCheck,
    UserRound,
    UsersRound
} from "lucide-react";
import { Navigate, NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { isReaderUser } from "../utils/authRole";
import { isAdmin } from "../utils/roleUtils";

const staffMenu = [
    { to: "/", label: "Tổng quan", icon: Home },
    { to: "/books", label: "Đầu sách", icon: BookOpen },
    { to: "/book-copies", label: "Cuốn sách", icon: Boxes },
    { to: "/readers", label: "Độc giả", icon: UsersRound },
    { to: "/staff/loans", label: "Mượn sách", icon: RefreshCcw },
    { to: "/staff/returns", label: "Trả sách", icon: RotateCcw },
    { to: "/staff/payments", label: "Thu tiền", icon: CreditCard },
    { to: "/admin/comments", label: "Kiểm duyệt bình luận", icon: MessageSquare },
    { to: "/reports", label: "Báo cáo", icon: BarChart3 },
    { to: "/settings", label: "Cài đặt", icon: Settings }
];

const adminExtraMenu = [
    { to: "/admin/reports", label: "Báo cáo hệ thống", icon: BarChart3 },
    { to: "/admin/rules", label: "Quy định hệ thống", icon: Settings },
    { to: "/admin/comments", label: "Kiểm duyệt bình luận", icon: MessageSquare },
    { to: "/admin/librarians", label: "Tài khoản thủ thư", icon: ShieldCheck }
];

export default function AppLayout() {
    const { user } = useAuth();
    const displayName = getUserDisplayName(user);
    const menu = isAdmin(user)
        ? [
            ...staffMenu.filter((item) => item.to !== "/reports" && item.to !== "/admin/comments"),
            ...adminExtraMenu
        ]
        : staffMenu;

    if (isReaderUser(user)) {
        return <Navigate to="/reader" replace />;
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
                        <b>{displayName}</b>
                        <span>{user?.tenVaiTro || "ROLE"}</span>
                    </div>
                </div>
            </aside>

            <main className="workspace">
                <header className="topbar">
                    <div className="topbar-actions">
                        <div className="user-chip">
                            {displayName}
                        </div>
                    </div>
                </header>

                <section className="page-container">
                    <Outlet />
                </section>
            </main>
        </div>
    );
}

function getUserDisplayName(user) {
    return user?.hoTen ||
        user?.tenNhanVien ||
        user?.tenDocGia ||
        user?.tenDangNhap ||
        user?.maNhanVien ||
        user?.maDocGia ||
        user?.maTaiKhoan ||
        "user";
}
