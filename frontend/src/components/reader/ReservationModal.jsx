import { useState } from "react";
import { readerApi } from "../../api/readerApi";
import { notifyReaderNotificationsChanged } from "../../utils/notificationEvents";
import { useToast } from "../ToastProvider";

export default function ReservationModal({
    maDauSach,
    maCuonSach,
    maChiNhanh,
    onClose,
    onSuccess
}) {
    const toast = useToast();
    const [ghiChu, setGhiChu] = useState(
        maCuonSach
            ? `Đặt đúng cuốn ${maCuonSach}`
            : `Đặt trước đầu sách ${maDauSach}`
    );
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        setLoading(true);

        try {
            const payload = {
                maDauSach,
                maChiNhanh,
                ghiChu
            };

            const data = maCuonSach
                ? await readerApi.reserveByCopy({ ...payload, maCuonSach })
                : await readerApi.reserveByTitle(payload);

            toast.success("Đặt trước thành công");
            notifyReaderNotificationsChanged();
            onSuccess?.(data);
        } catch (err) {
            toast.error(err.message || "Đặt trước thất bại");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="reader-modal-backdrop">
            <form className="reader-modal" onSubmit={handleSubmit}>
                <div className="reader-modal-header">
                    <h2>Xác nhận đặt trước</h2>
                    <button type="button" className="reader-secondary-button" onClick={onClose}>
                        Đóng
                    </button>
                </div>

                <div className="reader-modal-info">
                    <div>
                        <span>Mã đầu sách</span>
                        <b>{maDauSach}</b>
                    </div>

                    {maCuonSach && (
                        <div>
                            <span>Mã cuốn sách</span>
                            <b>{maCuonSach}</b>
                        </div>
                    )}

                    <div>
                        <span>Chi nhánh</span>
                        <b>{maChiNhanh}</b>
                    </div>
                </div>

                <label className="reader-form-row">
                    <span>Ghi chú</span>
                    <textarea value={ghiChu} onChange={(e) => setGhiChu(e.target.value)} />
                </label>

                <div className="reader-modal-actions">
                    <button type="button" className="reader-secondary-button" onClick={onClose}>
                        Hủy
                    </button>

                    <button type="submit" disabled={loading}>
                        {loading ? "Đang xử lý..." : "Xác nhận đặt trước"}
                    </button>
                </div>
            </form>
        </div>
    );
}

