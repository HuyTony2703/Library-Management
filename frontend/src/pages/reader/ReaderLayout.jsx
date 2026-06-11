import {
    BookOpen,
    BookmarkCheck,
    CircleHelp,
    CreditCard,
    Heart,
    Home,
    Library,
    LogOut,
    Search,
    ShieldCheck,
    UserRound
} from "lucide-react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import NotificationBell from "../../components/reader/NotificationBell";
import { useAuth } from "../../context/AuthContext";
import "./reader.css";

const menuGroups = [
    {
        label: "Chính",
        items: [
            { to: "/reader", label: "Trang chủ", icon: Home },
            { to: "/reader/books", label: "Tra cứu sách", icon: Search }
        ]
    },
    {
        label: "Hoạt động của tôi",
        items: [
            { to: "/reader/loans", label: "Sách đang mượn", icon: BookOpen },
            { to: "/reader/reservations", label: "Đặt trước", icon: BookmarkCheck },
            { to: "/reader/favorites", label: "Sách yêu thích", icon: Heart }
        ]
    },
    {
        label: "Tài khoản",
        items: [
            { to: "/reader/membership", label: "Gói độc giả", icon: CreditCard }
        ]
    },
    {
        label: "Hỗ trợ",
        items: [
            { to: "/reader/guide", label: "Hướng dẫn", icon: CircleHelp },
            { to: "/reader/rules", label: "Quy định", icon: ShieldCheck }
        ]
    }
];

export default function ReaderLayout() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    function handleLogout() {
        logout();
        navigate("/login");
    }

    return (
        <div className="reader-shell">
            <aside className="reader-sidebar">
                <div className="reader-brand">
                    <div className="reader-brand-icon">
                        <Library size={24} />
                    </div>
                    <div>
                        <h2>LibraDesk</h2>
                        <p>Cổng độc giả</p>
                    </div>
                </div>

                <nav className="reader-nav">
                    {menuGroups.map((group) => (
                        <div className="reader-nav-group" key={group.label}>
                            <span>{group.label}</span>
                            {group.items.map((item) => {
                                const Icon = item.icon;

                                return (
                                    <NavLink key={item.to} to={item.to} end={item.to === "/reader"}>
                                        <Icon size={18} />
                                        <span>{item.label}</span>
                                    </NavLink>
                                );
                            })}
                        </div>
                    ))}
                </nav>

                <div className="reader-account">
                    <div className="reader-avatar">
                        <UserRound size={20} />
                    </div>
                    <div>
                        <b>{user?.hoTen || user?.tenDangNhap || "reader"}</b>
                        <span>{user?.maDocGia || user?.maTaiKhoan || "Độc giả"}</span>
                    </div>
                </div>
            </aside>

            <main className="reader-content">
                <header className="reader-topbar">
                    <div className="reader-topbar-actions">
                        <NotificationBell />
                        <button type="button" className="reader-logout-button" onClick={handleLogout}>
                            <LogOut size={18} />
                            Đăng xuất
                        </button>
                    </div>
                </header>

                <section className="reader-page">
                    <Outlet />
                </section>
            </main>
        </div>
    );
}
