import {
    BookOpen,
    BookmarkCheck,
    CircleHelp,
    CreditCard,
    Heart,
    Home,
    Search,
    Settings,
    ShieldCheck
} from "lucide-react";
import NotificationBell from "../../components/reader/NotificationBell";
import { useAuth } from "../../context/AuthContext";
import "../../styles/role-theme.css";
import AppShell from "../../components/AppShell";
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

    return (
        <AppShell
            role="reader"
            brandTitle="LibraDesk"
            brandSubtitle="Cổng độc giả"
            menuGroups={menuGroups}
            roleBadge={<span className="role-chip role-chip-reader">ĐỘC GIẢ</span>}
            topbarTitle="Không gian đọc sách"
            topbarSubtitle="Tra cứu • Mượn • Đặt trước"
            topbarActions={<NotificationBell />}
            profilePath="/reader/settings#profile"
            profileTargetId="settings-profile"
            userName={user?.hoTen || user?.tenDangNhap || "Độc giả"}
            userSub={user?.maDocGia || user?.maTaiKhoan || "Độc giả"}
        />
    );
}
