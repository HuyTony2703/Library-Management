import { Bell } from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { readerApi } from "../../api/readerApi";

function getUnreadCountFromList(items) {
    if (!Array.isArray(items)) {
        return 0;
    }

    return items.filter((item) => !item.daDoc).length;
}

export default function NotificationBell() {
    const navigate = useNavigate();
    const [count, setCount] = useState(0);

    async function loadUnreadCount() {
        try {
            const result = await readerApi.getUnreadNotificationCount();
            const parsed = Number(result?.unreadCount ?? result?.count ?? result ?? 0);
            const unreadCount = Number.isFinite(parsed) ? parsed : 0;

            if (unreadCount > 0) {
                setCount(unreadCount);
                return;
            }

            const notifications = await readerApi.getNotifications();
            setCount(getUnreadCountFromList(notifications));
        } catch (err) {
            console.error("Unable to load unread notification count:", err);
        }
    }

    useEffect(() => {
        loadUnreadCount();

        const timer = window.setInterval(loadUnreadCount, 30000);
        const handleNotificationsChanged = (event) => {
            const nextCount = Number(event.detail?.unreadCount);

            if (Number.isFinite(nextCount)) {
                setCount(nextCount);
                return;
            }

            loadUnreadCount();
        };

        window.addEventListener("reader-notifications-changed", handleNotificationsChanged);

        return () => {
            window.clearInterval(timer);
            window.removeEventListener("reader-notifications-changed", handleNotificationsChanged);
        };
    }, []);

    return (
        <button
            type="button"
            className="notification-bell"
            onClick={() => navigate("/reader/notifications")}
            title="Th\u00f4ng b\u00e1o"
        >
            <Bell size={20} />
            {count > 0 && <span className="notification-badge" aria-label="C\u00f3 th\u00f4ng b\u00e1o ch\u01b0a \u0111\u1ecdc" />}
        </button>
    );
}
