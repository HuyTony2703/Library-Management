import { useEffect, useState } from "react";
import { RefreshCcw, Plus, Lock, Unlock, KeyRound, Save, Trash2 } from "lucide-react";
import { adminApi } from "../../api/adminApi";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";

export default function AdminLibrariansPage() {
    const toast = useToast();

    const [librarians, setLibrarians] = useState([]);
    const [selected, setSelected] = useState(null);
    const [loading, setLoading] = useState(false);

    const [createForm, setCreateForm] = useState({
        maNhanVien: "NV_TT_TEST_001",
        maTaiKhoan: "TK_TT_TEST_001",
        tenDangNhap: "thuthu_test_001",
        matKhau: "123456",
        emailDangNhap: "thuthu_test_001@library.vn",
        maChiNhanh: "CN_TD",
        hoTen: "Trần Thủ Thư Test",
        ngaySinh: "1998-01-01",
        email: "thuthu_test_001@library.vn",
        soDienThoai: "0933333333",
        diaChi: "TP.HCM"
    });

    const [resetPassword, setResetPassword] = useState("123456");

    async function loadLibrarians() {
        try {
            const data = await adminApi.getLibrarians();
            setLibrarians(Array.isArray(data) ? data : []);
        } catch (err) {
            toast.error(err.message || "Không tải được danh sách thủ thư");
        }
    }

    useEffect(() => {
        loadLibrarians();
    }, []);

    function updateCreateField(field, value) {
        setCreateForm((prev) => ({ ...prev, [field]: value }));
    }

    function updateSelectedField(field, value) {
        setSelected((prev) => ({ ...prev, [field]: value }));
    }

    async function createLibrarian(e) {
        e.preventDefault();
        setLoading(true);

        try {
            await adminApi.createLibrarian(createForm);
            toast.success("Thêm thủ thư thành công");
            await loadLibrarians();
        } catch (err) {
            toast.error(err.message || "Thêm thủ thư thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function saveSelected(e) {
        e.preventDefault();

        if (!selected) {
            toast.error("Vui lòng chọn thủ thư cần sửa");
            return;
        }

        setLoading(true);

        try {
            await adminApi.updateLibrarian(selected.maNhanVien, {
                maChiNhanh: selected.maChiNhanh,
                hoTen: selected.hoTen,
                ngaySinh: selected.ngaySinh,
                email: selected.email,
                soDienThoai: selected.soDienThoai,
                diaChi: selected.diaChi
            });

            toast.success("Cập nhật thủ thư thành công");
            await loadLibrarians();
        } catch (err) {
            toast.error(err.message || "Cập nhật thủ thư thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function changeStatus(maNhanVien, trangThaiNhanVien) {
        setLoading(true);

        try {
            await adminApi.updateLibrarianStatus(maNhanVien, { trangThaiNhanVien });
            toast.success("Đổi trạng thái thành công");
            await loadLibrarians();

            if (selected?.maNhanVien === maNhanVien) {
                const updated = await adminApi.getLibrarian(maNhanVien);
                setSelected(updated);
            }
        } catch (err) {
            toast.error(err.message || "Đổi trạng thái thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function resetSelectedPassword() {
        if (!selected) {
            toast.error("Vui lòng chọn thủ thư");
            return;
        }

        if (!resetPassword || resetPassword.length < 6) {
            toast.error("Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }

        setLoading(true);

        try {
            await adminApi.resetLibrarianPassword(selected.maNhanVien, {
                matKhauMoi: resetPassword
            });

            toast.success("Reset mật khẩu thành công");
        } catch (err) {
            toast.error(err.message || "Reset mật khẩu thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function deleteLibrarian(maNhanVien) {
        if (!window.confirm(`Xóa thủ thư ${maNhanVien}? Tài khoản sẽ bị ngừng hoạt động.`)) {
            return;
        }

        setLoading(true);

        try {
            await adminApi.deleteLibrarian(maNhanVien);
            toast.success("Xóa thủ thư thành công");

            if (selected?.maNhanVien === maNhanVien) {
                setSelected(null);
            }

            await loadLibrarians();
        } catch (err) {
            toast.error(err.message || "Xóa thủ thư thất bại");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <PageHeader
                eyebrow="Admin"
                title="Quản lý thủ thư"
                description="Thêm, sửa, khóa, mở khóa, xóa và reset mật khẩu tài khoản thủ thư."
                right={
                    <button className="soft-button" onClick={loadLibrarians}>
                        <RefreshCcw size={17} />
                        Tải lại
                    </button>
                }
            />

            <div className="panel">
                <div className="panel-title">
                    <h2>Danh sách thủ thư</h2>
                    <span>{librarians.length} người</span>
                </div>

                <DataTable
                    data={librarians}
                    columns={[
                        { key: "maNhanVien", title: "Mã NV" },
                        { key: "tenDangNhap", title: "Tên đăng nhập" },
                        { key: "hoTen", title: "Họ tên" },
                        { key: "maChiNhanh", title: "Chi nhánh" },
                        { key: "trangThaiTaiKhoan", title: "Tài khoản", render: (row) => <StatusBadge value={row.trangThaiTaiKhoan} /> },
                        { key: "trangThaiNhanVien", title: "Nhân viên", render: (row) => <StatusBadge value={row.trangThaiNhanVien} /> },
                        {
                            key: "actions",
                            title: "Thao tác",
                            render: (row) => (
                                <div className="table-actions">
                                    <button className="soft-button" onClick={() => setSelected(row)}>
                                        Sửa
                                    </button>

                                    {row.trangThaiNhanVien === "Đang làm" ? (
                                        <button className="soft-button" onClick={() => changeStatus(row.maNhanVien, "Tạm khóa")}>
                                            <Lock size={15} />
                                            Khóa
                                        </button>
                                    ) : (
                                        <button className="soft-button" onClick={() => changeStatus(row.maNhanVien, "Đang làm")}>
                                            <Unlock size={15} />
                                            Mở
                                        </button>
                                    )}

                                    <button className="soft-button" onClick={() => changeStatus(row.maNhanVien, "Nghỉ việc")}>
                                        Nghỉ
                                    </button>

                                    <button className="soft-button danger-button" onClick={() => deleteLibrarian(row.maNhanVien)}>
                                        <Trash2 size={15} />
                                        Xóa
                                    </button>
                                </div>
                            )
                        }
                    ]}
                />
            </div>

            <div className="grid-2">
                <form className="panel form-panel" onSubmit={createLibrarian}>
                    <div className="panel-title">
                        <h2>Thêm thủ thư</h2>
                        <Plus size={20} />
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Mã nhân viên</label>
                            <input value={createForm.maNhanVien} onChange={(e) => updateCreateField("maNhanVien", e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Mã tài khoản</label>
                            <input value={createForm.maTaiKhoan} onChange={(e) => updateCreateField("maTaiKhoan", e.target.value)} />
                        </div>
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Tên đăng nhập</label>
                            <input value={createForm.tenDangNhap} onChange={(e) => updateCreateField("tenDangNhap", e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Mật khẩu</label>
                            <input type="password" value={createForm.matKhau} onChange={(e) => updateCreateField("matKhau", e.target.value)} />
                        </div>
                    </div>

                    <div className="form-row">
                        <label>Email đăng nhập</label>
                        <input value={createForm.emailDangNhap} onChange={(e) => updateCreateField("emailDangNhap", e.target.value)} />
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Họ tên</label>
                            <input value={createForm.hoTen} onChange={(e) => updateCreateField("hoTen", e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Ngày sinh</label>
                            <input type="date" value={createForm.ngaySinh} onChange={(e) => updateCreateField("ngaySinh", e.target.value)} />
                        </div>
                    </div>

                    <div className="form-grid-2">
                        <div className="form-row">
                            <label>Chi nhánh</label>
                            <input value={createForm.maChiNhanh} onChange={(e) => updateCreateField("maChiNhanh", e.target.value)} />
                        </div>

                        <div className="form-row">
                            <label>Số điện thoại</label>
                            <input value={createForm.soDienThoai} onChange={(e) => updateCreateField("soDienThoai", e.target.value)} />
                        </div>
                    </div>

                    <div className="form-row">
                        <label>Email nhân viên</label>
                        <input value={createForm.email} onChange={(e) => updateCreateField("email", e.target.value)} />
                    </div>

                    <div className="form-row">
                        <label>Địa chỉ</label>
                        <textarea value={createForm.diaChi} onChange={(e) => updateCreateField("diaChi", e.target.value)} />
                    </div>

                    <button className="primary-button" disabled={loading}>
                        Thêm thủ thư
                    </button>
                </form>

                <form className="panel form-panel" onSubmit={saveSelected}>
                    <div className="panel-title">
                        <h2>Sửa thủ thư</h2>
                        <Save size={20} />
                    </div>

                    {selected ? (
                        <>
                            <div className="form-grid-2">
                                <div className="form-row">
                                    <label>Mã nhân viên</label>
                                    <input value={selected.maNhanVien || ""} disabled />
                                </div>

                                <div className="form-row">
                                    <label>Tên đăng nhập</label>
                                    <input value={selected.tenDangNhap || ""} disabled />
                                </div>
                            </div>

                            <div className="form-row">
                                <label>Họ tên</label>
                                <input value={selected.hoTen || ""} onChange={(e) => updateSelectedField("hoTen", e.target.value)} />
                            </div>

                            <div className="form-grid-2">
                                <div className="form-row">
                                    <label>Ngày sinh</label>
                                    <input type="date" value={selected.ngaySinh || ""} onChange={(e) => updateSelectedField("ngaySinh", e.target.value)} />
                                </div>

                                <div className="form-row">
                                    <label>Chi nhánh</label>
                                    <input value={selected.maChiNhanh || ""} onChange={(e) => updateSelectedField("maChiNhanh", e.target.value)} />
                                </div>
                            </div>

                            <div className="form-grid-2">
                                <div className="form-row">
                                    <label>Email</label>
                                    <input value={selected.email || ""} onChange={(e) => updateSelectedField("email", e.target.value)} />
                                </div>

                                <div className="form-row">
                                    <label>Số điện thoại</label>
                                    <input value={selected.soDienThoai || ""} onChange={(e) => updateSelectedField("soDienThoai", e.target.value)} />
                                </div>
                            </div>

                            <div className="form-row">
                                <label>Địa chỉ</label>
                                <textarea value={selected.diaChi || ""} onChange={(e) => updateSelectedField("diaChi", e.target.value)} />
                            </div>

                            <button className="primary-button" disabled={loading}>
                                Lưu thay đổi
                            </button>

                            <div className="form-row">
                                <label>Mật khẩu mới</label>
                                <div className="inline-control">
                                    <input type="password" value={resetPassword} onChange={(e) => setResetPassword(e.target.value)} />
                                    <button type="button" className="soft-button" onClick={resetSelectedPassword}>
                                        <KeyRound size={16} />
                                        Reset
                                    </button>
                                </div>
                            </div>
                        </>
                    ) : (
                        <p>Chọn một thủ thư trong bảng để sửa thông tin.</p>
                    )}
                </form>
            </div>
        </div>
    );
}
