import {
    Bell,
    BookmarkCheck,
    CalendarClock,
    CheckCircle2,
    CircleAlert,
    CircleDollarSign,
    Trash2
} from "lucide-react";

const NOTIFICATION_ICONS = {
    TB_SAP_DEN_HAN: CalendarClock,
    TB_GIA_HAN_TC: CheckCircle2,
    TB_MUA_GOI_TC: CheckCircle2,
    TB_BI_PHAT: CircleDollarSign,
    TB_DAT_TRUOC_TC: BookmarkCheck,
    TB_SACH_DA_CO: BookmarkCheck,
    TB_QUA_HAN: CircleAlert
};

export default function NotificationItem({ item, selected, onSelect, onRead, onDelete }) {
    const Icon = NOTIFICATION_ICONS[item.maLoaiThongBao] ?? Bell;

    return (
        <div className={`notification-item ${item.daDoc ? "is-read" : "is-unread"}`}>
            <label className="notification-select">
                <input
                    type="checkbox"
                    checked={selected}
                    onChange={(event) => onSelect(item.maThongBao, event.target.checked)}
                    aria-label={`Chọn thông báo ${item.tieuDe}`}
                />
            </label>

            <div className="notification-icon">
                <Icon size={26} strokeWidth={2.2} />
            </div>

            <div className="notification-content">
                <div className="notification-title-row">
                    <h3>{item.tieuDe}</h3>
                    {!item.daDoc && <span className="unread-pill">Mới</span>}
                </div>

                <p>{item.noiDung}</p>

                <div className="notification-meta">
                    <span>{item.tenLoaiThongBao || item.maLoaiThongBao}</span>
                    <span>{formatDateTime(item.ngayTao)}</span>
                </div>
            </div>

            <div className="notification-actions">
                {!item.daDoc && (
                    <button
                        type="button"
                        className="reader-secondary-button"
                        onClick={() => onRead(item.maThongBao)}
                    >
                        Đánh dấu đã đọc
                    </button>
                )}

                <button
                    type="button"
                    className="reader-danger-button"
                    onClick={() => onDelete(item)}
                    title="Xóa thông báo"
                >
                    <Trash2 size={16} />
                    Xóa
                </button>
            </div>
        </div>
    );
}

function formatDateTime(value) {
    if (!value) {
        return "";
    }

    return new Intl.DateTimeFormat("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    }).format(new Date(value));
}
