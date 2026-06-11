import { CheckCircle2, Crown, LockKeyhole } from "lucide-react";

function getPlanBenefits(plan) {
    const name = String(plan.tenGoi || "").toLowerCase();
    const maxBooks = plan.soSachMuonToiDa || plan.soSachToiDa || (name.includes("premium") ? 12 : name.includes("vip") ? 8 : 5);
    const maxRenewals = plan.soLanGiaHanToiDa || (name.includes("premium") ? 3 : name.includes("vip") ? 2 : 1);
    const fit = name.includes("premium")
        ? "Phù hợp với độc giả có nhu cầu mượn sách thường xuyên hơn."
        : name.includes("vip")
          ? "Phù hợp với độc giả mượn sách thường xuyên."
          : "Gói mặc định cho nhu cầu đọc cơ bản.";

    return [
        `Mượn tối đa ${maxBooks} sách`,
        `Gia hạn tối đa ${maxRenewals} lần`,
        fit
    ];
}

export default function MembershipPlanCard({ plan, actionState, onPurchase }) {
    const isCurrent = actionState === "current";
    const isDisabled = actionState === "base" || actionState === "lower" || actionState === "unavailable";
    const canUpgrade = actionState === "upgrade";

    return (
        <div className={`membership-card ${isCurrent ? "is-current" : ""} ${isDisabled ? "is-disabled" : ""}`}>
            <div className="membership-card-header">
                <div className="membership-icon">
                    {isDisabled && !isCurrent ? <LockKeyhole size={22} /> : <Crown size={22} />}
                </div>

                {isCurrent && (
                    <span className="current-plan-pill">
                        <CheckCircle2 size={14} />
                        Đang sử dụng
                    </span>
                )}
            </div>

            <h3>{plan.tenGoi}</h3>
            <p>{plan.moTa || "Gói độc giả thư viện"}</p>

            <div className="membership-price">
                {formatCurrency(plan.giaTien)}
                <span> / {plan.thoiHanGoiTheoNgay || 180} ngày</span>
            </div>

            <ul className="membership-benefits">
                {getPlanBenefits(plan).map((benefit) => (
                    <li key={benefit}>{benefit}</li>
                ))}
            </ul>

            {canUpgrade && (
                <button
                    type="button"
                    className="reader-primary-button"
                    onClick={() => onPurchase(plan)}
                >
                    Nâng cấp
                </button>
            )}

            {actionState === "current" && (
                <span className="membership-state-label is-current">Đang sử dụng</span>
            )}

            {actionState === "base" && (
                <span className="membership-state-label">Gói mặc định</span>
            )}

            {actionState === "lower" && (
                <span className="membership-state-label">Không thể quay lại gói thấp hơn</span>
            )}

            {actionState === "unavailable" && (
                <span className="membership-state-label">Không khả dụng</span>
            )}
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
