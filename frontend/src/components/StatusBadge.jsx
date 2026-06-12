import { displayStatus } from "../utils/displayUtils";

export default function StatusBadge({ value }) {
    const text = displayStatus(value);
    const lower = text.toLowerCase();

    const type =
        lower.includes("sẵn") ||
        lower.includes("hoạt động") ||
        lower.includes("thành công") ||
        lower.includes("đã thanh toán") ||
        lower.includes("đã ẩn") ||
        lower.includes("đã xóa")
            ? "good"
            : lower.includes("mượn") ||
                lower.includes("đang") ||
                lower.includes("một phần") ||
                lower.includes("chưa thanh toán")
                ? "warn"
                : lower.includes("mất") ||
                    lower.includes("hỏng") ||
                    lower.includes("khóa") ||
                    lower.includes("ngừng")
                    ? "bad"
                    : "neutral";

    return <span className={`status-badge status-${type}`}>{text}</span>;
}
