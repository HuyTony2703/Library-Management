import { EyeOff, Pencil, Plus, RotateCcw, Trash2 } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { libraryApi } from "../api/libraryApi";
import DataTable from "../components/DataTable";
import InlineActionMenu from "../components/InlineActionMenu";
import PageHeader from "../components/PageHeader";
import ResultModal from "../components/ResultModal";
import StatusBadge from "../components/StatusBadge";
import { useActionDialog } from "../components/ActionDialogProvider";
import { useToast } from "../components/ToastProvider";
import { formatDate } from "../utils/displayUtils";

const FALLBACK_STATUS_OPTIONS = [
    { value: "TT_SANCO", label: "Sẵn có" },
    { value: "TT_DANGMUON", label: "Đang mượn" },
    { value: "TT_DANGDATTRUOC", label: "Đang đặt trước" },
    { value: "TT_HONG", label: "Bị hỏng" },
    { value: "TT_MAT", label: "Bị mất" },
    { value: "TT_NGUNGLUUTHONG", label: "Ngừng lưu thông" }
];

export default function BookCopiesPage() {
    const toast = useToast();
    const actionDialog = useActionDialog();
    const [searchParams, setSearchParams] = useSearchParams();
    const [data, setData] = useState([]);
    const [statusOptions, setStatusOptions] = useState(FALLBACK_STATUS_OPTIONS);
    const [loading, setLoading] = useState(false);
    const [search, setSearch] = useState(searchParams.get("search") || "");
    const [showModal, setShowModal] = useState(false);
    const [editingCopy, setEditingCopy] = useState(null);
    const [selectedIds, setSelectedIds] = useState([]);
    const [form, setForm] = useState(() => buildDefaultCopyForm());

    async function load() {
        try {
            setData(await libraryApi.bookCopies());
        } catch (err) {
            toast.error(err.message || "Không tải được danh sách cuốn sách");
        }
    }

    useEffect(() => {
        load();
        libraryApi.bookCopyStatuses()
            .then((options) => setStatusOptions(options?.length ? options : FALLBACK_STATUS_OPTIONS))
            .catch(() => setStatusOptions(FALLBACK_STATUS_OPTIONS));
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
                row.maCuonSach,
                row.maDauSach,
                row.tenDauSach,
                row.maChiNhanh,
                row.maViTri,
                row.maTrangThai,
                row.tenTrangThai,
                row.maVach
            ]
                .filter((value) => value !== null && value !== undefined)
                .some((value) => String(value).toLowerCase().includes(keyword))
        );
    }, [data, search]);

    useEffect(() => {
        setSelectedIds((prev) => prev.filter((id) => data.some((row) => row.maCuonSach === id)));
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
        setSelectedIds(filteredData.map((row) => row.maCuonSach));
    }

    function clearSelected() {
        setSelectedIds([]);
    }

    function openCreateModal() {
        setEditingCopy(null);
        setForm(buildDefaultCopyForm());
        setShowModal(true);
    }

    function openEditModal(row) {
        setEditingCopy(row);
        setForm({
            maCuonSach: row.maCuonSach || "",
            maDauSach: row.maDauSach || "",
            maChiNhanh: row.maChiNhanh || "CN_TD",
            maViTri: row.maViTri || "VT_M01_N01",
            maTrangThai: row.maTrangThai || "TT_SANCO",
            maVach: row.maVach || "",
            maQrCode: row.maQrCode || "",
            ngayNhapSach: row.ngayNhapSach || new Date().toISOString().slice(0, 10),
            ghiChu: row.ghiChu || ""
        });
        setShowModal(true);
    }

    function closeModal() {
        setShowModal(false);
        setEditingCopy(null);
        setForm(buildDefaultCopyForm());
    }

    function buildPayload() {
        return {
            ...form,
            maTrangThai: editingCopy ? form.maTrangThai : "TT_SANCO",
            maVach: form.maVach || null,
            maQrCode: form.maQrCode || null,
            ghiChu: form.ghiChu || null
        };
    }

    async function saveBookCopy(event) {
        event.preventDefault();
        setLoading(true);

        try {
            const payload = buildPayload();

            if (editingCopy) {
                await libraryApi.updateBookCopy(editingCopy.maCuonSach, payload);
            } else {
                await libraryApi.createBookCopy(payload);
            }

            toast.success(editingCopy ? "Cập nhật cuốn sách thành công" : "Thêm cuốn sách thành công");
            closeModal();
            await load();
        } catch (err) {
            toast.error(err.message || "Lưu cuốn sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function hideBookCopy(maCuonSach) {
        const confirmed = await actionDialog.confirm({
            title: `Ẩn cuốn sách ${maCuonSach}`,
            message: "Cuốn sách sẽ ngừng lưu thông nhưng dữ liệu vẫn được giữ lại.",
            confirmLabel: "Ẩn"
        });

        if (!confirmed) return;

        setLoading(true);
        try {
            await libraryApi.deleteBookCopy(maCuonSach, "soft");
            toast.success("Đã ẩn cuốn sách");
            await load();
        } catch (err) {
            toast.error(err.message || "Ẩn cuốn sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function hardDeleteBookCopy(maCuonSach) {
        const confirmed = await actionDialog.confirm({
            title: `Xóa cuốn sách ${maCuonSach}`,
            message: "Thao tác này sẽ xóa vĩnh viễn nếu dữ liệu chưa có liên kết nghiệp vụ.",
            confirmLabel: "Xóa",
            danger: true
        });

        if (!confirmed) return;

        setLoading(true);
        try {
            await libraryApi.deleteBookCopy(maCuonSach, "hard");
            toast.success("Đã xóa cuốn sách");
            setSelectedIds((prev) => prev.filter((id) => id !== maCuonSach));
            await load();
        } catch (err) {
            toast.error(err.message || "Xóa cuốn sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function restoreBookCopy(maCuonSach) {
        const confirmed = await actionDialog.confirm({
            title: `Khôi phục cuốn sách ${maCuonSach}`,
            message: "Cuốn sách sẽ được đưa về trạng thái Sẵn có.",
            confirmLabel: "Khôi phục"
        });

        if (!confirmed) return;

        setLoading(true);
        try {
            await libraryApi.restoreBookCopy(maCuonSach);
            toast.success("Đã khôi phục cuốn sách");
            await load();
        } catch (err) {
            toast.error(err.message || "Khôi phục cuốn sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function deleteSelectedBookCopies() {
        if (selectedIds.length === 0) {
            toast.error("Vui lòng chọn ít nhất một cuốn sách");
            return;
        }

        const mode = await actionDialog.chooseDeleteMode(`${selectedIds.length} cuốn sách đã chọn`);

        if (!mode) return;

        setLoading(true);
        try {
            for (const id of selectedIds) {
                await libraryApi.deleteBookCopy(id, mode);
            }

            toast.success(mode === "hard" ? "Đã xóa các cuốn sách đã chọn" : "Đã ngừng lưu thông các cuốn sách đã chọn");
            setSelectedIds([]);
            await load();
        } catch (err) {
            toast.error(err.message || "Xóa các cuốn sách đã chọn thất bại");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <PageHeader
                eyebrow="Inventory"
                title="Quản lý cuốn sách"
                description="Theo dõi từng bản sách vật lý, vị trí, chi nhánh và trạng thái."
            />

            <div className="panel search-panel">
                <input
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder="Tìm theo mã cuốn, đầu sách, chi nhánh, vị trí, trạng thái..."
                />
            </div>

            {showModal && (
                <ResultModal title={editingCopy ? "Sửa thông tin cuốn sách" : "Thêm cuốn sách"} onClose={closeModal} className="form-modal-card">
                    <form className="form-panel modal-form" onSubmit={saveBookCopy}>
                        <div className="form-grid-3">
                            <div className="form-row">
                                <label>Mã cuốn sách</label>
                                <input value={form.maCuonSach} onChange={(e) => updateField("maCuonSach", e.target.value)} disabled={Boolean(editingCopy)} />
                            </div>
                            <div className="form-row">
                                <label>Mã đầu sách</label>
                                <input value={form.maDauSach} onChange={(e) => updateField("maDauSach", e.target.value)} />
                            </div>
                            <div className="form-row">
                                <label>Chi nhánh</label>
                                <input value={form.maChiNhanh} onChange={(e) => updateField("maChiNhanh", e.target.value)} />
                            </div>
                        </div>

                        <div className="form-grid-3">
                            <div className="form-row">
                                <label>Vị trí</label>
                                <input value={form.maViTri} onChange={(e) => updateField("maViTri", e.target.value)} />
                            </div>
                            <div className="form-row">
                                <label>Trạng thái</label>
                                {editingCopy ? (
                                    <select value={form.maTrangThai} onChange={(e) => updateField("maTrangThai", e.target.value)}>
                                        {statusOptions.map((option) => (
                                            <option key={option.value} value={option.value}>
                                                {option.label}
                                            </option>
                                        ))}
                                    </select>
                                ) : (
                                    <input value="Sẵn có" readOnly className="read-only-field" />
                                )}
                            </div>
                            <div className="form-row">
                                <label>Ngày nhập sách</label>
                                <input type="date" value={form.ngayNhapSach} onChange={(e) => updateField("ngayNhapSach", e.target.value)} />
                            </div>
                        </div>

                        <div className="form-grid-3">
                            <div className="form-row">
                                <label>Mã vạch</label>
                                <input value={form.maVach} onChange={(e) => updateField("maVach", e.target.value)} />
                            </div>
                            <div className="form-row">
                                <label>Mã QR</label>
                                <input value={form.maQrCode} onChange={(e) => updateField("maQrCode", e.target.value)} />
                            </div>
                            <div className="form-row">
                                <label>Ghi chú</label>
                                <input value={form.ghiChu} onChange={(e) => updateField("ghiChu", e.target.value)} />
                            </div>
                        </div>

                        <button className="primary-button" disabled={loading}>
                            {editingCopy ? "Lưu thông tin" : "Thêm cuốn sách"}
                        </button>
                    </form>
                </ResultModal>
            )}

            <div className="list-toolbar">
                <button className="primary-button" type="button" onClick={openCreateModal}>
                    <Plus size={17} />
                    Thêm cuốn sách
                </button>

                <div className="selection-toolbar">
                    <button className="ghost-button" type="button" onClick={selectAllVisible}>
                        Chọn tất cả
                    </button>
                    <button className="soft-button" type="button" onClick={clearSelected}>
                        Bỏ chọn tất cả
                    </button>
                    <button className="soft-button danger-button" type="button" onClick={deleteSelectedBookCopies} disabled={selectedIds.length === 0 || loading}>
                        <Trash2 size={15} />
                        Xóa
                    </button>
                </div>
            </div>

            <DataTable
                data={filteredData}
                rowClassName={(row) => selectedIds.includes(row.maCuonSach) ? "selected-row" : ""}
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
                                checked={selectedIds.includes(row.maCuonSach)}
                                onChange={() => toggleSelected(row.maCuonSach)}
                            />
                        )
                    },
                    { key: "maCuonSach", title: "Mã cuốn" },
                    { key: "maDauSach", title: "Đầu sách" },
                    { key: "maChiNhanh", title: "Chi nhánh" },
                    { key: "maViTri", title: "Vị trí" },
                    { key: "maTrangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.maTrangThai} /> },
                    { key: "ngayNhapSach", title: "Ngày nhập", render: (row) => formatDate(row.ngayNhapSach) },
                    { key: "maVach", title: "Mã vạch" },
                    {
                        key: "actions",
                        title: "Thao tác",
                        width: "120px",
                        render: (row) => (
                            <InlineActionMenu
                                label={`Mở thao tác cho cuốn sách ${row.maCuonSach}`}
                                disabled={loading}
                                actions={[
                                    { key: "edit", label: "Sửa thông tin", icon: Pencil, onClick: () => openEditModal(row) },
                                    { key: "hide", label: "Ẩn", icon: EyeOff, onClick: () => hideBookCopy(row.maCuonSach), disabled: row.maTrangThai === "TT_NGUNGLUUTHONG" },
                                    { key: "restore", label: "Khôi phục", icon: RotateCcw, onClick: () => restoreBookCopy(row.maCuonSach), disabled: row.maTrangThai === "TT_SANCO" },
                                    { key: "delete", label: "Xóa", icon: Trash2, danger: true, onClick: () => hardDeleteBookCopy(row.maCuonSach) }
                                ]}
                            />
                        )
                    }
                ]}
            />
        </div>
    );
}

function buildDefaultCopyForm() {
    const suffix = Date.now().toString().slice(-6);

    return {
        maCuonSach: `COPY_${suffix}`,
        maDauSach: "F01",
        maChiNhanh: "CN_TD",
        maViTri: "VT_M01_N01",
        maTrangThai: "TT_SANCO",
        maVach: "",
        maQrCode: "",
        ngayNhapSach: new Date().toISOString().slice(0, 10),
        ghiChu: ""
    };
}
