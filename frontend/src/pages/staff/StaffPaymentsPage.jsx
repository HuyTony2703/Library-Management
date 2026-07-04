import { useCallback, useEffect, useMemo, useState } from "react";
import { CreditCard, ListChecks, Search, Trash2 } from "lucide-react";
import { libraryApi } from "../../api/libraryApi";
import { staffApi } from "../../api/staffApi";
import AsyncEntityPicker from "../../components/AsyncEntityPicker";
import DataTable from "../../components/DataTable";
import PageHeader from "../../components/PageHeader";
import ResultModal from "../../components/ResultModal";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";
import { useAuth } from "../../context/AuthContext";
import { displayCode, formatDateTime, formatMoney } from "../../utils/displayUtils";

export default function StaffPaymentsPage() {
    const toast = useToast();
    const { staffContext } = useAuth();

    const [selectedReader, setSelectedReader] = useState(null);
    const [debtContext, setDebtContext] = useState(null);
    const [debts, setDebts] = useState([]);
    const [paymentMethods, setPaymentMethods] = useState([]);
    const [selectedAllocations, setSelectedAllocations] = useState({});
    const [activeTab, setActiveTab] = useState("AUTO");
    const [hidePaid, setHidePaid] = useState(true);
    const [autoAmount, setAutoAmount] = useState("");
    const [paymentMethodId, setPaymentMethodId] = useState("");
    const [note, setNote] = useState("");
    const [loadingReaderData, setLoadingReaderData] = useState(false);
    const [readerDataError, setReaderDataError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [result, setResult] = useState(null);
    const [showResult, setShowResult] = useState(false);

    const selectedReaderId = selectedReader?.readerId || selectedReader?.value || "";

    const loadDebtorOptions = useCallback((query, options) => {
        return staffApi.searchDebtors(query, options).then((rows) => (
            Array.isArray(rows)
                ? rows.map((row) => ({
                    ...row,
                    value: row.readerId,
                    label: row.readerName || row.readerId,
                    code: row.readerId,
                    metadata: [
                        row.phone,
                        row.email,
                        `${formatMoney(row.outstandingAmount)} còn nợ`
                    ].filter(Boolean).join(" · ")
                }))
                : []
        ));
    }, []);

    function handleReaderChange(reader) {
        setSelectedReader(reader);
        if (!reader) {
            setDebtContext(null);
            setDebts([]);
            setSelectedAllocations({});
            setAutoAmount("");
            setReaderDataError("");
        }
    }

    useEffect(() => {
        let mounted = true;
        libraryApi.paymentMethods()
            .then((data) => {
                if (!mounted) return;
                const methods = Array.isArray(data) ? data : [];
                setPaymentMethods(methods);
                setPaymentMethodId((current) => (
                    methods.some((method) => method.value === current)
                        ? current
                        : methods[0]?.value || ""
                ));
            })
            .catch((err) => {
                if (!mounted) return;
                setPaymentMethods([]);
                toast.error(err.message || "Không tải được phương thức thanh toán");
            });

        return () => {
            mounted = false;
        };
    }, [toast]);

    useEffect(() => {
        if (!selectedReaderId) {
            return undefined;
        }

        const controller = new AbortController();

        Promise.resolve()
            .then(() => {
                if (controller.signal.aborted) return null;
                setLoadingReaderData(true);
                setReaderDataError("");
                return Promise.all([
                    staffApi.getReaderDebtContext(selectedReaderId, { signal: controller.signal }),
                    staffApi.getReaderDebts(selectedReaderId, { signal: controller.signal })
                ]);
            })
            .then((result) => {
                if (!result || controller.signal.aborted) return;
                const [contextData, debtRows] = result;
                setDebtContext(contextData);
                setDebts(Array.isArray(debtRows) ? debtRows : []);
                setSelectedAllocations({});
                setAutoAmount("");
            })
            .catch((err) => {
                if (controller.signal.aborted) return;
                setDebtContext(null);
                setDebts([]);
                setSelectedAllocations({});
                setReaderDataError(err.message || "Không tải được công nợ độc giả");
            })
            .finally(() => {
                if (!controller.signal.aborted) setLoadingReaderData(false);
            });

        return () => controller.abort();
    }, [selectedReaderId]);

    const visibleDebts = useMemo(() => (
        hidePaid ? debts.filter((row) => getDebtRemaining(row) > 0) : debts
    ), [debts, hidePaid]);

    const outstandingTotal = Number(debtContext?.outstandingAmount ?? selectedReader?.outstandingAmount ?? 0);

    const manualTotal = useMemo(() => (
        Object.values(selectedAllocations).reduce((sum, value) => sum + Number(value || 0), 0)
    ), [selectedAllocations]);

    const manualDebtCount = useMemo(() => (
        Object.values(selectedAllocations).filter((value) => Number(value || 0) > 0).length
    ), [selectedAllocations]);

    const autoNumber = Math.max(0, Number(autoAmount || 0));
    const autoProjection = useMemo(() => projectAutoAllocation(debts, autoNumber), [debts, autoNumber]);
    const summaryAmount = activeTab === "AUTO" ? autoProjection.total : manualTotal;
    const summaryDebtCount = activeTab === "AUTO" ? autoProjection.count : manualDebtCount;
    const balanceAfter = Math.max(0, outstandingTotal - summaryAmount);

    function toggleDebt(row) {
        const debtId = getDebtId(row);
        const remaining = getDebtRemaining(row);
        if (!debtId || remaining <= 0) return;

        setSelectedAllocations((current) => {
            const next = { ...current };
            if (next[debtId] !== undefined) {
                delete next[debtId];
            } else {
                next[debtId] = remaining;
            }
            return next;
        });
    }

    function updateAllocation(row, value) {
        const debtId = getDebtId(row);
        if (!debtId) return;
        const amount = Number(value);
        const remaining = getDebtRemaining(row);
        setSelectedAllocations((current) => ({
            ...current,
            [debtId]: Math.max(0, Math.min(Number.isFinite(amount) ? amount : 0, remaining))
        }));
    }

    function selectAllOutstanding() {
        const next = {};
        debts.forEach((row) => {
            const debtId = getDebtId(row);
            const remaining = getDebtRemaining(row);
            if (debtId && remaining > 0) next[debtId] = remaining;
        });
        setSelectedAllocations(next);
        if (Object.keys(next).length === 0) {
            toast.info("Không có khoản nợ còn lại để chọn");
        }
    }

    function clearSelected() {
        setSelectedAllocations({});
    }

    async function refreshReaderData() {
        if (!selectedReaderId) return;
        const [contextData, debtRows] = await Promise.all([
            staffApi.getReaderDebtContext(selectedReaderId),
            staffApi.getReaderDebts(selectedReaderId)
        ]);
        setDebtContext(contextData);
        setDebts(Array.isArray(debtRows) ? debtRows : []);
        setSelectedAllocations({});
        setAutoAmount("");
    }

    async function submitPayment(event) {
        event.preventDefault();
        if (submitting) return;

        if (!staffContext?.operational) {
            toast.error("Tài khoản chưa có staff context hợp lệ để thu tiền");
            return;
        }
        if (!selectedReaderId) {
            toast.error("Vui lòng chọn độc giả còn nợ");
            return;
        }
        if (!paymentMethodId) {
            toast.error("Chưa có phương thức thanh toán hợp lệ");
            return;
        }

        const idempotencyKey = buildPaymentIdempotencyKey();
        const payload = {
            maDocGia: selectedReaderId,
            maPhuongThuc: paymentMethodId,
            soTienThu: summaryAmount,
            ghiChu: note
        };

        if (activeTab === "AUTO") {
            if (autoNumber <= 0) {
                toast.error("Số tiền thu phải lớn hơn 0");
                return;
            }
            payload.soTienThu = autoNumber;
            payload.chiTietNo = [];
        } else {
            const chiTietNo = Object.entries(selectedAllocations)
                .filter(([, amount]) => Number(amount || 0) > 0)
                .map(([maKhoanNo, soTienApDung]) => ({
                    maKhoanNo,
                    soTienApDung: Number(soTienApDung)
                }));

            if (chiTietNo.length === 0) {
                toast.error("Vui lòng chọn ít nhất một khoản nợ");
                return;
            }
            if (manualTotal <= 0) {
                toast.error("Tổng tiền áp dụng phải lớn hơn 0");
                return;
            }
            payload.soTienThu = manualTotal;
            payload.chiTietNo = chiTietNo;
        }

        setSubmitting(true);
        try {
            const data = await staffApi.createPayment(payload, idempotencyKey);
            setResult(data);
            setShowResult(true);
            toast.success("Thu tiền thành công");
            await refreshReaderData();
        } catch (err) {
            toast.error(err.message || "Thu tiền thất bại");
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="staff-payment-workspace">
            <PageHeader
                eyebrow="Staff"
                title="Thu tiền"
                description="Chọn độc giả còn nợ, thu tự động hoặc phân bổ thủ công theo từng khoản nợ."
                right={result ? (
                    <button className="soft-button" type="button" onClick={() => setShowResult(true)}>
                        Xem lại kết quả
                    </button>
                ) : null}
            />

            <div className="payment-reader-panel panel">
                <div className="payment-picker-heading">
                    <span className="payment-picker-icon"><Search size={18} /></span>
                    <div>
                        <h2>Độc giả còn nợ</h2>
                        <p>Tìm theo mã, tên, điện thoại hoặc email.</p>
                    </div>
                </div>
                <AsyncEntityPicker
                    value={selectedReader}
                    onChange={handleReaderChange}
                    loadOptions={loadDebtorOptions}
                    placeholder="Nhập mã, tên, điện thoại hoặc email"
                    ariaLabel="Tìm độc giả còn nợ"
                    minQueryLength={1}
                    getOptionKey={(option) => option.readerId || option.value}
                    getOptionLabel={(option) => option.readerName || option.label || option.readerId}
                    getOptionCode={(option) => option.readerId || option.code}
                    getOptionMetadata={(option) => option.metadata}
                />
            </div>

            {selectedReaderId && (
                <div className="payment-context-grid">
                    <DebtOverview context={debtContext} loading={loadingReaderData} error={readerDataError} />

                    <form className="panel payment-form-panel" onSubmit={submitPayment}>
                        <div className="payment-tabs" role="tablist" aria-label="Chế độ thu tiền">
                            <button
                                type="button"
                                className={activeTab === "AUTO" ? "is-active" : ""}
                                onClick={() => setActiveTab("AUTO")}
                            >
                                <CreditCard size={16} />
                                Thu tự động
                            </button>
                            <button
                                type="button"
                                className={activeTab === "MANUAL" ? "is-active" : ""}
                                onClick={() => setActiveTab("MANUAL")}
                            >
                                <ListChecks size={16} />
                                Chọn khoản nợ
                            </button>
                        </div>

                        <div className="payment-base-fields">
                            <label>
                                <span>Phương thức</span>
                                <select
                                    value={paymentMethodId}
                                    onChange={(event) => setPaymentMethodId(event.target.value)}
                                    disabled={paymentMethods.length === 0 || submitting}
                                >
                                    {paymentMethods.map((method) => (
                                        <option key={method.value} value={method.value}>{method.label}</option>
                                    ))}
                                </select>
                            </label>
                            <label>
                                <span>Ghi chú</span>
                                <input value={note} onChange={(event) => setNote(event.target.value)} disabled={submitting} />
                            </label>
                        </div>

                        {activeTab === "AUTO" ? (
                            <AutoPaymentPanel
                                amount={autoAmount}
                                onAmountChange={setAutoAmount}
                                disabled={submitting}
                                projection={autoProjection}
                            />
                        ) : (
                            <ManualPaymentPanel
                                debts={visibleDebts}
                                selectedAllocations={selectedAllocations}
                                hidePaid={hidePaid}
                                onHidePaidChange={setHidePaid}
                                onToggleDebt={toggleDebt}
                                onUpdateAllocation={updateAllocation}
                                onSelectAll={selectAllOutstanding}
                                onClearSelected={clearSelected}
                                loading={loadingReaderData}
                                error={readerDataError}
                            />
                        )}

                        <PaymentStickySummary
                            debtCount={summaryDebtCount}
                            balanceBefore={outstandingTotal}
                            amount={summaryAmount}
                            balanceAfter={balanceAfter}
                            submitting={submitting}
                            disabled={loadingReaderData || !selectedReaderId || paymentMethods.length === 0}
                        />
                    </form>
                </div>
            )}

            {!selectedReaderId && (
                <div className="panel payment-empty-state">
                    <Search size={24} />
                    <h2>Chọn độc giả để bắt đầu</h2>
                    <p>Danh sách khoản nợ, tổng quan công nợ và biểu mẫu thu sẽ hiển thị sau khi chọn độc giả còn nợ.</p>
                </div>
            )}

            {result && showResult && (
                <PaymentResultPanel
                    result={result}
                    paymentMethods={paymentMethods}
                    onClose={() => setShowResult(false)}
                />
            )}
        </div>
    );
}

function DebtOverview({ context, loading, error }) {
    if (loading) {
        return (
            <div className="panel payment-overview-panel">
                <div className="payment-overview-loading">Đang tải tổng quan công nợ...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="panel payment-overview-panel">
                <div className="payment-error" role="alert">{error}</div>
            </div>
        );
    }

    return (
        <div className="panel payment-overview-panel">
            <div className="panel-title">
                <h2>Tổng quan công nợ</h2>
                <span>{context?.readerId || "-"}</span>
            </div>
            <div className="payment-overview-reader">
                <strong>{context?.readerName || "-"}</strong>
                <span>{context?.borrowingImpacted ? "Có công nợ ảnh hưởng nghiệp vụ mượn" : "Không còn dư nợ ảnh hưởng mượn"}</span>
            </div>
            <div className="payment-metric-grid">
                <Metric label="Tổng còn nợ" value={formatMoney(context?.outstandingAmount || 0)} />
                <Metric label="Khoản còn nợ" value={context?.outstandingDebtCount ?? 0} />
                <Metric label="Đã thanh toán" value={formatMoney(context?.totalPaid || 0)} />
                <Metric label="Khoản cũ nhất" value={context?.oldestOutstandingDebtDate ? formatDateTime(context.oldestOutstandingDebtDate) : "-"} />
            </div>
        </div>
    );
}

function AutoPaymentPanel({ amount, onAmountChange, disabled, projection }) {
    return (
        <div className="payment-tab-panel">
            <label className="payment-amount-field">
                <span>Số tiền thu</span>
                <input
                    type="number"
                    min="0"
                    step="1000"
                    value={amount}
                    onChange={(event) => onAmountChange(event.target.value)}
                    disabled={disabled}
                />
            </label>
            <div className="payment-auto-projection">
                <span>Khoản dự kiến được phân bổ</span>
                <strong>{projection.count}</strong>
                <span>Tổng dự kiến thu</span>
                <strong>{formatMoney(projection.total)}</strong>
            </div>
        </div>
    );
}

function ManualPaymentPanel({
    debts,
    selectedAllocations,
    hidePaid,
    onHidePaidChange,
    onToggleDebt,
    onUpdateAllocation,
    onSelectAll,
    onClearSelected,
    loading,
    error
}) {
    return (
        <div className="payment-tab-panel">
            <div className="payment-debt-actions">
                <label className="payment-toggle">
                    <input
                        type="checkbox"
                        checked={hidePaid}
                        onChange={(event) => onHidePaidChange(event.target.checked)}
                    />
                    <span>Ẩn khoản đã thanh toán</span>
                </label>
                <div>
                    <button type="button" className="ghost-button" onClick={onSelectAll}>Chọn tất cả còn nợ</button>
                    <button type="button" className="soft-button danger-button" onClick={onClearSelected}>
                        <Trash2 size={16} />
                        Bỏ chọn
                    </button>
                </div>
            </div>

            {error && <div className="payment-error" role="alert">{error}</div>}
            {loading && <div className="payment-table-state">Đang tải khoản nợ...</div>}
            {!loading && debts.length === 0 && !error && (
                <div className="payment-table-state">Không có khoản nợ phù hợp bộ lọc hiện tại.</div>
            )}

            {!loading && debts.length > 0 && (
                <DataTable
                    data={debts}
                    rowClassName={(row) => selectedAllocations[getDebtId(row)] !== undefined ? "selected-row" : ""}
                    columns={[
                        {
                            key: "chon",
                            title: "",
                            className: "selection-count-cell",
                            width: "70px",
                            render: (row) => {
                                const debtId = getDebtId(row);
                                const disabled = getDebtRemaining(row) <= 0;
                                return (
                                    <input
                                        className="table-checkbox"
                                        type="checkbox"
                                        checked={selectedAllocations[debtId] !== undefined}
                                        disabled={disabled}
                                        onChange={() => onToggleDebt(row)}
                                    />
                                );
                            }
                        },
                        { key: "maKhoanNo", title: "Mã nợ" },
                        { key: "maLoaiKhoanNo", title: "Loại", render: (row) => displayCode(row.maLoaiKhoanNo) },
                        { key: "lyDo", title: "Lý do" },
                        { key: "soTienPhatSinh", title: "Phát sinh", render: (row) => formatMoney(row.soTienPhatSinh) },
                        { key: "soTienDaThanhToan", title: "Đã trả", render: (row) => formatMoney(row.soTienDaThanhToan) },
                        { key: "soTienConLai", title: "Còn lại", render: (row) => formatMoney(getDebtRemaining(row)) },
                        {
                            key: "soTienApDung",
                            title: "Tiền áp dụng",
                            render: (row) => {
                                const debtId = getDebtId(row);
                                return selectedAllocations[debtId] !== undefined ? (
                                    <input
                                        type="number"
                                        min="0"
                                        max={getDebtRemaining(row)}
                                        value={selectedAllocations[debtId]}
                                        onChange={(event) => onUpdateAllocation(row, event.target.value)}
                                    />
                                ) : "-";
                            }
                        },
                        { key: "trangThai", title: "Trạng thái", render: (row) => <StatusBadge value={row.trangThai} /> }
                    ]}
                />
            )}
        </div>
    );
}

function PaymentStickySummary({ debtCount, balanceBefore, amount, balanceAfter, submitting, disabled }) {
    return (
        <div className="payment-sticky-summary">
            <div className="payment-summary-grid">
                <Metric label="Số khoản" value={debtCount} />
                <Metric label="Dư nợ trước" value={formatMoney(balanceBefore)} />
                <Metric label="Tổng thu" value={formatMoney(amount)} />
                <Metric label="Dư nợ sau" value={formatMoney(balanceAfter)} />
            </div>
            <button className="primary-button" type="submit" disabled={disabled || submitting || amount <= 0}>
                {submitting ? "Đang thu tiền..." : "Xác nhận thu tiền"}
            </button>
        </div>
    );
}

function PaymentResultPanel({ result, paymentMethods, onClose }) {
    const method = paymentMethods.find((item) => item.value === result?.maPhuongThuc);

    return (
        <ResultModal title="Kết quả phiếu thu" onClose={onClose}>
            <div className="result-stack">
                <div className="result-grid">
                    <ResultItem label="Mã phiếu" value={result.maPhieuThu} />
                    <ResultItem label="Độc giả" value={result.maDocGia} />
                    <ResultItem label="Nhân viên thu" value={result.maNhanVienThu} />
                    <ResultItem label="Phương thức" value={method?.label || displayCode(result.maPhuongThuc)} />
                    <ResultItem label="Loại thu" value={result.loaiThu} />
                    <ResultItem label="Số tiền thu" value={formatMoney(result.soTienThu)} />
                    <ResultItem label="Ngày thu" value={formatDateTime(result.ngayThu)} />
                    <ResultItem label="Trạng thái" value={<StatusBadge value={result.trangThai} />} />
                </div>

                <DataTable
                    data={result.chiTietNo || []}
                    columns={[
                        { key: "maChiTietPhieuThu", title: "Mã chi tiết" },
                        { key: "maKhoanNo", title: "Khoản nợ" },
                        { key: "soTienApDung", title: "Tiền áp dụng", render: (row) => formatMoney(row.soTienApDung) }
                    ]}
                />
            </div>
        </ResultModal>
    );
}

function Metric({ label, value }) {
    return (
        <span className="payment-metric">
            <small>{label}</small>
            <strong>{value ?? "-"}</strong>
        </span>
    );
}

function ResultItem({ label, value }) {
    return (
        <div className="result-item">
            <span>{label}</span>
            <strong>{value || "-"}</strong>
        </div>
    );
}

function getDebtId(row) {
    return row.maKhoanNo ?? row.MaKhoanNo ?? row.maNo ?? row.id;
}

function getDebtRemaining(row) {
    return Number(row.soTienConLai ?? row.SoTienConLai ?? row.conLai ?? 0);
}

function projectAutoAllocation(debts, amount) {
    let remaining = Math.max(0, Number(amount || 0));
    let total = 0;
    let count = 0;
    const openDebts = [...debts]
        .filter((row) => getDebtRemaining(row) > 0)
        .sort((a, b) => {
            const dateCompare = new Date(a.ngayPhatSinh || 0) - new Date(b.ngayPhatSinh || 0);
            if (dateCompare !== 0) return dateCompare;
            return String(getDebtId(a)).localeCompare(String(getDebtId(b)));
        });

    for (const debt of openDebts) {
        if (remaining <= 0) break;
        const applied = Math.min(remaining, getDebtRemaining(debt));
        if (applied > 0) {
            count += 1;
            total += applied;
            remaining -= applied;
        }
    }

    return { count, total };
}

function buildPaymentIdempotencyKey() {
    const timestamp = Date.now().toString(36).toUpperCase();
    const random = Math.random().toString(36).slice(2, 6).toUpperCase();
    return `PAY-${timestamp}-${random}`;
}
