export function isReaderUser(user) {
    if (!user) {
        return false;
    }

    return user.maVaiTro === "VT_DOC_GIA" ||
        user.tenVaiTro === "DOC_GIA" ||
        user.tenVaiTro === "Độc giả" ||
        Boolean(user.maDocGia);
}
