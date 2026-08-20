import { Download, Loader2 } from "lucide-react";
import { useState } from "react";

export default function ExportMenu({ items, onExport, loading = false }) {
    const [open, setOpen] = useState(false);

    return (
        <details
            className="export-menu"
            open={open}
            onToggle={(event) => setOpen(event.currentTarget.open)}
        >
            <summary className="soft-button">
                {loading ? <Loader2 size={15} className="export-menu-spinner" /> : <Download size={15} />}
                Export
            </summary>
            <div className="export-menu-list">
                {items.map((item) => (
                    <button
                        key={item.key}
                        type="button"
                        disabled={item.disabled || loading}
                        onClick={() => {
                            setOpen(false);
                            onExport(item.scope);
                        }}
                    >
                        <span>{item.label}</span>
                        {item.count != null && (
                            <span className="export-menu-count">{Number(item.count).toLocaleString("vi-VN")}</span>
                        )}
                    </button>
                ))}
            </div>
        </details>
    );
}