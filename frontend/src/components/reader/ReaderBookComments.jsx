import { MessageCircle, Pencil, Trash2 } from "lucide-react";
import { useEffect, useState } from "react";
import { readerApi } from "../../api/readerApi";
import { useActionDialog } from "../ActionDialogProvider";
import { useToast } from "../ToastProvider";
import CommentForm from "./CommentForm";

export default function ReaderBookComments({ maDauSach, embedded = false }) {
    const toast = useToast();
    const actionDialog = useActionDialog();

    const [comments, setComments] = useState([]);
    const [editingId, setEditingId] = useState(null);
    const [loading, setLoading] = useState(false);

    async function loadComments() {
        setLoading(true);

        try {
            const result = await readerApi.getBookComments(maDauSach);
            setComments(Array.isArray(result) ? result : []);
        } catch (err) {
            toast.error(err.message || "Không tải được bình luận");
        } finally {
            setLoading(false);
        }
    }

    async function handleCreate(noiDung) {
        try {
            await readerApi.createBookComment(maDauSach, { noiDung });
            toast.success("Đã gửi bình luận");
            await loadComments();
        } catch (err) {
            toast.error(err.message || "Gửi bình luận thất bại");
        }
    }

    async function handleUpdate(maBinhLuan, noiDung) {
        try {
            await readerApi.updateComment(maBinhLuan, { noiDung });
            toast.success("Đã cập nhật bình luận");
            setEditingId(null);
            await loadComments();
        } catch (err) {
            toast.error(err.message || "Cập nhật bình luận thất bại");
        }
    }

    async function handleDelete(maBinhLuan) {
        const ok = await actionDialog.confirm({
            title: "Xóa bình luận",
            message: "Bạn có chắc muốn xóa bình luận này không?",
            confirmLabel: "Xóa",
            danger: true
        });

        if (!ok) {
            return;
        }

        try {
            await readerApi.deleteComment(maBinhLuan);
            toast.success("Đã xóa bình luận");
            await loadComments();
        } catch (err) {
            toast.error(err.message || "Xóa bình luận thất bại");
        }
    }

    useEffect(() => {
        if (maDauSach) {
            loadComments();
        }
    }, [maDauSach]);

    const Container = embedded ? "div" : "section";

    return (
        <Container className={embedded ? "feedback-block" : "reader-section"}>
            {!embedded ? (
                <div className="section-title-row">
                    <div>
                        <p className="reader-eyebrow">Comments</p>
                        <h2>Bình luận</h2>
                    </div>

                    <span className="reader-muted">
                        {loading ? "Đang tải..." : `${comments.length} bình luận`}
                    </span>
                </div>
            ) : (
                <span className="reader-muted">
                    {loading ? "Đang tải bình luận..." : `${comments.length} bình luận`}
                </span>
            )}

            <CommentForm onSubmit={handleCreate} />

            <div className="comment-list">
                {comments.length === 0 ? (
                    <div className="reader-empty-box">
                        Chưa có bình luận nào cho đầu sách này.
                    </div>
                ) : (
                    comments.map((comment) => (
                        <div className="comment-item" key={comment.maBinhLuan}>
                            <div className="comment-avatar">
                                <MessageCircle size={18} />
                            </div>

                            <div className="comment-body">
                                <div className="comment-header">
                                    <div>
                                        <b>{comment.hoTenDocGia}</b>
                                        <span>{formatDateTime(comment.ngayBinhLuan)}</span>
                                    </div>

                                    {comment.cuaToi && editingId !== comment.maBinhLuan && (
                                        <div className="comment-actions">
                                            <button
                                                type="button"
                                                className="ghost-icon-button"
                                                onClick={() => setEditingId(comment.maBinhLuan)}
                                                title="Sửa"
                                            >
                                                <Pencil size={16} />
                                            </button>

                                            <button
                                                type="button"
                                                className="ghost-icon-button danger"
                                                onClick={() => handleDelete(comment.maBinhLuan)}
                                                title="Xóa"
                                            >
                                                <Trash2 size={16} />
                                            </button>
                                        </div>
                                    )}
                                </div>

                                {editingId === comment.maBinhLuan ? (
                                    <CommentForm
                                        initialValue={comment.noiDung}
                                        submitLabel="Lưu thay đổi"
                                        onSubmit={(noiDung) => handleUpdate(comment.maBinhLuan, noiDung)}
                                        onCancel={() => setEditingId(null)}
                                    />
                                ) : (
                                    <p>{comment.noiDung}</p>
                                )}
                            </div>
                        </div>
                    ))
                )}
            </div>
        </Container>
    );
}

function formatDateTime(value) {
    if (!value) {
        return "";
    }

    return new Date(value).toLocaleString("vi-VN");
}
