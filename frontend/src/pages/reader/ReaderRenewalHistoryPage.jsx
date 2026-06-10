import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { readerApi } from "../../api/readerApi";

function formatDateTime(value) {
    if (!value) {
        return "Chưa có";
    }

    return new Date(value).toLocaleString("vi-VN");
}

export default function ReaderRenewalHistoryPage() {
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    async function loadHistory() {
        setLoading(true);
        setError("");

        try {
            const data = await readerApi.renewalHistory();
            setHistory(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(err.message || "Không tải được lịch sử gia hạn");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadHistory();
    }, []);

    return (
        <div>
            <div className="reader-home-header">
                <small>Renewal History</small>
                <h1>Lịch sử gia hạn</h1>
                <p>Theo dõi các lần gia hạn sách của bạn.</p>
            </div>

            <div className="reader-page-actions">
                <Link to="/reader/loans">Quay lại sách đang mượn</Link>
                <button type="button" onClick={loadHistory}>
                    Tải lại
                </button>
            </div>

            {error && <div className="reader-error">{error}</div>}

            {loading && <p>Đang tải lịch sử gia hạn...</p>}

            {!loading && !error && history.length === 0 && (
                <div className="reader-empty-box">
                    Bạn chưa có lịch sử gia hạn nào.
                </div>
            )}

            {!loading && !error && history.length > 0 && (
                <div className="reader-table-card">
                    <table className="reader-table">
                        <thead>
                            <tr>
                                <th>Mã gia hạn</th>
                                <th>Sách</th>
                                <th>Ngày gia hạn</th>
                                <th>Hạn cũ</th>
                                <th>Hạn mới</th>
                                <th>Lần</th>
                                <th>Trạng thái</th>
                            </tr>
                        </thead>

                        <tbody>
                            {history.map((item) => (
                                <tr key={item.maGiaHan}>
                                    <td>{item.maGiaHan}</td>
                                    <td>{item.tenDauSach}</td>
                                    <td>{formatDateTime(item.ngayGiaHan)}</td>
                                    <td>{formatDateTime(item.hanTraCu)}</td>
                                    <td>{formatDateTime(item.hanTraMoi)}</td>
                                    <td>{item.lanGiaHanThu}</td>
                                    <td>{item.trangThai}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
