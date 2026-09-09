import {
    ArrowLeftRight,
    BarChart3,
    BookCopy,
    BookOpen,
    CircleHelp,
    ClipboardList,
    CreditCard,
    Home,
    MessageSquare,
    Settings,
    ShieldCheck,
    UserCog,
    UsersRound
} from "lucide-react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { isReaderUser } from "../utils/authRole";
import { isAdmin } from "../utils/roleUtils";
import "../styles/role-theme.css";
import AppShell from "./AppShell";

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
    const adminUser = isAdmin(user);
    const menuGroups = adminUser ? adminMenuGroups : staffMenuGroups;

    if (isReaderUser(user)) {
        return <Navigate to="/reader" replace />;
    }

    const role = adminUser ? "admin" : "staff";
    const roleBadge = (
        <span className={`role-chip role-chip-${role}`}>
            {adminUser ? "ADMIN" : "THỦ THƯ"}
        </span>
    );

    return (
        <AppShell
            role={role}
            brandTitle="LibraDesk"
            brandSubtitle={adminUser ? "Quản trị hệ thống" : "Quầy thủ thư"}
            menuGroups={menuGroups}
            roleBadge={roleBadge}
            topbarTitle={adminUser ? "Trung tâm điều hành" : "Không gian tác nghiệp"}
            topbarSubtitle={adminUser ? "Quản trị • Báo cáo • Quy định" : "Mượn • Trả • Thu tiền"}
            userName={getUserDisplayName(user)}
            userSub={user?.tenVaiTro || user?.maVaiTro || (adminUser ? "Admin" : "Thủ thư")}
            profilePath="/settings#profile"
            profileTargetId="settings-profile"
        />
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
