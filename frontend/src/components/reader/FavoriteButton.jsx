import { Heart } from "lucide-react";
import { useEffect, useState } from "react";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../ToastProvider";

export default function FavoriteButton({
    maDauSach,
    initialFavorite = null,
    compact = false,
    onChanged
}) {
    const toast = useToast();

    const [favorite, setFavorite] = useState(Boolean(initialFavorite));
    const [loading, setLoading] = useState(false);

    async function loadFavoriteStatus() {
        if (!maDauSach || initialFavorite !== null) {
            return;
        }

        try {
            const result = await readerApi.isFavorite(maDauSach);
            setFavorite(Boolean(result?.exists));
        } catch (err) {
            console.error("Không kiểm tra được trạng thái yêu thích:", err);
        }
    }

    async function toggleFavorite(e) {
        e?.preventDefault?.();
        e?.stopPropagation?.();

        if (!maDauSach || loading) {
            return;
        }

        setLoading(true);

        try {
            if (favorite) {
                await readerApi.removeFavorite(maDauSach);
                setFavorite(false);
                toast.success("Đã xóa khỏi sách yêu thích");
                onChanged?.(false);
            } else {
                await readerApi.addFavorite(maDauSach);
                setFavorite(true);
                toast.success("Đã thêm vào sách yêu thích");
                onChanged?.(true);
            }
        } catch (err) {
            toast.error(err.message || "Không thể cập nhật sách yêu thích");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (initialFavorite !== null) {
            setFavorite(Boolean(initialFavorite));
            return;
        }

        loadFavoriteStatus();
    }, [maDauSach, initialFavorite]);

    return (
        <button
            type="button"
            className={`favorite-button ${favorite ? "is-active" : ""} ${compact ? "is-compact" : ""}`}
            onClick={toggleFavorite}
            disabled={loading}
            title={favorite ? "Xóa khỏi yêu thích" : "Thêm vào yêu thích"}
        >
            <Heart size={18} fill={favorite ? "currentColor" : "none"} />
            {!compact && (
                <span>
                    {loading
                        ? "Đang xử lý..."
                        : favorite
                          ? "Đã yêu thích"
                          : "Yêu thích"}
                </span>
            )}
        </button>
    );
}
