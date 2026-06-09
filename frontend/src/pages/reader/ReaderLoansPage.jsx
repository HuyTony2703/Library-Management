import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../../components/ToastProvider";
import ReaderLoanCard from "../../components/reader/ReaderLoanCard";
import RenewalConfirmModal from "../../components/reader/RenewalConfirmModal";

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
        } catch (err) {
            setError(err.message || "Không tải được sách đang mượn");
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
            toast.success("Gia hạn sách thành công");
            setSelectedLoan(null);
            await loadCurrentLoans();
        } catch (err) {
            toast.error(err.message || "Gia hạn thất bại");
        } finally {
            setRenewing(false);
        }
    }

    return (
        <div>
            <div className="reader-home-header">
                <small>Current Loans</small>
                <h1>Sách đang mượn</h1>
                <p>Xem danh sách sách đang mượn và gia hạn khi còn lượt gia hạn.</p>
            </div>

            <div className="reader-page-actions">
                <button type="button" onClick={loadCurrentLoans}>
                    Tải lại
                </button>

                <Link to="/reader/loans/renewal-history">
                    Xem lịch sử gia hạn
                </Link>
            </div>

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

