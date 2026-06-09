import {
    Bell,
    BookOpen,
    ClipboardList,
    Heart,
    HelpCircle,
    Home,
    Lightbulb,
    LogOut,
    RotateCcw,
    ShoppingBag,
    UserRound
} from "lucide-react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import "./reader.css";

const menuItems = [
    { to: "/reader", label: "Trang chủ", icon: Home, end: true },
    { to: "/reader/books", label: "Tra cứu sách", icon: BookOpen },
    { to: "/reader/loans", label: "Sách đang mượn", icon: RotateCcw },
    { to: "/reader/reservations", label: "Đặt trước", icon: ClipboardList },
    { to: "/reader/notifications", label: "Thông báo", icon: Bell },
    { to: "/reader/membership", label: "Gói thành viên", icon: ShoppingBag },
    { to: "/reader/favorites", label: "Sách yêu thích", icon: Heart },
    { to: "/reader/recommendations", label: "Gợi ý sách", icon: Lightbulb },
    { to: "/reader/guide", label: "Hướng dẫn", icon: HelpCircle }
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
                        <BookOpen size={24} />
                    </div>
                    <div>
                        <h2>LibraDesk</h2>
                        <p>Reader Portal</p>
                    </div>
                </div>

                <nav className="reader-nav">
                    {menuItems.map((item) => {
                        const Icon = item.icon;

                        return (
                            <NavLink key={item.to} to={item.to} end={item.end}>
                                <Icon size={18} />
                                <span>{item.label}</span>
                            </NavLink>
                        );
                    })}
                </nav>

                <div className="reader-account">
                    <div className="reader-avatar">
                        <UserRound size={20} />
                    </div>
                    <div>
                        <b>{user?.tenDangNhap || "Độc giả"}</b>
                        <span>{user?.maDocGia || "DOC_GIA"}</span>
                    </div>
                </div>
            </aside>

            <main className="reader-content">
                <header className="reader-topbar">
                    <div>
                        <strong>Cổng độc giả</strong>
                        <span>Theo dõi sách, đặt trước, gia hạn và thông báo</span>
                    </div>

                    <button className="reader-logout-button" onClick={handleLogout}>
                        <LogOut size={18} />
                        Đăng xuất
                    </button>
                </header>

                <section className="reader-page">
                    <Outlet />
                </section>
            </main>
        </div>
    );
}
