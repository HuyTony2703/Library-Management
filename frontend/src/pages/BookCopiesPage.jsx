import { Download, EyeOff, Pencil, Plus, Printer, RotateCcw, Trash2, X } from "lucide-react";
import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { libraryApi } from "../api/libraryApi";
import AsyncEntityPicker from "../components/AsyncEntityPicker";
import BookCopyActionDialog from "../components/BookCopyActionDialog";
import BookCopyDetailDrawer from "../components/BookCopyDetailDrawer";
import DataTable from "../components/DataTable";
import InlineActionMenu from "../components/InlineActionMenu";
import PageHeader from "../components/PageHeader";
import ResultModal from "../components/ResultModal";
import StatusBadge from "../components/StatusBadge";
import { useActionDialog } from "../components/ActionDialogProvider";
import { useToast } from "../components/ToastProvider";
import { useAuth } from "../context/AuthContext";
import { formatDate } from "../utils/displayUtils";
import { downloadCsvExport, resolveExportResult } from "../utils/exportUtils";
import { isAdmin } from "../utils/roleUtils";

const EMPTY_LOCATIONS = { areas: [], shelves: [], locations: [] };

export default function BookCopiesPage() {
    const toast = useToast();
    const actionDialog = useActionDialog();
    const { user, staffContext } = useAuth();
    const submittingRef = useRef(false);
    const requestSequence = useRef(0);
    const [searchParams, setSearchParams] = useSearchParams();
    const [data, setData] = useState([]);
    const [pageInfo, setPageInfo] = useState({ totalItems: 0, totalPages: 0 });
    const [statusOptions, setStatusOptions] = useState([]);
    const [branchOptions, setBranchOptions] = useState([]);
    const [locationOptions, setLocationOptions] = useState(EMPTY_LOCATIONS);
    const [filterLoading, setFilterLoading] = useState(true);
    const [filterError, setFilterError] = useState("");
    const [tableLoading, setTableLoading] = useState(false);
    const [tableError, setTableError] = useState("");
    const [operationLoading, setOperationLoading] = useState(false);
    const [exportLoading, setExportLoading] = useState(false);
    const [reloadVersion, setReloadVersion] = useState(0);
    const [showModal, setShowModal] = useState(false);
    const [editingCopy, setEditingCopy] = useState(null);
    const [selectedBookTitle, setSelectedBookTitle] = useState(null);
    const [titleFilterLabels, setTitleFilterLabels] = useState({});
    const [selectedIds, setSelectedIds] = useState([]);
    const [form, setForm] = useState(() => buildDefaultCopyForm());
    const [batchForm, setBatchForm] = useState(() => buildDefaultBatchForm());
    const [batchLocations, setBatchLocations] = useState(EMPTY_LOCATIONS);
    const [batchLocationLoading, setBatchLocationLoading] = useState(false);
    const [batchLocationError, setBatchLocationError] = useState("");
    const [batchPreview, setBatchPreview] = useState(false);
    const [batchResult, setBatchResult] = useState(null);
    const [formError, setFormError] = useState("");
    const [drawerCopyId, setDrawerCopyId] = useState(null);
    const [copyAction, setCopyAction] = useState(null);
    const [labelCopyIds, setLabelCopyIds] = useState([]);
    const [labelTemplate, setLabelTemplate] = useState("STANDARD");
    const [labelPreview, setLabelPreview] = useState(null);
    const [labelLoading, setLabelLoading] = useState(false);
    const [labelError, setLabelError] = useState("");

    const urlSearch = searchParams.get("search") || "";
    const [searchDraft, setSearchDraft] = useState({ urlValue: urlSearch, value: urlSearch });
    const search = searchDraft.urlValue === urlSearch ? searchDraft.value : urlSearch;
    const page = readPositiveInteger(searchParams.get("page"), 1);
    const pageSize = readPageSize(searchParams.get("pageSize"));
    const sort = readSort(searchParams.get("sort"));
    const statusIds = searchParams.get("statusIds") || "";
    const branchId = searchParams.get("branchIds") || "";
    const titleId = searchParams.get("titleIds") || "";
    const areaId = searchParams.get("areaIds") || "";
    const shelfId = searchParams.get("shelfIds") || "";
    const locationId = searchParams.get("locationIds") || "";
    const importedFrom = searchParams.get("importedFrom") || "";
    const importedTo = searchParams.get("importedTo") || "";
    const hasBarcode = readBoolean(searchParams.get("hasBarcode"));
    const hasQr = readBoolean(searchParams.get("hasQr"));
    const activePreset = searchParams.get("preset") || "";
    const titleFilterOption = titleId ? { value: titleId, code: titleId, label: titleFilterLabels[titleId] || titleId } : null;
    const visibleShelves = locationOptions.shelves.filter((option) => !areaId || option.parentId === areaId);
    const visibleShelfIds = new Set(visibleShelves.map((option) => option.value));
    const visibleLocations = locationOptions.locations.filter((option) => shelfId
        ? option.parentId === shelfId
        : !areaId || visibleShelfIds.has(option.parentId));

    const updateUrl = useCallback((changes, replace = false) => {
        setSearchParams((current) => {
            const next = new URLSearchParams(current);
            Object.entries(changes).forEach(([key, value]) => {
                if (value === null || value === undefined || value === "") next.delete(key);
                else next.set(key, String(value));
            });
            return next;
        }, { replace });
    }, [setSearchParams]);

    function reloadCopies() {
        setReloadVersion((value) => value + 1);
    }

    useEffect(() => {
        const controller = new AbortController();
        const sequence = ++requestSequence.current;
        // Fetching is an external synchronization; each query starts a new loading state.
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setTableLoading(true);
        setTableError("");
        libraryApi.bookCopiesPage({
            page,
            pageSize,
            search: urlSearch,
            sort: `${sort.field},${sort.direction}`,
            statusIds: splitValues(statusIds),
            branchIds: branchId ? [branchId] : [],
            titleIds: titleId ? [titleId] : [],
            areaIds: areaId ? [areaId] : [],
            shelfIds: shelfId ? [shelfId] : [],
            locationIds: locationId ? [locationId] : [],
            importedFrom,
            importedTo,
            hasBarcode,
            hasQr
        }, { signal: controller.signal })
            .then((response) => {
                if (sequence !== requestSequence.current) return;
                setData(Array.isArray(response.items) ? response.items : []);
                setPageInfo({
                    totalItems: Number(response.totalItems) || 0,
                    totalPages: Number(response.totalPages) || 0
                });
                if (response.totalPages > 0 && page > response.totalPages) {
                    updateUrl({ page: response.totalPages });
                }
            })
            .catch((error) => {
                if (error.name !== "AbortError" && sequence === requestSequence.current) {
                    setTableError(error.message || "Không tải được danh sách cuốn sách");
                }
            })
            .finally(() => {
                if (sequence === requestSequence.current) setTableLoading(false);
            });
        return () => controller.abort();
    }, [page, pageSize, urlSearch, sort.field, sort.direction, statusIds, branchId, titleId, areaId, shelfId, locationId, importedFrom, importedTo, hasBarcode, hasQr, reloadVersion, updateUrl]);

    useEffect(() => {
        let active = true;
        // Options are synchronized with the effective branch scope.
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setFilterLoading(true);
        Promise.all([
            libraryApi.bookCopyStatuses(),
            libraryApi.bookCopyLocationFilters(branchId ? [branchId] : []),
            isAdmin(user)
                ? libraryApi.branches()
                : Promise.resolve(staffContext?.allowedBranches?.map((branch) => ({ value: branch.id, label: branch.name })) || [])
        ]).then(([statuses, locations, branches]) => {
            if (!active) return;
            setStatusOptions(Array.isArray(statuses) ? statuses : []);
            setLocationOptions(locations || EMPTY_LOCATIONS);
            setBranchOptions(Array.isArray(branches) ? branches : []);
            setFilterError("");
        }).catch((error) => {
            if (active) setFilterError(error.message || "Không tải được dữ liệu bộ lọc");
        }).finally(() => {
            if (active) setFilterLoading(false);
        });
        return () => { active = false; };
    }, [branchId, staffContext, user]);

    useEffect(() => {
        const timer = window.setTimeout(() => {
            const normalized = search.trim();
            if (normalized !== urlSearch) updateUrl({ search: normalized || null, page: 1 }, true);
        }, 250);
        return () => window.clearTimeout(timer);
    }, [search, urlSearch, updateUrl]);

    function changeFilter(name, value, extra = {}) {
        updateUrl({ [name]: value || null, preset: null, page: 1, ...extra });
    }

    function clearFilters() {
        setSearchDraft({ urlValue: "", value: "" });
        updateUrl({
            search: null,
            statusIds: null,
            branchIds: null,
            titleIds: null,
            areaIds: null,
            shelfIds: null,
            locationIds: null,
            importedFrom: null,
            importedTo: null,
            hasBarcode: null,
            hasQr: null,
            preset: null,
            page: 1
        });
    }

    function applyPreset(preset) {
        const statusValues = preset === "available"
            ? findStatusValues(statusOptions, ["sẵn có"])
            : preset === "borrowed"
                ? findStatusValues(statusOptions, ["đang được mượn", "đang mượn"])
                : preset === "damaged-lost"
                    ? findStatusValues(statusOptions, ["bị hỏng", "hỏng", "bị mất", "mất"])
                    : [];
        const dateRange = preset === "recent" ? lastThirtyDays() : { from: null, to: null };
        updateUrl({
            statusIds: statusValues.length ? statusValues.join(",") : null,
            importedFrom: dateRange.from,
            importedTo: dateRange.to,
            preset,
            page: 1
        });
    }

    function updateField(field, value) {
        setForm((current) => ({ ...current, [field]: value }));
    }

    function updateBatchField(field, value) {
        setBatchPreview(false);
        setFormError("");
        setBatchForm((current) => ({ ...current, [field]: value }));
    }

    function setBatchBranch(branchId) {
        setBatchPreview(false);
        setFormError("");
        setBatchForm((current) => ({ ...current, branchId, locationId: "" }));
    }

    function openCreateModal() {
        setEditingCopy(null);
        setSelectedBookTitle(null);
        setBatchForm(buildDefaultBatchForm(staffContext?.defaultBranch?.id));
        setBatchPreview(false);
        setBatchResult(null);
        setFormError("");
        setShowModal(true);
    }

    function openEditModal(row) {
        setEditingCopy(row);
        setSelectedBookTitle({ value: row.maDauSach, code: row.maDauSach, label: row.tenDauSach || row.maDauSach });
        setForm({
            maCuonSach: row.maCuonSach || "",
            maDauSach: row.maDauSach || "",
            maChiNhanh: row.maChiNhanh || "",
            maViTri: row.maViTri || "",
            maTrangThai: row.maTrangThai || "",
            maVach: row.maVach || "",
            maQrCode: row.maQrCode || "",
            ngayNhapSach: row.ngayNhapSach || "",
            ghiChu: row.ghiChu || ""
        });
        setDrawerCopyId(null);
        setShowModal(true);
    }

    function closeModal() {
        setShowModal(false);
        setEditingCopy(null);
        setSelectedBookTitle(null);
        setForm(buildDefaultCopyForm());
        setBatchForm(buildDefaultBatchForm());
        setBatchLocations(EMPTY_LOCATIONS);
        setBatchPreview(false);
        setFormError("");
    }

    useEffect(() => {
        if (!showModal || editingCopy || !batchForm.branchId) {
            // The available locations mirror the currently selected branch.
            // eslint-disable-next-line react-hooks/set-state-in-effect
            setBatchLocations(EMPTY_LOCATIONS);
            setBatchLocationError("");
            return;
        }
        const controller = new AbortController();
        setBatchLocationLoading(true);
        setBatchLocationError("");
        libraryApi.bookCopyLocationFilters([batchForm.branchId], { signal: controller.signal })
            .then((result) => setBatchLocations(result || EMPTY_LOCATIONS))
            .catch((error) => {
                if (error.name !== "AbortError") setBatchLocationError(error.message || "Không tải được vị trí của chi nhánh");
            })
            .finally(() => {
                if (!controller.signal.aborted) setBatchLocationLoading(false);
            });
        return () => controller.abort();
    }, [showModal, editingCopy, batchForm.branchId]);

    function updateManualBarcode(index, field, value) {
        setBatchPreview(false);
        setFormError("");
        setBatchForm((current) => ({
            ...current,
            copies: current.copies.map((copy, copyIndex) => copyIndex === index ? { ...copy, [field]: value } : copy)
        }));
    }

    function addManualBarcode() {
        if (batchForm.copies.length >= 100) return;
        setBatchPreview(false);
        setBatchForm((current) => ({ ...current, copies: [...current.copies, { barcode: "", note: "" }] }));
    }

    function removeManualBarcode(index) {
        if (batchForm.copies.length === 1) return;
        setBatchPreview(false);
        setBatchForm((current) => ({ ...current, copies: current.copies.filter((_, copyIndex) => copyIndex !== index) }));
    }

    function previewBatch(event) {
        event.preventDefault();
        setFormError("");
        setBatchPreview(true);
    }

    async function saveBookCopy(event) {
        event.preventDefault();
        if (submittingRef.current) return;
        submittingRef.current = true;
        setOperationLoading(true);
        setFormError("");
        try {
            if (editingCopy) {
                const payload = {
                    ...form,
                    maVach: form.maVach || null,
                    maQrCode: form.maQrCode || null,
                    ngayNhapSach: form.ngayNhapSach || null,
                    ghiChu: form.ghiChu || null
                };
                await libraryApi.updateBookCopy(editingCopy.maCuonSach, payload);
                toast.success("Cập nhật cuốn sách thành công");
                closeModal();
            } else {
                const payload = {
                    titleId: batchForm.titleId,
                    branchId: batchForm.branchId,
                    locationId: batchForm.locationId,
                    importDate: batchForm.importDate,
                    barcodeMode: batchForm.barcodeMode,
                    quantity: batchForm.barcodeMode === "MANUAL" ? null : Number(batchForm.quantity),
                    note: batchForm.note || null,
                    copies: batchForm.barcodeMode === "MANUAL"
                        ? batchForm.copies.map((copy) => ({ barcode: copy.barcode.trim(), note: copy.note.trim() || null }))
                        : null
                };
                const result = await libraryApi.createBookCopyBatch(payload);
                setBatchResult(result);
                setShowModal(false);
                toast.success(`Đã nhập ${result.created} cuốn sách`);
            }
            reloadCopies();
        } catch (error) {
            const message = error.message || "Lưu cuốn sách thất bại";
            setFormError(message);
            toast.error(message);
        } finally {
            submittingRef.current = false;
            setOperationLoading(false);
        }
    }

    function openCopyAction(copy, action) {
        setDrawerCopyId(null);
        setCopyAction({ copy, action });
    }

    function completeCopyAction(result) {
        const messages = {
            MOVE_LOCATION: "Đã chuyển vị trí cuốn sách",
            MARK_DAMAGED: "Đã báo hỏng cuốn sách",
            MARK_LOST: "Đã báo mất cuốn sách",
            WITHDRAW: "Đã ngừng lưu thông cuốn sách",
            RESTORE_AFTER_REPAIR: "Đã khôi phục cuốn sách sau sửa chữa",
            RESTORE_FOUND: "Đã khôi phục cuốn sách sau khi tìm lại"
        };
        toast.success(messages[result.action] || "Đã cập nhật cuốn sách");
        setCopyAction(null);
        reloadCopies();
    }

    async function hardDeleteBookCopy(copyId) {
        if (operationLoading) return;
        const confirmed = await actionDialog.confirm({
            title: `Xóa cuốn sách ${copyId}`,
            message: "Thao tác này sẽ xóa vĩnh viễn nếu dữ liệu chưa có liên kết nghiệp vụ.",
            confirmLabel: "Xóa",
            danger: true
        });
        if (!confirmed) return;
        setOperationLoading(true);
        try {
            await libraryApi.deleteBookCopy(copyId, "hard");
            toast.success("Đã xóa cuốn sách");
            setSelectedIds((current) => current.filter((id) => id !== copyId));
            setDrawerCopyId(null);
            reloadCopies();
        } catch (error) {
            toast.error(error.message || "Xóa cuốn sách thất bại");
        } finally {
            setOperationLoading(false);
        }
    }

    function currentExportFilters() {
        return {
            page,
            pageSize,
            search: urlSearch,
            sort: `${sort.field},${sort.direction}`,
            statusIds: splitValues(statusIds),
            branchIds: branchId ? [branchId] : [],
            titleIds: titleId ? [titleId] : [],
            areaIds: areaId ? [areaId] : [],
            shelfIds: shelfId ? [shelfId] : [],
            locationIds: locationId ? [locationId] : [],
            importedFrom,
            importedTo,
            hasBarcode,
            hasQr
        };
    }

    async function exportBookCopies(scope) {
        if (exportLoading) return;
        if (scope === "SELECTED" && selectedIds.length === 0) {
            toast.error("Chưa chọn cuốn sách để export");
            return;
        }
        setExportLoading(true);
        try {
            const result = await libraryApi.exportBookCopies({
                scope,
                ids: scope === "SELECTED" ? selectedIds : [],
                filters: currentExportFilters(),
                excludedIds: []
            });
            const ready = await resolveExportResult(result, () => toast.info("Tập dữ liệu lớn, đang tạo file export"));
            downloadCsvExport(ready);
            toast.success(`Đã export ${Number(ready.totalRows || 0).toLocaleString("vi-VN")} dòng`);
        } catch (error) {
            toast.error(error.message || "Export cuốn sách thất bại");
        } finally {
            setExportLoading(false);
        }
    }

    async function deleteSelectedBookCopies() {
        if (!selectedIds.length || operationLoading) return;
        const confirmed = await actionDialog.confirm({
            title: `Xóa vĩnh viễn ${selectedIds.length} cuốn sách đã chọn`,
            message: "Chỉ các cuốn chưa có liên kết nghiệp vụ mới có thể xóa. Ngừng lưu thông phải thực hiện từng cuốn và nhập lý do.",
            confirmLabel: "Xóa vĩnh viễn",
            danger: true
        });
        if (!confirmed) return;
        setOperationLoading(true);
        try {
            for (const id of selectedIds) await libraryApi.deleteBookCopy(id, "hard");
            toast.success("Đã xóa các cuốn sách đã chọn");
            setSelectedIds([]);
            reloadCopies();
        } catch (error) {
            toast.error(error.message || "Không xử lý được các cuốn sách đã chọn");
        } finally {
            setOperationLoading(false);
        }
    }

    async function openLabelPreview(copyIds) {
        const ids = [...new Set(copyIds.filter(Boolean))];
        if (!ids.length) return;
        setDrawerCopyId(null);
        setLabelCopyIds(ids);
        setLabelError("");
        setLabelPreview(null);
        setLabelLoading(true);
        try {
            const result = await libraryApi.previewBookCopyBarcodeLabels({
                copyIds: ids,
                template: labelTemplate,
                generateMissing: true
            });
            setLabelPreview(result);
            if (result.generatedBarcodes > 0) reloadCopies();
        } catch (error) {
            setLabelError(error.message || "Không tạo được preview nhãn barcode");
        } finally {
            setLabelLoading(false);
        }
    }

    async function changeLabelTemplate(template) {
        setLabelTemplate(template);
        if (labelCopyIds.length) {
            setLabelError("");
            setLabelLoading(true);
            try {
                const result = await libraryApi.previewBookCopyBarcodeLabels({
                    copyIds: labelCopyIds,
                    template,
                    generateMissing: true
                });
                setLabelPreview(result);
                if (result.generatedBarcodes > 0) reloadCopies();
            } catch (error) {
                setLabelError(error.message || "Không tạo được preview nhãn barcode");
            } finally {
                setLabelLoading(false);
            }
        }
    }

    async function printLabels() {
        if (!labelCopyIds.length || labelLoading) return;
        setLabelError("");
        setLabelLoading(true);
        try {
            const result = await libraryApi.printBookCopyBarcodeLabels({
                copyIds: labelCopyIds,
                template: labelTemplate,
                generateMissing: true
            });
            setLabelPreview(result);
            if (result.generatedBarcodes > 0) reloadCopies();
            window.setTimeout(() => window.print(), 80);
        } catch (error) {
            setLabelError(error.message || "Không ghi được lịch sử in nhãn");
        } finally {
            setLabelLoading(false);
        }
    }

    function closeLabelPreview() {
        setLabelCopyIds([]);
        setLabelPreview(null);
        setLabelError("");
    }

    return (
        <div>
            <PageHeader eyebrow="Inventory" title="Quản lý cuốn sách" description="Theo dõi từng bản sách vật lý, vị trí, chi nhánh và trạng thái." />

            <div className="copy-presets" aria-label="Bộ lọc nhanh">
                <PresetButton active={activePreset === "available"} onClick={() => applyPreset("available")} disabled={filterLoading}>Sẵn có</PresetButton>
                <PresetButton active={activePreset === "borrowed"} onClick={() => applyPreset("borrowed")} disabled={filterLoading}>Đang mượn</PresetButton>
                <PresetButton active={activePreset === "damaged-lost"} onClick={() => applyPreset("damaged-lost")} disabled={filterLoading}>Hỏng / mất</PresetButton>
                <PresetButton active={activePreset === "recent"} onClick={() => applyPreset("recent")}>Nhập 30 ngày</PresetButton>
            </div>

            <div className="panel copy-filter-panel">
                <FilterField label="Tìm kiếm" wide>
                    <input value={search} onChange={(event) => setSearchDraft({ urlValue: urlSearch, value: event.target.value })} placeholder="Mã cuốn, barcode, QR, tên đầu sách hoặc ISBN" />
                </FilterField>
                <FilterField label="Trạng thái">
                    <select value={splitValues(statusIds).length === 1 ? statusIds : ""} onChange={(event) => changeFilter("statusIds", event.target.value)} disabled={filterLoading}>
                        <option value="">Tất cả trạng thái</option>
                        {statusOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                    </select>
                </FilterField>
                <FilterField label="Chi nhánh">
                    <select value={branchId} onChange={(event) => changeFilter("branchIds", event.target.value, { areaIds: null, shelfIds: null, locationIds: null })} disabled={filterLoading}>
                        <option value="">Tất cả chi nhánh được phép</option>
                        {branchOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                    </select>
                </FilterField>
                <FilterField label="Đầu sách">
                    <AsyncEntityPicker
                        key={titleId || "all-titles"}
                        value={titleFilterOption}
                        onChange={(option) => {
                            if (option) setTitleFilterLabels((current) => ({ ...current, [option.value]: option.label }));
                            changeFilter("titleIds", option?.value || null);
                        }}
                        loadOptions={searchAllBookTitles}
                        placeholder="Tìm mã, tên hoặc ISBN"
                        ariaLabel="Lọc theo đầu sách"
                    />
                </FilterField>
                <FilterField label="Khu / kho">
                    <select value={areaId} onChange={(event) => changeFilter("areaIds", event.target.value, { shelfIds: null, locationIds: null })} disabled={filterLoading}>
                        <option value="">Tất cả khu / kho</option>
                        {locationOptions.areas.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                    </select>
                </FilterField>
                <FilterField label="Kệ">
                    <select value={shelfId} onChange={(event) => changeFilter("shelfIds", event.target.value, { locationIds: null })} disabled={filterLoading}>
                        <option value="">Tất cả kệ</option>
                        {visibleShelves.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                    </select>
                </FilterField>
                <FilterField label="Vị trí">
                    <select value={locationId} onChange={(event) => changeFilter("locationIds", event.target.value)} disabled={filterLoading}>
                        <option value="">Tất cả vị trí</option>
                        {visibleLocations.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                    </select>
                </FilterField>
                <FilterField label="Ngày nhập từ"><input type="date" value={importedFrom} onChange={(event) => changeFilter("importedFrom", event.target.value)} /></FilterField>
                <FilterField label="Ngày nhập đến"><input type="date" value={importedTo} onChange={(event) => changeFilter("importedTo", event.target.value)} /></FilterField>
                <FilterField label="Barcode">
                    <PresenceSelect value={hasBarcode} onChange={(value) => changeFilter("hasBarcode", value)} />
                </FilterField>
                <FilterField label="QR">
                    <PresenceSelect value={hasQr} onChange={(value) => changeFilter("hasQr", value)} />
                </FilterField>
                <button className="ghost-button copy-clear-filters" type="button" onClick={clearFilters}><X size={16} /> Xóa bộ lọc</button>
                {filterError && <div className="copy-filter-error" role="alert">{filterError}</div>}
            </div>

            {showModal && (
                <ResultModal title={editingCopy ? "Sửa thông tin cuốn sách" : "Nhập lô cuốn sách"} onClose={closeModal} className="form-modal-card batch-copy-modal">
                    {editingCopy ? <form className="form-panel modal-form" onSubmit={saveBookCopy}>
                        <div className="form-grid-3">
                            <FilterField label="Mã cuốn sách"><input value={form.maCuonSach} onChange={(event) => updateField("maCuonSach", event.target.value)} disabled={Boolean(editingCopy)} required /></FilterField>
                            <FilterField label="Đầu sách">
                                <AsyncEntityPicker value={selectedBookTitle} onChange={(option) => { setSelectedBookTitle(option); updateField("maDauSach", option?.value || ""); }} loadOptions={libraryApi.searchBookTitles} placeholder="Tìm mã, tên hoặc ISBN" ariaLabel="Chọn đầu sách" required disabled={operationLoading} />
                            </FilterField>
                            <FilterField label="Chi nhánh"><input value={editingCopy.tenChiNhanh || form.maChiNhanh} disabled /></FilterField>
                        </div>
                        <div className="form-grid-3">
                            <FilterField label="Vị trí"><input value={editingCopy.viTriLabel || form.maViTri} disabled /></FilterField>
                            <FilterField label="Trạng thái"><input value={editingCopy.tenTrangThai || form.maTrangThai} disabled /></FilterField>
                            <FilterField label="Ngày nhập"><input type="date" value={form.ngayNhapSach} onChange={(event) => updateField("ngayNhapSach", event.target.value)} /></FilterField>
                        </div>
                        <div className="form-grid-3">
                            <FilterField label="Mã vạch"><input value={form.maVach} onChange={(event) => updateField("maVach", event.target.value)} /></FilterField>
                            <FilterField label="Mã QR"><input value={form.maQrCode} onChange={(event) => updateField("maQrCode", event.target.value)} /></FilterField>
                            <FilterField label="Ghi chú"><input value={form.ghiChu} onChange={(event) => updateField("ghiChu", event.target.value)} /></FilterField>
                        </div>
                        {formError && <div className="copy-filter-error" role="alert">{formError}</div>}
                        <button className="primary-button" disabled={operationLoading}>{operationLoading ? "Đang lưu..." : "Lưu thông tin"}</button>
                    </form> : <form className="batch-copy-form" onSubmit={batchPreview ? saveBookCopy : previewBatch}>
                        <fieldset disabled={operationLoading}>
                            <legend>Thông tin lô</legend>
                            <div className="form-grid-2">
                                <FilterField label="Đầu sách">
                                    <AsyncEntityPicker value={selectedBookTitle} onChange={(option) => { setSelectedBookTitle(option); updateBatchField("titleId", option?.value || ""); }} loadOptions={libraryApi.searchBookTitles} placeholder="Tìm mã, tên hoặc ISBN" ariaLabel="Chọn đầu sách đang hiển thị" required disabled={operationLoading} />
                                </FilterField>
                                <FilterField label="Chi nhánh theo quyền">
                                    <select value={batchForm.branchId} onChange={(event) => setBatchBranch(event.target.value)} required>
                                        <option value="">Chọn chi nhánh</option>
                                        {branchOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                                    </select>
                                </FilterField>
                                <FilterField label="Vị trí">
                                    <select value={batchForm.locationId} onChange={(event) => updateBatchField("locationId", event.target.value)} required disabled={!batchForm.branchId || batchLocationLoading || Boolean(batchLocationError)}>
                                        <option value="">{batchLocationLoading
                                            ? "Đang tải vị trí..."
                                            : batchForm.branchId && batchLocations.locations.length === 0
                                                ? "Chi nhánh chưa có vị trí"
                                                : "Chọn vị trí"}</option>
                                        {batchLocations.locations.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
                                    </select>
                                </FilterField>
                                <FilterField label="Ngày nhập"><input type="date" value={batchForm.importDate} onChange={(event) => updateBatchField("importDate", event.target.value)} required /></FilterField>
                            </div>
                            {batchLocationError && <div className="copy-filter-error" role="alert">{batchLocationError}</div>}
                        </fieldset>
                        <fieldset disabled={operationLoading}>
                            <legend>Barcode và số lượng</legend>
                            <div className="batch-barcode-modes">
                                {[["AUTO", "Tự sinh"], ["MANUAL", "Nhập / quét thủ công"], ["LATER", "Bổ sung sau"]].map(([value, label]) => (
                                    <label key={value}><input type="radio" name="barcodeMode" value={value} checked={batchForm.barcodeMode === value} onChange={(event) => updateBatchField("barcodeMode", event.target.value)} /> {label}</label>
                                ))}
                            </div>
                            {batchForm.barcodeMode === "MANUAL" ? <div className="manual-barcode-list">
                                {batchForm.copies.map((copy, index) => <div className="manual-barcode-row" key={index}>
                                    <span>{index + 1}</span>
                                    <input value={copy.barcode} onChange={(event) => updateManualBarcode(index, "barcode", event.target.value)} placeholder="Barcode" aria-label={`Barcode dòng ${index + 1}`} required />
                                    <input value={copy.note} onChange={(event) => updateManualBarcode(index, "note", event.target.value)} placeholder="Ghi chú riêng (không bắt buộc)" aria-label={`Ghi chú dòng ${index + 1}`} />
                                    <button className="icon-button" type="button" onClick={() => removeManualBarcode(index)} disabled={batchForm.copies.length === 1} aria-label={`Xóa dòng ${index + 1}`}><X size={16} /></button>
                                </div>)}
                                <button className="soft-button" type="button" onClick={addManualBarcode} disabled={batchForm.copies.length >= 100}><Plus size={16} /> Thêm barcode</button>
                            </div> : <div className="form-grid-2">
                                <FilterField label="Số lượng"><input type="number" min="1" max="100" value={batchForm.quantity} onChange={(event) => updateBatchField("quantity", event.target.value)} required /></FilterField>
                                <FilterField label="Ghi chú chung"><input value={batchForm.note} maxLength="255" onChange={(event) => updateBatchField("note", event.target.value)} placeholder="Không bắt buộc" /></FilterField>
                            </div>}
                        </fieldset>
                        {batchPreview && <BatchPreview form={batchForm} title={selectedBookTitle} branches={branchOptions} locations={batchLocations.locations} />}
                        {formError && <div className="copy-filter-error" role="alert">{formError}</div>}
                        <div className="batch-form-actions">
                            {batchPreview && <button className="ghost-button" type="button" onClick={() => setBatchPreview(false)} disabled={operationLoading}>Sửa thông tin</button>}
                            <button className="primary-button" disabled={operationLoading || batchLocationLoading}>{operationLoading ? "Đang tạo lô..." : batchPreview ? "Xác nhận nhập lô" : "Xem trước lô"}</button>
                        </div>
                    </form>}
                </ResultModal>
            )}

            {batchResult && <ResultModal title="Kết quả nhập lô" onClose={() => setBatchResult(null)} className="batch-result-modal">
                <div className="batch-result-summary"><strong>{batchResult.created} cuốn đã được tạo</strong><span>Mã lô: {batchResult.batchId}</span><span>{batchResult.labelReady ? "Đã đủ barcode để in nhãn sau này" : "Có cuốn cần bổ sung barcode trước khi in nhãn"}</span></div>
                <div className="batch-result-list">{batchResult.copies?.map((copy) => <div key={copy.id}><strong>{copy.id}</strong><span>{copy.barcode || "Chưa có barcode"}</span><StatusBadge value={copy.status} /></div>)}</div>
            </ResultModal>}

            {copyAction && <BookCopyActionDialog
                copy={copyAction.copy}
                action={copyAction.action}
                onClose={() => setCopyAction(null)}
                onSuccess={completeCopyAction}
            />}

            {drawerCopyId && <BookCopyDetailDrawer copyId={drawerCopyId} onClose={() => setDrawerCopyId(null)} onEdit={openEditModal} />}

            {labelCopyIds.length > 0 && <ResultModal title="In nhãn barcode" onClose={closeLabelPreview} className="barcode-label-modal">
                <div className="barcode-label-toolbar">
                    <FilterField label="Template">
                        <select value={labelTemplate} onChange={(event) => changeLabelTemplate(event.target.value)} disabled={labelLoading}>
                            <option value="STANDARD">Tiêu chuẩn</option>
                            <option value="COMPACT">Nhỏ gọn</option>
                        </select>
                    </FilterField>
                    <div className="barcode-label-actions">
                        <span>{labelPreview?.total || labelCopyIds.length} nhãn</span>
                        <button className="primary-button" type="button" onClick={printLabels} disabled={labelLoading || !labelPreview?.labels?.length}><Printer size={16} /> In nhãn</button>
                    </div>
                </div>
                {labelError && <div className="copy-filter-error" role="alert">{labelError}</div>}
                {labelLoading && <div className="copy-filter-error">Đang tạo preview nhãn...</div>}
                {labelPreview?.generatedBarcodes > 0 && <div className="barcode-label-note">Đã sinh {labelPreview.generatedBarcodes} barcode mới cho các cuốn chưa có mã.</div>}
                {labelPreview?.labels?.length > 0 && <BarcodeLabelSheet labels={labelPreview.labels} template={labelTemplate} />}
            </ResultModal>}

            <div className="list-toolbar">
                <button className="primary-button" type="button" onClick={openCreateModal}><Plus size={17} /> Nhập lô cuốn sách</button>
                <div className="selection-toolbar">
                    <button className="soft-button" type="button" onClick={() => openLabelPreview(selectedIds)} disabled={!selectedIds.length || operationLoading}><Printer size={15} /> In nhãn đã chọn</button>
                    <button className="soft-button" type="button" onClick={() => exportBookCopies("SELECTED")} disabled={!selectedIds.length || exportLoading}><Download size={15} /> Export đã chọn</button>
                    <button className="soft-button" type="button" onClick={() => exportBookCopies("PAGE")} disabled={!data.length || exportLoading}><Download size={15} /> Export trang này</button>
                    <button className="soft-button" type="button" onClick={() => exportBookCopies("ALL_MATCHING")} disabled={pageInfo.totalItems === 0 || exportLoading}><Download size={15} /> Export tất cả kết quả</button>
                    <button className="soft-button danger-button" type="button" onClick={deleteSelectedBookCopies} disabled={!selectedIds.length || operationLoading}><Trash2 size={15} /> Xóa mục đã chọn</button>
                </div>
            </div>

            <DataTable
                mode="server"
                data={data}
                rowKey="maCuonSach"
                loading={tableLoading}
                error={tableError}
                onRetry={reloadCopies}
                emptyText="Không có cuốn sách phù hợp"
                onRowClick={(row) => setDrawerCopyId(row.maCuonSach)}
                pagination={{
                    page,
                    pageSize,
                    totalItems: pageInfo.totalItems,
                    totalPages: pageInfo.totalPages,
                    onPageChange: (nextPage) => updateUrl({ page: nextPage }),
                    onPageSizeChange: (nextSize) => updateUrl({ pageSize: nextSize, page: 1 })
                }}
                sort={sort}
                onSortChange={(nextSort) => updateUrl({ sort: nextSort ? `${nextSort.field},${nextSort.direction}` : null, page: 1 })}
                selectable
                selectedRowKeys={selectedIds}
                onSelectionChange={setSelectedIds}
                bulkActions={[]}
                columns={copyColumns(operationLoading, openEditModal, openCopyAction, hardDeleteBookCopy, openLabelPreview)}
            />
        </div>
    );
}

function copyColumns(operationLoading, openEditModal, openCopyAction, hardDeleteBookCopy, openLabelPreview) {
    return [
        {
            key: "title",
            title: "Đầu sách",
            width: "30%",
            minWidth: "280px",
            wrap: true,
            sortable: true,
            sortKey: "title",
            render: (row) => <span className="copy-title-cell"><strong>{row.tenDauSach}</strong><span>{[row.maDauSach, row.isbn].filter(Boolean).join(" · ")}</span></span>
        },
        { key: "id", title: "Mã cuốn", width: "150px", sortable: true, sortKey: "id", render: (row) => row.maCuonSach },
        { key: "branch", title: "Chi nhánh", width: "190px", wrap: true, sortable: true, sortKey: "branch", render: (row) => <span className="copy-secondary-cell"><strong>{row.tenChiNhanh}</strong><span>{row.maChiNhanh}</span></span> },
        { key: "location", title: "Vị trí", width: "230px", wrap: true, sortable: true, sortKey: "location", render: (row) => <span className="copy-secondary-cell"><strong>{row.viTriLabel}</strong><span>{[row.tenKhu, row.tenKeSach].filter(Boolean).join(" · ")}</span></span> },
        { key: "status", title: "Trạng thái", width: "170px", sortable: true, sortKey: "status", render: (row) => <StatusBadge value={row.tenTrangThai} /> },
        { key: "importedAt", title: "Ngày nhập", width: "140px", sortable: true, sortKey: "importedAt", render: (row) => formatDate(row.ngayNhapSach) },
        { key: "barcode", title: "Barcode / QR", width: "190px", sortable: true, sortKey: "barcode", render: (row) => <span className="copy-secondary-cell"><strong>{row.maVach || "Chưa có barcode"}</strong><span>{row.maQrCode || "Chưa có QR"}</span></span> },
        {
            key: "actions",
            title: "Thao tác",
            width: "120px",
            sticky: "right",
            render: (row) => (
                <InlineActionMenu
                    label={`Mở thao tác cho cuốn sách ${row.maCuonSach}`}
                    disabled={operationLoading}
                    actions={[
                        { key: "edit", label: "Sửa thông tin", icon: Pencil, onClick: () => openEditModal(row) },
                        { key: "print-label", label: "In nhãn barcode", icon: Printer, onClick: () => openLabelPreview([row.maCuonSach]) },
                        { key: "move", label: "Chuyển vị trí", icon: Pencil, onClick: () => openCopyAction(row, "MOVE_LOCATION"), disabled: ["TT_DANGMUON", "TT_DANGDATTRUOC", "TT_MAT"].includes(row.maTrangThai) },
                        { key: "damaged", label: "Báo hỏng", icon: EyeOff, onClick: () => openCopyAction(row, "MARK_DAMAGED"), disabled: row.maTrangThai !== "TT_SANCO" },
                        { key: "lost", label: "Báo mất", icon: EyeOff, onClick: () => openCopyAction(row, "MARK_LOST"), disabled: row.maTrangThai !== "TT_SANCO" },
                        { key: "withdraw", label: "Ngừng lưu thông", icon: EyeOff, onClick: () => openCopyAction(row, "WITHDRAW"), disabled: row.maTrangThai !== "TT_SANCO" },
                        { key: "repair", label: "Khôi phục sau sửa chữa", icon: RotateCcw, onClick: () => openCopyAction(row, "RESTORE_AFTER_REPAIR"), disabled: row.maTrangThai !== "TT_HONG" },
                        { key: "found", label: "Khôi phục sau khi tìm lại", icon: RotateCcw, onClick: () => openCopyAction(row, "RESTORE_FOUND"), disabled: row.maTrangThai !== "TT_MAT" },
                        { key: "delete", label: "Xóa", icon: Trash2, danger: true, onClick: () => hardDeleteBookCopy(row.maCuonSach) }
                    ]}
                />
            )
        }
    ];
}

function BarcodeLabelSheet({ labels, template }) {
    return <div className={`barcode-label-sheet ${template === "COMPACT" ? "is-compact" : ""}`}>
        {labels.map((label) => <article className="barcode-label" key={label.copyId}>
            <div className="barcode-label-heading">
                <strong>{label.title || "Cuốn sách"}</strong>
                <span>{label.copyId}</span>
            </div>
            <Code39Barcode value={label.barcode} />
            <div className="barcode-label-meta">
                <span>{label.barcode}</span>
                <span>{[label.branchName, label.locationLabel].filter(Boolean).join(" / ")}</span>
            </div>
        </article>)}
    </div>;
}

function Code39Barcode({ value }) {
    const bars = code39Bars(value);
    return <div className="code39" role="img" aria-label={`Barcode ${value}`}>
        {bars.map((bar, index) => <i key={index} className={bar.on ? "is-bar" : ""} style={{ width: `${bar.width}px` }} />)}
    </div>;
}

function FilterField({ label, children, wide = false }) {
    return <div className={`copy-filter-field${wide ? " is-wide" : ""}`}><label>{label}</label>{children}</div>;
}

function PresenceSelect({ value, onChange }) {
    return <select value={value === null ? "" : String(value)} onChange={(event) => onChange(event.target.value)}><option value="">Tất cả</option><option value="true">Có</option><option value="false">Chưa có</option></select>;
}

function PresetButton({ active, onClick, disabled = false, children }) {
    return <button className={`soft-button${active ? " is-active" : ""}`} type="button" onClick={onClick} disabled={disabled}>{children}</button>;
}

function buildDefaultCopyForm() {
    return { maCuonSach: "", maDauSach: "", maChiNhanh: "", maViTri: "", maTrangThai: "", maVach: "", maQrCode: "", ngayNhapSach: "", ghiChu: "" };
}

function buildDefaultBatchForm(defaultBranchId = "") {
    return {
        titleId: "",
        branchId: defaultBranchId || "",
        locationId: "",
        importDate: formatInputDate(new Date()),
        quantity: 1,
        barcodeMode: "AUTO",
        note: "",
        copies: [{ barcode: "", note: "" }]
    };
}

function BatchPreview({ form, title, branches, locations }) {
    const quantity = form.barcodeMode === "MANUAL" ? form.copies.length : Number(form.quantity);
    const branch = branches.find((option) => option.value === form.branchId);
    const location = locations.find((option) => option.value === form.locationId);
    const barcodeLabel = form.barcodeMode === "AUTO" ? "Server tự sinh" : form.barcodeMode === "MANUAL" ? "Barcode thủ công" : "Bổ sung sau";
    return <section className="batch-preview" aria-label="Xem trước lô">
        <h3>Xem trước lô</h3>
        <div><span>Đầu sách</span><strong>{title?.label}</strong><small>{title?.code}</small></div>
        <div><span>Chi nhánh</span><strong>{branch?.label}</strong><small>{form.branchId}</small></div>
        <div><span>Vị trí</span><strong>{location?.label}</strong><small>{form.locationId}</small></div>
        <div><span>Ngày nhập</span><strong>{formatDate(form.importDate)}</strong></div>
        <div><span>Số lượng</span><strong>{quantity}</strong></div>
        <div><span>Barcode</span><strong>{barcodeLabel}</strong></div>
    </section>;
}

function readPositiveInteger(value, fallback) {
    const parsed = Number.parseInt(value, 10);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback;
}

function readPageSize(value) {
    const parsed = Number.parseInt(value, 10);
    return [20, 50, 100].includes(parsed) ? parsed : 20;
}

function readSort(value) {
    const allowed = new Set(["id", "title", "branch", "location", "status", "importedAt", "barcode"]);
    if (!value) return { field: "importedAt", direction: "desc" };
    const [field, direction] = value.split(",");
    return allowed.has(field) && ["asc", "desc"].includes(direction)
        ? { field, direction }
        : { field: "importedAt", direction: "desc" };
}

function readBoolean(value) {
    if (value === "true") return true;
    if (value === "false") return false;
    return null;
}

function splitValues(value) {
    return value ? value.split(",").map((item) => item.trim()).filter(Boolean) : [];
}

function findStatusValues(options, labels) {
    const expected = labels.map(normalizeText);
    return options.filter((option) => expected.includes(normalizeText(option.label))).map((option) => option.value);
}

function normalizeText(value) {
    return String(value || "").normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().trim();
}

function lastThirtyDays() {
    const to = new Date();
    const from = new Date(to);
    from.setDate(from.getDate() - 29);
    return { from: formatInputDate(from), to: formatInputDate(to) };
}

function formatInputDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

function searchAllBookTitles(query, options = {}) {
    return libraryApi.searchBookTitles(query, { ...options, activeOnly: false });
}

const CODE39_PATTERNS = {
    "0": "nnnwwnwnn",
    "1": "wnnwnnnnw",
    "2": "nnwwnnnnw",
    "3": "wnwwnnnnn",
    "4": "nnnwwnnnw",
    "5": "wnnwwnnnn",
    "6": "nnwwwnnnn",
    "7": "nnnwnnwnw",
    "8": "wnnwnnwnn",
    "9": "nnwwnnwnn",
    "A": "wnnnnwnnw",
    "B": "nnwnnwnnw",
    "C": "wnwnnwnnn",
    "D": "nnnnwwnnw",
    "E": "wnnnwwnnn",
    "F": "nnwnwwnnn",
    "G": "nnnnnwwnw",
    "H": "wnnnnwwnn",
    "I": "nnwnnwwnn",
    "J": "nnnnwwwnn",
    "K": "wnnnnnnww",
    "L": "nnwnnnnww",
    "M": "wnwnnnnwn",
    "N": "nnnnwnnww",
    "O": "wnnnwnnwn",
    "P": "nnwnwnnwn",
    "Q": "nnnnnnwww",
    "R": "wnnnnnwwn",
    "S": "nnwnnnwwn",
    "T": "nnnnwnwwn",
    "U": "wwnnnnnnw",
    "V": "nwwnnnnnw",
    "W": "wwwnnnnnn",
    "X": "nwnnwnnnw",
    "Y": "wwnnwnnnn",
    "Z": "nwwnwnnnn",
    "-": "nwnnnnwnw",
    ".": "wwnnnnwnn",
    " ": "nwwnnnwnn",
    "$": "nwnwnwnnn",
    "/": "nwnwnnnwn",
    "+": "nwnnnwnwn",
    "%": "nnnwnwnwn",
    "*": "nwnnwnwnn"
};

function code39Bars(value) {
    const safeValue = String(value || "").toUpperCase().replace(/[^0-9A-Z-. $/+%]/g, "-");
    const encoded = `*${safeValue}*`;
    const bars = [];
    encoded.split("").forEach((char, charIndex) => {
        const pattern = CODE39_PATTERNS[char] || CODE39_PATTERNS["-"];
        pattern.split("").forEach((widthCode, index) => {
            bars.push({ on: index % 2 === 0, width: widthCode === "w" ? 3 : 1 });
        });
        if (charIndex < encoded.length - 1) bars.push({ on: false, width: 1 });
    });
    return bars;
}
