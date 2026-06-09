import { Bell } from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { readerApi } from "../../api/readerApi";

export default function NotificationBell() {
    const navigate = useNavigate();
    const [count, setCount] = useState(0);

    async function loadUnreadCount() {
        try {
            const result = await readerApi.getUnreadNotificationCount();
            setCount(result?.unreadCount ?? 0);
        } catch (err) {
            console.error("Không tải được số thông báo chưa đọc:", err);
        }
    }

    useEffect(() => {
        loadUnreadCount();

        const timer = window.setInterval(loadUnreadCount, 30000);
        window.addEventListener("reader-notifications-changed", loadUnreadCount);

        return () => {
            window.clearInterval(timer);
            window.removeEventListener("reader-notifications-changed", loadUnreadCount);
        };
    }, []);

    return (
        <button
            type="button"
            className="notification-bell"
            onClick={() => navigate("/reader/notifications")}
            title="Thông báo"
        >
            <Bell size={20} />
            {count > 0 && (
                <span className="notification-badge">
                    {count > 99 ? "99+" : count}
                </span>
            )}
        </button>
    );
}
