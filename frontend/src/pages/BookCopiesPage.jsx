import { Plus, Trash2 } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { libraryApi } from "../api/libraryApi";
import DataTable from "../components/DataTable";
import PageHeader from "../components/PageHeader";
import ResultModal from "../components/ResultModal";
import StatusBadge from "../components/StatusBadge";
import { useToast } from "../components/ToastProvider";
import { useActionDialog } from "../components/ActionDialogProvider";
import { formatDate } from "../utils/displayUtils";

export default function BookCopiesPage() {
    const toast = useToast();
    const actionDialog = useActionDialog();
    const [searchParams, setSearchParams] = useSearchParams();
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [search, setSearch] = useState(searchParams.get("search") || "");
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [selectedIds, setSelectedIds] = useState([]);
    const [form, setForm] = useState({
        maCuonSach: `COPY_${Date.now().toString().slice(-6)}`,
        maDauSach: "F01",
        maChiNhanh: "CN_TD",
        maViTri: "VT_M01_N01",
        maTrangThai: "TT_SANCO",
        maVach: "",
        maQrCode: "",
        ngayNhapSach: new Date().toISOString().slice(0, 10),
        ghiChu: ""
    });

    async function load() {
        try {
            setData(await libraryApi.bookCopies());
        } catch (err) {
            toast.error(err.message);
        }
    }

    useEffect(() => {
        load();
    }, []);

    useEffect(() => {
        setSearch(searchParams.get("search") || "");
    }, [searchParams]);

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

    function submitSearch(event) {
        event.preventDefault();
        setSearchParams(search.trim() ? { search: search.trim() } : {});
    }

    function updateField(field, value) {
        setForm((prev) => ({ ...prev, [field]: value }));
    }

    async function createBookCopy(e) {
        e.preventDefault();
        setLoading(true);

        try {
            await libraryApi.createBookCopy({
                ...form,
                maVach: form.maVach || null,
                maQrCode: form.maQrCode || null,
                ghiChu: form.ghiChu || null
            });

            toast.success("Thêm cuốn sách thành công");
            updateField("maCuonSach", `COPY_${Date.now().toString().slice(-6)}`);
            setShowCreateModal(false);
            await load();
        } catch (err) {
            toast.error(err.message || "Thêm cuốn sách thất bại");
        } finally {
            setLoading(false);
        }
    }

    async function deleteBookCopy(maCuonSach) {
        const mode = await actionDialog.chooseDeleteMode(`cuốn sách ${maCuonSach}`);

        if (!mode) {
            return;
        }

        setLoading(true);

        try {
            await libraryApi.deleteBookCopy(maCuonSach, mode);
            toast.success(mode === "hard" ? "Đã xóa cuốn sách" : "Đã ngừng lưu thông cuốn sách");
            setSelectedIds((prev) => prev.filter((id) => id !== maCuonSach));
            await load();
        } catch (err) {
            toast.error(err.message || "Xóa cuốn sách thất bại");
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

        if (!mode) {
            return;
        }

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

            <form className="panel search-panel" onSubmit={submitSearch}>
                <input
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder="Tìm theo mã cuốn, đầu sách, chi nhánh, vị trí, trạng thái..."
                />
                <button className="soft-button" type="submit">Tìm kiếm</button>
            </form>

            {showCreateModal && (
                <ResultModal title="Thêm cuốn sách" onClose={() => setShowCreateModal(false)} className="form-modal-card">
            <form className="form-panel modal-form" onSubmit={createBookCopy}>
                <div className="panel-title">
                    <h2>Thêm cuốn sách</h2>
                    <Plus size={20} />
                </div>

                <div className="form-grid-3">
                    <div className="form-row">
                        <label>Mã cuốn sách</label>
                        <input value={form.maCuonSach} onChange={(e) => updateField("maCuonSach", e.target.value)} />
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
                        <select value={form.maTrangThai} onChange={(e) => updateField("maTrangThai", e.target.value)}>
                            <option value="TT_SANCO">Sẵn có</option>
                            <option value="TT_DANGMUON">Đang mượn</option>
                            <option value="TT_DANGDATTRUOC">Đang đặt trước</option>
                            <option value="TT_HONG">Hỏng</option>
                            <option value="TT_MAT">Mất</option>
                            <option value="TT_NGUNGLUUTHONG">Ngừng lưu thông</option>
                        </select>
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
                    Thêm cuốn sách
                </button>
            </form>
                </ResultModal>
            )}

            <div className="list-toolbar">
                <button className="primary-button" type="button" onClick={() => setShowCreateModal(true)}>
                    <Plus size={17} />
                    Thêm cuốn sách
                </button>

                <div className="selection-toolbar">
                    <button className="soft-button" type="button" onClick={selectAllVisible}>
                        Chọn tất cả
                    </button>
                    <button className="ghost-button" type="button" onClick={clearSelected}>
                        Bỏ chọn tất cả
                    </button>
                    <button className="soft-button danger-button" type="button" onClick={deleteSelectedBookCopies} disabled={selectedIds.length === 0 || loading}>
                        <Trash2 size={15} />
                        Xóa
                    </button>
                    <span>{selectedIds.length} mục đã chọn</span>
                </div>
            </div>

            <DataTable
                data={filteredData}
                columns={[
                    {
                        key: "select",
                        title: "",
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
                        render: (row) => (
                            <button className="soft-button danger-button" onClick={() => deleteBookCopy(row.maCuonSach)}>
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
