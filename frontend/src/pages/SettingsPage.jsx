import AccountSettingsContent from "../components/AccountSettingsContent";
import PageHeader from "../components/PageHeader";

export default function SettingsPage() {
    return (
        <div>
            <PageHeader
                eyebrow="Settings"
                title="Cài đặt"
                description="Quản lý tài khoản, đổi mật khẩu, giao diện, thông báo và đăng xuất."
            />

            <AccountSettingsContent portal="staff" />
        </div>
    );
}
