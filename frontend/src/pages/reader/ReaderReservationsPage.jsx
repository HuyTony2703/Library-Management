import { useEffect, useState } from "react";
import { readerApi } from "../../api/readerApi";
import { useActionDialog } from "../../components/ActionDialogProvider";
import { useToast } from "../../components/ToastProvider";
import ReservationHistoryTable from "../../components/reader/ReservationHistoryTable";

export default function ReaderReservationsPage() {
    const toast = useToast();
    const actionDialog = useActionDialog();
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);

    async function loadReservations() {
        setLoading(true);

        try {
            const result = await readerApi.reservations();
            setData(Array.isArray(result) ? result : []);
        } catch (err) {
            toast.error(err.message || "Không tải được danh sách đặt trước. Vui lòng thử lại.");
        } finally {
            setLoading(false);
        }
    }

    async function handleCancel(item) {
        const ok = await actionDialog.confirm({
            title: "Hủy đặt trước?",
            message: `Bạn có chắc muốn hủy đặt trước cho "${item.tenDauSach}"? Nếu hủy, bạn có thể mất vị trí hiện tại trong hàng chờ.`,
            confirmLabel: "Xác nhận hủy",
            cancelLabel: "Giữ đặt trước",
            danger: true,
            cancelPrimary: true
        });

        if (!ok) {
            return;
        }

        try {
            await readerApi.cancelReservation(item.maPhieuDatTruoc);
            toast.success(`Phiếu đặt trước ${item.maPhieuDatTruoc} đã được hủy.`);
            await loadReservations();
        } catch (err) {
            toast.error(
                err.message ||
                "Không thể hủy đặt trước. Phiếu có thể đã được xử lý, vui lòng tải lại dữ liệu."
            );
        }
    }

    useEffect(() => {
        loadReservations();
    }, []);

    return (
        <div>
            <div className="reader-home-header">
                <small>ĐẶT TRƯỚC</small>
                <h1>Đặt trước sách</h1>
                <p>Theo dõi sách đang chờ, sách đã được giữ và thời hạn nhận sách.</p>
            </div>

            <ReservationHistoryTable data={data} onCancel={handleCancel} />
        </div>
    );
}
