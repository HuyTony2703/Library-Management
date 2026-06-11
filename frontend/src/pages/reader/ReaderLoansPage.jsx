import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../../components/ToastProvider";
import ReaderLoanCard from "../../components/reader/ReaderLoanCard";
import RenewalConfirmModal from "../../components/reader/RenewalConfirmModal";
import { notifyReaderNotificationsChanged } from "../../utils/notificationEvents";

function getDueStatus(hanTra) {
    if (!hanTra) {
        return "unknown";
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const due = new Date(hanTra);
    due.setHours(0, 0, 0, 0);
    const diffDays = Math.ceil((due.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

    if (diffDays < 0) return "overdue";
    if (diffDays <= 3) return "soon";
    return "normal";
}

export default function ReaderLoansPage() {
    const toast = useToast();

    const [loans, setLoans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [renewing, setRenewing] = useState(false);
    const [error, setError] = useState("");
    const [selectedLoan, setSelectedLoan] = useState(null);

    async function loadCurrentLoans() {
        setLoading(true);
        setError("");

        try {
            const data = await readerApi.currentLoans();
            setLoans(Array.isArray(data) ? data : []);
            if (!loading) {
                toast.info("Dữ liệu sách đang mượn đã được cập nhật.");
            }
        } catch (err) {
            setError(err.message || "Không tải được sách đang mượn. Vui lòng thử lại.");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadCurrentLoans();
    }, []);

    async function handleRenew() {
        if (!selectedLoan) {
            return;
        }

        setRenewing(true);

        try {
            await readerApi.renewLoan(selectedLoan.maChiTietMuon);
            toast.success("Gia hạn sách thành công. Vui lòng kiểm tra hạn trả mới.");
            notifyReaderNotificationsChanged();
            setSelectedLoan(null);
            await loadCurrentLoans();
        } catch (err) {
            toast.error(
                err.message ||
                "Không thể gia hạn sách. Vui lòng kiểm tra số lượt gia hạn còn lại hoặc liên hệ thủ thư."
            );
        } finally {
            setRenewing(false);
        }
    }

    const summary = useMemo(() => {
        const dueSoon = loans.filter((loan) => getDueStatus(loan.hanTra) === "soon").length;
        const overdue = loans.filter((loan) => getDueStatus(loan.hanTra) === "overdue").length;
        const penalty = loans.reduce((sum, loan) => sum + Number(loan.tienPhatHienTai || loan.tienPhat || 0), 0);

        return { dueSoon, overdue, penalty };
    }, [loans]);

    return (
        <div>
            <div className="reader-home-header">
                <small>SÁCH ĐANG MƯỢN</small>
                <h1>Sách đang mượn</h1>
                <p>Xem hạn trả, số ngày còn lại và gia hạn sách khi còn lượt.</p>
            </div>

            <div className="reader-page-actions">
                <Link to="/reader/loans/renewal-history">
                    Xem lịch sử gia hạn
                </Link>
            </div>

            {!loading && loans.length > 0 && (
                <section className="reader-loan-summary">
                    <div>
                        <span>Đang mượn</span>
                        <b>{loans.length}</b>
                    </div>
                    <div>
                        <span>Sắp đến hạn</span>
                        <b>{summary.dueSoon}</b>
                    </div>
                    <div>
                        <span>Quá hạn</span>
                        <b>{summary.overdue}</b>
                    </div>
                    <div>
                        <span>Phạt hiện tại</span>
                        <b>{summary.penalty.toLocaleString("vi-VN")}đ</b>
                    </div>
                </section>
            )}

            {error && <div className="reader-error">{error}</div>}

            {loading && <p>Đang tải danh sách sách đang mượn...</p>}

            {!loading && !error && loans.length === 0 && (
                <div className="reader-empty-box">
                    Bạn hiện không có sách nào đang mượn.
                </div>
            )}

            {!loading && !error && loans.length > 0 && (
                <div className="reader-loan-grid">
                    {loans.map((loan) => (
                        <ReaderLoanCard
                            key={loan.maChiTietMuon}
                            loan={loan}
                            renewing={renewing && selectedLoan?.maChiTietMuon === loan.maChiTietMuon}
                            onRenew={setSelectedLoan}
                        />
                    ))}
                </div>
            )}

            <RenewalConfirmModal
                loan={selectedLoan}
                loading={renewing}
                onClose={() => setSelectedLoan(null)}
                onConfirm={handleRenew}
            />
        </div>
    );
}
