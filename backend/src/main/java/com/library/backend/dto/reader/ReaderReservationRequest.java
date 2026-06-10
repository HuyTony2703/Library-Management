package com.library.backend.dto.reader;

public class ReaderReservationRequest {

    private String maDauSach;
    private String maCuonSach;
    private String maChiNhanh;
    private String ghiChu;

    public String getMaDauSach() { return maDauSach; }
    public void setMaDauSach(String maDauSach) { this.maDauSach = maDauSach; }

    public String getMaCuonSach() { return maCuonSach; }
    public void setMaCuonSach(String maCuonSach) { this.maCuonSach = maCuonSach; }

    public String getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(String maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}

