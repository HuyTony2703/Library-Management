package com.library.backend.dto;

public class ModerateCommentRequest {

    private String maNhanVienXuLy;
    private String lyDoAnXoa;

    public String getMaNhanVienXuLy() {
        return maNhanVienXuLy;
    }

    public void setMaNhanVienXuLy(String maNhanVienXuLy) {
        this.maNhanVienXuLy = maNhanVienXuLy;
    }

    public String getLyDoAnXoa() {
        return lyDoAnXoa;
    }

    public void setLyDoAnXoa(String lyDoAnXoa) {
        this.lyDoAnXoa = lyDoAnXoa;
    }
}
