import { RefreshCcw } from "lucide-react";
import { useEffect, useState } from "react";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../../components/ToastProvider";
import MembershipHistoryTable from "../../components/reader/MembershipHistoryTable";
import MembershipPlanCard from "../../components/reader/MembershipPlanCard";
import SimpleCheckoutModal from "../../components/reader/SimpleCheckoutModal";

export default function ReaderMembershipPage() {
    const toast = useToast();

    const [current, setCurrent] = useState(null);
    const [plans, setPlans] = useState([]);
    const [history, setHistory] = useState([]);
    const [selectedPlan, setSelectedPlan] = useState(null);
    const [loading, setLoading] = useState(false);

    async function loadMembershipData() {
        setLoading(true);

        try {
            const [currentData, planData, historyData] = await Promise.all([
                readerApi.getCurrentMembership(),
                readerApi.getMembershipPlans(),
                readerApi.getMembershipHistory()
            ]);

            setCurrent(currentData);
            setPlans(Array.isArray(planData) ? planData : []);
            setHistory(Array.isArray(historyData) ? historyData : []);
        } catch (err) {
            toast.error(err.message || "Không tải được dữ liệu gói độc giả");
        } finally {
            setLoading(false);
        }
    }

    async function handlePurchaseSuccess() {
        setSelectedPlan(null);
        window.dispatchEvent(new Event("reader-notifications-changed"));
        await loadMembershipData();
    }

    useEffect(() => {
        loadMembershipData();
    }, []);

    return (
        <div>
            <div className="reader-home-header">
                <small>Membership</small>
                <h1>Gói độc giả</h1>
                <p>Xem gói hiện tại, mua hoặc gia hạn gói thành viên của bạn.</p>
            </div>

            <div className="reader-page-actions">
                <button type="button" onClick={loadMembershipData}>
                    <RefreshCcw size={17} />
                    {loading ? "Đang tải..." : "Tải lại"}
                </button>
            </div>

            <section className="current-membership-box">
                <h2>Gói hiện tại</h2>

                {current ? (
                    <div className="current-membership-content">
                        <div>
                            <p className="reader-muted">Tên gói</p>
                            <h3>{current.tenGoi}</h3>
                        </div>

                        <div>
                            <p className="reader-muted">Ngày bắt đầu</p>
                            <b>{formatDate(current.ngayBatDau)}</b>
                        </div>

                        <div>
                            <p className="reader-muted">Ngày kết thúc</p>
                            <b>{formatDate(current.ngayKetThuc)}</b>
                        </div>

                        <div>
                            <p className="reader-muted">Trạng thái</p>
                            <b>{current.trangThai}</b>
                        </div>
                    </div>
                ) : (
                    <div className="reader-empty-box">
                        Bạn chưa có gói thành viên đang sử dụng.
                    </div>
                )}
            </section>

            <section className="reader-section-block">
                <h2>Các gói có thể mua</h2>

                {plans.length === 0 ? (
                    <div className="reader-empty-box">
                        Chưa có gói phù hợp với nhóm độc giả của bạn.
                    </div>
                ) : (
                    <div className="membership-grid">
                        {plans.map((plan) => (
                            <MembershipPlanCard
                                key={plan.maGoiThanhVien}
                                plan={plan}
                                onPurchase={setSelectedPlan}
                            />
                        ))}
                    </div>
                )}
            </section>

            <section className="reader-section-block">
                <h2>Lịch sử gói</h2>
                <MembershipHistoryTable data={history} />
            </section>

            {selectedPlan && (
                <SimpleCheckoutModal
                    plan={selectedPlan}
                    onClose={() => setSelectedPlan(null)}
                    onSuccess={handlePurchaseSuccess}
                />
            )}
        </div>
    );
}

function formatDate(value) {
    if (!value) {
        return "";
    }

    return new Date(value).toLocaleDateString("vi-VN");
}
