import { Calculator, RefreshCcw } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../../components/ToastProvider";

export default function PenaltyRulesPage() {
    const toast = useToast();
    const [rules, setRules] = useState(null);
    const [soNgayTre, setSoNgayTre] = useState(3);
    const [loading, setLoading] = useState(false);

    async function loadRules() {
        setLoading(true);

        try {
            const result = await readerApi.getCurrentRules();
            setRules(result);
        } catch (err) {
            toast.error(err.message || "Không tải được quy định phạt");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadRules();
    }, []);

    const tienPhat = useMemo(() => {
        const mucPhat = Number(rules?.mucPhatTreMoiNgay || 1000);
        const ngayTre = Number(soNgayTre || 0);
        return Math.max(0, ngayTre) * mucPhat;
    }, [rules, soNgayTre]);

    return (
        <div>
            <div className="reader-home-header">
                <small>Penalty</small>
                <h1>Quy định phạt và cách tính</h1>
                <p>Giải thích cách tính tiền phạt khi trả sách trễ, sách hỏng hoặc mất.</p>
            </div>

            <div className="reader-page-actions">
                <button type="button" onClick={loadRules}>
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
                        <b>{formatCurrency(rules?.mucPhatTreMoiNgay || 1000)} / ngày</b>
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
                            type="number"
                            min="0"
                            value={soNgayTre}
                            onChange={(e) => setSoNgayTre(e.target.value)}
                        />
                    </label>

                    <div className="penalty-result">
                        <span>Số tiền phạt dự kiến</span>
                        <strong>{formatCurrency(tienPhat)}</strong>
                    </div>
                </div>
            </section>

            <section className="reader-section">
                <div className="section-title-row">
                    <div>
                        <p className="reader-eyebrow">Damaged or lost books</p>
                        <h2>Quy định sách hỏng hoặc mất</h2>
                    </div>
                </div>

                <div className="rules-text-list">
                    <p>
                        Nếu sách bị hỏng hoặc mất, thủ thư sẽ kiểm tra tình trạng thực tế và
                        ghi nhận khoản phạt tương ứng.
                    </p>
                    <p>
                        Tiền phạt có thể dựa trên trị giá sách, mức độ hư hại và quy định xử lý
                        của thư viện.
                    </p>
                </div>
            </section>

            <section className="reader-section">
                <div className="section-title-row">
                    <div>
                        <p className="reader-eyebrow">Notifications</p>
                        <h2>Quy định thông báo</h2>
                    </div>
                </div>

                <div className="rules-text-list">
                    <p>
                        Hệ thống có thể thông báo trước khi sách đến hạn trả khoảng{" "}
                        <b>{rules?.soNgayNhacTruocHan || 3} ngày</b>.
                    </p>
                    <p>
                        Độc giả cũng có thể nhận thông báo khi gia hạn thành công, đặt trước
                        thành công, phát sinh khoản phạt hoặc gói thành viên sắp hết hạn.
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
