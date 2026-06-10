import {
    BookOpen,
    ChevronLeft,
    ChevronRight,
    Heart,
    RefreshCcw,
    Sparkles,
    Star,
    TrendingUp
} from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../ToastProvider";
import FavoriteButton from "./FavoriteButton";

const recommendationTypes = [
    {
        type: "new",
        label: "Mới",
        title: "Sách mới đề xuất",
        description: "Các đầu sách vừa được bổ sung vào thư viện.",
        icon: Sparkles
    },
    {
        type: "trending",
        label: "Thịnh hành",
        title: "Đang được mượn nhiều",
        description: "Xếp theo lượt mượn trong 90 ngày gần đây.",
        icon: TrendingUp
    },
    {
        type: "favorite",
        label: "Yêu thích",
        title: "Được độc giả yêu thích",
        description: "Các đầu sách có nhiều lượt lưu vào danh sách yêu thích.",
        icon: Heart
    },
    {
        type: "top-rated",
        label: "Đánh giá cao",
        title: "Được đánh giá cao",
        description: "Ưu tiên điểm đánh giá và số lượt đánh giá.",
        icon: Star
    }
];

function getCacheKey(type, limit) {
    return `${type}:${limit}`;
}

export default function RandomBookSection({ limit = 12 }) {
    const toast = useToast();
    const navigate = useNavigate();
    const trackRef = useRef(null);

    const [activeType, setActiveType] = useState(recommendationTypes[0].type);
    const [booksByType, setBooksByType] = useState({});
    const [loadingKey, setLoadingKey] = useState("");

    const activeMeta = recommendationTypes.find((item) => item.type === activeType) ?? recommendationTypes[0];
    const activeCacheKey = getCacheKey(activeType, limit);
    const books = booksByType[activeCacheKey] ?? [];
    const loading = loadingKey === activeCacheKey;

    async function loadBooks(type = activeType, force = false) {
        const cacheKey = getCacheKey(type, limit);

        if (!force && booksByType[cacheKey]) {
            return;
        }

        setLoadingKey(cacheKey);

        try {
            const result = await readerApi.getRecommendations({ type, limit });
            setBooksByType((current) => ({
                ...current,
                [cacheKey]: Array.isArray(result) ? result : []
            }));
        } catch (err) {
            toast.error(err.message || "Không tải được sách đề xuất");
        } finally {
            setLoadingKey((current) => (current === cacheKey ? "" : current));
        }
    }

    function handleSelectType(type) {
        setActiveType(type);
        trackRef.current?.scrollTo({ left: 0, behavior: "smooth" });
    }

    function scrollBooks(direction) {
        const track = trackRef.current;
        if (!track) {
            return;
        }

        track.scrollBy({
            left: direction * Math.max(track.clientWidth - 48, 280),
            behavior: "smooth"
        });
    }

    useEffect(() => {
        loadBooks(activeType);
    }, [activeType, limit]);

    return (
        <section className="random-book-section">
            <div className="recommendation-header">
                <div>
                    <p className="reader-eyebrow">Khám phá sách</p>
                    <h2>{activeMeta.title}</h2>
                    <span>{activeMeta.description}</span>
                </div>

                <div className="recommendation-header-actions">
                    <div className="recommendation-tabs" aria-label="Nhóm sách đề xuất">
                        {recommendationTypes.map((item) => {
                            const Icon = item.icon;

                            return (
                                <button
                                    key={item.type}
                                    type="button"
                                    className={item.type === activeType ? "is-active" : ""}
                                    onClick={() => handleSelectType(item.type)}
                                >
                                    <Icon size={16} />
                                    <span>{item.label}</span>
                                </button>
                            );
                        })}
                    </div>

                    <button
                        type="button"
                        className="reader-icon-button"
                        onClick={() => loadBooks(activeType, true)}
                        title="Làm mới"
                        disabled={loading}
                    >
                        <RefreshCcw size={17} />
                    </button>
                </div>
            </div>

            {loading && books.length === 0 ? (
                <div className="reader-empty-box recommendation-empty">
                    <Sparkles size={24} />
                    <p>Đang tải sách đề xuất...</p>
                </div>
            ) : books.length === 0 ? (
                <div className="reader-empty-box recommendation-empty">
                    <BookOpen size={24} />
                    <p>Hiện chưa có sách phù hợp để hiển thị.</p>
                </div>
            ) : (
                <div className="recommendation-carousel">
                    <button
                        type="button"
                        className="reader-icon-button recommendation-arrow"
                        onClick={() => scrollBooks(-1)}
                        title="Sách trước"
                    >
                        <ChevronLeft size={19} />
                    </button>

                    <div className="recommendation-track" ref={trackRef}>
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
                                        <BookOpen size={32} />
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

                                    <p className="recommendation-highlight">
                                        {book.thongTinNoiBat || "Sách đang hoạt động"}
                                    </p>

                                    <div className="recommendation-meta">
                                        <span>{book.namXuatBan || "Chưa rõ năm"}</span>
                                        <span>{book.soCuonSanCo ?? 0} cuốn sẵn có</span>
                                    </div>
                                </div>
                            </article>
                        ))}
                    </div>

                    <button
                        type="button"
                        className="reader-icon-button recommendation-arrow"
                        onClick={() => scrollBooks(1)}
                        title="Sách tiếp theo"
                    >
                        <ChevronRight size={19} />
                    </button>
                </div>
            )}
        </section>
    );
}
