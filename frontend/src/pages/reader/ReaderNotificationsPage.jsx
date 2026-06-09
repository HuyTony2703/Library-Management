import { CheckCheck, RefreshCcw } from "lucide-react";
import { useEffect, useState } from "react";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../../components/ToastProvider";
import NotificationItem from "../../components/reader/NotificationItem";

export default function ReaderNotificationsPage() {
    const toast = useToast();
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);

    async function loadNotifications() {
        setLoading(true);

        try {
            const result = await readerApi.getNotifications();
            setData(Array.isArray(result) ? result : []);
        } catch (err) {
            toast.error(err.message || "Không tải được thông báo");
        } finally {
            setLoading(false);
        }
    }

    async function handleRead(maThongBao) {
        try {
            await readerApi.markNotificationAsRead(maThongBao);

            setData((prev) =>
                prev.map((item) =>
                    item.maThongBao === maThongBao
                        ? {
                              ...item,
                              daDoc: true,
                              thoiGianDoc: new Date().toISOString()
                          }
                        : item
                )
            );

            window.dispatchEvent(new Event("reader-notifications-changed"));
            toast.success("Đã đánh dấu là đã đọc");
        } catch (err) {
            toast.error(err.message || "Không thể đánh dấu đã đọc");
        }
    }

    async function handleReadAll() {
        try {
            await readerApi.markAllNotificationsAsRead();

            setData((prev) =>
                prev.map((item) => ({
                    ...item,
                    daDoc: true,
                    thoiGianDoc: item.thoiGianDoc || new Date().toISOString()
                }))
            );

            window.dispatchEvent(new Event("reader-notifications-changed"));
            toast.success("Đã đánh dấu tất cả là đã đọc");
        } catch (err) {
            toast.error(err.message || "Không thể đánh dấu tất cả đã đọc");
        }
    }

    useEffect(() => {
        loadNotifications();
    }, []);

    const unreadCount = data.filter((item) => !item.daDoc).length;

    return (
        <div>
            <div className="reader-home-header">
                <small>Notifications</small>
                <h1>Thông báo</h1>
                <p>Bạn có {unreadCount} thông báo chưa đọc.</p>
            </div>

            <div className="reader-page-actions">
                <button type="button" onClick={loadNotifications}>
                    <RefreshCcw size={17} />
                    {loading ? "Đang tải..." : "Tải lại"}
                </button>

                <button type="button" onClick={handleReadAll} disabled={unreadCount === 0}>
                    <CheckCheck size={17} />
                    Đọc tất cả
                </button>
            </div>

            {data.length === 0 ? (
                <div className="reader-empty-box">
                    Bạn chưa có thông báo nào.
                </div>
            ) : (
                <div className="notification-list">
                    {data.map((item) => (
                        <NotificationItem
                            key={item.maThongBao}
                            item={item}
                            onRead={handleRead}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}
