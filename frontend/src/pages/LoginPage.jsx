import { Library, LockKeyhole, UserRound } from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../components/ToastProvider";
import { isReaderUser } from "../utils/authRole";

export default function LoginPage() {
    const navigate = useNavigate();
    const { login } = useAuth();
    const toast = useToast();

    const [usernameOrEmail, setUsernameOrEmail] = useState("thuthu01");
    const [password, setPassword] = useState("123456");
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        setLoading(true);

        try {
            const data = await login(usernameOrEmail, password);
            toast.success("Đăng nhập thành công");

            navigate(isReaderUser(data) ? "/reader" : "/");
        } catch (err) {
            toast.error(err.message || "Đăng nhập thất bại");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="login-page">
            <div className="login-hero">
                <div className="hero-badge">Desktop Library System</div>
                <h1>Quản lý thư viện thông minh, hiện đại và dễ dùng.</h1>
                <p>
                    Theo dõi đầu sách, cuốn sách, độc giả, phiếu mượn, phiếu trả,
                    khoản nợ và báo cáo trong một giao diện desktop chuyên nghiệp.
                </p>

                <div className="hero-panel">
                    <div>
                        <b>Demo nhanh</b>
                        <span>Thủ thư: thuthu01 / 123456</span>
                    </div>
                    <Library size={42} />
                </div>
            </div>

            <form className="login-card" onSubmit={handleSubmit}>
                <div className="login-logo">
                    <div className="brand-icon">
                        <Library size={24} />
                    </div>
                    <div>
                        <h2>LibraDesk</h2>
                        <p>Đăng nhập hệ thống</p>
                    </div>
                </div>

                <label>Tên đăng nhập hoặc email</label>
                <div className="input-icon">
                    <UserRound size={18} />
                    <input
                        value={usernameOrEmail}
                        onChange={(e) => setUsernameOrEmail(e.target.value)}
                        placeholder="thuthu01"
                    />
                </div>

                <label>Mật khẩu</label>
                <div className="input-icon">
                    <LockKeyhole size={18} />
                    <input
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="123456"
                    />
                </div>

                <button className="primary-button" disabled={loading}>
                    {loading ? "Đang đăng nhập..." : "Đăng nhập"}
                </button>
            </form>
        </div>
    );
}
