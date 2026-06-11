import { useEffect, useMemo, useState } from "react";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../../components/ToastProvider";
import MembershipHistoryTable from "../../components/reader/MembershipHistoryTable";
import MembershipPlanCard from "../../components/reader/MembershipPlanCard";
import SimpleCheckoutModal from "../../components/reader/SimpleCheckoutModal";
import { notifyReaderNotificationsChanged } from "../../utils/notificationEvents";

const PLAN_LEVEL = {
    GOI_THUONG: 1,
    GOI_VIP: 2,
    GOI_PREMIUM: 3
};

const PLAN_ORDER = ["GOI_THUONG", "GOI_VIP", "GOI_PREMIUM"];

const PREMIUM_FALLBACK = {
    maGoiThanhVien: "GOI_PREMIUM",
    tenGoi: "Premium",
    moTa: "G\u00f3i \u0111\u1ed9c gi\u1ea3 cao c\u1ea5p",
    trangThai: "Ho\u1ea1t \u0111\u1ed9ng",
    giaTien: 100000,
    thoiHanGoiTheoNgay: 180,
    goiHienTai: false
};


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
            toast.error(err.message || "Không tải được dữ liệu gói độc giả. Vui lòng thử lại.");
        } finally {
            setLoading(false);
        }
    }

    async function handlePurchaseSuccess() {
        setSelectedPlan(null);
        notifyReaderNotificationsChanged();
        await loadMembershipData();
    }

    useEffect(() => {
        loadMembershipData();
    }, []);

    const visiblePlans = useMemo(() => {
        const byId = new Map(plans.map((plan) => [plan.maGoiThanhVien, plan]));

        if (!byId.has("GOI_PREMIUM")) {
            byId.set("GOI_PREMIUM", PREMIUM_FALLBACK);
        }

        return [...byId.values()].sort((a, b) => {
            return (PLAN_LEVEL[a.maGoiThanhVien] || 99) - (PLAN_LEVEL[b.maGoiThanhVien] || 99);
        });
    }, [plans]);

    const currentPlanId =
        current?.maGoiThanhVien ||
        visiblePlans.find((plan) => plan.goiHienTai)?.maGoiThanhVien ||
        "GOI_THUONG";
    const currentLevel = PLAN_LEVEL[currentPlanId] || 1;

    function getActionState(plan) {
        const level = PLAN_LEVEL[plan.maGoiThanhVien] || 0;

        if (plan.maGoiThanhVien === currentPlanId || plan.goiHienTai) {
            return "current";
        }

        if (plan.maGoiThanhVien === "GOI_THUONG") {
            return "base";
        }

        if (level <= currentLevel) {
            return "lower";
        }

        if (!PLAN_ORDER.includes(plan.maGoiThanhVien)) {
            return "unavailable";
        }

        return "upgrade";
    }

    return (
        <div>
            <div className="reader-home-header">
                <small>GÓI ĐỘC GIẢ</small>
                <h1>Gói độc giả</h1>
                <p>Gói Thường là mặc định. Bạn chỉ có thể nâng cấp lên VIP hoặc Premium.</p>
            </div>

            <section className="current-membership-box">
                <h2>Gói đang sử dụng</h2>

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
                            <b>{current.trangThai === "Đang dùng" ? "Đang sử dụng" : current.trangThai}</b>
                        </div>
                    </div>
                ) : (
                    <div className="reader-empty-box">
                        Bạn đang dùng gói Thường mặc định.
                    </div>
                )}
            </section>

            <section className="reader-section-block">
                <h2>Các gói độc giả</h2>

                {loading && <p>Đang tải gói độc giả...</p>}

                {!loading && visiblePlans.length === 0 ? (
                    <div className="reader-empty-box">
                        Chưa có gói phù hợp với nhóm độc giả của bạn.
                    </div>
                ) : (
                    <div className="membership-grid">
                        {visiblePlans.map((plan) => (
                            <MembershipPlanCard
                                key={plan.maGoiThanhVien}
                                plan={plan}
                                actionState={getActionState(plan)}
                                onPurchase={setSelectedPlan}
                            />
                        ))}
                    </div>
                )}
            </section>

            <section className="reader-section-block">
                <h2>Lịch sử gói độc giả</h2>
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
        return "Chưa cập nhật";
    }

    return new Intl.DateTimeFormat("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric"
    }).format(new Date(value));
}
