import { Eye, EyeOff } from "lucide-react";
import { useState } from "react";

export default function PasswordInput({ className = "", ...props }) {
    const [visible, setVisible] = useState(false);

    return (
        <div className={`password-input-wrap ${className}`.trim()}>
            <input
                {...props}
                type={visible ? "text" : "password"}
            />
            <button
                type="button"
                className="password-toggle"
                onClick={() => setVisible((current) => !current)}
                aria-label={visible ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
            >
                {visible ? <EyeOff size={17} /> : <Eye size={17} />}
            </button>
        </div>
    );
}
