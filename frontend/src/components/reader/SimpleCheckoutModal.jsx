import { useState } from "react";
import { readerApi } from "../../api/readerApi";
import { notifyReaderNotificationsChanged } from "../../utils/notificationEvents";
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

            toast.success(`${plan.tenGoi} đã được kích hoạt. Vui lòng kiểm tra ngày hết hạn mới.`);
            notifyReaderNotificationsChanged();
            onSuccess?.(result);
        } catch (err) {
            toast.error(
                err.message ||
                "Không thể mua gói. Giao dịch chưa được ghi nhận, vui lòng thử lại hoặc liên hệ thủ thư."
            );
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="reader-modal-backdrop">
            <form className="reader-modal" onSubmit={handleSubmit}>
                <div className="reader-modal-header">
                    <h2>Xác nhận mua gói {plan.tenGoi}</h2>

                    <button type="button" className="reader-secondary-button" onClick={onClose} disabled={loading}>
                        Đóng
                    </button>
                </div>

                <p className="reader-muted">
                    Gói {plan.tenGoi} có giá {formatCurrency(plan.giaTien)} và hiệu lực trong {plan.thoiHanGoiTheoNgay} ngày.
                    Bạn có muốn tiếp tục giao dịch này?
                </p>

                <div className="checkout-summary">
                    <div>
                        <span>Gói độc giả</span>
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
                        disabled={loading}
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
                        disabled={loading}
                    />
                </label>

                <div className="reader-modal-actions">
                    <button type="button" className="reader-secondary-button" onClick={onClose} disabled={loading}>
                        Hủy
                    </button>
                    <button type="submit" disabled={loading}>
                        {loading ? "Đang xử lý giao dịch..." : "Xác nhận mua gói"}
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
