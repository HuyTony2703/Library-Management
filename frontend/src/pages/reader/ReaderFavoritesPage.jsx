import { Heart, Search } from "lucide-react";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../../components/ToastProvider";
import ReaderBookCard from "../../components/reader/ReaderBookCard";

export default function ReaderFavoritesPage() {
    const toast = useToast();

    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);

    async function loadFavorites() {
        setLoading(true);

        try {
            const result = await readerApi.getFavorites();
            setData(Array.isArray(result) ? result : []);
        } catch (err) {
            toast.error(err.message || "Không tải được sách yêu thích. Vui lòng thử lại.");
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
                <small>SÁCH YÊU THÍCH</small>
                <h1>Sách yêu thích</h1>
                <p>Những đầu sách bạn đã lưu để xem lại nhanh.</p>
            </div>

            {loading && <p>Đang tải sách yêu thích...</p>}

            {!loading && data.length === 0 ? (
                <div className="reader-empty-box favorite-empty">
                    <Heart size={24} />
                    <div>
                        <p>Bạn chưa có sách yêu thích.</p>
                        <span>Nhấn biểu tượng trái tim trên trang Tra cứu sách để lưu sách bạn quan tâm.</span>
                    </div>
                    <Link className="reader-primary-button" to="/reader/books">
                        <Search size={17} />
                        Tra cứu sách
                    </Link>
                </div>
            ) : null}

            {!loading && data.length > 0 && (
                <div className="reader-book-grid">
                    {data.map((book) => (
                        <ReaderBookCard
                            key={book.maYeuThich || book.maDauSach}
                            book={book}
                            initialFavorite={true}
                            onFavoriteChanged={(isFavorite) =>
                                handleFavoriteChanged(book.maDauSach, isFavorite)
                            }
                        />
                    ))}
                </div>
            )}
        </div>
    );
}
