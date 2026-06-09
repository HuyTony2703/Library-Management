import {
    Bell,
    BookmarkCheck,
    CalendarClock,
    CheckCircle2,
    CircleAlert,
    CircleDollarSign
} from "lucide-react";

export default function NotificationItem({ item, onRead }) {
    const Icon = getIcon(item.maLoaiThongBao);

    return (
        <div className={`notification-item ${item.daDoc ? "is-read" : "is-unread"}`}>
            <div className="notification-icon">
                <Icon size={20} />
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

            {!item.daDoc && (
                <button
                    type="button"
                    className="reader-secondary-button"
                    onClick={() => onRead(item.maThongBao)}
                >
                    Đã đọc
                </button>
            )}
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

    return new Date(value).toLocaleString("vi-VN");
}
