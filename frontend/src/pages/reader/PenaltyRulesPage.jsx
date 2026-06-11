import { Calculator, RefreshCcw } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../../components/ToastProvider";

const MAX_OVERDUE_DAYS = 365;

export default function PenaltyRulesPage() {
    const toast = useToast();
    const [rules, setRules] = useState(null);
    const [soNgayTre, setSoNgayTre] = useState("3");
    const [loading, setLoading] = useState(false);

    async function loadRules() {
        setLoading(true);

        try {
            const result = await readerApi.getCurrentRules();
            setRules(result);
        } catch (err) {
            toast.error(err.message || "Không tải được quy định phạt. Vui lòng thử lại.");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadRules();
    }, []);

    const validationError = useMemo(() => {
        if (soNgayTre === "") {
            return "Vui lòng nhập số ngày trả trễ.";
        }

        if (!/^\d+$/.test(soNgayTre)) {
            return "Số ngày trả trễ phải là số nguyên lớn hơn hoặc bằng 0.";
        }

        if (Number(soNgayTre) > MAX_OVERDUE_DAYS) {
            return `Số ngày trả trễ không được vượt quá ${MAX_OVERDUE_DAYS} ngày.`;
        }

        return "";
    }, [soNgayTre]);

    const mucPhat = Number(rules?.mucPhatTreMoiNgay || 1000);
    const ngayTre = validationError ? 0 : Number(soNgayTre);
    const tienPhat = ngayTre * mucPhat;

    return (
        <div>
            <div className="reader-home-header">
                <small>QUY ĐỊNH PHẠT</small>
                <h1>Quy định phạt và cách tính</h1>
                <p>Giải thích cách tính tiền phạt khi trả sách trễ, sách hỏng hoặc mất.</p>
            </div>

            <div className="reader-page-actions">
                <button type="button" onClick={loadRules} disabled={loading}>
                    <RefreshCcw size={17} />
                    {loading ? "Đang tải..." : "Tải lại quy định"}
                </button>
            </div>

            <section className="penalty-layout">
                <div className="penalty-info-card">
                    <h2>Phạt trả trễ</h2>
                    <p>
                        Khi sách được trả sau hạn trả, hệ thống tính số ngày trễ và nhân với
                        mức phạt trả trễ mỗi ngày.
                    </p>

                    <div className="formula-box">
                        Tiền phạt = Số ngày trễ x Mức phạt mỗi ngày
                    </div>

                    <div className="rule-highlight">
                        Mức phạt hiện tại:
                        <b>{formatCurrency(mucPhat)} / ngày</b>
                    </div>
                </div>

                <div className="penalty-calculator-card">
                    <div className="calculator-title">
                        <Calculator size={22} />
                        <h2>Ví dụ tính phạt</h2>
                    </div>

                    <label className="reader-form-row">
                        <span>Số ngày trả trễ</span>
                        <input
                            inputMode="numeric"
                            value={soNgayTre}
                            onChange={(e) => setSoNgayTre(e.target.value.trim())}
                            aria-invalid={Boolean(validationError)}
                        />
                    </label>

                    {validationError && <p className="reader-field-error">{validationError}</p>}

                    {!validationError && ngayTre === 0 && (
                        <p className="reader-muted">Không phát sinh tiền phạt.</p>
                    )}

                    {!validationError && (
                        <div className="penalty-result">
                            <span>
                                Tiền phạt = {ngayTre} x {formatCurrency(mucPhat)} = {formatCurrency(tienPhat)}
                            </span>
                            <strong>{formatCurrency(tienPhat)}</strong>
                        </div>
                    )}
                </div>
            </section>

            <section className="reader-section">
                <div className="section-title-row">
                    <div>
                        <p className="reader-eyebrow">SÁCH HỎNG HOẶC MẤT</p>
                        <h2>Quy định xử lý</h2>
                    </div>
                </div>

                <div className="rules-text-list">
                    <p>
                        Nếu sách bị hỏng hoặc mất, thủ thư sẽ kiểm tra tình trạng thực tế và ghi nhận
                        khoản phạt tương ứng.
                    </p>
                    <p>
                        Tiền phạt có thể dựa trên trị giá sách, mức độ hư hại và quy định xử lý của thư viện.
                    </p>
                </div>
            </section>

            <section className="reader-section">
                <div className="section-title-row">
                    <div>
                        <p className="reader-eyebrow">THÔNG BÁO</p>
                        <h2>Nhắc trước hạn trả</h2>
                    </div>
                </div>

                <div className="rules-text-list">
                    <p>
                        Hệ thống có thể thông báo trước khi sách đến hạn trả khoảng{" "}
                        <b>{rules?.soNgayNhacTruocHan || 3} ngày</b>.
                    </p>
                    <p>
                        Hãy gia hạn hoặc trả sách đúng hạn để tránh phát sinh tiền phạt.
                    </p>
                </div>
            </section>
        </div>
    );
}

function formatCurrency(value) {
    return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND"
    }).format(Number(value || 0));
}
