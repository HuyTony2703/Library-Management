import {
    Bell,
    BookOpen,
    CreditCard,
    KeyRound,
    LogOut,
    MessageSquare,
    RefreshCcw,
    RotateCcw,
    Save,
    Settings,
    ShieldCheck,
    SlidersHorizontal,
    UserRoundCog,
    UsersRound
} from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { changePasswordApi, updateProfileApi } from "../api/authApi";
import { useAuth } from "../context/AuthContext";
import { normalizeRole } from "../utils/roleUtils";
import { useActionDialog } from "./ActionDialogProvider";
import PasswordInput from "./PasswordInput";
import { useToast } from "./ToastProvider";

const defaultPreferences = {
    theme: "light",
    density: "comfortable",
    accent: "blue"
};

const defaultNotificationSettings = {
    inApp: true,
    browser: false,
    email: false,
    loanReminders: true,
    debtAndFineAlerts: true,
    reservationAlerts: true,
    membershipAlerts: true,
    accountStatusAlerts: true
};

const notificationCoverageChecks = [
    { key: "loanReminders", events: ["due_soon", "overdue"] },
    { key: "debtAndFineAlerts", events: ["fine_created"] },
    { key: "reservationAlerts", events: ["reservation_ready"] },
    { key: "membershipAlerts", events: ["membership_success", "membership_expiring"] },
    { key: "accountStatusAlerts", events: ["account_status_changed"] }
];

export default function AccountSettingsContent({ portal = "staff" }) {
    const { user, logout, refreshUser } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const toast = useToast();
    const actionDialog = useActionDialog();
    const role = normalizeRole(user);
    const displayName = getDisplayName(user);

    const [profileForm, setProfileForm] = useState(() => buildProfileForm(user));
    const [passwordForm, setPasswordForm] = useState({
        currentPassword: "",
        newPassword: "",
        confirmPassword: ""
    });
    const [preferences, setPreferences] = useState(() => loadJson("library_ui_preferences", defaultPreferences));
    const [notifications, setNotifications] = useState(() => loadJson("library_notification_preferences", defaultNotificationSettings));
    const [savingPassword, setSavingPassword] = useState(false);
    const [savingProfile, setSavingProfile] = useState(false);
    const [refreshingProfile, setRefreshingProfile] = useState(false);
    const [editingProfile, setEditingProfile] = useState(false);

    useEffect(() => {
        setProfileForm(buildProfileForm(user));
        setEditingProfile(false);
    }, [user]);

    useEffect(() => {
        applyPreferences(preferences);
    }, [preferences]);

    useEffect(() => {
        if (location.hash !== "#profile") {
            return;
        }

        window.requestAnimationFrame(() => {
            document.getElementById("settings-profile")?.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
        });
    }, [location.hash]);

    const shortcutActions = useMemo(() => getShortcutActions(role, portal), [role, portal]);

    function updatePasswordField(field, value) {
        setPasswordForm((prev) => ({ ...prev, [field]: value }));
    }

    function updateProfileField(field, value) {
        setProfileForm((prev) => ({ ...prev, [field]: value }));
    }

    function updatePreference(field, value) {
        setPreferences((prev) => {
            const next = { ...prev, [field]: value };
            localStorage.setItem("library_ui_preferences", JSON.stringify(next));
            applyPreferences(next);
            return next;
        });
    }

    function updateNotification(field, value) {
        setNotifications((prev) => ({ ...prev, [field]: value }));
    }

    async function submitPassword(event) {
        event.preventDefault();

        if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
            toast.error("Vui lòng nhập đầy đủ thông tin đổi mật khẩu");
            return;
        }

        if (passwordForm.newPassword.length < 6) {
            toast.error("Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }

        if (passwordForm.newPassword !== passwordForm.confirmPassword) {
            toast.error("Mật khẩu xác nhận không khớp");
            return;
        }

        setSavingPassword(true);

        try {
            await changePasswordApi({
                currentPassword: passwordForm.currentPassword,
                newPassword: passwordForm.newPassword
            });

            setPasswordForm({
                currentPassword: "",
                newPassword: "",
                confirmPassword: ""
            });
            toast.success("Đổi mật khẩu thành công");
        } catch (err) {
            toast.error(err.message || "Đổi mật khẩu thất bại");
        } finally {
            setSavingPassword(false);
        }
    }

    async function submitProfile(event) {
        event.preventDefault();

        if (!editingProfile) {
            setEditingProfile(true);
            return;
        }

        if (!profileForm.hoTen.trim()) {
            toast.error("Vui lòng nhập họ tên");
            return;
        }

        if (!profileForm.email.trim()) {
            toast.error("Vui lòng nhập email");
            return;
        }

        setSavingProfile(true);

        try {
            const updatedUser = await updateProfileApi({
                hoTen: profileForm.hoTen.trim(),
                email: profileForm.email.trim(),
                soDienThoai: profileForm.soDienThoai.trim(),
                diaChi: profileForm.diaChi.trim()
            });

            if (updatedUser) {
                const currentUser = loadJson("user", {});
                localStorage.setItem("user", JSON.stringify({ ...currentUser, ...updatedUser }));
            }

            await refreshUser?.();
            setEditingProfile(false);
            toast.success("Đã cập nhật thông tin cá nhân");
        } catch (err) {
            toast.error(err.message || "Cập nhật thông tin cá nhân thất bại");
        } finally {
            setSavingProfile(false);
        }
    }

    async function reloadProfile() {
        if (!refreshUser) {
            toast.error("Không thể làm mới thông tin tài khoản");
            return;
        }

        setRefreshingProfile(true);

        try {
            await refreshUser();
            setEditingProfile(false);
            toast.success("Đã làm mới thông tin tài khoản");
        } catch (err) {
            toast.error(err.message || "Không làm mới được thông tin tài khoản");
        } finally {
            setRefreshingProfile(false);
        }
    }

    function cancelEditProfile() {
        setProfileForm(buildProfileForm(user));
        setEditingProfile(false);
    }

    function savePreferences() {
        localStorage.setItem("library_ui_preferences", JSON.stringify(preferences));
        applyPreferences(preferences);
        toast.success("Đã lưu cài đặt giao diện");
    }

    function resetPreferences() {
        setPreferences(defaultPreferences);
        localStorage.setItem("library_ui_preferences", JSON.stringify(defaultPreferences));
        applyPreferences(defaultPreferences);
        toast.success("Đã đặt lại giao diện mặc định");
    }

    async function saveNotifications() {
        if (notifications.browser && !("Notification" in window)) {
            updateNotification("browser", false);
            toast.error("Trình duyệt hiện tại không hỗ trợ thông báo");
            return;
        }

        if (notifications.browser && "Notification" in window && Notification.permission === "default") {
            const permission = await Notification.requestPermission();

            if (permission !== "granted") {
                updateNotification("browser", false);
                toast.error("Trình duyệt chưa cho phép gửi thông báo");
                return;
            }
        }

        localStorage.setItem("library_notification_preferences", JSON.stringify(notifications));
        toast.success("Đã lưu cài đặt thông báo");
    }

    async function testNotifications() {
        if (!notifications.inApp && !notifications.browser && !notifications.email) {
            toast.error("Vui lòng bật ít nhất một kênh thông báo trước khi kiểm tra");
            return;
        }

        const disabledCoverage = notificationCoverageChecks
            .filter((check) => !notifications[check.key])
            .flatMap((check) => check.events);

        if (disabledCoverage.length === notificationCoverageChecks.flatMap((check) => check.events).length) {
            toast.error("Vui lòng bật ít nhất một nhóm nội dung thông báo");
            return;
        }

        toast.success("Thông báo trong ứng dụng đang hoạt động");

        if (notifications.browser) {
            if (!("Notification" in window)) {
                toast.error("Trình duyệt hiện tại không hỗ trợ thông báo");
                return;
            }

            if (Notification.permission === "default") {
                await Notification.requestPermission();
            }

            if (Notification.permission === "granted") {
                new Notification("LibraDesk", {
                    body: "Thông báo trình duyệt đã được bật cho tài khoản này."
                });
            } else {
                toast.error("Trình duyệt chưa cho phép gửi thông báo");
            }
        }
    }

    async function confirmLogout() {
        const confirmed = await actionDialog.confirm({
            title: "Đăng xuất",
            message: "Bạn chắc chắn muốn đăng xuất khỏi tài khoản hiện tại?",
            confirmLabel: "Đăng xuất",
            cancelLabel: "Ở lại",
            danger: true
        });

        if (!confirmed) {
            return;
        }

        logout();
        navigate("/login", { replace: true });
    }

    return (
        <div className="account-settings">
            <section className="settings-summary-grid">
                <article className="panel settings-profile-card">
                    <div>
                        <p className="eyebrow">Tài khoản hiện tại</p>
                        <h2>{displayName}</h2>
                        <p className="settings-muted">{user?.tenDangNhap || user?.maTaiKhoan}</p>
                    </div>
                    <span className="status-badge status-neutral settings-role-badge">
                        {displayRole(user, role)}
                    </span>
                </article>

                <article className="panel settings-note-card">
                    <h2>Phạm vi thao tác</h2>
                    <p>{getRoleDescription(role)}</p>
                </article>
            </section>

            <section className="settings-dashboard-grid">
                <div className="settings-column settings-column-left">
                    <form id="settings-profile" className="panel settings-section settings-profile-section" onSubmit={submitProfile}>
                        <div className="settings-section-title">
                            <UserRoundCog size={22} />
                            <div>
                                <h2>Thông tin cá nhân</h2>
                                <p>
                                    {editingProfile
                                        ? "Đang chỉnh sửa thông tin liên hệ của tài khoản."
                                        : "Thông tin hiện tại của tài khoản đang đăng nhập."}
                                </p>
                            </div>
                        </div>

                        <div className="form-grid-2">
                            <label className="form-row">
                                <span>Họ tên</span>
                                <input
                                    className={!editingProfile ? "read-only-field" : ""}
                                    readOnly={!editingProfile}
                                    value={profileForm.hoTen}
                                    onChange={(event) => updateProfileField("hoTen", event.target.value)}
                                />
                            </label>
                            <label className="form-row">
                                <span>Email</span>
                                <input
                                    className={!editingProfile ? "read-only-field" : ""}
                                    readOnly={!editingProfile}
                                    type="email"
                                    value={profileForm.email}
                                    onChange={(event) => updateProfileField("email", event.target.value)}
                                />
                            </label>
                        </div>

                        <div className="form-grid-2">
                            <label className="form-row">
                                <span>Số điện thoại</span>
                                <input
                                    className={!editingProfile ? "read-only-field" : ""}
                                    readOnly={!editingProfile}
                                    value={profileForm.soDienThoai}
                                    onChange={(event) => updateProfileField("soDienThoai", event.target.value)}
                                />
                            </label>
                            <label className="form-row">
                                <span>Địa chỉ</span>
                                <input
                                    className={!editingProfile ? "read-only-field" : ""}
                                    readOnly={!editingProfile}
                                    value={profileForm.diaChi}
                                    onChange={(event) => updateProfileField("diaChi", event.target.value)}
                                />
                            </label>
                        </div>

                        <div className="settings-info-list">
                            <InfoRow label="Tên đăng nhập" value={user?.tenDangNhap} />
                            <InfoRow label="Mã tài khoản" value={user?.maTaiKhoan} />
                            <InfoRow label="Mã nhân viên" value={user?.maNhanVien} />
                            <InfoRow label="Mã độc giả" value={user?.maDocGia} />
                            <InfoRow label="Vai trò" value={displayRole(user, role)} />
                        </div>

                        <div className="settings-section-actions">
                            {!editingProfile ? (
                                <button type="submit" className="primary-button">
                                    <SlidersHorizontal size={17} />
                                    Cài đặt thông tin
                                </button>
                            ) : (
                                <>
                                    <button type="submit" className="primary-button" disabled={savingProfile}>
                                        <Save size={17} />
                                        {savingProfile ? "Đang lưu..." : "Lưu thông tin"}
                                    </button>
                                    <button type="button" className="soft-button" onClick={cancelEditProfile}>
                                        Hủy
                                    </button>
                                </>
                            )}
                            <button type="button" className="soft-button" onClick={reloadProfile} disabled={refreshingProfile}>
                                <RefreshCcw size={17} />
                                {refreshingProfile ? "Đang tải..." : "Làm mới"}
                            </button>
                        </div>
                    </form>

                    <SettingsShortcuts
                        actions={shortcutActions}
                        onNavigate={navigate}
                        onLogout={confirmLogout}
                    />
                </div>

                <div className="settings-column settings-column-right">
                    <form className="panel settings-section settings-password-section" onSubmit={submitPassword}>
                        <div className="settings-section-title">
                            <KeyRound size={22} />
                            <div>
                                <h2>Đổi mật khẩu</h2>
                                <p>Xác thực mật khẩu hiện tại trước khi đặt mật khẩu mới.</p>
                            </div>
                        </div>

                        <div className="form-grid-3">
                            <label className="form-row">
                                <span>Mật khẩu hiện tại</span>
                                <PasswordInput
                                    value={passwordForm.currentPassword}
                                    onChange={(event) => updatePasswordField("currentPassword", event.target.value)}
                                />
                            </label>
                            <label className="form-row">
                                <span>Mật khẩu mới</span>
                                <PasswordInput
                                    value={passwordForm.newPassword}
                                    onChange={(event) => updatePasswordField("newPassword", event.target.value)}
                                />
                            </label>
                            <label className="form-row">
                                <span>Xác nhận mật khẩu</span>
                                <PasswordInput
                                    value={passwordForm.confirmPassword}
                                    onChange={(event) => updatePasswordField("confirmPassword", event.target.value)}
                                />
                            </label>
                        </div>

                        <div className="settings-section-actions settings-password-actions">
                            <button type="submit" className="primary-button" disabled={savingPassword}>
                                <Save size={17} />
                                {savingPassword ? "Đang lưu..." : "Lưu mật khẩu"}
                            </button>
                        </div>
                    </form>

                    <article className="panel settings-section settings-appearance-section">
                        <div className="settings-section-title">
                            <Settings size={22} />
                            <div>
                                <h2>Giao diện</h2>
                                <p>Cài đặt được lưu trên trình duyệt đang sử dụng.</p>
                            </div>
                        </div>

                        <div className="form-grid-3">
                            <label className="form-row">
                                <span>Chế độ màu</span>
                                <select value={preferences.theme} onChange={(event) => updatePreference("theme", event.target.value)}>
                                    <option value="light">Sáng</option>
                                    <option value="dark">Tối</option>
                                    <option value="system">Theo hệ thống</option>
                                </select>
                            </label>
                            <label className="form-row">
                                <span>Mật độ hiển thị</span>
                                <select value={preferences.density} onChange={(event) => updatePreference("density", event.target.value)}>
                                    <option value="comfortable">Thoải mái</option>
                                    <option value="compact">Gọn</option>
                                </select>
                            </label>
                            <label className="form-row">
                                <span>Màu nhấn</span>
                                <select value={preferences.accent} onChange={(event) => updatePreference("accent", event.target.value)}>
                                    <option value="blue">Xanh dương</option>
                                    <option value="green">Xanh lá</option>
                                    <option value="violet">Tím</option>
                                </select>
                            </label>
                        </div>

                        <div className="settings-section-actions">
                            <button type="button" className="primary-button" onClick={savePreferences}>
                                <Save size={17} />
                                Lưu giao diện
                            </button>
                            <button type="button" className="soft-button" onClick={resetPreferences}>
                                <RotateCcw size={17} />
                                Mặc định
                            </button>
                        </div>
                    </article>

                    <article className="panel settings-section settings-notification-section">
                        <div className="settings-section-title">
                            <Bell size={22} />
                            <div>
                                <h2>Thông báo cá nhân</h2>
                                <p>Chọn cách nhận thông báo phù hợp với tài khoản.</p>
                            </div>
                        </div>

                        <div className="settings-notification-layout">
                            <div className="settings-notification-group">
                                <p className="settings-subtitle">Kênh nhận</p>
                                <div className="settings-toggle-list compact-toggle-list">
                                    <ToggleRow
                                        label="Trong ứng dụng"
                                        checked={notifications.inApp}
                                        onChange={(checked) => updateNotification("inApp", checked)}
                                    />
                                    <ToggleRow
                                        label="Trình duyệt"
                                        checked={notifications.browser}
                                        onChange={(checked) => updateNotification("browser", checked)}
                                    />
                                    <ToggleRow
                                        label="Email quan trọng"
                                        checked={notifications.email}
                                        onChange={(checked) => updateNotification("email", checked)}
                                    />
                                </div>
                            </div>

                            <div className="settings-notification-group">
                                <p className="settings-subtitle">Nội dung muốn nhận</p>
                                <div className="settings-toggle-list compact-toggle-list">
                                    <ToggleRow
                                        label="Nhắc hạn trả và quá hạn"
                                        checked={notifications.loanReminders}
                                        onChange={(checked) => updateNotification("loanReminders", checked)}
                                    />
                                    <ToggleRow
                                        label="Phát sinh tiền phạt"
                                        checked={notifications.debtAndFineAlerts}
                                        onChange={(checked) => updateNotification("debtAndFineAlerts", checked)}
                                    />
                                    <ToggleRow
                                        label="Sách đặt trước đã có"
                                        checked={notifications.reservationAlerts}
                                        onChange={(checked) => updateNotification("reservationAlerts", checked)}
                                    />
                                    <ToggleRow
                                        label="Gói thành viên"
                                        checked={notifications.membershipAlerts}
                                        onChange={(checked) => updateNotification("membershipAlerts", checked)}
                                    />
                                    <ToggleRow
                                        label="Trạng thái tài khoản hoặc thẻ"
                                        checked={notifications.accountStatusAlerts}
                                        onChange={(checked) => updateNotification("accountStatusAlerts", checked)}
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="settings-section-actions">
                            <button type="button" className="primary-button" onClick={saveNotifications}>
                                <Save size={17} />
                                Lưu thông báo
                            </button>
                            <button type="button" className="soft-button" onClick={testNotifications}>
                                <Bell size={17} />
                                Kiểm tra
                            </button>
                        </div>
                    </article>
                </div>
            </section>
        </div>
    );
}

function SettingsShortcuts({ actions, onNavigate, onLogout }) {
    return (
        <section className="panel settings-section settings-shortcuts-section">
            <div className="settings-section-title">
                <ShieldCheck size={22} />
                <div>
                    <h2>Chức năng theo quyền</h2>
                    <p>Các lối tắt bên dưới thay đổi theo vai trò tài khoản.</p>
                </div>
            </div>

            <div className="settings-shortcut-grid">
                {actions.map((action) => {
                    const Icon = action.icon;

                    return (
                        <button
                            key={action.to}
                            type="button"
                            className="settings-shortcut"
                            onClick={() => onNavigate(action.to)}
                        >
                            <Icon size={20} />
                            <span>{action.label}</span>
                        </button>
                    );
                })}

                <button type="button" className="settings-shortcut danger-shortcut" onClick={onLogout}>
                    <LogOut size={20} />
                    <span>Đăng xuất</span>
                </button>
            </div>
        </section>
    );
}

function InfoRow({ label, value }) {
    if (!value) {
        return null;
    }

    return (
        <div className="settings-info-row">
            <span>{label}</span>
            <b>{value}</b>
        </div>
    );
}

function ToggleRow({ label, checked, onChange }) {
    return (
        <label className="settings-toggle-row">
            <span>{label}</span>
            <input
                type="checkbox"
                checked={checked}
                onChange={(event) => onChange(event.target.checked)}
            />
        </label>
    );
}

function buildProfileForm(user) {
    const displayName = getDisplayName(user);

    return {
        hoTen: displayName === "Tài khoản" ? "" : displayName,
        email: user?.email || "",
        soDienThoai: user?.soDienThoai || "",
        diaChi: user?.diaChi || ""
    };
}

function getDisplayName(user) {
    return user?.hoTen ||
        user?.tenNhanVien ||
        user?.tenDocGia ||
        user?.tenDangNhap ||
        user?.maNhanVien ||
        user?.maDocGia ||
        user?.maTaiKhoan ||
        "Tài khoản";
}

function displayRole(user, role) {
    if (user?.tenVaiTro) {
        return user.tenVaiTro;
    }

    if (role === "ADMIN") return "Quản trị viên";
    if (role === "STAFF") return "Thủ thư";
    if (role === "READER") return "Độc giả";
    return user?.maVaiTro || "Không xác định";
}

function getRoleDescription(role) {
    if (role === "ADMIN") {
        return "Admin có toàn bộ quyền của thủ thư và thêm quyền quản trị hệ thống, quy định, báo cáo và tài khoản thủ thư.";
    }

    if (role === "STAFF") {
        return "Thủ thư xử lý nghiệp vụ sách, độc giả, mượn trả, thu tiền và kiểm duyệt bình luận trong phạm vi được cấp.";
    }

    return "Độc giả có thể xem hồ sơ, tra cứu sách, quản lý mượn trả, đặt trước, yêu thích, gói thành viên và thông báo cá nhân.";
}

function getShortcutActions(role, portal) {
    if (role === "ADMIN") {
        return [
            { to: "/books", label: "Quản lý đầu sách", icon: BookOpen },
            { to: "/readers", label: "Quản lý độc giả", icon: UsersRound },
            { to: "/admin/librarians", label: "Quản lý thủ thư", icon: UsersRound },
            { to: "/admin/comments", label: "Kiểm duyệt bình luận", icon: MessageSquare },
            { to: "/admin/rules", label: "Quy định hệ thống", icon: ShieldCheck },
            { to: "/admin/reports", label: "Báo cáo hệ thống", icon: BookOpen }
        ];
    }

    if (role === "STAFF") {
        return [
            { to: "/books", label: "Quản lý đầu sách", icon: BookOpen },
            { to: "/readers", label: "Quản lý độc giả", icon: UsersRound },
            { to: "/admin/comments", label: "Kiểm duyệt bình luận", icon: MessageSquare },
            { to: "/staff/payments", label: "Thu tiền", icon: CreditCard }
        ];
    }

    return [
        { to: portal === "reader" ? "/reader" : "/", label: "Thông tin cá nhân", icon: UserRoundCog },
        { to: "/reader/notifications", label: "Thông báo", icon: Bell },
        { to: "/reader/rules", label: "Quy định thư viện", icon: ShieldCheck },
        { to: "/reader/favorites", label: "Sách yêu thích", icon: BookOpen }
    ];
}

function loadJson(key, fallback) {
    try {
        const value = localStorage.getItem(key);
        return value ? { ...fallback, ...JSON.parse(value) } : fallback;
    } catch {
        return fallback;
    }
}

function applyPreferences(preferences) {
    const theme = preferences.theme === "system"
        ? (window.matchMedia?.("(prefers-color-scheme: dark)").matches ? "dark" : "light")
        : preferences.theme;

    document.documentElement.dataset.theme = theme || "light";
    document.body.dataset.density = preferences.density || "comfortable";
    document.documentElement.dataset.accent = preferences.accent || "blue";
}
