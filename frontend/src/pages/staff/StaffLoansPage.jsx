import { useCallback, useEffect, useState } from "react";
import { Building2, CheckCircle2, Printer, RotateCcw, ScanLine, ShoppingCart, Trash2, UserRound } from "lucide-react";
import { staffApi } from "../../api/staffApi";
import AsyncEntityPicker from "../../components/AsyncEntityPicker";
import PageHeader from "../../components/PageHeader";
import StatusBadge from "../../components/StatusBadge";
import { formatDate, formatDateTime, formatMoney } from "../../utils/displayUtils";

export default function StaffLoansPage() {
    const [staffContext, setStaffContext] = useState(null);
    const [staffLoading, setStaffLoading] = useState(true);
    const [staffError, setStaffError] = useState("");
    const [selectedReader, setSelectedReader] = useState(null);
    const [borrowingContext, setBorrowingContext] = useState(null);
    const [currentLoans, setCurrentLoans] = useState([]);
    const [readerLoading, setReaderLoading] = useState(false);
    const [readerError, setReaderError] = useState("");
    const [readerRetryKey, setReaderRetryKey] = useState(0);
    const [copySelection, setCopySelection] = useState(null);
    const [cart, setCart] = useState([]);
    const [cartMessage, setCartMessage] = useState("");
    const [preview, setPreview] = useState(null);
    const [previewLoading, setPreviewLoading] = useState(false);
    const [previewError, setPreviewError] = useState("");
    const [note, setNote] = useState("");
    const [submitLoading, setSubmitLoading] = useState(false);
    const [submitError, setSubmitError] = useState("");
    const [idempotencyKey, setIdempotencyKey] = useState(() => createIdempotencyKey());
    const [loanResult, setLoanResult] = useState(null);

    const loadReaderOptions = useCallback(
        (query, options) => staffApi.searchReaders(query, options),
        []
    );

    const loadCopyOptions = useCallback(
        (query, options) => selectedReader
            ? staffApi.searchLoanCopies(query, selectedReader.value, options)
            : Promise.resolve([]),
        [selectedReader]
    );

    const loadExactCopy = useCallback(async (code, options) => {
        if (!selectedReader) return null;
        const copy = await staffApi.getLoanCopyByCode(code, selectedReader.value, options);
        if (!copy.borrowable) {
            const reason = copy.blockingReasons?.[0];
            throw new Error(reason ? `${reason.code}: ${reason.message}` : "Cuốn sách không hợp lệ để mượn");
        }
        return copy;
    }, [selectedReader]);

    const loadStaffContext = useCallback(async () => {
        setStaffLoading(true);
        setStaffError("");
        try {
            setStaffContext(await staffApi.getContext());
        } catch (error) {
            setStaffContext(null);
            setStaffError(error.message || "Không tải được ngữ cảnh nhân viên");
        } finally {
            setStaffLoading(false);
        }
    }, []);

    useEffect(() => {
        let active = true;
        staffApi.getContext().then((context) => {
            if (active) setStaffContext(context);
        }).catch((error) => {
            if (active) setStaffError(error.message || "Không tải được ngữ cảnh nhân viên");
        }).finally(() => {
            if (active) setStaffLoading(false);
        });
        return () => { active = false; };
    }, []);

    useEffect(() => {
        if (!selectedReader) return undefined;

        const controller = new AbortController();
        Promise.all([
            staffApi.getReaderBorrowingContext(selectedReader.value, { signal: controller.signal }),
            staffApi.getReaderCurrentLoansDetail(selectedReader.value, { signal: controller.signal })
        ]).then(([context, loans]) => {
            if (controller.signal.aborted) return;
            setBorrowingContext(context);
            setCurrentLoans(Array.isArray(loans) ? loans : []);
        }).catch((error) => {
            if (controller.signal.aborted) return;
            setBorrowingContext(null);
            setCurrentLoans([]);
            setReaderError(error.message || "Không tải được borrowing context của độc giả");
        }).finally(() => {
            if (!controller.signal.aborted) setReaderLoading(false);
        });

        return () => controller.abort();
    }, [readerRetryKey, selectedReader]);

    useEffect(() => {
        if (!selectedReader || cart.length === 0) return undefined;

        const controller = new AbortController();
        const timer = window.setTimeout(() => {
            setPreviewLoading(true);
            setPreviewError("");
            staffApi.previewLoan({
                readerId: selectedReader.value,
                copyIds: cart.map((item) => item.copyId)
            }, { signal: controller.signal }).then((result) => {
                if (!controller.signal.aborted) setPreview(result);
            }).catch((error) => {
                if (controller.signal.aborted) return;
                setPreview(null);
                setPreviewError(error.message || "Không preview được giỏ mượn");
            }).finally(() => {
                if (!controller.signal.aborted) setPreviewLoading(false);
            });
        }, 180);

        return () => {
            window.clearTimeout(timer);
            controller.abort();
        };
    }, [cart, selectedReader]);

    function changeSelectedReader(reader) {
        setSelectedReader(reader);
        setBorrowingContext(null);
        setCurrentLoans([]);
        setReaderError("");
        setReaderLoading(Boolean(reader));
        setCopySelection(null);
        setCart([]);
        setCartMessage("");
        setPreview(null);
        setPreviewError("");
        setNote("");
        setSubmitError("");
        setLoanResult(null);
        setIdempotencyKey(createIdempotencyKey());
    }

    function retryReaderContext() {
        setReaderLoading(true);
        setReaderError("");
        setReaderRetryKey((value) => value + 1);
    }

    function addCopy(copy) {
        setCopySelection(null);
        if (!copy) return;
        if (cart.some((item) => item.copyId === copy.copyId)) {
            setCartMessage(`DUPLICATE_COPY: Cuốn ${copy.copyId} đã có trong giỏ.`);
            return;
        }
        const remaining = borrowingContext?.quota?.remaining;
        if (remaining !== null && remaining !== undefined && cart.length >= remaining) {
            setCartMessage(`BORROW_QUOTA_EXCEEDED: Độc giả chỉ còn ${remaining} lượt mượn.`);
            return;
        }
        setCart((items) => [...items, copy]);
        setCartMessage("");
        setPreviewLoading(true);
        setPreviewError("");
    }

    function removeCopy(copyId) {
        const next = cart.filter((item) => item.copyId !== copyId);
        setCart(next);
        if (next.length === 0) {
            setPreview(null);
            setPreviewLoading(false);
            setPreviewError("");
        } else {
            setPreviewLoading(true);
            setPreviewError("");
        }
        setCartMessage("");
    }

    async function submitLoan() {
        if (!selectedReader || !staffContext?.defaultBranch?.id || cart.length === 0 || !preview?.eligible) return;
        setSubmitLoading(true);
        setSubmitError("");
        try {
            const response = await staffApi.createLoan({
                readerId: selectedReader.value,
                branchId: staffContext.defaultBranch.id,
                copyIds: cart.map((item) => item.copyId),
                note: note.trim() || null
            }, idempotencyKey);
            setLoanResult(response);
            setCurrentLoans(await staffApi.getReaderCurrentLoansDetail(selectedReader.value));
        } catch (error) {
            setSubmitError(error.message || "Khong tao duoc phieu muon");
        } finally {
            setSubmitLoading(false);
        }
    }

    function resetTransaction() {
        setSelectedReader(null);
        setBorrowingContext(null);
        setCurrentLoans([]);
        setReaderLoading(false);
        setReaderError("");
        setCopySelection(null);
        setCart([]);
        setCartMessage("");
        setPreview(null);
        setPreviewLoading(false);
        setPreviewError("");
        setNote("");
        setSubmitLoading(false);
        setSubmitError("");
        setLoanResult(null);
        setIdempotencyKey(createIdempotencyKey());
    }

    return (
        <div className="staff-loan-reader-step">
            <PageHeader
                eyebrow="Staff · Bước 1"
                title="Chọn độc giả mượn sách"
                description="Quét thẻ hoặc tìm theo mã, tên, email, điện thoại. Điều kiện mượn được xác thực từ backend."
            />

            <section className="panel loan-staff-context" aria-label="Ngữ cảnh nhân viên và chi nhánh">
                <div className="panel-title">
                    <h2>Điểm phục vụ</h2>
                    {staffContext?.operational && <StatusBadge value="Sẵn sàng" />}
                </div>
                {staffLoading && <p className="loan-state-message" role="status">Đang tải ngữ cảnh nhân viên...</p>}
                {!staffLoading && staffError && (
                    <div className="loan-message is-error" role="alert">
                        <span>{staffError}</span>
                        <button className="soft-button" type="button" onClick={loadStaffContext}>Thử lại</button>
                    </div>
                )}
                {!staffLoading && staffContext && (
                    <div className="loan-staff-grid">
                        <ContextIdentity icon={<UserRound size={18} />} label="Nhân viên" primary={staffContext.staffName} secondary={staffContext.staffId} />
                        <ContextIdentity icon={<Building2 size={18} />} label="Chi nhánh" primary={staffContext.defaultBranch?.name} secondary={staffContext.defaultBranch?.id} />
                    </div>
                )}
                {staffContext && !staffContext.operational && (
                    <div className="loan-message is-blocking" role="alert">
                        Không thể tiếp tục nghiệp vụ: {staffContext.operationalBlockReason || "staff context chưa hợp lệ"}.
                    </div>
                )}
            </section>

            <section className="panel loan-reader-picker-panel">
                <div className="loan-picker-heading">
                    <span className="loan-picker-icon"><ScanLine size={22} /></span>
                    <div>
                        <h2>Quét thẻ hoặc tìm độc giả</h2>
                        <p>Máy quét nhập mã thẻ rồi gửi Enter sẽ chọn exact match.</p>
                    </div>
                </div>
                <AsyncEntityPicker
                    value={selectedReader}
                    onChange={changeSelectedReader}
                    loadOptions={loadReaderOptions}
                    minQueryLength={1}
                    placeholder="Mã thẻ, tên, email hoặc điện thoại"
                    ariaLabel="Tìm hoặc quét thẻ độc giả"
                    disabled={staffLoading || !staffContext?.operational}
                    requireExactMatchOnEnter
                    getOptionMetadata={(option) => [option.metadata?.email, option.metadata?.phone].filter(Boolean).join(" · ")}
                />
            </section>

            {!selectedReader && (
                <section className="panel loan-empty-state">
                    <UserRound size={32} />
                    <h2>Chưa chọn độc giả</h2>
                    <p>Borrowing context và sách đang mượn sẽ xuất hiện sau khi quét hoặc chọn đúng độc giả.</p>
                </section>
            )}

            {selectedReader && readerLoading && (
                <section className="panel loan-empty-state" role="status">
                    <h2>Đang kiểm tra điều kiện mượn...</h2>
                    <p>Đang đồng bộ thẻ, gói, quota, quá hạn và nợ từ backend.</p>
                </section>
            )}

            {selectedReader && !readerLoading && readerError && (
                <section className="panel loan-message is-error" role="alert">
                    <span>{readerError}</span>
                    <button className="soft-button" type="button" onClick={retryReaderContext}>Thử lại</button>
                </section>
            )}

            {borrowingContext && !readerLoading && (
                <>
                    <ReaderBorrowingContext context={borrowingContext} />
                    <LoanCartStep
                        borrowingContext={borrowingContext}
                        cart={cart}
                        cartMessage={cartMessage}
                        copySelection={copySelection}
                        loadCopyOptions={loadCopyOptions}
                        loadExactCopy={loadExactCopy}
                        onCopySelection={addCopy}
                        onRemove={removeCopy}
                        preview={preview}
                        previewLoading={previewLoading}
                        previewError={previewError}
                        note={note}
                        onNoteChange={setNote}
                        onSubmit={submitLoan}
                        submitLoading={submitLoading}
                        submitError={submitError}
                        loanResult={loanResult}
                        onResetTransaction={resetTransaction}
                    />
                    <CurrentLoans loans={currentLoans} readerName={borrowingContext.reader?.name} />
                </>
            )}
        </div>
    );
}

function createIdempotencyKey() {
    if (window.crypto?.randomUUID) return window.crypto.randomUUID();
    return `loan-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function ContextIdentity({ icon, label, primary, secondary }) {
    return (
        <div className="loan-context-identity">
            <span>{icon}</span>
            <div><small>{label}</small><strong>{primary || "Chưa xác định"}</strong><em>{secondary || "Không có mã"}</em></div>
        </div>
    );
}

function ReaderBorrowingContext({ context }) {
    const notices = [
        ...(context.blockingReasons || []).map((item) => ({ ...item, type: "blocking" })),
        ...(context.warnings || []).map((item) => ({ ...item, type: "warning" }))
    ];
    const quota = context.quota || {};
    return (
        <section className="panel loan-borrowing-context">
            <div className="panel-title">
                <div><h2>{context.reader?.name}</h2><p className="loan-reader-code">{context.reader?.id}</p></div>
                <span className={`loan-eligibility ${context.eligible ? "is-eligible" : "is-blocked"}`}>
                    {context.eligible ? "Đủ điều kiện mượn" : "Đang bị chặn"}
                </span>
            </div>
            <div className="loan-context-metrics">
                <Metric label="Hạn thẻ" value={formatDate(context.card?.expiryDate)} detail={statusLabel(context.card?.status)} />
                <Metric label="Gói" value={context.membership?.planName || context.membership?.planId || "Không có"} detail={formatDate(context.membership?.expiryDate)} />
                <Metric label="Sách đang mượn" value={`${quota.current ?? context.obligations?.currentLoans ?? 0} / ${quota.maximum ?? "—"}`} detail={`${quota.remaining ?? "—"} lượt còn lại`} />
                <Metric label="Quá hạn" value={`${context.overdue?.count ?? context.obligations?.overdueLoans ?? 0} cuốn`} detail={`${context.overdue?.maxDays ?? 0} ngày nhiều nhất`} danger={(context.overdue?.count || 0) > 0} />
                <Metric label="Nợ còn lại" value={formatMoney(context.debt?.outstanding ?? context.obligations?.outstandingDebt ?? 0)} detail="Backend xác thực khi mượn" danger={Number(context.debt?.outstanding || 0) > 0} />
            </div>
            {notices.length > 0 ? (
                <div className="loan-notice-list">
                    {notices.map((notice) => (
                        <div className={`loan-message is-${notice.type}`} key={`${notice.type}-${notice.code}`}>
                            <code>{notice.code}</code><span>{notice.message}</span>
                        </div>
                    ))}
                </div>
            ) : <div className="loan-message is-success">Không có cảnh báo hoặc lý do chặn.</div>}
        </section>
    );
}

function Metric({ label, value, detail, danger = false }) {
    return <div className={`loan-context-metric${danger ? " is-danger" : ""}`}><small>{label}</small><strong>{value || "—"}</strong><span>{detail || "—"}</span></div>;
}

function LoanCartStep({
    borrowingContext,
    cart,
    cartMessage,
    copySelection,
    loadCopyOptions,
    loadExactCopy,
    onCopySelection,
    onRemove,
    preview,
    previewLoading,
    previewError,
    note,
    onNoteChange,
    onSubmit,
    submitLoading,
    submitError,
    loanResult,
    onResetTransaction
}) {
    const remaining = borrowingContext.quota?.remaining;
    const pickerBlocked = !borrowingContext.eligible || remaining === 0;
    const previewItems = new Map((preview?.items || []).map((item) => [item.copyId, item]));
    const notices = [
        ...(preview?.blockingReasons || []).map((item) => ({ ...item, type: "blocking" })),
        ...(preview?.warnings || []).map((item) => ({ ...item, type: "warning" }))
    ];
    const canSubmit = cart.length > 0 && preview?.eligible && !previewLoading && !submitLoading && !loanResult;

    return (
        <section className="panel loan-cart-panel" aria-label="Picker cuốn và giỏ mượn">
            <div className="loan-picker-heading">
                <span className="loan-picker-icon"><ShoppingCart size={22} /></span>
                <div>
                    <h2>Quét cuốn hoặc tìm đầu sách</h2>
                    <p>Enter chỉ nhận barcode/mã cuốn exact; tìm tên hoặc ISBN rồi chọn đúng cuốn tại chi nhánh hiện tại.</p>
                </div>
            </div>

            <AsyncEntityPicker
                value={copySelection}
                onChange={onCopySelection}
                loadOptions={loadCopyOptions}
                loadExactOption={loadExactCopy}
                minQueryLength={1}
                placeholder="Barcode, mã cuốn, tên đầu sách hoặc ISBN"
                ariaLabel="Quét hoặc tìm cuốn sách để thêm vào giỏ"
                disabled={pickerBlocked}
                requireExactMatchOnEnter
                exactNotFoundMessage="Không tìm thấy barcode hoặc mã cuốn khớp chính xác"
                autoFocus
                getOptionCode={(option) => option.barcode || option.copyId}
                getOptionMetadata={(option) => [option.copyId, option.isbn, option.locationLabel].filter(Boolean).join(" · ")}
            />

            {pickerBlocked && (
                <div className="loan-message is-blocking" role="alert">
                    {remaining === 0 ? "BORROW_QUOTA_REACHED: Độc giả đã hết hạn mức mượn." : "Độc giả đang có lý do chặn mượn; không thể thêm cuốn vào giỏ."}
                </div>
            )}
            {cartMessage && <div className="loan-message is-warning" role="status">{cartMessage}</div>}

            <div className="loan-cart-heading">
                <div><h3>Giỏ mượn</h3><span>{cart.length} cuốn</span></div>
                {preview?.quota && (
                    <strong className={preview.eligible ? "is-valid" : "is-invalid"}>
                        Sau giỏ: {preview.quota.after} / {preview.quota.maximum ?? "—"}
                    </strong>
                )}
            </div>

            {cart.length === 0 ? (
                <div className="loan-cart-empty">
                    <ShoppingCart size={28} />
                    <p>Giỏ đang trống. Không tải bảng toàn bộ cuốn sẵn có.</p>
                </div>
            ) : (
                <div className="loan-cart-list">
                    {cart.map((copy) => (
                        <LoanCartItem
                            key={copy.copyId}
                            copy={{ ...copy, ...(previewItems.get(copy.copyId) || {}) }}
                            onRemove={() => onRemove(copy.copyId)}
                        />
                    ))}
                </div>
            )}

            {previewLoading && <p className="loan-state-message" role="status">Đang cập nhật preview hạn mức và hạn trả...</p>}
            {!previewLoading && previewError && <div className="loan-message is-error" role="alert">{previewError}</div>}
            {!previewLoading && preview && notices.length > 0 && (
                <div className="loan-notice-list">
                    {notices.map((notice) => (
                        <div className={`loan-message is-${notice.type}`} key={`${notice.type}-${notice.code}`}>
                            <code>{notice.code}</code><span>{notice.message}</span>
                        </div>
                    ))}
                </div>
            )}
            {!previewLoading && preview && notices.length === 0 && (
                <div className="loan-message is-success">Preview hợp lệ. Backend sẽ kiểm tra lại khi tạo phiếu.</div>
            )}
            {preview?.disclaimer && <p className="loan-preview-disclaimer">{preview.disclaimer}</p>}

            {cart.length > 0 && (
                <div className="loan-submit-box">
                    <label>
                        <span>Ghi chu</span>
                        <textarea
                            value={note}
                            onChange={(event) => onNoteChange(event.target.value)}
                            maxLength={255}
                            rows={3}
                            disabled={submitLoading || Boolean(loanResult)}
                            placeholder="Thong tin can in tren phieu"
                        />
                    </label>
                    {submitError && <div className="loan-message is-error" role="alert">{submitError}</div>}
                    <button className="primary-button loan-submit-button" type="button" onClick={onSubmit} disabled={!canSubmit}>
                        {submitLoading ? "Dang tao phieu..." : "Tao phieu muon"}
                    </button>
                </div>
            )}

            {loanResult && <LoanResult result={loanResult} onResetTransaction={onResetTransaction} />}
        </section>
    );
}

function LoanResult({ result, onResetTransaction }) {
    const items = result.printData?.items || result.items || result.chiTiet || [];
    return (
        <section className="loan-result-panel" aria-label="Ket qua tao phieu muon">
            <div className="loan-result-header">
                <span><CheckCircle2 size={22} /></span>
                <div>
                    <h3>Da tao phieu {result.loanId || result.maPhieuMuon}</h3>
                    <p>{formatDateTime(result.createdAt || result.ngayMuon)} · {result.branchId || result.maChiNhanh}</p>
                </div>
            </div>
            <div className="loan-result-grid">
                <Metric label="Doc gia" value={result.readerId || result.maDocGia} />
                <Metric label="Nhan vien" value={result.staffId || result.maNhanVienLap} />
                <Metric label="Trang thai" value={result.status || result.trangThai} />
            </div>
            <div className="loan-print-list">
                {items.map((item) => (
                    <div key={item.loanDetailId || item.maChiTietMuon}>
                        <strong>{item.copyId || item.maCuonSach}</strong>
                        <span>{formatDateTime(item.dueAt || item.hanTra)}</span>
                    </div>
                ))}
            </div>
            <div className="loan-result-actions">
                <button className="soft-button" type="button" onClick={() => window.print()}>
                    <Printer size={16} /> In phieu
                </button>
                <button className="primary-button" type="button" onClick={onResetTransaction}>
                    <RotateCcw size={16} /> Giao dich moi
                </button>
            </div>
        </section>
    );
}

function LoanCartItem({ copy, onRemove }) {
    const itemNotices = [
        ...(copy.blockingReasons || []).map((item) => ({ ...item, type: "blocking" })),
        ...(copy.warnings || []).map((item) => ({ ...item, type: "warning" }))
    ];
    return (
        <article className={`loan-cart-item${copy.borrowable === false ? " is-invalid" : ""}`}>
            <div className="loan-cart-item-main">
                <div>
                    <strong>{copy.titleName}</strong>
                    <span>{copy.copyId}{copy.barcode ? ` · ${copy.barcode}` : ""}</span>
                </div>
                <button className="icon-button" type="button" onClick={onRemove} aria-label={`Xóa cuốn ${copy.copyId} khỏi giỏ`}>
                    <Trash2 size={17} />
                </button>
            </div>
            <div className="loan-cart-item-meta">
                <span><small>Vị trí</small><b>{copy.locationLabel || copy.locationId || "—"}</b></span>
                <span><small>Quy định</small><b>{copy.ruleId ? `${copy.ruleId} · ${copy.borrowDays} ngày` : "Đang preview"}</b></span>
                <span><small>Hạn trả dự kiến</small><b>{copy.expectedDueAt ? formatDateTime(copy.expectedDueAt) : "Đang preview"}</b></span>
            </div>
            {itemNotices.map((notice) => (
                <div className={`loan-cart-item-notice is-${notice.type}`} key={`${notice.type}-${notice.code}`}>
                    <code>{notice.code}</code><span>{notice.message}</span>
                </div>
            ))}
        </article>
    );
}

function CurrentLoans({ loans, readerName }) {
    const overdueCount = loans.filter((loan) => loan.overdueDays > 0).length;
    return (
        <details className="panel loan-current-loans" open={overdueCount > 0}>
            <summary>
                <span>Sách đang mượn của {readerName}</span>
                <strong>{loans.length} cuốn{overdueCount > 0 ? ` · ${overdueCount} quá hạn` : ""}</strong>
            </summary>
            {loans.length === 0 ? <p className="loan-state-message">Độc giả chưa có sách đang mượn.</p> : (
                <div className="table-wrap">
                    <table className="data-table">
                        <thead><tr><th>Cuốn sách</th><th>Đầu sách</th><th>Chi nhánh</th><th>Ngày mượn</th><th>Hạn trả</th><th>Trạng thái</th></tr></thead>
                        <tbody>{loans.map((loan) => (
                            <tr className={loan.overdueDays > 0 ? "is-overdue" : ""} key={loan.loanDetailId}>
                                <td><strong>{loan.copyId}</strong><small>{loan.barcode || "Không có barcode"}</small></td>
                                <td><strong>{loan.titleName}</strong><small>{loan.titleId}</small></td>
                                <td>{loan.branchName || loan.branchId}</td>
                                <td>{formatDateTime(loan.borrowedAt)}</td>
                                <td>{formatDateTime(loan.dueAt)}</td>
                                <td>{loan.overdueDays > 0 ? <span className="loan-overdue-badge">Quá hạn {loan.overdueDays} ngày</span> : <StatusBadge value={loan.status} />}</td>
                            </tr>
                        ))}</tbody>
                    </table>
                </div>
            )}
        </details>
    );
}

function statusLabel(value) {
    return ({ VALID: "Còn hiệu lực", EXPIRING: "Sắp hết hạn", EXPIRED: "Hết hạn" })[value] || value || "Chưa xác định";
}
