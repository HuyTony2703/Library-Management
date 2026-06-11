function formatDateTime(value) {
    if (!value) {
        return "Chưa có";
    }

    return new Intl.DateTimeFormat("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    }).format(new Date(value));
}

function normalizeReservationStatus(status) {
    const value = status || "Chưa cập nhật";

    if (value === "Đã giữ chỗ") {
        return "Sách đã được giữ";
    }

    if (value === "Chưa giữ chỗ") {
        return "Chưa có sách sẵn";
    }

    return value;
}

function getReservationStatusClass(status) {
    if (status === "Đã giữ chỗ") {
        return "copy-status-good";
    }

    if (status === "Đang chờ") {
        return "copy-status-warn";
    }

    if (status === "Đã hủy" || status === "Đã hết hạn") {
        return "copy-status-bad";
    }

    return "copy-status-neutral";
}

export default function ReservationHistoryTable({ data = [], onCancel }) {
    if (!data.length) {
        return (
            <div className="reader-empty-box">
                Bạn chưa có phiếu đặt trước nào.
            </div>
        );
    }

    return (
        <div className="reader-table-card">
            <table className="reader-table">
                <thead>
                    <tr>
                        <th>Mã phiếu</th>
                        <th>Đầu sách</th>
                        <th>Sách được giữ</th>
                        <th>Ngày đặt</th>
                        <th>Hết hạn giữ</th>
                        <th>Trạng thái</th>
                        <th></th>
                    </tr>
                </thead>

                <tbody>
                    {data.map((item) => {
                        const canCancel =
                            item.trangThai === "Đang chờ" ||
                            item.trangThai === "Đã giữ chỗ";

                        return (
                            <tr key={item.maPhieuDatTruoc}>
                                <td>{item.maPhieuDatTruoc}</td>
                                <td>
                                    <b>{item.tenDauSach}</b>
                                    <div className="reader-muted">{item.maDauSach}</div>
                                    {item.viTriHangDoi && (
                                        <div className="reader-muted">Vị trí chờ: #{item.viTriHangDoi}</div>
                                    )}
                                </td>
                                <td>{item.maCuonSachDuocGiu || "Đặt trước theo đầu sách"}</td>
                                <td>{formatDateTime(item.ngayDat)}</td>
                                <td>
                                    {item.ngayHetHanGiuCho
                                        ? formatDateTime(item.ngayHetHanGiuCho)
                                        : "Chưa có sách sẵn"}
                                </td>
                                <td>
                                    <span className={`copy-status ${getReservationStatusClass(item.trangThai)}`}>
                                        {normalizeReservationStatus(item.trangThai)}
                                    </span>
                                </td>
                                <td>
                                    {canCancel && (
                                        <button
                                            type="button"
                                            className="reader-danger-button"
                                            onClick={() => onCancel(item)}
                                        >
                                            Hủy đặt trước
                                        </button>
                                    )}
                                </td>
                            </tr>
                        );
                    })}
                </tbody>
            </table>
        </div>
    );
}
