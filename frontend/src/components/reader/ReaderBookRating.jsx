import { useEffect, useState } from "react";
import { readerApi } from "../../api/readerApi";
import { useToast } from "../ToastProvider";
import RatingStars from "./RatingStars";

export default function ReaderBookRating({ maDauSach }) {
    const toast = useToast();

    const [summary, setSummary] = useState(null);
    const [soSao, setSoSao] = useState(5);
    const [noiDung, setNoiDung] = useState("");
    const [loading, setLoading] = useState(false);

    async function loadSummary() {
        try {
            const result = await readerApi.getRatingSummary(maDauSach);
            setSummary(result);

            if (result?.soSaoCuaToi) {
                setSoSao(result.soSaoCuaToi);
                setNoiDung(result.noiDungCuaToi || "");
            }
        } catch (err) {
            toast.error(err.message || "Không tải được đánh giá");
        }
    }

    async function handleSubmit(e) {
        e.preventDefault();
        setLoading(true);

        try {
            const payload = { soSao, noiDung };
            const result = summary?.soSaoCuaToi
                ? await readerApi.updateMyRating(maDauSach, payload)
                : await readerApi.createRating(maDauSach, payload);

            toast.success(summary?.soSaoCuaToi ? "Đã cập nhật đánh giá" : "Đã gửi đánh giá");
            setSummary(result);
        } catch (err) {
            toast.error(err.message || "Gửi đánh giá thất bại");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (maDauSach) {
            loadSummary();
        }
    }, [maDauSach]);

    const average = Number(summary?.diemTrungBinh || 0);
    const total = summary?.tongSoDanhGia || 0;

    return (
        <section className="reader-section">
            <div className="section-title-row">
                <div>
                    <p className="reader-eyebrow">Rating</p>
                    <h2>Đánh giá đầu sách</h2>
                </div>
            </div>

            <div className="rating-summary-box">
                <div className="rating-score">
                    <strong>{average.toFixed(1)}</strong>
                    <RatingStars value={Math.round(average)} readonly />
                    <span>{total} lượt đánh giá</span>
                </div>

                <div className="rating-bars">
                    <RatingBar label="5 sao" value={summary?.soSao5 || 0} total={total} />
                    <RatingBar label="4 sao" value={summary?.soSao4 || 0} total={total} />
                    <RatingBar label="3 sao" value={summary?.soSao3 || 0} total={total} />
                    <RatingBar label="2 sao" value={summary?.soSao2 || 0} total={total} />
                    <RatingBar label="1 sao" value={summary?.soSao1 || 0} total={total} />
                </div>
            </div>

            <form className="rating-form" onSubmit={handleSubmit}>
                <label>Đánh giá của bạn</label>

                <RatingStars value={soSao} onChange={setSoSao} />

                <textarea
                    value={noiDung}
                    onChange={(e) => setNoiDung(e.target.value)}
                    placeholder="Viết cảm nhận ngắn về đầu sách này..."
                    maxLength={1000}
                />

                <button type="submit" className="reader-primary-button" disabled={loading}>
                    {loading
                        ? "Đang gửi..."
                        : summary?.soSaoCuaToi
                          ? "Cập nhật đánh giá"
                          : "Gửi đánh giá"}
                </button>
            </form>
        </section>
    );
}

function RatingBar({ label, value, total }) {
    const percent = total > 0 ? Math.round((value / total) * 100) : 0;

    return (
        <div className="rating-bar-row">
            <span>{label}</span>
            <div className="rating-bar-track">
                <div className="rating-bar-fill" style={{ width: `${percent}%` }} />
            </div>
            <b>{value}</b>
        </div>
    );
}
