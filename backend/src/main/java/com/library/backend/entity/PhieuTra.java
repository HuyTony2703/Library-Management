package com.library.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "PHIEUTRA")
public class PhieuTra {

    @Id
    @Column(name = "MaPhieuTra", length = 30)
    private String maPhieuTra;

    @Column(name = "MaDocGia", nullable = false, length = 30)
    private String maDocGia;

    @Column(name = "MaNhanVienNhan", nullable = false, length = 30)
    private String maNhanVienNhan;

    @Column(name = "MaChiNhanh", nullable = false, length = 30)
    private String maChiNhanh;

    @Column(name = "NgayTra", nullable = false)
    private LocalDateTime ngayTra;

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    @Column(name = "IdempotencyKey", length = 100)
    private String idempotencyKey;

    @Column(name = "RequestFingerprint", length = 64)
    private String requestFingerprint;

    public String getMaPhieuTra() { return maPhieuTra; }
    public void setMaPhieuTra(String maPhieuTra) { this.maPhieuTra = maPhieuTra; }

    public String getMaDocGia() { return maDocGia; }
    public void setMaDocGia(String maDocGia) { this.maDocGia = maDocGia; }

    public String getMaNhanVienNhan() { return maNhanVienNhan; }
    public void setMaNhanVienNhan(String maNhanVienNhan) { this.maNhanVienNhan = maNhanVienNhan; }

    public String getMaChiNhanh() { return maChiNhanh; }
    public void setMaChiNhanh(String maChiNhanh) { this.maChiNhanh = maChiNhanh; }

    public LocalDateTime getNgayTra() { return ngayTra; }
    public void setNgayTra(LocalDateTime ngayTra) { this.ngayTra = ngayTra; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getRequestFingerprint() { return requestFingerprint; }
    public void setRequestFingerprint(String requestFingerprint) { this.requestFingerprint = requestFingerprint; }
}
