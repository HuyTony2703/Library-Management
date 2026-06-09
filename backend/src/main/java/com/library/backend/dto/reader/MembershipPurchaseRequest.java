package com.library.backend.dto.reader;

public class MembershipPurchaseRequest {

    private String maGoiThanhVien;
    private String maPhuongThuc;
    private String ghiChu;

    public String getMaGoiThanhVien() {
        return maGoiThanhVien;
    }

    public void setMaGoiThanhVien(String maGoiThanhVien) {
        this.maGoiThanhVien = maGoiThanhVien;
    }

    public String getMaPhuongThuc() {
        return maPhuongThuc;
    }

    public void setMaPhuongThuc(String maPhuongThuc) {
        this.maPhuongThuc = maPhuongThuc;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
