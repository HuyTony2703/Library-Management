function formatDateTime(value) {
    if (!value) {
        return "Chưa có";
    }

    return new Date(value).toLocaleString("vi-VN");
}

export default function RenewalConfirmModal({
    loan,
    loading,
    onClose,
    onConfirm
}) {
    if (!loan) {
        return null;
    }

    const hanTraCu = loan.hanTra;
    const hanTraMoi = new Date(loan.hanTra);
    hanTraMoi.setDate(hanTraMoi.getDate() + Number(loan.soNgayGiaHanMoiLan || 0));

    return (
        <div className="reader-modal-backdrop">
            <div className="reader-modal">
                <h2>Xác nhận gia hạn</h2>

                <p>
                    Bạn đang gia hạn sách <b>{loan.tenDauSach}</b>.
                </p>

                <div className="reader-modal-info">
                    <div>
                        <span>Hạn trả hiện tại</span>
                        <b>{formatDateTime(hanTraCu)}</b>
                    </div>

                    <div>
                        <span>Số ngày gia hạn</span>
                        <b>{loan.soNgayGiaHanMoiLan} ngày</b>
                    </div>

                    <div>
                        <span>Hạn trả dự kiến</span>
                        <b>{formatDateTime(hanTraMoi)}</b>
                    </div>
                </div>

                <div className="reader-modal-actions">
                    <button type="button" className="reader-secondary-button" onClick={onClose}>
                        Hủy
                    </button>

                    <button type="button" disabled={loading} onClick={onConfirm}>
                        {loading ? "Đang gia hạn..." : "Xác nhận gia hạn"}
                    </button>
                </div>
            </div>
        </div>
    );
}

