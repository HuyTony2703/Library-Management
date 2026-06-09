import { Star } from "lucide-react";

export default function RatingStars({ value = 0, onChange, readonly = false }) {
    return (
        <div className="rating-stars">
            {[1, 2, 3, 4, 5].map((star) => {
                const active = star <= value;

                return (
                    <button
                        key={star}
                        type="button"
                        className={`star-button ${active ? "active" : ""}`}
                        disabled={readonly}
                        onClick={() => !readonly && onChange?.(star)}
                        title={`${star} sao`}
                    >
                        <Star size={22} fill={active ? "currentColor" : "none"} />
                    </button>
                );
            })}
        </div>
    );
}
