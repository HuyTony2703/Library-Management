import { RefreshCcw, Plus, Trash2 } from "lucide-react";
import { useEffect, useState } from "react";
import { libraryApi } from "../api/libraryApi";
import DataTable from "../components/DataTable";
import PageHeader from "../components/PageHeader";
import StatusBadge from "../components/StatusBadge";
import { useToast } from "../components/ToastProvider";
import { formatDate } from "../utils/displayUtils";

export default function ReadersPage() {
    const toast = useToast();
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [form, setForm] = useState(() => buildDefaultForm());

    async function load() {
        try {
            setData(await libraryApi.readers());
        } catch (err) {
            toast.error(err.message);
        }
    }

    useEffect(() => {
        load();
    }, []);

    function updateField(field, value) {
        setForm((prev) => ({ ...prev, [field]: value }));
    }

    async function createReader(e) {
        e.preventDefault();
        setLoading(true);

        try {
            await libraryApi.createReader({
                maDocGia: form.maDocGia,
                maTaiKhoan: form.maTaiKhoan,
                tenDangNhap: form.tenDangNhap,
                matKhau: form.matKhau,
                maNhomDocGia: form.maNhomDocGia,
                maGoiThanhVien: form.maGoiThanhVien || null,
                hoTen: form.hoTen,
                ngaySinh: form.ngaySinh,
                diaChi: form.diaChi,
                email: form.email,
                soDienThoai: form.soDienThoai,
                ngayLapThe: form.ngayLapThe || null
            });

            toast.success("Thêm độc giả thành công");
            setForm(buildDefaultForm());
            await load();
        } catch (err) {
            toast.error(err.message || "Thêm độc giả thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function deleteReader(maDocGia) {
        if (!window.confirm(`Xóa/ngừng hoạt động độc giả ${maDocGia}?`)) {
            return;
        }

        setLoading(true);

        try {
            await libraryApi.deleteReader(maDocGia);
            toast.success("Đã ngừng hoạt động độc giả");
            await load();
        } catch (err) {
            toast.error(err.message || "Xóa độc giả thất bại");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <PageHeader
                eyebrow="Members"
                title="Quản lý độc giả"
                description="Thông tin độc giả, email, số điện thoại, hạn thẻ và trạng thái."
                right={<button className="soft-button" onClick={load}><RefreshCcw size={17} /> Tải lại</button>}
            />

            <form className="panel form-panel" onSubmit={createReader}>
                <div className="panel-title">
                    <h2>Thêm độc giả</h2>
                    <Plus size={20} />
                </div>

                <div className="form-grid-3">
                    <div className="form-row">
                        <label>Mã độc giả</label>
                        <input value={form.maDocGia} onChange={(e) => updateField("maDocGia", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Mã tài khoản</label>
                        <input value={form.maTaiKhoan} onChange={(e) => updateField("maTaiKhoan", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Tên đăng nhập</label>
                        <input value={form.tenDangNhap} onChange={(e) => updateField("tenDangNhap", e.target.value)} />
                    </div>
                </div>

                <div className="form-grid-3">
                    <div className="form-row">
                        <label>Mật khẩu</label>
                        <input type="password" value={form.matKhau} onChange={(e) => updateField("matKhau", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Nhóm độc giả</label>
                        <input value={form.maNhomDocGia} onChange={(e) => updateField("maNhomDocGia", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Gói thành viên</label>
                        <input value={form.maGoiThanhVien} onChange={(e) => updateField("maGoiThanhVien", e.target.value)} />
                    </div>
                </div>

                <div className="form-grid-3">
                    <div className="form-row">
                        <label>Họ tên</label>
                        <input value={form.hoTen} onChange={(e) => updateField("hoTen", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Ngày sinh</label>
                        <input type="date" value={form.ngaySinh} onChange={(e) => updateField("ngaySinh", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Ngày lập thẻ</label>
                        <input type="date" value={form.ngayLapThe} onChange={(e) => updateField("ngayLapThe", e.target.value)} />
                    </div>
                </div>

                <div className="form-grid-3">
                    <div className="form-row">
                        <label>Email</label>
                        <input value={form.email} onChange={(e) => updateField("email", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Số điện thoại</label>
                        <input value={form.soDienThoai} onChange={(e) => updateField("soDienThoai", e.target.value)} />
                    </div>
                    <div className="form-row">
                        <label>Địa chỉ</label>
                        <input value={form.diaChi} onChange={(e) => updateField("diaChi", e.target.value)} />
                    </div>
                </div>

                <button className="primary-button" disabled={loading}>
                    Thêm độc giả
                </button>
            </form>

            <DataTable
                data={data}
                columns={[
                    { key: "maDocGia", title: "Mã ĐG" },
                    { key: "hoTen", title: "Họ tên" },
                    { key: "email", title: "Email" },
                    { key: "soDienThoai", title: "SĐT" },
                    { key: "ngayHetHanThe", title: "Hạn thẻ", render: (row) => formatDate(row.ngayHetHanThe) },
                    { key: "trangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.trangThai} /> },
                    {
                        key: "actions",
                        title: "Thao tác",
                        render: (row) => (
                            <button className="soft-button danger-button" onClick={() => deleteReader(row.maDocGia)}>
                                <Trash2 size={15} />
                                Xóa
                            </button>
                        )
                    }
                ]}
            />
        </div>
    );
}

function buildDefaultForm() {
    const suffix = Date.now().toString().slice(-6);

    return {
        maDocGia: `DG_TEST_${suffix}`,
        maTaiKhoan: `TK_DG_TEST_${suffix}`,
        tenDangNhap: `docgia_test_${suffix}`,
        matKhau: "123456",
        maNhomDocGia: "NHOM_SINHVIEN",
        maGoiThanhVien: "GOI_THUONG",
        hoTen: "Độc Giả Test",
        ngaySinh: "2000-01-01",
        diaChi: "TP.HCM",
        email: `docgia_test_${suffix}@library.vn`,
        soDienThoai: "0922222999",
        ngayLapThe: new Date().toISOString().slice(0, 10)
    };
}
