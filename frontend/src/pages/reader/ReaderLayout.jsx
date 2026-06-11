import {
    Bell,
    BookOpen,
    BookmarkCheck,
    Calculator,
    CircleHelp,
    CreditCard,
    Heart,
    Home,
    Library,
    Search,
    ShieldCheck,
    Settings,
    UserRound
} from "lucide-react";
import { NavLink, Outlet } from "react-router-dom";
import NotificationBell from "../../components/reader/NotificationBell";
import { useAuth } from "../../context/AuthContext";
import "./reader.css";

const menuItems = [
    { to: "/reader", label: "Trang chủ", icon: Home },
    { to: "/reader/books", label: "Tra cứu sách", icon: Search },
    { to: "/reader/loans", label: "Sách đang mượn", icon: BookOpen },
    { to: "/reader/reservations", label: "Đặt trước", icon: BookmarkCheck },
    { to: "/reader/notifications", label: "Thông báo", icon: Bell },
    { to: "/reader/membership", label: "Gói thành viên", icon: CreditCard },
    { to: "/reader/favorites", label: "Sách yêu thích", icon: Heart },
    { to: "/reader/guide", label: "Hướng dẫn", icon: CircleHelp },
    { to: "/reader/rules", label: "Quy định", icon: ShieldCheck },
    { to: "/reader/penalty-rules", label: "Cách tính phạt", icon: Calculator },
    { to: "/reader/settings", label: "Cài đặt", icon: Settings }
];

export default function ReaderLayout() {
    const { user } = useAuth();

    return (
        <div className="reader-shell">
            <aside className="reader-sidebar">
                <div className="reader-brand">
                    <div className="reader-brand-icon">
                        <Library size={24} />
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
                            <NavLink key={item.to} to={item.to} end={item.to === "/reader"}>
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
                        <b>{user?.hoTen || user?.tenDangNhap || "reader"}</b>
                        <span>{user?.maDocGia || user?.maTaiKhoan || "Độc giả"}</span>
                    </div>
                </div>
            </aside>

            <main className="reader-content">
                <header className="reader-topbar">
                    <div className="reader-topbar-actions">
                        <NotificationBell />
                    </div>
                </header>

                <section className="reader-page">
                    <Outlet />
                </section>
            </main>
        </div>
    );
}
