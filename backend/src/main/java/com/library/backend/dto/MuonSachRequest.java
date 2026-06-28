package com.library.backend.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public class MuonSachRequest {

    @Size(max = 30, message = "Ma phieu muon toi da 30 ky tu")
    private String maPhieuMuon;

    @Size(max = 30, message = "Ma doc gia toi da 30 ky tu")
    private String maDocGia;

    @Size(max = 30, message = "Ma doc gia toi da 30 ky tu")
    private String readerId;

    @Size(max = 30, message = "Ma nhan vien lap toi da 30 ky tu")
    private String maNhanVienLap;

    @Size(max = 30, message = "Ma chi nhanh toi da 30 ky tu")
    private String maChiNhanh;

    @Size(max = 30, message = "Ma chi nhanh toi da 30 ky tu")
    private String branchId;

    private List<String> maCuonSachs;
    private List<String> copyIds;

    private String ghiChu;
    private String note;

    public String getMaPhieuMuon() { return maPhieuMuon; }
    public void setMaPhieuMuon(String maPhieuMuon) { this.maPhieuMuon = maPhieuMuon; }

    public String getMaDocGia() { return maDocGia; }
    public void setMaDocGia(String maDocGia) { this.maDocGia = maDocGia; }

    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }

    public String getMaNhanVienLap() { return maNhanVienLap; }
    public void setMaNhanVienLap(String maNhanVienLap) { this.maNhanVienLap = maNhanVienLap; }

    public String getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(String maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }

    public List<String> getMaCuonSachs() { return maCuonSachs; }
    public void setMaCuonSachs(List<String> maCuonSachs) { this.maCuonSachs = maCuonSachs; }

    public List<String> getCopyIds() { return copyIds; }
    public void setCopyIds(List<String> copyIds) { this.copyIds = copyIds; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
