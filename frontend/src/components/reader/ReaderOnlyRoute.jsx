import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { isReaderUser } from "../../utils/authRole";

export default function ReaderOnlyRoute({ children }) {
    const { user, loadingUser } = useAuth();
    const location = useLocation();

    if (loadingUser) {
        return <div className="reader-loading">Đang kiểm tra tài khoản...</div>;
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    if (!isReaderUser(user)) {
        return <Navigate to="/" replace />;
    }

    if (user.mustChangePassword && location.pathname !== "/reader/settings") {
        return <Navigate to="/reader/settings#security" replace />;
    }

    return children;
}
