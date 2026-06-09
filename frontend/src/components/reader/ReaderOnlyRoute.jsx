import { Navigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { isReaderUser } from "../../utils/authRole";

export default function ReaderOnlyRoute({ children }) {
    const { user, loadingUser } = useAuth();

    if (loadingUser) {
        return <div className="reader-loading">Đang kiểm tra tài khoản...</div>;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    if (!isReaderUser(user)) {
        return <Navigate to="/" replace />;
    }

    return children;
}
