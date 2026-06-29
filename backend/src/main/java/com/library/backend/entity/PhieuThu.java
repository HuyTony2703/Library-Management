package com.library.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PHIEUTHU")
public class PhieuThu {

    @Id
    @Column(name = "MaPhieuThu", length = 30)
    private String maPhieuThu;

    @Column(name = "MaDocGia", nullable = false, length = 30)
    private String maDocGia;

    @Column(name = "MaNhanVienThu", length = 30)
    private String maNhanVienThu;

    @Column(name = "MaPhuongThuc", nullable = false, length = 30)
    private String maPhuongThuc;

    @Column(name = "LoaiThu", nullable = false, length = 50)
    private String loaiThu;

    @Column(name = "SoTienThu", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienThu;

    @Column(name = "NgayThu", nullable = false)
    private LocalDateTime ngayThu;

    @Column(name = "MaGiaoDichNgoai", length = 100)
    private String maGiaoDichNgoai;

    @Column(name = "TienKhachDua", precision = 18, scale = 2)
    private BigDecimal tienKhachDua;

    @Column(name = "TienThua", precision = 18, scale = 2)
    private BigDecimal tienThua;

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    @Column(name = "IdempotencyKey", length = 100)
    private String idempotencyKey;

    @Column(name = "RequestFingerprint", length = 64)
    private String requestFingerprint;

    public String getMaPhieuThu() { return maPhieuThu; }
    public void setMaPhieuThu(String maPhieuThu) { this.maPhieuThu = maPhieuThu; }

    public String getMaDocGia() { return maDocGia; }
    public void setMaDocGia(String maDocGia) { this.maDocGia = maDocGia; }

    public String getMaNhanVienThu() { return maNhanVienThu; }
    public void setMaNhanVienThu(String maNhanVienThu) { this.maNhanVienThu = maNhanVienThu; }

    public String getMaPhuongThuc() { return maPhuongThuc; }
    public void setMaPhuongThuc(String maPhuongThuc) { this.maPhuongThuc = maPhuongThuc; }

    public String getLoaiThu() { return loaiThu; }
    public void setLoaiThu(String loaiThu) { this.loaiThu = loaiThu; }

    public BigDecimal getSoTienThu() { return soTienThu; }
    public void setSoTienThu(BigDecimal soTienThu) { this.soTienThu = soTienThu; }

    public LocalDateTime getNgayThu() { return ngayThu; }
    public void setNgayThu(LocalDateTime ngayThu) { this.ngayThu = ngayThu; }

    public String getMaGiaoDichNgoai() { return maGiaoDichNgoai; }
    public void setMaGiaoDichNgoai(String maGiaoDichNgoai) { this.maGiaoDichNgoai = maGiaoDichNgoai; }

    public BigDecimal getTienKhachDua() { return tienKhachDua; }
    public void setTienKhachDua(BigDecimal tienKhachDua) { this.tienKhachDua = tienKhachDua; }

    public BigDecimal getTienThua() { return tienThua; }
    public void setTienThua(BigDecimal tienThua) { this.tienThua = tienThua; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getRequestFingerprint() { return requestFingerprint; }
    public void setRequestFingerprint(String requestFingerprint) { this.requestFingerprint = requestFingerprint; }
}
