import { Heart } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
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
    const [prevInitial, setPrevInitial] = useState(initialFavorite);

    if (prevInitial !== initialFavorite) {
        setPrevInitial(initialFavorite);
        setFavorite(Boolean(initialFavorite));
    }

    const loadFavoriteStatus = useCallback(async () => {
        if (!maDauSach || initialFavorite !== null) {
            return;
        }

        try {
            const result = await readerApi.isFavorite(maDauSach);
            setFavorite(Boolean(result?.exists));
        } catch (err) {
            console.error("Không kiểm tra được trạng thái yêu thích:", err);
        }
    }, [maDauSach, initialFavorite]);

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
                toast.success("Đã bỏ khỏi sách yêu thích.");
                onChanged?.(false);
            } else {
                await readerApi.addFavorite(maDauSach);
                setFavorite(true);
                toast.success("Đã thêm vào sách yêu thích.");
                onChanged?.(true);
            }
        } catch (err) {
            toast.error(err.message || "Không thể cập nhật sách yêu thích. Vui lòng thử lại.");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (initialFavorite !== null) {
            return;
        }

        const run = async () => {
            await loadFavoriteStatus();
        };

        run();
    }, [maDauSach, initialFavorite, loadFavoriteStatus]);

    const title = loading
        ? "Đang cập nhật yêu thích"
        : favorite
          ? "Đã thêm vào yêu thích"
          : "Thêm vào yêu thích";

    return (
        <button
            type="button"
            className={`favorite-button ${favorite ? "is-active" : ""} ${compact ? "is-compact" : ""}`}
            onClick={toggleFavorite}
            disabled={loading}
            title={title}
            aria-label={title}
        >
            <Heart size={18} fill={favorite ? "currentColor" : "none"} />
            {!compact && (
                <span>
                    {loading
                        ? "Đang xử lý..."
                        : favorite
                          ? "Đã yêu thích"
                          : "Thêm yêu thích"}
                </span>
            )}
        </button>
    );
}
