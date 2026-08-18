import { useState } from "react";
import { libraryApi } from "../api/libraryApi";
import PageHeader from "../components/PageHeader";
import { useToast } from "../components/ToastProvider";

export default function ReturnsPage() {
    const toast = useToast();

    const [form, setForm] = useState(() => ({
        maPhieuTra: `PTR_FE_${Date.now().toString().slice(-8)}`,
        maDocGia: "DG001",
        maNhanVienNhan: "NV_TT001",
        maChiNhanh: "CN_TD",
        maChiTietMuon: "",
        tinhTrangKhiTra: "Bình thường",
        tienPhatHongMat: 0
    }));

    const [result, setResult] = useState(null);

    function update(key, value) {
        setForm((prev) => ({ ...prev, [key]: value }));
    }

    async function submit(e) {
        e.preventDefault();

        try {
            const data = await libraryApi.createReturn({
                maPhieuTra: form.maPhieuTra,
                maDocGia: form.maDocGia,
                maNhanVienNhan: form.maNhanVienNhan,
                maChiNhanh: form.maChiNhanh,
                ghiChu: "Trả sách từ desktop app",
                chiTiet: [
                    {
                        maChiTietMuon: form.maChiTietMuon,
                        tinhTrangKhiTra: form.tinhTrangKhiTra,
                        tienPhatHongMat: Number(form.tienPhatHongMat),
                        ghiChu: "Giao diện desktop"
                    }
                ]
            });

            setResult(data);
            toast.success("Tạo phiếu trả thành công");
        } catch (err) {
            toast.error(err.message);
        }
    }

    return (
        <div>
            <PageHeader
                eyebrow="Return"
                title="Trả sách"
                description="Ghi nhận trả sách, tự tính trễ hạn và tạo khoản nợ nếu hỏng/mất."
            />

            <div className="form-layout">
                <form className="panel form-panel" onSubmit={submit}>
                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Mã phiếu trả</label>
                            <input value={form.maPhieuTra} onChange={(e) => update("maPhieuTra", e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Mã độc giả</label>
                            <input value={form.maDocGia} onChange={(e) => update("maDocGia", e.target.value)} />
                        </div>
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Mã nhân viên</label>
                            <input value={form.maNhanVienNhan} onChange={(e) => update("maNhanVienNhan", e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Chi nhánh</label>
                            <input value={form.maChiNhanh} onChange={(e) => update("maChiNhanh", e.target.value)} />
                        </div>
                    </div>

                    <div className="form-row">
                        <label>Mã chi tiết mượn</label>
                        <input
                            value={form.maChiTietMuon}
                            onChange={(e) => update("maChiTietMuon", e.target.value)}
                            placeholder="Ví dụ: CTM_PM_FE_12345678_01"
                        />
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Tình trạng</label>
                            <select value={form.tinhTrangKhiTra} onChange={(e) => update("tinhTrangKhiTra", e.target.value)}>
                                <option>Bình thường</option>
                                <option>Hỏng</option>
                                <option>Mất</option>
                            </select>
                        </div>

                        <div className="form-row">
                            <label>Phạt hỏng/mất</label>
                            <input type="number" value={form.tienPhatHongMat} onChange={(e) => update("tienPhatHongMat", e.target.value)} />
                        </div>
                    </div>

                    <button className="primary-button">Tạo phiếu trả</button>
                </form>

                <div className="panel preview-panel">
                    <h2>Kết quả</h2>
                    <pre>{result ? JSON.stringify(result, null, 2) : "Chưa có dữ liệu"}</pre>
                </div>
            </div>
        </div>
    );
}