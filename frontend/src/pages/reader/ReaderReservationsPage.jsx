import { RefreshCcw } from "lucide-react";
import { useEffect, useState } from "react";
import { readerApi } from "../../api/readerApi";
import ReservationHistoryTable from "../../components/reader/ReservationHistoryTable";
import { useToast } from "../../components/ToastProvider";

export default function ReaderReservationsPage() {
    const toast = useToast();
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);

    async function loadReservations() {
        setLoading(true);

        try {
            const result = await readerApi.reservations();
            setData(Array.isArray(result) ? result : []);
        } catch (err) {
            toast.error(err.message || "Không tải được danh sách đặt trước");
        } finally {
            setLoading(false);
        }
    }

    async function handleCancel(maPhieuDatTruoc) {
        const ok = window.confirm("Bạn có chắc muốn hủy phiếu đặt trước này không?");

        if (!ok) {
            return;
        }

        try {
            await readerApi.cancelReservation(maPhieuDatTruoc);
            toast.success("Hủy đặt trước thành công");
            await loadReservations();
        } catch (err) {
            toast.error(err.message || "Hủy đặt trước thất bại");
        }
    }

    useEffect(() => {
        loadReservations();
    }, []);

    return (
        <div>
            <div className="reader-home-header">
                <small>Reservation</small>
                <h1>Đặt trước sách</h1>
                <p>Theo dõi các phiếu đặt trước theo đầu sách hoặc theo cuốn cụ thể.</p>
            </div>

            <div className="reader-page-actions">
                <button type="button" onClick={loadReservations}>
                    <RefreshCcw size={17} />
                    {loading ? "Đang tải..." : "Tải lại"}
                </button>
            </div>

            <ReservationHistoryTable data={data} onCancel={handleCancel} />
        </div>
    );
}
