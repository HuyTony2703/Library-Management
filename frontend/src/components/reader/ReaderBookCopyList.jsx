import ReservationButton from "./ReservationButton";

function getCopyStatusClass(maTrangThai) {
    if (maTrangThai === "TT_SANCO") {
        return "copy-status-good";
    }

    if (maTrangThai === "TT_DANGMUON" || maTrangThai === "TT_DANGDATTRUOC") {
        return "copy-status-warn";
    }

    if (maTrangThai === "TT_HONG" || maTrangThai === "TT_MAT") {
        return "copy-status-bad";
    }

    return "copy-status-neutral";
}

export default function ReaderBookCopyList({ maDauSach, copies = [], onReserved }) {
    if (!copies.length) {
        return (
            <div className="reader-empty-box">
                Chưa có cuốn sách vật lý nào thuộc đầu sách này.
            </div>
        );
    }

    return (
        <div className="reader-copy-list">
            {copies.map((copy) => (
                <div className="reader-copy-item" key={copy.maCuonSach}>
                    <div>
                        <b>{copy.maCuonSach}</b>
                        <p>
                            {copy.tenChiNhanh || copy.maChiNhanh} -{" "}
                            {copy.viTriHienThi || copy.maViTri}
                        </p>
                    </div>

                    <div className="reader-copy-actions">
                        <span className={`copy-status ${getCopyStatusClass(copy.maTrangThai)}`}>
                            {copy.tenTrangThai || copy.maTrangThai}
                        </span>

                        {copy.maTrangThai === "TT_SANCO" && (
                            <ReservationButton
                                maDauSach={maDauSach}
                                maCuonSach={copy.maCuonSach}
                                maChiNhanh={copy.maChiNhanh}
                                label="Đặt cuốn này"
                                onSuccess={onReserved}
                            />
                        )}
                    </div>
                </div>
            ))}
        </div>
    );
}
