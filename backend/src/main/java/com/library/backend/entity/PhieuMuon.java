package com.library.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "PHIEUMUON")
public class PhieuMuon {

    @Id
    @Column(name = "MaPhieuMuon", length = 30)
    private String maPhieuMuon;

    @Column(name = "MaDocGia", nullable = false, length = 30)
    private String maDocGia;

    @Column(name = "MaNhanVienLap", nullable = false, length = 30)
    private String maNhanVienLap;

    @Column(name = "MaChiNhanh", nullable = false, length = 30)
    private String maChiNhanh;

    @Column(name = "MaPhienBanQuyDinh", nullable = false, length = 30)
    private String maPhienBanQuyDinh;

    @Column(name = "NgayMuon", nullable = false)
    private LocalDateTime ngayMuon;

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    @Column(name = "IdempotencyKey", length = 100)
    private String idempotencyKey;

    @Column(name = "RequestFingerprint", length = 64)
    private String requestFingerprint;

    public String getMaPhieuMuon() { return maPhieuMuon; }
    public void setMaPhieuMuon(String maPhieuMuon) { this.maPhieuMuon = maPhieuMuon; }

    public String getMaDocGia() { return maDocGia; }
    public void setMaDocGia(String maDocGia) { this.maDocGia = maDocGia; }

    public String getMaNhanVienLap() { return maNhanVienLap; }
    public void setMaNhanVienLap(String maNhanVienLap) { this.maNhanVienLap = maNhanVienLap; }

    public String getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(String maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public String getMaPhienBanQuyDinh() { return maPhienBanQuyDinh; }
    public void setMaPhienBanQuyDinh(String maPhienBanQuyDinh) { this.maPhienBanQuyDinh = maPhienBanQuyDinh; }

    public LocalDateTime getNgayMuon() { return ngayMuon; }
    public void setNgayMuon(LocalDateTime ngayMuon) { this.ngayMuon = ngayMuon; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getRequestFingerprint() { return requestFingerprint; }
    public void setRequestFingerprint(String requestFingerprint) { this.requestFingerprint = requestFingerprint; }
}
