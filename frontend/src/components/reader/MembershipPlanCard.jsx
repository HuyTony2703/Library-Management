import { CheckCircle2, Crown } from "lucide-react";

export default function MembershipPlanCard({ plan, onPurchase }) {
    return (
        <div className={`membership-card ${plan.goiHienTai ? "is-current" : ""}`}>
            <div className="membership-card-header">
                <div className="membership-icon">
                    <Crown size={22} />
                </div>

                {plan.goiHienTai && (
                    <span className="current-plan-pill">
                        <CheckCircle2 size={14} />
                        Đang dùng
                    </span>
                )}
            </div>

            <h3>{plan.tenGoi}</h3>
            <p>{plan.moTa || "Gói thành viên thư viện"}</p>

            <div className="membership-price">
                {formatCurrency(plan.giaTien)}
            </div>

            <div className="membership-duration">
                Thời hạn: {plan.thoiHanGoiTheoNgay} ngày
            </div>

            <button
                type="button"
                className={plan.goiHienTai ? "reader-soft-button" : "reader-primary-button"}
                onClick={() => onPurchase(plan)}
            >
                {plan.goiHienTai ? "Gia hạn gói này" : "Mua gói"}
            </button>
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
