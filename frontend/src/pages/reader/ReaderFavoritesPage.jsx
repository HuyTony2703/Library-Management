import { Heart, RefreshCcw } from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../../components/ToastProvider";
import FavoriteButton from "../../components/reader/FavoriteButton";

export default function ReaderFavoritesPage() {
    const toast = useToast();
    const navigate = useNavigate();

    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);

    async function loadFavorites() {
        setLoading(true);

        try {
            const result = await readerApi.getFavorites();
            setData(Array.isArray(result) ? result : []);
        } catch (err) {
            toast.error(err.message || "Không tải được sách yêu thích");
        } finally {
            setLoading(false);
        }
    }

    function handleFavoriteChanged(maDauSach, isFavorite) {
        if (!isFavorite) {
            setData((prev) => prev.filter((item) => item.maDauSach !== maDauSach));
        }
    }

    useEffect(() => {
        loadFavorites();
    }, []);

    return (
        <div>
            <div className="reader-home-header">
                <small>Favorites</small>
                <h1>Sách yêu thích</h1>
                <p>Danh sách các đầu sách bạn đã lưu để xem lại nhanh.</p>
            </div>

            <div className="reader-page-actions">
                <button type="button" onClick={loadFavorites}>
                    <RefreshCcw size={17} />
                    {loading ? "Đang tải..." : "Tải lại"}
                </button>
            </div>

            {data.length === 0 ? (
                <div className="reader-empty-box favorite-empty">
                    <Heart size={24} />
                    <p>Bạn chưa thêm sách nào vào yêu thích.</p>
                </div>
            ) : (
                <div className="favorite-grid">
                    {data.map((book) => (
                        <article
                            className="favorite-card"
                            key={book.maYeuThich}
                            onClick={() => navigate(`/reader/books/${book.maDauSach}`)}
                        >
                            <div className="favorite-cover">
                                {book.anhBia ? (
                                    <img src={book.anhBia} alt={book.tenDauSach} />
                                ) : (
                                    <span>{book.tenDauSach?.charAt(0) || "S"}</span>
                                )}
                            </div>

                            <div className="favorite-info">
                                <div className="favorite-title-row">
                                    <h3>{book.tenDauSach}</h3>

                                    <FavoriteButton
                                        maDauSach={book.maDauSach}
                                        initialFavorite={true}
                                        compact
                                        onChanged={(isFavorite) =>
                                            handleFavoriteChanged(book.maDauSach, isFavorite)
                                        }
                                    />
                                </div>

                                <p className="reader-muted">{book.maDauSach}</p>

                                <div className="favorite-meta">
                                    <span>Năm XB: {book.namXuatBan || "Không rõ"}</span>
                                    <span>Sẵn có: {book.soCuonSanCo ?? 0}</span>
                                </div>

                                <div className="favorite-price">
                                    {formatCurrency(book.triGia)}
                                </div>

                                <p className="reader-muted">
                                    Đã thêm: {formatDateTime(book.ngayThem)}
                                </p>
                            </div>
                        </article>
                    ))}
                </div>
            )}
        </div>
    );
}

function formatCurrency(value) {
    const number = Number(value || 0);

    return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND"
    }).format(number);
}

function formatDateTime(value) {
    if (!value) {
        return "";
    }

    return new Date(value).toLocaleString("vi-VN");
}
