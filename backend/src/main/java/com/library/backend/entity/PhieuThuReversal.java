package com.library.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PHIEUTHU_REVERSAL")
public class PhieuThuReversal {

    @Id
    @Column(name = "MaDaoPhieuThu", length = 30)
    private String maDaoPhieuThu;

    @Column(name = "MaPhieuThuGoc", nullable = false, length = 30)
    private String maPhieuThuGoc;

    @Column(name = "MaDocGia", nullable = false, length = 30)
    private String maDocGia;

    @Column(name = "MaNhanVienDao", nullable = false, length = 30)
    private String maNhanVienDao;

    @Column(name = "SoTienHoan", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienHoan;

    @Column(name = "LyDo", nullable = false, length = 255)
    private String lyDo;

    @Column(name = "ApprovalReference", nullable = false, length = 100)
    private String approvalReference;

    @Column(name = "NgayDao", nullable = false)
    private LocalDateTime ngayDao;

    public String getMaDaoPhieuThu() { return maDaoPhieuThu; }
    public void setMaDaoPhieuThu(String maDaoPhieuThu) { this.maDaoPhieuThu = maDaoPhieuThu; }

    public String getMaPhieuThuGoc() { return maPhieuThuGoc; }
    public void setMaPhieuThuGoc(String maPhieuThuGoc) { this.maPhieuThuGoc = maPhieuThuGoc; }

    public String getMaDocGia() { return maDocGia; }
    public void setMaDocGia(String maDocGia) { this.maDocGia = maDocGia; }

    public String getMaNhanVienDao() { return maNhanVienDao; }
    public void setMaNhanVienDao(String maNhanVienDao) { this.maNhanVienDao = maNhanVienDao; }

    public BigDecimal getSoTienHoan() { return soTienHoan; }
    public void setSoTienHoan(BigDecimal soTienHoan) { this.soTienHoan = soTienHoan; }

    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }

    public String getApprovalReference() { return approvalReference; }
    public void setApprovalReference(String approvalReference) { this.approvalReference = approvalReference; }

    public LocalDateTime getNgayDao() { return ngayDao; }
    public void setNgayDao(LocalDateTime ngayDao) { this.ngayDao = ngayDao; }
}
