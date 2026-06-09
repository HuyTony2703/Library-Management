function formatDateTime(value) {
    if (!value) {
        return "Chưa có";
    }

    return new Date(value).toLocaleString("vi-VN");
}

function getReservationStatusClass(status) {
    if (status === "Đang chờ" || status === "Đã giữ chỗ") {
        return "copy-status-good";
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
                        <th>Cuốn giữ chỗ</th>
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
                                </td>
                                <td>{item.maCuonSachDuocGiu || "Theo đầu sách"}</td>
                                <td>{formatDateTime(item.ngayDat)}</td>
                                <td>
                                    {item.ngayHetHanGiuCho
                                        ? formatDateTime(item.ngayHetHanGiuCho)
                                        : "Chưa giữ chỗ"}
                                </td>
                                <td>
                                    <span className={`copy-status ${getReservationStatusClass(item.trangThai)}`}>
                                        {item.trangThai}
                                    </span>
                                </td>
                                <td>
                                    {canCancel && (
                                        <button
                                            type="button"
                                            className="reader-secondary-button"
                                            onClick={() => onCancel(item.maPhieuDatTruoc)}
                                        >
                                            Hủy
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

