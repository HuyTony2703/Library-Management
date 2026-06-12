const STATUS_LABELS = {
    TT_SANCO: "Sẵn có",
    TT_DANGMUON: "Đang mượn",
    TT_DANGDATTRUOC: "Đang đặt trước",
    TT_MAT: "Bị mất",
    TT_HONG: "Bị hỏng",
    TT_NGUNGLUUTHONG: "Ngừng lưu thông",
    GOI_THUONG: "Gói thường",
    GOI_VIP: "Gói VIP",
    NHOM_SINHVIEN: "Sinh viên",
    NHOM_HOCSINH: "Học sinh",
    NHOM_GIAOVIEN: "Giáo viên",
    NHOM_KHAC: "Khác",
    TL_MANGA: "Manga",
    PT_TIEN_MAT: "Tiền mặt",
    NO_TRA_TRE: "Nợ trả trễ",
    NO_HONG_SACH: "Nợ hỏng sách",
    NO_MAT_SACH: "Nợ mất sách",
    "Hoat dong": "Hoạt động",
    "Hoạt động": "Hoạt động",
    "Ngung hoat dong": "Ngừng hoạt động",
    "Ngừng hoạt động": "Ngừng hoạt động",
    "Ngung hien thi": "Ngừng hiển thị",
    "Ngừng hiển thị": "Ngừng hiển thị",
    "Sẵn có": "Sẵn có",
    "Đang mượn": "Đang mượn",
    "Đang đặt trước": "Đang đặt trước",
    "Bị mất": "Bị mất",
    "Bị hỏng": "Bị hỏng",
    "Ngừng lưu thông": "Ngừng lưu thông",
    "Chưa thanh toán": "Chưa thanh toán",
    "Thanh toán một phần": "Thanh toán một phần",
    "Đã thanh toán": "Đã thanh toán",
    "Thành công": "Thành công",
    "Hiển thị": "Hiển thị",
    "Đã ẩn": "Đã ẩn",
    "Đã xóa": "Đã xóa"
};

const CODE_PREFIX_LABELS = [
    [/^QDM_.*MANGA/i, "Quy định mượn Manga"],
    [/^QDM_/i, "Quy định mượn"],
    [/^QDG_/i, "Quy định gói"],
    [/^GG_/i, "Giá gói"],
    [/^TS_/i, "Tham số"],
    [/^PT_/, "Phiếu thu"],
    [/^PM_/, "Phiếu mượn"],
    [/^PTR_/, "Phiếu trả"]
];

export function displayStatus(value) {
    if (!value) {
        return "Không rõ";
    }

    return STATUS_LABELS[value] || value;
}

export function displayCode(value) {
    if (!value) {
        return "";
    }

    if (STATUS_LABELS[value]) {
        return STATUS_LABELS[value];
    }

    const match = CODE_PREFIX_LABELS.find(([pattern]) => pattern.test(value));
    return match ? `${match[1]} (${value})` : value;
}

export function formatMoney(value) {
    return `${Number(value || 0).toLocaleString("vi-VN")}đ`;
}

export function formatDateTime(value) {
    if (!value) {
        return "";
    }

    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString("vi-VN");
}

export function formatDate(value) {
    if (!value) {
        return "";
    }

    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString("vi-VN");
}
