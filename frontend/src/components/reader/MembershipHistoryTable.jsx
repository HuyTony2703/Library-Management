export default function MembershipHistoryTable({ data = [] }) {
    if (!data.length) {
        return (
            <div className="reader-empty-box">
                Bạn chưa có lịch sử mua gói.
            </div>
        );
    }

    return (
        <div className="reader-table-card">
            <table className="reader-table">
                <thead>
                    <tr>
                        <th>Mã lịch sử</th>
                        <th>Gói</th>
                        <th>Phiếu thu</th>
                        <th>Số tiền</th>
                        <th>Ngày bắt đầu</th>
                        <th>Ngày kết thúc</th>
                        <th>Trạng thái</th>
                    </tr>
                </thead>

                <tbody>
                    {data.map((item) => (
                        <tr key={item.maLichSuGoi}>
                            <td>{item.maLichSuGoi}</td>
                            <td>
                                <b>{item.tenGoi}</b>
                                <div className="reader-muted">{item.maGoiThanhVien}</div>
                            </td>
                            <td>{item.maPhieuThu || "Miễn phí"}</td>
                            <td>{formatCurrency(item.soTienThu)}</td>
                            <td>{formatDate(item.ngayBatDau)}</td>
                            <td>{formatDate(item.ngayKetThuc)}</td>
                            <td>
                                <span className={`copy-status ${getStatusClass(item.trangThai)}`}>
                                    {item.trangThai || "Không rõ"}
                                </span>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}

function formatCurrency(value) {
    const number = Number(value || 0);

    return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND"
    }).format(number);
}

function formatDate(value) {
    if (!value) {
        return "";
    }

    return new Date(value).toLocaleDateString("vi-VN");
}

function getStatusClass(status = "") {
    if (status.includes("Đang")) {
        return "copy-status-good";
    }

    if (status.includes("hạn") || status.includes("hủy")) {
        return "copy-status-bad";
    }

    if (status.includes("nâng cấp")) {
        return "copy-status-warn";
    }

    return "copy-status-neutral";
}
