import {
    Bell,
    BookmarkCheck,
    CalendarClock,
    CheckCircle2,
    CircleAlert,
    CircleDollarSign,
    Trash2
} from "lucide-react";

export default function NotificationItem({ item, selected, onSelect, onRead, onDelete }) {
    const Icon = getIcon(item.maLoaiThongBao);

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

function getIcon(maLoaiThongBao) {
    switch (maLoaiThongBao) {
        case "TB_SAP_DEN_HAN":
            return CalendarClock;
        case "TB_GIA_HAN_TC":
        case "TB_MUA_GOI_TC":
            return CheckCircle2;
        case "TB_BI_PHAT":
            return CircleDollarSign;
        case "TB_DAT_TRUOC_TC":
        case "TB_SACH_DA_CO":
            return BookmarkCheck;
        case "TB_QUA_HAN":
            return CircleAlert;
        default:
            return Bell;
    }
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
