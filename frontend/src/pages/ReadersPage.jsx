import { EyeOff, Pencil, Plus, RotateCcw, Trash2 } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { libraryApi } from "../api/libraryApi";
import DataTable from "../components/DataTable";
import InlineActionMenu from "../components/InlineActionMenu";
import PageHeader from "../components/PageHeader";
import PasswordInput from "../components/PasswordInput";
import ResultModal from "../components/ResultModal";
import StatusBadge from "../components/StatusBadge";
import { useActionDialog } from "../components/ActionDialogProvider";
import { useToast } from "../components/ToastProvider";
import { formatDate } from "../utils/displayUtils";

const FALLBACK_READER_GROUPS = [
    { value: "NHOM_HOCSINH", label: "Học sinh" },
    { value: "NHOM_SINHVIEN", label: "Sinh viên" },
    { value: "NHOM_GIAOVIEN", label: "Giáo viên" },
    { value: "NHOM_KHAC", label: "Khác" }
];

const FALLBACK_MEMBERSHIP_PLANS = [
    { value: "GOI_THUONG", label: "Gói thường" },
    { value: "GOI_VIP", label: "Gói VIP" },
    { value: "GOI_PREMIUM", label: "Gói Premium" }
];

export default function ReadersPage() {
    const toast = useToast();
    const actionDialog = useActionDialog();
    const [searchParams, setSearchParams] = useSearchParams();
    const [data, setData] = useState([]);
    const [readerGroups, setReaderGroups] = useState(FALLBACK_READER_GROUPS);
    const [membershipPlans, setMembershipPlans] = useState(FALLBACK_MEMBERSHIP_PLANS);
    const [loading, setLoading] = useState(false);
    const [search, setSearch] = useState(searchParams.get("search") || "");
    const [form, setForm] = useState(() => buildDefaultForm());
    const [showModal, setShowModal] = useState(false);
    const [editingReader, setEditingReader] = useState(null);
    const [selectedIds, setSelectedIds] = useState([]);

    async function load() {
        try {
            setData(await libraryApi.readers());
        } catch (err) {
            toast.error(err.message || "Không tải được danh sách độc giả");
        }
    }

    useEffect(() => {
        load();
        libraryApi.readerGroups()
            .then((options) => setReaderGroups(options?.length ? options : FALLBACK_READER_GROUPS))
            .catch(() => setReaderGroups(FALLBACK_READER_GROUPS));
        libraryApi.membershipPlans()
            .then((options) => setMembershipPlans(options?.length ? options : FALLBACK_MEMBERSHIP_PLANS))
            .catch(() => setMembershipPlans(FALLBACK_MEMBERSHIP_PLANS));
    }, []);

    useEffect(() => {
        const timer = window.setTimeout(() => {
            setSearchParams(search.trim() ? { search: search.trim() } : {}, { replace: true });
        }, 250);

        return () => window.clearTimeout(timer);
    }, [search, setSearchParams]);

    const filteredData = useMemo(() => {
        const keyword = search.trim().toLowerCase();

        if (!keyword) {
            return data;
        }

        return data.filter((row) =>
            [
                row.maDocGia,
                row.maTaiKhoan,
                row.tenDangNhap,
                row.maNhomDocGia,
                row.hoTen,
                row.email,
                row.soDienThoai,
                row.diaChi,
                row.trangThai,
                row.maGoiThanhVien
            ]
                .filter((value) => value !== null && value !== undefined)
                .some((value) => String(value).toLowerCase().includes(keyword))
        );
    }, [data, search]);

    useEffect(() => {
        setSelectedIds((prev) => prev.filter((id) => data.some((row) => row.maDocGia === id)));
    }, [data]);

    function updateField(field, value) {
        setForm((prev) => ({ ...prev, [field]: value }));
    }

    function toggleSelected(id) {
        setSelectedIds((prev) => prev.includes(id)
            ? prev.filter((value) => value !== id)
            : [...prev, id]);
    }

    function selectAllVisible() {
        setSelectedIds(filteredData.map((row) => row.maDocGia));
    }

    function clearSelected() {
        setSelectedIds([]);
    }

    function openCreateModal() {
        setEditingReader(null);
        setForm(buildDefaultForm());
        setShowModal(true);
    }

    function openEditModal(row) {
        setEditingReader(row);
        setForm({
            maDocGia: row.maDocGia || "",
            maTaiKhoan: row.maTaiKhoan || "",
            tenDangNhap: row.tenDangNhap || "",
            matKhau: "123456",
            maNhomDocGia: row.maNhomDocGia || "NHOM_SINHVIEN",
            maGoiThanhVien: row.maGoiThanhVien || "GOI_THUONG",
            hoTen: row.hoTen || "",
            ngaySinh: row.ngaySinh || "2000-01-01",
            diaChi: row.diaChi || "",
            email: row.email || "",
            soDienThoai: row.soDienThoai || "",
            ngayLapThe: row.ngayLapThe || new Date().toISOString().slice(0, 10)
        });
        setShowModal(true);
    }

    function closeModal() {
        setShowModal(false);
        setEditingReader(null);
        setForm(buildDefaultForm());
    }

    function buildPayload() {
        return {
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
        };
    }

    async function saveReader(event) {
        event.preventDefault();
        setLoading(true);

        try {
            const payload = buildPayload();

            if (editingReader) {
                await libraryApi.updateReader(editingReader.maDocGia, payload);
            } else {
                await libraryApi.createReader(payload);
            }

            toast.success(editingReader ? "Cập nhật độc giả thành công" : "Thêm độc giả thành công");
            closeModal();
            await load();
        } catch (err) {
            toast.error(err.message || "Lưu độc giả thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function hideReader(maDocGia) {
        const confirmed = await actionDialog.confirm({
            title: `Ẩn độc giả ${maDocGia}`,
            message: "Độc giả sẽ ngừng hoạt động nhưng dữ liệu vẫn được giữ lại.",
            confirmLabel: "Ẩn"
        });

        if (!confirmed) return;

        setLoading(true);
        try {
            await libraryApi.deleteReader(maDocGia, "soft");
            toast.success("Đã ẩn độc giả");
            await load();
        } catch (err) {
            toast.error(err.message || "Ẩn độc giả thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function hardDeleteReader(maDocGia) {
        const confirmed = await actionDialog.confirm({
            title: `Xóa độc giả ${maDocGia}`,
            message: "Thao tác này sẽ xóa vĩnh viễn nếu dữ liệu chưa có liên kết nghiệp vụ.",
            confirmLabel: "Xóa",
            danger: true
        });

        if (!confirmed) return;

        setLoading(true);
        try {
            await libraryApi.deleteReader(maDocGia, "hard");
            toast.success("Đã xóa độc giả");
            setSelectedIds((prev) => prev.filter((id) => id !== maDocGia));
            await load();
        } catch (err) {
            toast.error(err.message || "Xóa độc giả thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function restoreReader(maDocGia) {
        const confirmed = await actionDialog.confirm({
            title: `Khôi phục độc giả ${maDocGia}`,
            message: "Độc giả sẽ được đưa về trạng thái hoạt động.",
            confirmLabel: "Khôi phục"
        });

        if (!confirmed) return;

        setLoading(true);
        try {
            await libraryApi.restoreReader(maDocGia);
            toast.success("Đã khôi phục độc giả");
            await load();
        } catch (err) {
            toast.error(err.message || "Khôi phục độc giả thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function deleteSelectedReaders() {
        if (selectedIds.length === 0) {
            toast.error("Vui lòng chọn ít nhất một độc giả");
            return;
        }

        const mode = await actionDialog.chooseDeleteMode(`${selectedIds.length} độc giả đã chọn`);

        if (!mode) return;

        setLoading(true);
        try {
            for (const id of selectedIds) {
                await libraryApi.deleteReader(id, mode);
            }

            toast.success(mode === "hard" ? "Đã xóa các độc giả đã chọn" : "Đã ngừng hoạt động các độc giả đã chọn");
            setSelectedIds([]);
            await load();
        } catch (err) {
            toast.error(err.message || "Xóa các độc giả đã chọn thất bại");
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
            />

            <div className="panel search-panel">
                <input
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder="Tìm theo mã, tên, email, số điện thoại, nhóm độc giả..."
                />
            </div>

            {showModal && (
                <ResultModal title={editingReader ? "Sửa thông tin độc giả" : "Thêm độc giả"} onClose={closeModal} className="form-modal-card">
                    <form className="form-panel modal-form" onSubmit={saveReader}>
                        <div className="form-grid-3">
                            <div className="form-row">
                                <label>Mã độc giả</label>
                                <input value={form.maDocGia} onChange={(e) => updateField("maDocGia", e.target.value)} disabled={Boolean(editingReader)} />
                            </div>
                            <div className="form-row">
                                <label>Mã tài khoản</label>
                                <input value={form.maTaiKhoan} onChange={(e) => updateField("maTaiKhoan", e.target.value)} disabled={Boolean(editingReader)} />
                            </div>
                            <div className="form-row">
                                <label>Tên đăng nhập</label>
                                <input value={form.tenDangNhap} onChange={(e) => updateField("tenDangNhap", e.target.value)} disabled={Boolean(editingReader)} />
                            </div>
                        </div>

                        <div className="form-grid-3">
                            <div className="form-row">
                                <label>Mật khẩu</label>
                                <PasswordInput value={form.matKhau} onChange={(e) => updateField("matKhau", e.target.value)} disabled={Boolean(editingReader)} />
                            </div>
                            <div className="form-row">
                                <label>Nhóm độc giả</label>
                                <select value={form.maNhomDocGia} onChange={(e) => updateField("maNhomDocGia", e.target.value)}>
                                    {readerGroups.map((option) => (
                                        <option key={option.value} value={option.value}>
                                            {option.label}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <div className="form-row">
                                <label>Gói thành viên</label>
                                <select value={form.maGoiThanhVien} onChange={(e) => updateField("maGoiThanhVien", e.target.value)}>
                                    {membershipPlans.map((option) => (
                                        <option key={option.value} value={option.value}>
                                            {option.label}
                                        </option>
                                    ))}
                                </select>
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
                                <input type="date" value={form.ngayLapThe} onChange={(e) => updateField("ngayLapThe", e.target.value)} disabled={Boolean(editingReader)} />
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
                            {editingReader ? "Lưu thông tin" : "Thêm độc giả"}
                        </button>
                    </form>
                </ResultModal>
            )}

            <div className="list-toolbar">
                <button className="primary-button" type="button" onClick={openCreateModal}>
                    <Plus size={17} />
                    Thêm độc giả
                </button>

                <div className="selection-toolbar">
                    <button className="ghost-button" type="button" onClick={selectAllVisible}>
                        Chọn tất cả
                    </button>
                    <button className="soft-button" type="button" onClick={clearSelected}>
                        Bỏ chọn tất cả
                    </button>
                    <button className="soft-button danger-button" type="button" onClick={deleteSelectedReaders} disabled={selectedIds.length === 0 || loading}>
                        <Trash2 size={15} />
                        Xóa
                    </button>
                </div>
            </div>

            <DataTable
                data={filteredData}
                rowClassName={(row) => selectedIds.includes(row.maDocGia) ? "selected-row" : ""}
                columns={[
                    {
                        key: "select",
                        title: "",
                        className: "selection-count-cell",
                        width: "76px",
                        render: (row) => (
                            <input
                                className="table-checkbox"
                                type="checkbox"
                                checked={selectedIds.includes(row.maDocGia)}
                                onChange={() => toggleSelected(row.maDocGia)}
                            />
                        )
                    },
                    { key: "maDocGia", title: "Mã ĐG" },
                    { key: "hoTen", title: "Họ tên" },
                    { key: "email", title: "Email" },
                    { key: "soDienThoai", title: "SĐT" },
                    { key: "ngayHetHanThe", title: "Hạn thẻ", render: (row) => formatDate(row.ngayHetHanThe) },
                    { key: "trangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.trangThai} /> },
                    {
                        key: "actions",
                        title: "Thao tác",
                        width: "120px",
                        render: (row) => (
                            <InlineActionMenu
                                label={`Mở thao tác cho độc giả ${row.maDocGia}`}
                                disabled={loading}
                                actions={[
                                    { key: "edit", label: "Sửa thông tin", icon: Pencil, onClick: () => openEditModal(row) },
                                    { key: "hide", label: "Ẩn", icon: EyeOff, onClick: () => hideReader(row.maDocGia), disabled: row.trangThai === "Ngừng hoạt động" },
                                    { key: "restore", label: "Khôi phục", icon: RotateCcw, onClick: () => restoreReader(row.maDocGia), disabled: row.trangThai === "Hoạt động" },
                                    { key: "delete", label: "Xóa", icon: Trash2, danger: true, onClick: () => hardDeleteReader(row.maDocGia) }
                                ]}
                            />
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
        maDocGia: `DG_MOI_${suffix}`,
        maTaiKhoan: `TK_DG_MOI_${suffix}`,
        tenDangNhap: `docgia_moi_${suffix}`,
        matKhau: "123456",
        maNhomDocGia: "NHOM_SINHVIEN",
        maGoiThanhVien: "GOI_THUONG",
        hoTen: "Độc giả mới 1",
        ngaySinh: "2000-01-01",
        diaChi: "TP.HCM",
        email: `docgia_moi_${suffix}@library.vn`,
        soDienThoai: "0922222999",
        ngayLapThe: new Date().toISOString().slice(0, 10)
    };
}
