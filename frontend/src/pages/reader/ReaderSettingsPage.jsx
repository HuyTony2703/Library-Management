import AccountSettingsContent, { SettingsLogoutButton } from "../../components/AccountSettingsContent";

export default function ReaderSettingsPage() {
    return (
        <div>
            <div className="reader-home-header">
                <div>
                    <p className="reader-eyebrow">Settings</p>
                    <h1>Cài đặt</h1>
                    <p>Quản lý tài khoản, đổi mật khẩu, giao diện, thông báo và đăng xuất.</p>
                </div>
                <div className="reader-home-header-actions">
                    <SettingsLogoutButton />
                </div>
            </div>

            <AccountSettingsContent />
        </div>
    );
}
