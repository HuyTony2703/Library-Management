import { RefreshCcw, ShieldCheck } from "lucide-react";
import { useEffect, useState } from "react";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../../components/ToastProvider";

export default function ReaderRulesPage() {
    const toast = useToast();
    const [rules, setRules] = useState(null);
    const [loading, setLoading] = useState(false);

    async function loadRules() {
        setLoading(true);

        try {
            const result = await readerApi.getCurrentRules();
            setRules(result);
        } catch (err) {
            toast.error(err.message || "Không tải được quy định hiện hành");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadRules();
    }, []);

    return (
        <div>
            <div className="reader-home-header">
                <small>Rules</small>
                <h1>Quy định thư viện</h1>
                <p>Dữ liệu quy định hiện hành được đọc trực tiếp từ database.</p>
            </div>

            <div className="reader-page-actions">
                <button type="button" onClick={loadRules}>
                    <RefreshCcw size={17} />
                    {loading ? "Đang tải..." : "Tải lại"}
                </button>
            </div>

            {!rules ? (
                <div className="reader-empty-box">
                    Chưa có dữ liệu quy định. Hãy kiểm tra API /api/reader/rules/current.
                </div>
            ) : (
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
                        <RuleCard title="Phạt trả trễ" value={formatCurrency(rules.mucPhatTreMoiNgay)} />
                    </section>

                    <section className="reader-section">
                        <div className="section-title-row">
                            <div>
                                <p className="reader-eyebrow">Membership rules</p>
                                <h2>Quy định theo gói thành viên</h2>
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
                                <p className="reader-eyebrow">Borrow rules</p>
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
