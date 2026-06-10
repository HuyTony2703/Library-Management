import { X } from "lucide-react";

export default function ResultModal({ title, children, onClose, className = "" }) {
    return (
        <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
            <div
                className={`modal-card result-modal-card ${className}`.trim()}
                role="dialog"
                aria-modal="true"
                aria-label={title}
                onMouseDown={(event) => event.stopPropagation()}
            >
                <div className="modal-title-row">
                    <h2>{title}</h2>
                    <button type="button" className="icon-button" onClick={onClose} title="Đóng">
                        <X size={18} />
                    </button>
                </div>

                {children}
            </div>
        </div>
    );
}
