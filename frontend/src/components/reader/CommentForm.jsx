import { useState } from "react";

export default function CommentForm({
    initialValue = "",
    submitLabel = "Gửi bình luận",
    onSubmit,
    onCancel
}) {
    const [noiDung, setNoiDung] = useState(initialValue);
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();

        if (!noiDung.trim()) {
            return;
        }

        setLoading(true);

        try {
            await onSubmit(noiDung.trim());
            setNoiDung("");
        } finally {
            setLoading(false);
        }
    }

    return (
        <form className="comment-form" onSubmit={handleSubmit}>
            <textarea
                value={noiDung}
                onChange={(e) => setNoiDung(e.target.value)}
                placeholder="Viết bình luận của bạn..."
                maxLength={1000}
            />

            <div className="comment-form-actions">
                {onCancel && (
                    <button type="button" className="reader-secondary-button" onClick={onCancel}>
                        Hủy
                    </button>
                )}

                <button
                    type="submit"
                    className="reader-primary-button"
                    disabled={loading || !noiDung.trim()}
                >
                    {loading ? "Đang gửi..." : submitLabel}
                </button>
            </div>
        </form>
    );
}
