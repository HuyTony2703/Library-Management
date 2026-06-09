export function normalizeRole(user) {
    const rawRole =
        user?.tenVaiTro ||
        user?.role ||
        user?.vaiTro ||
        user?.maVaiTro ||
        "";

    const role = String(rawRole).toUpperCase();

    if (
        role === "QUAN_TRI_VIEN" ||
        role === "ADMIN" ||
        role === "VT_ADMIN" ||
        role.includes("ADMIN") ||
        role.includes("QUAN_TRI")
    ) {
        return "ADMIN";
    }

    if (
        role === "THU_THU" ||
        role === "LIBRARIAN" ||
        role === "STAFF" ||
        role === "VT_THU_THU"
    ) {
        return "STAFF";
    }

    if (
        role === "DOC_GIA" ||
        role === "READER" ||
        role === "VT_DOC_GIA"
    ) {
        return "READER";
    }

    return "UNKNOWN";
}

export function isAdmin(user) {
    return normalizeRole(user) === "ADMIN";
}

export function isStaff(user) {
    return normalizeRole(user) === "STAFF";
}

export function isReader(user) {
    return normalizeRole(user) === "READER";
}
