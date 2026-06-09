import { useState } from "react";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../ToastProvider";

export default function SimpleCheckoutModal({ plan, onClose, onSuccess }) {
    const toast = useToast();

    const [maPhuongThuc, setMaPhuongThuc] = useState("PT_TIEN_MAT");
    const [ghiChu, setGhiChu] = useState(`Mua gói ${plan.tenGoi} từ giao diện độc giả`);
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        setLoading(true);

        try {
            const result = await readerApi.purchaseMembership({
                maGoiThanhVien: plan.maGoiThanhVien,
                maPhuongThuc,
                ghiChu
            });

            toast.success("Mua gói thành công");
            onSuccess?.(result);
        } catch (err) {
            toast.error(err.message || "Mua gói thất bại");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="reader-modal-backdrop">
            <form className="reader-modal" onSubmit={handleSubmit}>
                <div className="reader-modal-header">
                    <h2>Thanh toán gói {plan.tenGoi}</h2>

                    <button type="button" className="reader-secondary-button" onClick={onClose}>
                        Đóng
                    </button>
                </div>

                <div className="checkout-summary">
                    <div>
                        <span>Gói</span>
                        <b>{plan.tenGoi}</b>
                    </div>

                    <div>
                        <span>Thời hạn</span>
                        <b>{plan.thoiHanGoiTheoNgay} ngày</b>
                    </div>

                    <div>
                        <span>Số tiền</span>
                        <b>{formatCurrency(plan.giaTien)}</b>
                    </div>
                </div>

                <label className="reader-form-row">
                    <span>Phương thức thanh toán</span>
                    <select
                        value={maPhuongThuc}
                        onChange={(e) => setMaPhuongThuc(e.target.value)}
                    >
                        <option value="PT_TIEN_MAT">Tiền mặt</option>
                        <option value="PT_CHUYEN_KHOAN">Chuyển khoản</option>
                        <option value="PT_VI_DIEN_TU">Ví điện tử</option>
                    </select>
                </label>

                <label className="reader-form-row">
                    <span>Ghi chú</span>
                    <textarea
                        value={ghiChu}
                        maxLength={255}
                        onChange={(e) => setGhiChu(e.target.value)}
                    />
                </label>

                <div className="reader-modal-actions">
                    <button type="submit" disabled={loading}>
                        {loading ? "Đang xử lý..." : "Xác nhận thanh toán"}
                    </button>
                </div>
            </form>
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
