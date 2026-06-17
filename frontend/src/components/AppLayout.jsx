import {
    ArrowLeftRight,
    BarChart3,
    BookCopy,
    BookOpen,
    CircleHelp,
    ClipboardList,
    CreditCard,
    Home,
    Library,
    MessageSquare,
    Settings,
    ShieldCheck,
    UserCog,
    UserRound,
    UsersRound
} from "lucide-react";
import { Navigate, NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { isReaderUser } from "../utils/authRole";
import { isAdmin } from "../utils/roleUtils";

const staffMenuGroups = [
    {
        label: "Chính",
        items: [
            { to: "/", label: "Tổng quan", icon: Home }
        ]
    },
    {
        label: "Quản lý thư viện",
        items: [
            { to: "/books", label: "Đầu sách", icon: BookOpen },
            { to: "/book-copies", label: "Cuốn sách", icon: BookCopy },
            { to: "/readers", label: "Độc giả", icon: UsersRound }
        ]
    },
    {
        label: "Nghiệp vụ",
        items: [
            { to: "/staff/loans", label: "Mượn sách", icon: ArrowLeftRight },
            { to: "/staff/returns", label: "Trả sách", icon: ClipboardList },
            { to: "/staff/payments", label: "Thu tiền", icon: CreditCard }
        ]
    },
    {
        label: "Kiểm duyệt",
        items: [
            { to: "/admin/comments", label: "Kiểm duyệt bình luận", icon: MessageSquare }
        ]
    },
    {
        label: "Hỗ trợ",
        items: [
            { to: "/guide", label: "Hướng dẫn sử dụng", icon: CircleHelp }
        ]
    },
    {
        label: "Tài khoản",
        items: [
            { to: "/settings", label: "Cài đặt", icon: Settings }
        ]
    }
];

const adminMenuGroups = [
    {
        label: "Chính",
        items: [
            { to: "/", label: "Tổng quan", icon: Home }
        ]
    },
    {
        label: "Quản lý thư viện",
        items: [
            { to: "/books", label: "Đầu sách", icon: BookOpen },
            { to: "/book-copies", label: "Cuốn sách", icon: BookCopy },
            { to: "/readers", label: "Độc giả", icon: UsersRound }
        ]
    },
    {
        label: "Nghiệp vụ",
        items: [
            { to: "/staff/loans", label: "Mượn sách", icon: ArrowLeftRight },
            { to: "/staff/returns", label: "Trả sách", icon: ClipboardList },
            { to: "/staff/payments", label: "Thu tiền", icon: CreditCard }
        ]
    },
    {
        label: "Quản trị hệ thống",
        items: [
            { to: "/admin/reports", label: "Báo cáo hệ thống", icon: BarChart3 },
            { to: "/admin/rules", label: "Quy định hệ thống", icon: ShieldCheck },
            { to: "/admin/comments", label: "Kiểm duyệt bình luận", icon: MessageSquare },
            { to: "/admin/librarians", label: "Tài khoản thủ thư", icon: UserCog }
        ]
    },
    {
        label: "Hỗ trợ",
        items: [
            { to: "/admin/guide", label: "Hướng dẫn sử dụng", icon: CircleHelp }
        ]
    },
    {
        label: "Tài khoản",
        items: [
            { to: "/settings", label: "Cài đặt", icon: Settings }
        ]
    }
];

export default function AppLayout() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const displayName = getUserDisplayName(user);
    const adminUser = isAdmin(user);
    const menuGroups = adminUser ? adminMenuGroups : staffMenuGroups;

    if (isReaderUser(user)) {
        return <Navigate to="/reader" replace />;
    }

    function goToProfileSettings() {
        navigate("/settings#profile");
        window.setTimeout(() => {
            document.getElementById("settings-profile")?.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
        }, 50);
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

                <NavMenu groups={menuGroups} />

                <button type="button" className="sidebar-user sidebar-user-button" onClick={goToProfileSettings}>
                    <div className="avatar">
                        <UserRound size={20} />
                    </div>
                    <div className="user-meta">
                        <b>{displayName}</b>
                        <span>{user?.tenVaiTro || user?.maVaiTro || "Vai trò"}</span>
                    </div>
                </button>
            </aside>

            <main className="workspace">
                <section className="page-container">
                    <Outlet />
                </section>
            </main>
        </div>
    );
}

function NavMenu({ groups, className = "" }) {
    return (
        <nav className={`nav-menu ${className}`.trim()}>
            {groups.map((group) => (
                <div className="nav-group" key={group.label}>
                    <span>{group.label}</span>
                    {group.items.map((item) => {
                        const Icon = item.icon;

                        return (
                            <NavLink key={item.to} to={item.to} end={item.to === "/"}>
                                <Icon size={18} />
                                <span>{item.label}</span>
                            </NavLink>
                        );
                    })}
                </div>
            ))}
        </nav>
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
        "Tài khoản";
}
