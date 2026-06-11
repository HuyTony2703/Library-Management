import { CheckCheck, Trash2, X } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { readerApi } from "../../api/readerApi";
import { useActionDialog } from "../../components/ActionDialogProvider";
import { useToast } from "../../components/ToastProvider";
import NotificationItem from "../../components/reader/NotificationItem";

export default function ReaderNotificationsPage() {
    const toast = useToast();
    const actionDialog = useActionDialog();
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [selectedIds, setSelectedIds] = useState(() => new Set());

    async function loadNotifications() {
        setLoading(true);

        try {
            const result = await readerApi.getNotifications();
            const notifications = Array.isArray(result) ? result : [];
            setData(notifications);
            window.dispatchEvent(new CustomEvent("reader-notifications-changed", {
                detail: {
                    unreadCount: notifications.filter((item) => !item.daDoc).length
                }
            }));
        } catch (err) {
            toast.error(err.message || "Không tải được thông báo. Vui lòng thử lại.");
        } finally {
            setLoading(false);
        }
    }

    function removeFromList(ids) {
        const idSet = new Set(ids);
        setData((prev) => prev.filter((item) => !idSet.has(item.maThongBao)));
        setSelectedIds((prev) => {
            const next = new Set(prev);
            ids.forEach((id) => next.delete(id));
            return next;
        });
        window.dispatchEvent(new Event("reader-notifications-changed"));
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
            toast.success("Đã đánh dấu thông báo là đã đọc.");
        } catch (err) {
            toast.error(err.message || "Không thể đánh dấu đã đọc. Vui lòng thử lại.");
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
            toast.success("Đã đánh dấu tất cả là đã đọc.");
        } catch (err) {
            toast.error(err.message || "Không thể đánh dấu tất cả đã đọc. Vui lòng thử lại.");
        }
    }

    async function handleDelete(item) {
        const ok = await actionDialog.confirm({
            title: "Xóa thông báo?",
            message: `Bạn có chắc muốn xóa thông báo "${item.tieuDe}" không?`,
            confirmLabel: "Xóa thông báo",
            cancelLabel: "Giữ lại",
            danger: true,
            cancelPrimary: true
        });

        if (!ok) {
            return;
        }

        try {
            await readerApi.deleteNotification(item.maThongBao);
            removeFromList([item.maThongBao]);
            toast.success("Đã xóa thông báo.");
        } catch (err) {
            toast.error(err.message || "Không thể xóa thông báo. Vui lòng thử lại.");
        }
    }

    async function handleDeleteSelected() {
        const ids = [...selectedIds];

        if (ids.length === 0) {
            return;
        }

        const ok = await actionDialog.confirm({
            title: "Xóa thông báo đã chọn?",
            message: `Bạn có chắc muốn xóa ${ids.length} thông báo đã chọn không?`,
            confirmLabel: "Xóa đã chọn",
            cancelLabel: "Giữ lại",
            danger: true,
            cancelPrimary: true
        });

        if (!ok) {
            return;
        }

        try {
            await Promise.all(ids.map((id) => readerApi.deleteNotification(id)));
            removeFromList(ids);
            toast.success(`Đã xóa ${ids.length} thông báo.`);
        } catch (err) {
            toast.error(err.message || "Không thể xóa các thông báo đã chọn. Vui lòng thử lại.");
        }
    }

    function handleSelect(maThongBao, checked) {
        setSelectedIds((prev) => {
            const next = new Set(prev);

            if (checked) {
                next.add(maThongBao);
            } else {
                next.delete(maThongBao);
            }

            return next;
        });
    }

    function handleSelectAll(checked) {
        setSelectedIds(checked ? new Set(data.map((item) => item.maThongBao)) : new Set());
    }

    useEffect(() => {
        loadNotifications();
    }, []);

    const unreadCount = data.filter((item) => !item.daDoc).length;
    const allSelected = data.length > 0 && selectedIds.size === data.length;
    const selectedCount = selectedIds.size;

    const selectionLabel = useMemo(() => {
        return selectedCount > 0 ? `Đã chọn ${selectedCount} thông báo` : "Chưa chọn thông báo";
    }, [selectedCount]);

    return (
        <div>
            <div className="reader-home-header">
                <small>THÔNG BÁO</small>
                <h1>Thông báo</h1>
                <p>Bạn có {unreadCount} thông báo chưa đọc.</p>
            </div>

            <div className="notification-toolbar">
                <button
                    type="button"
                    className={`notification-select-all-button ${allSelected ? "is-selected" : ""}`}
                    disabled={data.length === 0}
                    onClick={() => handleSelectAll(!allSelected)}
                >
                    {allSelected ? "Bỏ chọn tất cả" : "Chọn tất cả"}
                </button>

                <button type="button" className="reader-secondary-button" onClick={handleReadAll} disabled={unreadCount === 0}>
                    <CheckCheck size={17} />
                    Đánh dấu tất cả là đã đọc
                </button>
            </div>

            {selectedCount > 0 && (
                <div className="notification-bulk-bar">
                    <b>{selectionLabel}</b>
                    <div>
                        <button type="button" className="reader-secondary-button" onClick={() => setSelectedIds(new Set())}>
                            <X size={16} />
                            Bỏ chọn
                        </button>
                        <button type="button" className="reader-danger-button" onClick={handleDeleteSelected}>
                            <Trash2 size={16} />
                            Xóa đã chọn
                        </button>
                    </div>
                </div>
            )}

            {loading && <p>Đang tải thông báo...</p>}

            {!loading && data.length === 0 ? (
                <div className="reader-empty-box">
                    Bạn chưa có thông báo nào.
                </div>
            ) : null}

            {!loading && data.length > 0 && (
                <div className="notification-list">
                    {data.map((item) => (
                        <NotificationItem
                            key={item.maThongBao}
                            item={item}
                            selected={selectedIds.has(item.maThongBao)}
                            onSelect={handleSelect}
                            onRead={handleRead}
                            onDelete={handleDelete}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}
