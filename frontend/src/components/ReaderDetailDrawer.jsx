import { useEffect, useRef, useState } from "react";
import { X } from "lucide-react";
import { libraryApi } from "../api/libraryApi";
import { formatDate, formatDateTime, formatMoney } from "../utils/displayUtils";
import StatusBadge from "./StatusBadge";
import ReaderStateActionDialog from "./ReaderStateActionDialog";
import { useToast } from "./ToastProvider";
import ReaderPasswordResetDialog from "./ReaderPasswordResetDialog";
import { useAuth } from "../context/AuthContext";

const TABS = [
    ["profile", "Hồ sơ"],
    ["membership", "Gói thành viên"],
    ["loans", "Sách đang mượn"],
    ["debts", "Nợ"],
    ["transactions", "Lịch sử giao dịch"]
];

export default function ReaderDetailDrawer({ readerId, onClose, onUpdated }) {
    const toast = useToast();
    const { staffContext } = useAuth();
    const [activeTab, setActiveTab] = useState("profile");
    const [overview, setOverview] = useState(null);
    const [overviewLoading, setOverviewLoading] = useState(true);
    const [overviewError, setOverviewError] = useState("");
    const [eligibility, setEligibility] = useState(null);
    const [action, setAction] = useState(null);
    const [passwordResetOpen, setPasswordResetOpen] = useState(false);
    const [tabState, setTabState] = useState({});
    const [transactionPage, setTransactionPage] = useState(1);
    const [retryKey, setRetryKey] = useState(0);
    const requestedTabsRef = useRef(new Set());

    useEffect(() => {
        const controller = new AbortController();
        // The drawer profile synchronizes with the selected reader.
        // eslint-disable-next-line react-hooks/set-state-in-effect
        setOverviewLoading(true);
        setOverviewError("");
        Promise.all([
            libraryApi.readerOverview(readerId, { signal: controller.signal }),
            libraryApi.readerBorrowingContext(readerId, { signal: controller.signal })
        ])
            .then(([profile, context]) => { setOverview(profile); setEligibility(context); })
            .catch((error) => {
                if (error.name !== "AbortError") setOverviewError(error.message || "Không tải được hồ sơ độc giả");
            })
            .finally(() => {
                if (!controller.signal.aborted) setOverviewLoading(false);
            });
        return () => controller.abort();
    }, [readerId, retryKey]);

    useEffect(() => {
        if (activeTab === "profile") return undefined;
        const cacheKey = activeTab === "transactions" ? `${activeTab}:${transactionPage}` : activeTab;
        const requestedTabs = requestedTabsRef.current;
        if (requestedTabs.has(cacheKey)) return undefined;
        requestedTabs.add(cacheKey);
        const controller = new AbortController();
        let settled = false;
        setTabState((current) => ({ ...current, [cacheKey]: { loading: true, error: "", data: null } }));
        const request = activeTab === "membership"
            ? libraryApi.readerMemberships(readerId, { signal: controller.signal })
            : activeTab === "loans"
                ? libraryApi.readerCurrentLoansDetail(readerId, { signal: controller.signal })
                : activeTab === "debts"
                    ? libraryApi.readerDebtsDetail(readerId, { signal: controller.signal })
                    : libraryApi.readerTransactions(readerId, transactionPage, 20, { signal: controller.signal });
        request.then((data) => {
            if (!controller.signal.aborted) {
                settled = true;
                setTabState((current) => ({ ...current, [cacheKey]: { loading: false, error: "", data } }));
            }
        }).catch((error) => {
            if (!controller.signal.aborted) {
                settled = true;
                requestedTabs.delete(cacheKey);
                setTabState((current) => ({ ...current, [cacheKey]: { loading: false, error: error.message || "Không tải được dữ liệu", data: null } }));
            }
        });
        return () => {
            controller.abort();
            if (!settled) requestedTabs.delete(cacheKey);
        };
    }, [activeTab, readerId, transactionPage, retryKey]);

    const cacheKey = activeTab === "transactions" ? `${activeTab}:${transactionPage}` : activeTab;
    const currentTab = tabState[cacheKey] || {};

    function retry() {
        if (activeTab !== "profile") {
            requestedTabsRef.current.delete(cacheKey);
            setTabState((current) => {
                const next = { ...current };
                delete next[cacheKey];
                return next;
            });
        }
        setRetryKey((value) => value + 1);
    }

    function actionSucceeded(result) {
        setAction(null);
        setEligibility(result.eligibility);
        setOverview((current) => current ? { ...current, profileStatus: result.profileStatus } : current);
        toast.success("Đã cập nhật trạng thái độc giả");
        onUpdated?.();
    }

    return <div className="drawer-backdrop" role="presentation" onMouseDown={onClose}>
        <aside className="reader-detail-drawer" role="dialog" aria-modal="true" aria-label="Chi tiết độc giả" onMouseDown={(event) => event.stopPropagation()}>
            <header>
                <div><span>Độc giả</span><h2>{overview?.fullName || readerId}</h2><small>{readerId}</small></div>
                <button className="icon-button" type="button" onClick={onClose} aria-label="Đóng chi tiết"><X size={19} /></button>
            </header>
            {overview && eligibility && <div className="reader-state-actions">
                {!eligibility.activeLocks?.some((item) => item.scope === "BORROWING") && <button className="soft-button" type="button" onClick={() => setAction("LOCK_BORROWING")}>Khóa quyền mượn</button>}
                {!eligibility.activeLocks?.some((item) => item.scope === "LOGIN") && <button className="soft-button" type="button" onClick={() => setAction("LOCK_LOGIN")}>Khóa đăng nhập</button>}
                {!!eligibility.activeLocks?.length && <button className="soft-button" type="button" onClick={() => setAction("UNLOCK")}>Mở khóa</button>}
                {overview.profileStatus === "Ngừng hoạt động"
                    ? <button className="soft-button" type="button" onClick={() => setAction("REACTIVATE")}>Khôi phục</button>
                    : <button className="soft-button danger-button" type="button" onClick={() => setAction("DEACTIVATE")}>Ngừng hoạt động</button>}
                {staffContext?.permissions?.includes("READER_PASSWORD_RESET") && <button className="soft-button" type="button" onClick={() => setPasswordResetOpen(true)}>Reset mật khẩu</button>}
            </div>}
            <nav className="reader-drawer-tabs" aria-label="Các phần chi tiết độc giả">
                {TABS.map(([value, label]) => <button className={activeTab === value ? "is-active" : ""} type="button" key={value} onClick={() => setActiveTab(value)}>{label}</button>)}
            </nav>
            <div className="reader-drawer-content">
                {activeTab === "profile" && <ProfileTab data={overview} eligibility={eligibility} loading={overviewLoading} error={overviewError} retry={retry} />}
                {activeTab !== "profile" && currentTab.loading && <DrawerState loading text="Đang tải dữ liệu..." />}
                {activeTab !== "profile" && currentTab.error && <DrawerState text={currentTab.error} action={retry} />}
                {activeTab === "membership" && currentTab.data && <MembershipTab items={currentTab.data} />}
                {activeTab === "loans" && currentTab.data && <LoansTab items={currentTab.data} />}
                {activeTab === "debts" && currentTab.data && <DebtsTab items={currentTab.data} />}
                {activeTab === "transactions" && currentTab.data && <TransactionsTab page={currentTab.data} onPageChange={setTransactionPage} />}
            </div>
        </aside>
        {action && <ReaderStateActionDialog reader={overview} action={action} activeLocks={eligibility?.activeLocks || []} onClose={() => setAction(null)} onSuccess={actionSucceeded} />}
        {passwordResetOpen && <ReaderPasswordResetDialog reader={overview} onClose={() => setPasswordResetOpen(false)} />}
    </div>;
}

function ProfileTab({ data, eligibility, loading, error, retry }) {
    if (loading) return <DrawerState loading text="Đang tải hồ sơ..." />;
    if (error) return <DrawerState text={error} action={retry} />;
    if (!data) return <DrawerState text="Không tìm thấy độc giả" />;
    return <div className="reader-profile-sections">
        <section><h3>Hồ sơ</h3><div className="reader-detail-grid">
            <Info label="Họ tên" value={data.fullName} /><Info label="Ngày sinh" value={formatDate(data.dateOfBirth)} />
            <Info label="Nhóm" value={data.groupName} secondary={data.groupId} /><Info label="Trạng thái hồ sơ"><StatusBadge value={data.profileStatus} /></Info>
            <Info label="Email" value={data.email} /><Info label="Số điện thoại" value={data.phone} />
            <Info label="Địa chỉ" value={data.address} wide />
        </div></section>
        <section><h3>Thẻ và tài khoản</h3><div className="reader-detail-grid">
            <Info label="Ngày lập thẻ" value={formatDate(data.cardIssuedAt)} /><Info label="Hạn thẻ" value={formatDate(data.cardExpiresAt)} secondary={statusLabel(data.cardStatus)} />
            <Info label="Tên đăng nhập" value={data.username} /><Info label="Trạng thái tài khoản"><StatusBadge value={data.accountStatus} /></Info>
        </div></section>
        <section><h3>Tổng quan nghĩa vụ</h3><div className="reader-detail-grid">
            <Info label="Sách đang mượn" value={String(data.currentLoans || 0)} /><Info label="Nợ còn lại" value={formatMoney(data.outstandingDebt)} />
        </div></section>
        <section><h3>Điều kiện mượn</h3>
            <div className={`reader-eligibility ${eligibility?.eligible ? "is-eligible" : "is-blocked"}`}><strong>{eligibility?.eligible ? "Có thể mượn theo các rule đã kích hoạt" : "Đang bị chặn mượn"}</strong></div>
            {!!eligibility?.blockingReasons?.length && <ReasonList title="Lý do chặn" items={eligibility.blockingReasons} />}
            {!!eligibility?.warnings?.length && <ReasonList title="Cảnh báo" items={eligibility.warnings} />}
            {!!eligibility?.activeLocks?.length && <ReasonList title="Khóa đang hiệu lực" items={eligibility.activeLocks.map((item) => ({ code: item.scope, message: `${item.reason}${item.lockedUntil ? ` · đến ${formatDate(item.lockedUntil)}` : " · không thời hạn"}` }))} />}
        </section>
    </div>;
}

function ReasonList({ title, items }) { return <div className="reader-reason-list"><span>{title}</span>{items.map((item) => <p key={`${item.code}-${item.message}`}><strong>{item.code}</strong> {item.message}</p>)}</div>; }

function MembershipTab({ items }) {
    if (!items.length) return <DrawerState text="Độc giả chưa có lịch sử gói thành viên" />;
    return <div className="reader-detail-list">{items.map((item) => <article key={item.id}><div><strong>{item.planName}</strong><span>{item.planId}</span></div><StatusBadge value={statusLabel(item.status)} /><p>{formatDate(item.startsAt)} – {formatDate(item.expiresAt)}</p>{item.note && <small>{item.note}</small>}</article>)}</div>;
}

function LoansTab({ items }) {
    if (!items.length) return <DrawerState text="Độc giả không có sách đang mượn" />;
    return <div className="reader-detail-list">{items.map((item) => <article key={item.loanDetailId}><div><strong>{item.titleName}</strong><span>{item.copyId}{item.barcode ? ` · ${item.barcode}` : ""}</span></div><StatusBadge value={item.overdueDays > 0 ? `Quá hạn ${item.overdueDays} ngày` : item.status} /><p>Mượn {formatDateTime(item.borrowedAt)} · Hạn {formatDateTime(item.dueAt)}</p><small>{item.branchName}</small></article>)}</div>;
}

function DebtsTab({ items }) {
    if (!items.length) return <DrawerState text="Độc giả không có khoản nợ" />;
    return <div className="reader-detail-list">{items.map((item) => <article key={item.debtId}><div><strong>{item.debtTypeName}</strong><span>{item.debtId}</span></div><StatusBadge value={item.status} /><p>Còn lại {formatMoney(item.outstandingAmount)} / {formatMoney(item.originalAmount)}</p><small>{formatDateTime(item.occurredAt)}{item.reason ? ` · ${item.reason}` : ""}</small></article>)}</div>;
}

function TransactionsTab({ page, onPageChange }) {
    if (!page.items?.length) return <DrawerState text="Chưa có lịch sử giao dịch" />;
    return <><div className="reader-detail-list">{page.items.map((item) => <article key={`${item.type}-${item.id}`}><div><strong>{transactionType(item.type)}</strong><span>{item.id}</span></div><StatusBadge value={item.status} /><p>{formatDateTime(item.occurredAt)}{item.amount !== null ? ` · ${formatMoney(item.amount)}` : ""}</p><small>{item.description}</small></article>)}</div><div className="reader-drawer-pagination"><button className="ghost-button" type="button" disabled={page.page <= 1} onClick={() => onPageChange(page.page - 1)}>Trang trước</button><span>{page.page}/{Math.max(page.totalPages, 1)}</span><button className="ghost-button" type="button" disabled={page.page >= page.totalPages} onClick={() => onPageChange(page.page + 1)}>Trang sau</button></div></>;
}

function Info({ label, value, secondary, wide = false, children }) {
    return <div className={wide ? "is-wide" : ""}><span>{label}</span>{children || <strong>{value || "—"}</strong>}{secondary && <small>{secondary}</small>}</div>;
}

function DrawerState({ text, loading = false, action }) {
    return <div className="book-drawer-state">{loading && <span className="table-spinner" />}<p>{text}</p>{action && <button className="soft-button" type="button" onClick={action}>Thử lại</button>}</div>;
}

function statusLabel(value) {
    return ({ VALID: "Còn hạn", EXPIRING: "Sắp hết hạn", EXPIRED: "Đã hết hạn", NONE: "Chưa có gói" })[value] || value;
}

function transactionType(value) {
    return ({ LOAN: "Mượn sách", RETURN: "Trả sách", PAYMENT: "Thanh toán", MEMBERSHIP: "Gói thành viên" })[value] || value;
}
