package com.library.backend.security;

public final class RoleConstants {

    public static final String ADMIN = "QUAN_TRI_VIEN";
    public static final String LIBRARIAN = "THU_THU";
    public static final String READER = "DOC_GIA";

    public static final String ROLE_PREFIX = "ROLE_";
    public static final String UNKNOWN_AUTHORITY = "ROLE_UNKNOWN";

    private RoleConstants() {
    }

    public static String toAuthority(String tenVaiTro) {
        if (tenVaiTro == null || tenVaiTro.isBlank()) {
            return UNKNOWN_AUTHORITY;
        }

        return ROLE_PREFIX + tenVaiTro.trim();
    }
}
