function formatDateTime(value) {
    if (!value) {
        return "Chưa có";
    }

    return new Date(value).toLocaleString("vi-VN");
}

function getDueStatus(hanTra) {
    if (!hanTra) {
        return "unknown";
    }

    const now = new Date();
    const due = new Date(hanTra);
    const diffMs = due.getTime() - now.getTime();
    const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays < 0) {
        return "overdue";
    }

    if (diffDays <= 2) {
        return "soon";
    }

    return "normal";
}

export default function ReaderLoanCard({ loan, onRenew }) {
    const dueStatus = getDueStatus(loan.hanTra);

    const dueText =
        dueStatus === "overdue"
            ? "Đã quá hạn"
            : dueStatus === "soon"
                ? "Sắp đến hạn"
                : "Còn hạn";

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
                    <span>Hạn trả: {formatDateTime(loan.hanTra)}</span>
                    <span>Chi nhánh: {loan.tenChiNhanh || loan.maChiNhanh}</span>
                    <span>
                        Gia hạn: {loan.soLanDaGiaHan}/{loan.soLanGiaHanToiDa} lần
                    </span>
                </div>

                <div className="reader-loan-footer">
                    <span className={`reader-due-badge due-${dueStatus}`}>
                        {dueText}
                    </span>

                    <button
                        type="button"
                        disabled={!loan.coTheGiaHan}
                        onClick={() => onRenew(loan)}
                    >
                        Gia hạn
                    </button>
                </div>

                {!loan.coTheGiaHan && loan.lyDoKhongTheGiaHan && (
                    <div className="reader-loan-warning">
                        {loan.lyDoKhongTheGiaHan}
                    </div>
                )}
            </div>
        </article>
    );
}

