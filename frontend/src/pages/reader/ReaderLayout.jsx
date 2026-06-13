import {
    BookOpen,
    BookmarkCheck,
    CircleHelp,
    CreditCard,
    Heart,
    Home,
    Library,
    Search,
    Settings,
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
        label: "Hỗ trợ",
        items: [
            { to: "/reader/guide", label: "Hướng dẫn", icon: CircleHelp },
            { to: "/reader/rules", label: "Quy định", icon: ShieldCheck }
        ]
    },
    {
        label: "Tài khoản",
        items: [
            { to: "/reader/membership", label: "Gói thành viên", icon: CreditCard },
            { to: "/reader/settings", label: "Cài đặt", icon: Settings }
        ]
    }
];

export default function ReaderLayout() {
    const { user } = useAuth();
    const navigate = useNavigate();

    function goToProfileSettings() {
        navigate("/reader/settings#profile");
        window.setTimeout(() => {
            document.getElementById("settings-profile")?.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
        }, 50);
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

                <button type="button" className="reader-account reader-account-button" onClick={goToProfileSettings}>
                    <div className="reader-avatar">
                        <UserRound size={20} />
                    </div>
                    <div>
                        <b>{user?.hoTen || user?.tenDangNhap || "Độc giả"}</b>
                        <span>{user?.maDocGia || user?.maTaiKhoan || "Độc giả"}</span>
                    </div>
                </button>
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
