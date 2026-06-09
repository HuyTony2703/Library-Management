import { CalendarPlus } from "lucide-react";
import { useState } from "react";
import ReservationModal from "./ReservationModal";

export default function ReservationButton({
    maDauSach,
    maCuonSach = null,
    maChiNhanh = "CN_TD",
    label = "Đặt trước",
    onSuccess
}) {
    const [open, setOpen] = useState(false);

    return (
        <>
            <button type="button" className="reader-soft-button" onClick={() => setOpen(true)}>
                <CalendarPlus size={16} />
                {label}
            </button>

            {open && (
                <ReservationModal
                    maDauSach={maDauSach}
                    maCuonSach={maCuonSach}
                    maChiNhanh={maChiNhanh}
                    onClose={() => setOpen(false)}
                    onSuccess={(data) => {
                        setOpen(false);
                        onSuccess?.(data);
                    }}
                />
            )}
        </>
    );
}

