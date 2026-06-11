import { Link } from "react-router-dom";

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

function getDueInfo(hanTra) {
    if (!hanTra) {
        return { status: "unknown", label: "Chưa có hạn trả", detail: "" };
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const due = new Date(hanTra);
    due.setHours(0, 0, 0, 0);

    const diffDays = Math.ceil((due.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

    if (diffDays < 0) {
        return {
            status: "overdue",
            label: "Quá hạn",
            detail: `Quá hạn ${Math.abs(diffDays)} ngày`
        };
    }

    if (diffDays === 0) {
        return { status: "soon", label: "Sắp đến hạn", detail: "Đến hạn hôm nay" };
    }

    if (diffDays <= 3) {
        return { status: "soon", label: "Sắp đến hạn", detail: `Còn ${diffDays} ngày` };
    }

    return { status: "normal", label: "Còn hạn", detail: `Còn ${diffDays} ngày` };
}

function formatCurrency(value) {
    return `${Number(value || 0).toLocaleString("vi-VN")}đ`;
}

export default function ReaderLoanCard({ loan, onRenew, renewing = false }) {
    const dueInfo = getDueInfo(loan.hanTra);
    const penalty = dueInfo.status === "overdue"
        ? Number(loan.tienPhatTamTinh || loan.tienPhatHienTai || loan.tienPhat || 0)
        : Number(loan.tienPhatHienTai || loan.tienPhat || 0);

    return (
        <article className="reader-loan-card">
            <div className="reader-loan-cover">
                {loan.anhBia ? (
                    <img src={loan.anhBia} alt={loan.tenDauSach} />
                ) : (
                    <div>{loan.tenDauSach?.slice(0, 1) || "S"}</div>
                )}
            </div>

            <div className="reader-loan-body">
                <div>
                    <div className="reader-book-code">{loan.maCuonSach}</div>
                    <h3>{loan.tenDauSach}</h3>
                    <p>Mã chi tiết mượn: {loan.maChiTietMuon}</p>
                </div>

                <div className="reader-loan-info">
                    <span>Ngày mượn: {formatDateTime(loan.ngayMuon)}</span>
                    <span>Hạn trả: {formatDateTime(loan.hanTra)} · {dueInfo.detail}</span>
                    <span>Gia hạn: {loan.soLanDaGiaHan}/{loan.soLanGiaHanToiDa} lần</span>
                    <span>Chi nhánh: {loan.tenChiNhanh || loan.maChiNhanh}</span>
                    <span>{dueInfo.status === "overdue" ? "Phạt dự kiến" : "Phạt hiện tại"}: {formatCurrency(penalty)}</span>
                </div>

                <div className="reader-loan-footer">
                    <span className={`reader-due-badge due-${dueInfo.status}`}>
                        {dueInfo.label}
                    </span>

                    {dueInfo.status === "overdue" ? (
                        <Link className="reader-secondary-button" to="/reader/rules#penalty-rules">
                            Xem quy định phạt
                        </Link>
                    ) : (
                        <button
                            type="button"
                            disabled={!loan.coTheGiaHan || renewing}
                            onClick={() => onRenew(loan)}
                        >
                            {renewing ? "Đang gia hạn..." : "Gia hạn sách"}
                        </button>
                    )}
                </div>

                {!loan.coTheGiaHan && loan.lyDoKhongTheGiaHan && dueInfo.status !== "overdue" && (
                    <div className="reader-loan-warning">
                        Không thể gia hạn sách. {loan.lyDoKhongTheGiaHan}
                    </div>
                )}
            </div>
        </article>
    );
}
