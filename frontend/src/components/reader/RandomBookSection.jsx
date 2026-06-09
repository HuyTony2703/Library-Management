import { RefreshCcw, Sparkles } from "lucide-react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../ToastProvider";
import FavoriteButton from "./FavoriteButton";

export default function RandomBookSection({ limit = 6 }) {
    const toast = useToast();
    const navigate = useNavigate();

    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(false);

    async function loadRandomBooks() {
        setLoading(true);

        try {
            const result = await readerApi.getRandomRecommendations(limit);
            setBooks(Array.isArray(result) ? result : []);
        } catch (err) {
            toast.error(err.message || "Không tải được sách gợi ý");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadRandomBooks();
    }, [limit]);

    return (
        <section className="random-book-section">
            <div className="section-title-row">
                <div>
                    <p className="reader-eyebrow">Recommendations</p>
                    <h2>Gợi ý ngẫu nhiên cho bạn</h2>
                </div>

                <button type="button" className="reader-soft-button" onClick={loadRandomBooks}>
                    <RefreshCcw size={17} />
                    {loading ? "Đang tải..." : "Làm mới"}
                </button>
            </div>

            {loading && books.length === 0 ? (
                <div className="reader-empty-box recommendation-empty">
                    <Sparkles size={24} />
                    <p>Đang tải sách gợi ý...</p>
                </div>
            ) : books.length === 0 ? (
                <div className="reader-empty-box recommendation-empty">
                    <Sparkles size={24} />
                    <p>Hiện chưa có sách phù hợp để gợi ý.</p>
                </div>
            ) : (
                <div className="recommendation-grid">
                    {books.map((book) => (
                        <article
                            key={book.maDauSach}
                            className="recommendation-card"
                            onClick={() => navigate(`/reader/books/${book.maDauSach}`)}
                        >
                            <div className="recommendation-cover">
                                {book.anhBia ? (
                                    <img src={book.anhBia} alt={book.tenDauSach} />
                                ) : (
                                    <span>{book.tenDauSach?.charAt(0) || "S"}</span>
                                )}
                            </div>

                            <div className="recommendation-content">
                                <div className="recommendation-title-row">
                                    <h3>{book.tenDauSach}</h3>

                                    <FavoriteButton
                                        maDauSach={book.maDauSach}
                                        initialFavorite={book.daYeuThich}
                                        compact
                                    />
                                </div>

                                <p className="reader-muted">{book.maDauSach}</p>

                                <div className="recommendation-meta">
                                    <span>Năm XB: {book.namXuatBan || "Không rõ"}</span>
                                    <span>Sẵn có: {book.soCuonSanCo ?? 0}</span>
                                </div>

                                <div className="recommendation-price">
                                    {formatCurrency(book.triGia)}
                                </div>

                                <button
                                    type="button"
                                    className="reader-primary-button"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        navigate(`/reader/books/${book.maDauSach}`);
                                    }}
                                >
                                    Xem chi tiết
                                </button>
                            </div>
                        </article>
                    ))}
                </div>
            )}
        </section>
    );
}

function formatCurrency(value) {
    const number = Number(value || 0);

    return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND"
    }).format(number);
}
