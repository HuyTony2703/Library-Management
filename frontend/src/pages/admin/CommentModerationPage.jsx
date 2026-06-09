import { useEffect, useState } from "react";
import { EyeOff, RefreshCcw, RotateCcw, Search, Trash2 } from "lucide-react";
import { adminApi } from "../../api/adminApi";
import PageHeader from "../../components/PageHeader";
import DataTable from "../../components/DataTable";
import StatusBadge from "../../components/StatusBadge";
import { useToast } from "../../components/ToastProvider";

export default function CommentModerationPage() {
    const toast = useToast();

    const [comments, setComments] = useState([]);
    const [selected, setSelected] = useState(null);
    const [action, setAction] = useState("hide");
    const [loading, setLoading] = useState(false);

    const [filter, setFilter] = useState({
        status: "Tất cả",
        maDauSach: "",
        keyword: ""
    });

    const [form, setForm] = useState({
        maNhanVienXuLy: "NV_ADMIN",
        lyDoAnXoa: ""
    });

    function updateFilter(field, value) {
        setFilter((prev) => ({
            ...prev,
            [field]: value
        }));
    }

    function updateForm(field, value) {
        setForm((prev) => ({
            ...prev,
            [field]: value
        }));
    }

    async function loadComments() {
        setLoading(true);

        try {
            const data = await adminApi.getComments(filter);
            setComments(Array.isArray(data) ? data : []);
            toast.success("Đã tải danh sách bình luận");
        } catch (err) {
            toast.error(err.message || "Không tải được bình luận");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadComments();
    }, []);

    function openAction(row, nextAction) {
        setSelected(row);
        setAction(nextAction);

        if (nextAction === "hide") {
            updateForm("lyDoAnXoa", "Bình luận không phù hợp");
        } else if (nextAction === "delete") {
            updateForm("lyDoAnXoa", "Nội dung vi phạm quy định thư viện");
        } else {
            updateForm("lyDoAnXoa", "");
        }
    }

    function updateAction(nextAction) {
        setAction(nextAction);

        if (nextAction === "hide") {
            updateForm("lyDoAnXoa", "Bình luận không phù hợp");
        } else if (nextAction === "delete") {
            updateForm("lyDoAnXoa", "Nội dung vi phạm quy định thư viện");
        } else {
            updateForm("lyDoAnXoa", "");
        }
    }

    async function submitModeration(event) {
        event.preventDefault();

        if (!selected) {
            toast.error("Vui lòng chọn bình luận cần xử lý");
            return;
        }

        if (!form.maNhanVienXuLy.trim()) {
            toast.error("Vui lòng nhập mã nhân viên xử lý");
            return;
        }

        if ((action === "hide" || action === "delete") && !form.lyDoAnXoa.trim()) {
            toast.error("Vui lòng nhập lý do xử lý");
            return;
        }

        setLoading(true);

        try {
            const payload = {
                maNhanVienXuLy: form.maNhanVienXuLy.trim(),
                lyDoAnXoa: form.lyDoAnXoa
            };

            let data;

            if (action === "hide") {
                data = await adminApi.hideComment(selected.maBinhLuan, payload);
            } else if (action === "delete") {
                data = await adminApi.deleteComment(selected.maBinhLuan, payload);
            } else {
                data = await adminApi.restoreComment(selected.maBinhLuan, payload);
            }

            setSelected(data);
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
                eyebrow="Admin"
                title="Kiểm duyệt bình luận"
                description="Ẩn, xóa mềm hoặc khôi phục bình luận của độc giả trên đầu sách."
                right={
                    <button className="soft-button" onClick={loadComments} disabled={loading}>
                        <RefreshCcw size={17} />
                        Tải lại
                    </button>
                }
            />

            <div className="panel compact-form">
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

                <button className="primary-button" onClick={loadComments} disabled={loading}>
                    <Search size={17} />
                    Tìm kiếm
                </button>
            </div>

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
                                <div className="table-actions">
                                    <button
                                        className="soft-button"
                                        onClick={() => openAction(row, "hide")}
                                        disabled={row.trangThai === "Đã ẩn"}
                                        type="button"
                                    >
                                        <EyeOff size={15} />
                                        Ẩn
                                    </button>

                                    <button
                                        className="soft-button"
                                        onClick={() => openAction(row, "delete")}
                                        disabled={row.trangThai === "Đã xóa"}
                                        type="button"
                                    >
                                        <Trash2 size={15} />
                                        Xóa
                                    </button>

                                    <button
                                        className="soft-button"
                                        onClick={() => openAction(row, "restore")}
                                        disabled={row.trangThai === "Hiển thị"}
                                        type="button"
                                    >
                                        <RotateCcw size={15} />
                                        Khôi phục
                                    </button>
                                </div>
                            )
                        }
                    ]}
                />
            </div>

            <div className="form-layout">
                <form className="panel form-panel" onSubmit={submitModeration}>
                    <div className="panel-title">
                        <h2>Xử lý bình luận</h2>
                        {selected && <StatusBadge value={selected.trangThai} />}
                    </div>

                    {selected ? (
                        <>
                            <div className="form-grid-2">
                                <div className="form-row">
                                    <label>Mã bình luận</label>
                                    <input value={selected.maBinhLuan || ""} disabled />
                                </div>

                                <div className="form-row">
                                    <label>Hành động</label>
                                    <select
                                        value={action}
                                        onChange={(event) => updateAction(event.target.value)}
                                    >
                                        <option value="hide">Ẩn bình luận</option>
                                        <option value="delete">Xóa bình luận</option>
                                        <option value="restore">Khôi phục bình luận</option>
                                    </select>
                                </div>
                            </div>

                            <div className="form-row">
                                <label>Nội dung bình luận</label>
                                <textarea value={selected.noiDung || ""} disabled />
                            </div>

                            <div className="form-grid-2">
                                <div className="form-row">
                                    <label>Mã nhân viên xử lý</label>
                                    <input
                                        value={form.maNhanVienXuLy}
                                        onChange={(event) => updateForm("maNhanVienXuLy", event.target.value)}
                                    />
                                </div>

                                <div className="form-row">
                                    <label>Lý do xử lý</label>
                                    <input
                                        value={form.lyDoAnXoa}
                                        onChange={(event) => updateForm("lyDoAnXoa", event.target.value)}
                                        disabled={action === "restore"}
                                    />
                                </div>
                            </div>

                            <button className="primary-button" disabled={loading}>
                                Xác nhận xử lý
                            </button>
                        </>
                    ) : (
                        <p>Chọn một bình luận trong bảng để xử lý.</p>
                    )}
                </form>

                <div className="panel preview-panel">
                    <h2>Chi tiết bình luận</h2>
                    <pre>{selected ? JSON.stringify(selected, null, 2) : "Chưa chọn bình luận"}</pre>
                </div>
            </div>
        </div>
    );
}
