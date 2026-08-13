import { useMemo } from "react";

const PALETTES = [
    { from: "#6366f1", to: "#8b5cf6" },
    { from: "#0ea5e9", to: "#14b8a6" },
    { from: "#f59e0b", to: "#ef4444" },
    { from: "#ec4899", to: "#8b5cf6" },
    { from: "#10b981", to: "#0ea5e9" },
    { from: "#f97316", to: "#eab308" },
    { from: "#3b82f6", to: "#06b6d4" },
    { from: "#ef4444", to: "#f59e0b" }
];

function hashCode(str) {
    let h = 0;
    for (let i = 0; i < str.length; i += 1) {
        h = (Math.imul(h, 31) + str.charCodeAt(i)) | 0;
    }
    return Math.abs(h);
}

export default function BookCover({ title = "", author = "", src = "", className = "", style, ...rest }) {
    const palette = useMemo(
        () => PALETTES[hashCode(title || "Sach") % PALETTES.length],
        [title]
    );

    if (src) {
        return <img src={src} alt={title} className={className} style={style} {...rest} />;
    }

    const initials = String(title || "S")
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((word) => word[0])
        .join("");

    return (
        <figure
            className={`book-cover-generated ${className}`.trim()}
            style={{ background: `linear-gradient(135deg, ${palette.from} 0%, ${palette.to} 100%)`, ...style }}
            {...rest}
        >
            <span className="book-cover-generated-badge">LibraDesk</span>
            <span className="book-cover-generated-initials">{initials}</span>
            <span className="book-cover-generated-title">{title}</span>
            {author ? <span className="book-cover-generated-author">{author}</span> : null}
        </figure>
    );
}