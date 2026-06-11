import { Calculator, ShieldCheck } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useLocation } from "react-router-dom";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../../components/ToastProvider";

const MAX_OVERDUE_DAYS = 365;

export default function ReaderRulesPage() {
    const toast = useToast();
    const location = useLocation();
    const [rules, setRules] = useState(null);
    const [loading, setLoading] = useState(false);
    const [soNgayTre, setSoNgayTre] = useState("3");

    async function loadRules() {
        setLoading(true);

        try {
            const result = await readerApi.getCurrentRules();
            setRules(result);
        } catch (err) {
            toast.error(err.message || "Không tải được quy định hiện hành. Vui lòng thử lại.");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadRules();
    }, []);

    useEffect(() => {
        if (!location.hash) {
            return;
        }

        window.requestAnimationFrame(() => {
            document.querySelector(location.hash)?.scrollIntoView({ behavior: "smooth", block: "start" });
        });
    }, [location.hash, rules]);

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
                <small>QUY ĐỊNH</small>
                <h1>Quy định thư viện</h1>
                <p>Quy định mượn sách, gói độc giả và cách tính tiền phạt đang áp dụng.</p>
            </div>

            {loading && <p>Đang tải quy định...</p>}

            {!loading && !rules ? (
                <div className="reader-empty-box">
                    Chưa có dữ liệu quy định. Hãy kiểm tra API /api/reader/rules/current.
                </div>
            ) : null}

            {rules && (
                <>
                    <section className="rules-hero">
                        <div className="rules-hero-icon">
                            <ShieldCheck size={26} />
                        </div>

                        <div>
                            <p className="reader-eyebrow">Phiên bản đang áp dụng</p>
                            <h2>{rules.tenPhienBan}</h2>
                            <p>{rules.ghiChu || "Quy định hiện tại của thư viện."}</p>
                            <span>
                                Mã phiên bản: <b>{rules.maPhienBan}</b>
                            </span>
                        </div>
                    </section>

                    <section className="rule-card-grid">
                        <RuleCard title="Độ tuổi độc giả" value={`${rules.tuoiToiThieu} - ${rules.tuoiToiDa} tuổi`} />
                        <RuleCard title="Thời hạn thẻ" value={`${rules.thoiHanTheTheoThang} tháng`} />
                        <RuleCard title="Khoảng cách năm xuất bản" value={`${rules.khoangCachNamXuatBan} năm`} />
                        <RuleCard title="Nhắc trước hạn" value={`${rules.soNgayNhacTruocHan} ngày`} />
                        <RuleCard title="Giữ đặt trước" value={`${rules.soNgayGiuDatTruoc} ngày`} />
                        <RuleCard title="Phạt trả trễ" value={`${formatCurrency(mucPhat)} / ngày`} />
                    </section>

                    <section className="reader-section">
                        <div className="section-title-row">
                            <div>
                                <p className="reader-eyebrow">GÓI ĐỘC GIẢ</p>
                                <h2>Quy định theo gói độc giả</h2>
                            </div>
                        </div>

                        <div className="reader-table-card">
                            <table className="reader-table">
                                <thead>
                                    <tr>
                                        <th>Gói</th>
                                        <th>Số sách mượn tối đa</th>
                                        <th>Số lần gia hạn tối đa</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {(rules.quyDinhGoi || []).map((item) => (
                                        <tr key={item.maGoiThanhVien}>
                                            <td>
                                                <b>{item.tenGoi}</b>
                                                <div className="reader-muted">{item.maGoiThanhVien}</div>
                                            </td>
                                            <td>{item.soSachMuonToiDa} sách</td>
                                            <td>{item.soLanGiaHanToiDa} lần</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </section>

                    <section className="reader-section">
                        <div className="section-title-row">
                            <div>
                                <p className="reader-eyebrow">MƯỢN SÁCH</p>
                                <h2>Quy định mượn theo thể loại</h2>
                            </div>
                        </div>

                        <div className="reader-table-card">
                            <table className="reader-table">
                                <thead>
                                    <tr>
                                        <th>Gói</th>
                                        <th>Thể loại</th>
                                        <th>Số ngày mượn</th>
                                        <th>Số ngày gia hạn mỗi lần</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {(rules.quyDinhMuonTheoTheLoai || []).map((item) => (
                                        <tr key={`${item.maGoiThanhVien}-${item.maTheLoai}`}>
                                            <td>{item.tenGoi}</td>
                                            <td>
                                                <b>{item.tenTheLoai}</b>
                                                <div className="reader-muted">{item.maTheLoai}</div>
                                            </td>
                                            <td>{item.soNgayMuon} ngày</td>
                                            <td>{item.soNgayGiaHanMoiLan} ngày</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </section>

                    <section className="reader-section" id="penalty-rules">
                        <div className="section-title-row">
                            <div>
                                <p className="reader-eyebrow">QUY ĐỊNH PHẠT</p>
                                <h2>Quy định phạt và cách tính</h2>
                            </div>
                        </div>

                        <div className="penalty-layout">
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
                        </div>

                        <div className="rules-text-list">
                            <p>
                                Nếu sách bị hỏng hoặc mất, thủ thư sẽ kiểm tra tình trạng thực tế và ghi nhận
                                khoản phạt tương ứng.
                            </p>
                            <p>
                                Hệ thống có thể thông báo trước hạn trả khoảng <b>{rules.soNgayNhacTruocHan || 3} ngày</b>.
                                Hãy gia hạn hoặc trả sách đúng hạn để tránh phát sinh tiền phạt.
                            </p>
                        </div>
                    </section>
                </>
            )}
        </div>
    );
}

function RuleCard({ title, value }) {
    return (
        <div className="rule-card">
            <p>{title}</p>
            <h3>{value}</h3>
        </div>
    );
}

function formatCurrency(value) {
    return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND"
    }).format(Number(value || 0));
}
