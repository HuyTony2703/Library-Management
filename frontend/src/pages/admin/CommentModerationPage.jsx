import { useEffect, useState } from "react";
import { EyeOff, MoreHorizontal, RefreshCcw, RotateCcw, Search, Trash2 } from "lucide-react";
import { useSearchParams } from "react-router-dom";
import { adminApi } from "../../api/adminApi";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";
import { useActionDialog } from "../../components/ActionDialogProvider";
import { useAuth } from "../../context/AuthContext";

const DEFAULT_REASONS = {
    hide: "Bình luận không phù hợp",
    delete: "Nội dung vi phạm quy định thư viện"
};

export default function CommentModerationPage() {
    const toast = useToast();
    const actionDialog = useActionDialog();
    const { user } = useAuth();
    const [searchParams, setSearchParams] = useSearchParams();

    const [comments, setComments] = useState([]);
    const [loading, setLoading] = useState(false);
    const [openActionMenu, setOpenActionMenu] = useState(null);
    const [filter, setFilter] = useState(() => ({
        status: searchParams.get("status") || "Tất cả",
        maDauSach: searchParams.get("maDauSach") || "",
        keyword: searchParams.get("keyword") || ""
    }));

    function updateFilter(field, value) {
        setFilter((prev) => ({
            ...prev,
            [field]: value
        }));
    }

    async function loadComments(nextFilter = filter) {
        setLoading(true);

        try {
            const data = await adminApi.getComments(nextFilter);
            setComments(Array.isArray(data) ? data : []);
        } catch (err) {
            toast.error(err.message || "Không tải được bình luận");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        const nextFilter = {
            status: searchParams.get("status") || "Tất cả",
            maDauSach: searchParams.get("maDauSach") || "",
            keyword: searchParams.get("keyword") || ""
        };

        setFilter(nextFilter);
        loadComments(nextFilter);
    }, [searchParams]);

    function submitFilter(event) {
        event.preventDefault();

        const params = {};

        if (filter.status && filter.status !== "Tất cả") {
            params.status = filter.status;
        }

        if (filter.maDauSach.trim()) {
            params.maDauSach = filter.maDauSach.trim();
        }

        if (filter.keyword.trim()) {
            params.keyword = filter.keyword.trim();
        }

        setSearchParams(params);
    }

    async function moderate(row, action) {
        setOpenActionMenu(null);

        const employeeId = user?.maNhanVien || "NV_ADMIN";
        const actionLabel = action === "hide"
            ? "ẩn"
            : action === "delete"
                ? "xóa"
                : "khôi phục";

        let reason = "";

        if (action === "hide" || action === "delete") {
            reason = await actionDialog.prompt({
                title: `Lý do ${actionLabel} bình luận`,
                message: `Nhập lý do xử lý bình luận ${row.maBinhLuan}.`,
                defaultValue: DEFAULT_REASONS[action],
                confirmLabel: "Tiếp tục"
            });

            if (reason === null) {
                return;
            }

            if (!reason.trim()) {
                toast.error("Vui lòng nhập lý do xử lý bình luận");
                return;
            }
        }

        const confirmed = await actionDialog.confirm({
            title: `Xác nhận ${actionLabel} bình luận`,
            message: `Bạn chắc chắn muốn ${actionLabel} bình luận ${row.maBinhLuan}?`,
            confirmLabel: action === "delete" ? "Xóa bình luận" : "Xác nhận",
            danger: action === "delete"
        });

        if (!confirmed) {
            return;
        }

        setLoading(true);

        try {
            const payload = {
                maNhanVienXuLy: employeeId,
                lyDoAnXoa: reason
            };

            if (action === "hide") {
                await adminApi.hideComment(row.maBinhLuan, payload);
            } else if (action === "delete") {
                await adminApi.deleteComment(row.maBinhLuan, payload);
            } else {
                await adminApi.restoreComment(row.maBinhLuan, payload);
            }

            toast.success("Xử lý bình luận thành công");
            await loadComments();
        } catch (err) {
            toast.error(err.message || "Xử lý bình luận thất bại");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <PageHeader
                eyebrow="Staff"
                title="Kiểm duyệt bình luận"
                description="Lọc, ẩn, xóa mềm hoặc khôi phục bình luận của độc giả trên đầu sách."
            />

            <form className="panel comment-filter-form" onSubmit={submitFilter}>
                <label>Trạng thái</label>
                <select
                    value={filter.status}
                    onChange={(event) => updateFilter("status", event.target.value)}
                >
                    <option value="Tất cả">Tất cả</option>
                    <option value="Hiển thị">Hiển thị</option>
                    <option value="Đã ẩn">Đã ẩn</option>
                    <option value="Đã xóa">Đã xóa</option>
                </select>

                <label>Mã đầu sách</label>
                <input
                    value={filter.maDauSach}
                    onChange={(event) => updateFilter("maDauSach", event.target.value)}
                    placeholder="Ví dụ: F01"
                />

                <label>Từ khóa</label>
                <input
                    value={filter.keyword}
                    onChange={(event) => updateFilter("keyword", event.target.value)}
                    placeholder="Tìm nội dung, độc giả, tên sách"
                />

                <button className="primary-button" disabled={loading}>
                    <Search size={17} />
                    Tìm kiếm
                </button>

                <button className="soft-button" type="button" onClick={() => loadComments()} disabled={loading}>
                    <RefreshCcw size={17} />
                    Tải lại
                </button>
            </form>

            <div className="panel">
                <div className="panel-title">
                    <h2>Danh sách bình luận</h2>
                    <span>{comments.length} bình luận</span>
                </div>

                <DataTable
                    data={comments}
                    columns={[
                        { key: "maBinhLuan", title: "Mã BL" },
                        { key: "maDocGia", title: "Độc giả" },
                        { key: "hoTenDocGia", title: "Họ tên" },
                        { key: "maDauSach", title: "Đầu sách" },
                        { key: "tenDauSach", title: "Tên sách" },
                        {
                            key: "noiDung",
                            title: "Nội dung",
                            render: (row) => (
                                <span className="comment-content">
                                    {row.noiDung}
                                </span>
                            )
                        },
                        {
                            key: "trangThai",
                            title: "Trạng thái",
                            render: (row) => <StatusBadge value={row.trangThai} />
                        },
                        {
                            key: "actions",
                            title: "Thao tác",
                            render: (row) => (
                                <CommentActionMenu
                                    row={row}
                                    loading={loading}
                                    open={openActionMenu === row.maBinhLuan}
                                    onToggle={() => setOpenActionMenu((current) =>
                                        current === row.maBinhLuan ? null : row.maBinhLuan
                                    )}
                                    onAction={moderate}
                                />
                            )
                        }
                    ]}
                />
            </div>
        </div>
    );
}

function CommentActionMenu({ row, loading, open, onToggle, onAction }) {
    return (
        <div className="action-menu-cell">
            <button
                className="icon-button compact-icon-button"
                type="button"
                onClick={onToggle}
                aria-label={`Mở thao tác cho bình luận ${row.maBinhLuan}`}
            >
                <MoreHorizontal size={19} />
            </button>

            {open && (
                <div className="inline-action-menu">
                    <button
                        className="soft-button"
                        onClick={() => onAction(row, "hide")}
                        disabled={loading || row.trangThai === "Đã ẩn"}
                        type="button"
                    >
                        <EyeOff size={15} />
                        Ẩn
                    </button>

                    <button
                        className="soft-button danger-button"
                        onClick={() => onAction(row, "delete")}
                        disabled={loading || row.trangThai === "Đã xóa"}
                        type="button"
                    >
                        <Trash2 size={15} />
                        Xóa
                    </button>

                    <button
                        className="soft-button"
                        onClick={() => onAction(row, "restore")}
                        disabled={loading || row.trangThai === "Hiển thị"}
                        type="button"
                    >
                        <RotateCcw size={15} />
                        Khôi phục
                    </button>
                </div>
            )}
        </div>
    );
}
