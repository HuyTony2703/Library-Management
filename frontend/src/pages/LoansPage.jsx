import { RefreshCcw } from "lucide-react";
import { useState } from "react";
import { libraryApi } from "../api/libraryApi";
import PageHeader from "../components/PageHeader";
import { useToast } from "../components/ToastProvider";

export default function LoansPage() {
    const toast = useToast();

    const [form, setForm] = useState(() => ({
        maPhieuMuon: `PM_FE_${Date.now().toString().slice(-8)}`,
        maDocGia: "DG001",
        maNhanVienLap: "NV_TT001",
        maChiNhanh: "CN_TD",
        maCuonSach: "F01-001",
        ghiChu: "Mượn sách từ giao diện desktop"
    }));

    const [result, setResult] = useState(null);

    function update(key, value) {
        setForm((prev) => ({ ...prev, [key]: value }));
    }

    async function submit(e) {
        e.preventDefault();

        try {
            const data = await libraryApi.createLoan({
                maPhieuMuon: form.maPhieuMuon,
                maDocGia: form.maDocGia,
                maNhanVienLap: form.maNhanVienLap,
                maChiNhanh: form.maChiNhanh,
                maCuonSachs: [form.maCuonSach],
                ghiChu: form.ghiChu
            });

            setResult(data);
            toast.success("Tạo phiếu mượn thành công");
        } catch (err) {
            toast.error(err.message);
        }
    }

    function regenerate() {
        update("maPhieuMuon", `PM_FE_${Date.now().toString().slice(-8)}`);
    }

    return (
        <div>
            <PageHeader
                eyebrow="Circulation"
                title="Mượn sách"
                description="Lập phiếu mượn, tự kiểm tra thẻ, nợ, trạng thái sách và quy định mượn."
            />

            <div className="form-layout">
                <form className="panel form-panel" onSubmit={submit}>
                    <div className="form-row">
                        <label>Mã phiếu mượn</label>
                        <div className="inline-control">
                            <input value={form.maPhieuMuon} onChange={(e) => update("maPhieuMuon", e.target.value)} />
                            <button type="button" className="icon-button" onClick={regenerate}>
                                <RefreshCcw size={17} />
                            </button>
                        </div>
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Mã độc giả</label>
                            <input value={form.maDocGia} onChange={(e) => update("maDocGia", e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Mã nhân viên</label>
                            <input value={form.maNhanVienLap} onChange={(e) => update("maNhanVienLap", e.target.value)} />
                        </div>
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Chi nhánh</label>
                            <input value={form.maChiNhanh} onChange={(e) => update("maChiNhanh", e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Mã cuốn sách</label>
                            <input value={form.maCuonSach} onChange={(e) => update("maCuonSach", e.target.value)} />
                        </div>
                    </div>

                    <div className="form-row">
                        <label>Ghi chú</label>
                        <textarea value={form.ghiChu} onChange={(e) => update("ghiChu", e.target.value)} />
                    </div>

                    <button className="primary-button">Tạo phiếu mượn</button>
                </form>

                <div className="panel preview-panel">
                    <h2>Kết quả</h2>
                    <p>Backend sẽ đổi trạng thái cuốn sách sang đang mượn nếu hợp lệ.</p>
                    <pre>{result ? JSON.stringify(result, null, 2) : "Chưa có dữ liệu"}</pre>
                </div>
            </div>
        </div>
    );
}