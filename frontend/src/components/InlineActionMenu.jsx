import { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { MoreHorizontal } from "lucide-react";

export default function InlineActionMenu({ label, actions, disabled = false }) {
    const [menuStyle, setMenuStyle] = useState(null);
    const open = Boolean(menuStyle);

    useEffect(() => {
        if (!open) {
            return undefined;
        }

        function closeOnOutsideClick(event) {
            if (event.target?.closest?.(".inline-action-menu, .compact-icon-button")) {
                return;
            }

            setMenuStyle(null);
        }

        function close() {
            setMenuStyle(null);
        }

        window.addEventListener("click", closeOnOutsideClick);
        window.addEventListener("resize", close);
        window.addEventListener("scroll", close, true);

        return () => {
            window.removeEventListener("click", closeOnOutsideClick);
            window.removeEventListener("resize", close);
            window.removeEventListener("scroll", close, true);
        };
    }, [open]);

    function toggle(event) {
        event.stopPropagation();

        if (disabled) {
            return;
        }

        if (open) {
            setMenuStyle(null);
            return;
        }

        const rect = event.currentTarget.getBoundingClientRect();
        const menuWidth = 210;
        const menuHeight = Math.max(174, actions.length * 48 + 24);
        const left = Math.min(Math.max(12, rect.right - menuWidth), window.innerWidth - menuWidth - 12);
        const topBelow = rect.bottom + 8;
        const top = topBelow + menuHeight > window.innerHeight - 12
            ? Math.max(12, rect.top - menuHeight - 8)
            : topBelow;

        setMenuStyle({ top, left });
    }

    function runAction(action) {
        setMenuStyle(null);
        action.onClick?.();
    }

    return (
        <div className="action-menu-cell">
            <button
                className="icon-button compact-icon-button"
                type="button"
                onClick={toggle}
                aria-label={label}
                disabled={disabled}
            >
                <MoreHorizontal size={19} />
            </button>

            {open && createPortal(
                <div className="inline-action-menu" style={menuStyle}>
                    {actions.map((action) => {
                        const Icon = action.icon;

                        return (
                            <button
                                key={action.key}
                                className={action.danger ? "soft-button danger-button" : "soft-button"}
                                onClick={() => runAction(action)}
                                disabled={action.disabled}
                                type="button"
                            >
                                {Icon && <Icon size={15} />}
                                {action.label}
                            </button>
                        );
                    })}
                </div>,
                document.body
            )}
        </div>
    );
}
