import { Download } from "lucide-react";
import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { libraryApi } from "../api/libraryApi";
import DataTable from "../components/DataTable";
import PageHeader from "../components/PageHeader";
import ReaderDetailDrawer from "../components/ReaderDetailDrawer";
import StatusBadge from "../components/StatusBadge";
import { useToast } from "../components/ToastProvider";
import { formatDate, formatMoney } from "../utils/displayUtils";
import { downloadCsvExport, resolveExportResult } from "../utils/exportUtils";

export default function ReadersPage() {
    const toast = useToast();
    const [searchParams, setSearchParams] = useSearchParams();
    const requestSequence = useRef(0);
    const [data, setData] = useState([]);
    const [pageInfo, setPageInfo] = useState({ totalItems: 0, totalPages: 0 });
    const [tableLoading, setTableLoading] = useState(false);
    const [tableError, setTableError] = useState("");
    const [reloadVersion, setReloadVersion] = useState(0);
    const [groupOptions, setGroupOptions] = useState([]);
    const [planOptions, setPlanOptions] = useState([]);
    const [profileStatusOptions, setProfileStatusOptions] = useState([]);
    const [filterLoading, setFilterLoading] = useState(true);
    const [filterError, setFilterError] = useState("");
    const [filterRetryKey, setFilterRetryKey] = useState(0);
    const [drawerReaderId, setDrawerReaderId] = useState(null);
    const [selectedIds, setSelectedIds] = useState([]);
    const [exportLoading, setExportLoading] = useState(false);

    const urlSearch = searchParams.get("search") || "";
    const [searchDraft, setSearchDraft] = useState({ urlValue: urlSearch, value: urlSearch });
    const search = searchDraft.urlValue === urlSearch ? searchDraft.value : urlSearch;
    const page = positiveInteger(searchParams.get("page"), 1);
    const pageSize = readPageSize(searchParams.get("pageSize"));
    const sort = readSort(searchParams.get("sort"));
    const groupId = searchParams.get("groupIds") || "";
    const planId = searchParams.get("planIds") || "";
    const profileStatus = searchParams.get("profileStatuses") || "";
    const cardStatus = searchParams.get("cardStatus") || "";
    const membershipStatus = searchParams.get("membershipStatus") || "";
    const cardExpiryFrom = searchParams.get("cardExpiryFrom") || "";
    const cardExpiryTo = searchParams.get("cardExpiryTo") || "";
    const membershipExpiryFrom = searchParams.get("membershipExpiryFrom") || "";
    const membershipExpiryTo = searchParams.get("membershipExpiryTo") || "";
    const locked = searchParams.get("locked") === "true";
    const preset = searchParams.get("preset") || "";

    const updateUrl = useCallback((changes, replace = false) => {
        setSearchParams((current) => {
            const next = new URLSearchParams(current);
            Object.entries(changes).forEach(([key, value]) => {
                if (value === null || value === undefined || value === "" || value === false) next.delete(key);
                else next.set(key, String(value));
            });
            return next;
        }, { replace });
    }, [setSearchParams]);

    useEffect(() => {
        const controller = new AbortController();
        const sequence = ++requestSequence.current;
        // Each URL query owns its loading/error lifecycle.
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setTableLoading(true);
        setTableError("");
        libraryApi.readersPage({
            page,
            pageSize,
            search: urlSearch,
            sort: `${sort.field},${sort.direction}`,
            groupIds: groupId ? [groupId] : [],
            planIds: planId ? [planId] : [],
            profileStatuses: profileStatus ? [profileStatus] : [],
            cardStatus,
            membershipStatus,
            cardExpiryFrom,
            cardExpiryTo,
            membershipExpiryFrom,
            membershipExpiryTo,
            locked: locked || null
        }, { signal: controller.signal }).then((response) => {
            if (sequence !== requestSequence.current) return;
            setData(Array.isArray(response.items) ? response.items : []);
            setPageInfo({ totalItems: Number(response.totalItems) || 0, totalPages: Number(response.totalPages) || 0 });
            if (response.totalPages > 0 && page > response.totalPages) updateUrl({ page: response.totalPages });
        }).catch((error) => {
            if (error.name !== "AbortError" && sequence === requestSequence.current) setTableError(error.message || "Không tải được danh sách độc giả");
        }).finally(() => {
            if (sequence === requestSequence.current) setTableLoading(false);
        });
        return () => controller.abort();
    }, [page, pageSize, urlSearch, sort.field, sort.direction, groupId, planId, profileStatus, cardStatus, membershipStatus, cardExpiryFrom, cardExpiryTo, membershipExpiryFrom, membershipExpiryTo, locked, reloadVersion, updateUrl]);

    useEffect(() => {
        let active = true;
        Promise.all([libraryApi.readerGroups(), libraryApi.membershipPlans(), libraryApi.readerProfileStatuses()])
            .then(([groups, plans, statuses]) => {
                if (!active) return;
                setGroupOptions(Array.isArray(groups) ? groups : []);
                setPlanOptions(Array.isArray(plans) ? plans : []);
                setProfileStatusOptions(Array.isArray(statuses) ? statuses : []);
                setFilterError("");
            }).catch((error) => {
                if (active) setFilterError(error.message || "Không tải được dữ liệu bộ lọc");
            }).finally(() => {
                if (active) setFilterLoading(false);
            });
        return () => { active = false; };
    }, [filterRetryKey]);

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
        updateUrl({ search: null, groupIds: null, planIds: null, profileStatuses: null, cardStatus: null,
            membershipStatus: null, cardExpiryFrom: null, cardExpiryTo: null, membershipExpiryFrom: null,
            membershipExpiryTo: null, locked: null, preset: null, page: 1 });
    }

    function applyPreset(value) {
        updateUrl({
            cardStatus: value === "card-expiring" ? "EXPIRING" : value === "card-expired" ? "EXPIRED" : null,
            membershipStatus: value === "membership-expiring" ? "EXPIRING" : value === "membership-expired" ? "EXPIRED" : null,
            locked: value === "locked" ? true : null,
            cardExpiryFrom: null,
            cardExpiryTo: null,
            membershipExpiryFrom: null,
            membershipExpiryTo: null,
            preset: value,
            page: 1
        });
    }

    function retryFilters() {
        setFilterError("");
        setFilterLoading(true);
        setFilterRetryKey((value) => value + 1);
    }

    function currentExportFilters() {
        return {
            page,
            pageSize,
            search: urlSearch,
            sort: `${sort.field},${sort.direction}`,
            groupIds: groupId ? [groupId] : [],
            planIds: planId ? [planId] : [],
            profileStatuses: profileStatus ? [profileStatus] : [],
            accountStatuses: [],
            cardStatus,
            membershipStatus,
            cardExpiryFrom,
            cardExpiryTo,
            membershipExpiryFrom,
            membershipExpiryTo,
            locked: locked || null
        };
    }

    async function exportReaders(scope) {
        if (exportLoading) return;
        if (scope === "SELECTED" && selectedIds.length === 0) {
            toast.error("Chưa chọn độc giả để export");
            return;
        }
        setExportLoading(true);
        try {
            const result = await libraryApi.exportReaders({
                scope,
                ids: scope === "SELECTED" ? selectedIds : [],
                filters: currentExportFilters(),
                excludedIds: []
            });
            const ready = await resolveExportResult(result, () => toast.info("Tập dữ liệu lớn, đang tạo file export"));
            downloadCsvExport(ready);
            toast.success(`Đã export ${Number(ready.totalRows || 0).toLocaleString("vi-VN")} dòng`);
        } catch (error) {
            toast.error(error.message || "Export độc giả thất bại");
        } finally {
            setExportLoading(false);
        }
    }

    return <div>
        <PageHeader eyebrow="Members" title="Quản lý độc giả" description="Tra cứu hồ sơ, hiệu lực thẻ, gói thành viên và nghĩa vụ hiện tại." />

        <div className="reader-presets" aria-label="Bộ lọc nhanh độc giả">
            <Preset active={preset === "card-expiring"} onClick={() => applyPreset("card-expiring")}>Thẻ sắp hết hạn</Preset>
            <Preset active={preset === "membership-expiring"} onClick={() => applyPreset("membership-expiring")}>Gói sắp hết hạn</Preset>
            <Preset active={preset === "card-expired"} onClick={() => applyPreset("card-expired")}>Thẻ đã hết hạn</Preset>
            <Preset active={preset === "membership-expired"} onClick={() => applyPreset("membership-expired")}>Gói đã hết hạn</Preset>
            <Preset active={preset === "locked"} onClick={() => applyPreset("locked")}>Đang khóa</Preset>
        </div>

        <div className="panel reader-filter-panel">
            <Filter label="Tìm kiếm" wide><input value={search} onChange={(event) => setSearchDraft({ urlValue: urlSearch, value: event.target.value })} placeholder="Mã, họ tên, email, số điện thoại hoặc tên đăng nhập" /></Filter>
            <Filter label="Nhóm độc giả"><select value={groupId} onChange={(event) => changeFilter("groupIds", event.target.value)} disabled={filterLoading}><option value="">Tất cả nhóm</option>{groupOptions.map(option)}</select></Filter>
            <Filter label="Gói thành viên"><select value={planId} onChange={(event) => changeFilter("planIds", event.target.value)} disabled={filterLoading}><option value="">Tất cả gói</option>{planOptions.map(option)}</select></Filter>
            <Filter label="Trạng thái hồ sơ"><select value={profileStatus} onChange={(event) => changeFilter("profileStatuses", event.target.value)} disabled={filterLoading}><option value="">Tất cả trạng thái</option>{profileStatusOptions.map(option)}</select></Filter>
            <Filter label="Hạn thẻ từ"><input type="date" value={cardExpiryFrom} onChange={(event) => changeFilter("cardExpiryFrom", event.target.value)} /></Filter>
            <Filter label="Hạn thẻ đến"><input type="date" value={cardExpiryTo} onChange={(event) => changeFilter("cardExpiryTo", event.target.value)} /></Filter>
            <Filter label="Hạn gói từ"><input type="date" value={membershipExpiryFrom} onChange={(event) => changeFilter("membershipExpiryFrom", event.target.value)} /></Filter>
            <Filter label="Hạn gói đến"><input type="date" value={membershipExpiryTo} onChange={(event) => changeFilter("membershipExpiryTo", event.target.value)} /></Filter>
            <button className="ghost-button reader-clear-filters" type="button" onClick={clearFilters}>Xóa bộ lọc</button>
            {filterError && <div className="reader-filter-error" role="alert"><span>{filterError}</span><button className="soft-button" type="button" onClick={retryFilters}>Thử lại</button></div>}
        </div>

        <div className="list-toolbar">
            <div className="selection-toolbar">
                <button className="soft-button" type="button" onClick={() => exportReaders("SELECTED")} disabled={!selectedIds.length || exportLoading}><Download size={15} /> Export đã chọn</button>
                <button className="soft-button" type="button" onClick={() => exportReaders("PAGE")} disabled={!data.length || exportLoading}><Download size={15} /> Export trang này</button>
                <button className="soft-button" type="button" onClick={() => exportReaders("ALL_MATCHING")} disabled={pageInfo.totalItems === 0 || exportLoading}><Download size={15} /> Export tất cả kết quả</button>
            </div>
        </div>

        <DataTable mode="server" data={data} rowKey="readerId" loading={tableLoading} error={tableError}
            onRetry={() => setReloadVersion((value) => value + 1)} emptyText="Không có độc giả phù hợp"
            onRowClick={(row) => setDrawerReaderId(row.readerId)}
            pagination={{ page, pageSize, totalItems: pageInfo.totalItems, totalPages: pageInfo.totalPages,
                onPageChange: (next) => updateUrl({ page: next }), onPageSizeChange: (next) => updateUrl({ pageSize: next, page: 1 }) }}
            sort={sort} onSortChange={(next) => updateUrl({ sort: next ? `${next.field},${next.direction}` : null, page: 1 })}
            selectable
            selectedRowKeys={selectedIds}
            onSelectionChange={setSelectedIds}
            columns={readerColumns()} />

        {drawerReaderId && <ReaderDetailDrawer key={drawerReaderId} readerId={drawerReaderId} onClose={() => setDrawerReaderId(null)} onUpdated={() => setReloadVersion((value) => value + 1)} />}
    </div>;
}

function readerColumns() {
    return [
        { key: "fullName", title: "Họ tên", width: "300px", minWidth: "260px", wrap: true, align: "left", sortable: true, sortKey: "fullName", render: (row) => <span className="reader-name-cell"><strong>{row.fullName}</strong><span>{row.readerId}</span></span> },
        { key: "group", title: "Nhóm", width: "170px", render: (row) => <Secondary primary={row.groupName} secondary={row.groupId} /> },
        { key: "plan", title: "Gói", width: "190px", render: (row) => <Secondary primary={row.planName || "Chưa có gói"} secondary={statusLabel(row.membershipStatus)} /> },
        { key: "cardIssued", title: "Ngày lập thẻ", width: "140px", sortable: true, sortKey: "cardIssued", render: (row) => formatDate(row.cardIssuedAt) },
        { key: "cardExpiry", title: "Hạn thẻ", width: "165px", sortable: true, sortKey: "cardExpiry", render: (row) => <Secondary primary={formatDate(row.cardExpiresAt)} secondary={statusLabel(row.cardStatus)} /> },
        { key: "membershipExpiry", title: "Hạn gói", width: "165px", sortable: true, sortKey: "membershipExpiry", render: (row) => <Secondary primary={formatDate(row.membershipExpiresAt) || "—"} secondary={statusLabel(row.membershipStatus)} /> },
        { key: "profileStatus", title: "Hồ sơ", width: "150px", render: (row) => <StatusBadge value={row.profileStatus} /> },
        { key: "summary", title: "Đang mượn / Nợ", width: "180px", render: (row) => <Secondary primary={`${row.currentLoans} cuốn`} secondary={formatMoney(row.outstandingDebt)} /> }
    ];
}

function Filter({ label, children, wide = false }) { return <div className={`reader-filter-field${wide ? " is-wide" : ""}`}><label>{label}</label>{children}</div>; }
function Preset({ active, onClick, children }) { return <button className={`soft-button${active ? " is-active" : ""}`} type="button" onClick={onClick}>{children}</button>; }
function Secondary({ primary, secondary }) { return <span className="reader-secondary-cell"><strong>{primary || "—"}</strong>{secondary && <span>{secondary}</span>}</span>; }
function option(item) { return <option key={item.value} value={item.value}>{item.label}</option>; }
function positiveInteger(value, fallback) { const parsed = Number.parseInt(value, 10); return Number.isInteger(parsed) && parsed > 0 ? parsed : fallback; }
function readPageSize(value) { const parsed = Number.parseInt(value, 10); return [20, 50, 100].includes(parsed) ? parsed : 20; }
function readSort(value) { const [field = "fullName", direction = "asc"] = String(value || "").split(","); return ["fullName", "cardIssued", "cardExpiry", "membershipExpiry"].includes(field) && ["asc", "desc"].includes(direction) ? { field, direction } : { field: "fullName", direction: "asc" }; }
function statusLabel(value) { return ({ VALID: "Còn hạn", EXPIRING: "Sắp hết hạn", EXPIRED: "Đã hết hạn", NONE: "Chưa có gói" })[value] || value; }
