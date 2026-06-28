package com.library.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class DauSachResponse {

    private String maDauSach;
    private String maNhaXuatBan;
    private String tenDauSach;
    private String isbn;
    private Integer namXuatBan;
    private String ngonNgu;
    private Integer soTrang;
    private String moTa;
    private String anhBia;
    private BigDecimal triGia;
    private String trangThai;
    private List<String> maTacGias;
    private List<String> maTheLoais;
    private String tenNhaXuatBan;
    private List<String> tenTacGias;
    private List<String> tenTheLoais;
    private Long tongSoBan;
    private Long soBanSanCo;

    public DauSachResponse(
            String maDauSach,
            String maNhaXuatBan,
            String tenDauSach,
            String isbn,
            Integer namXuatBan,
            String ngonNgu,
            Integer soTrang,
            String moTa,
            String anhBia,
            BigDecimal triGia,
            String trangThai,
            List<String> maTacGias,
            List<String> maTheLoais
    ) {
        this(maDauSach, maNhaXuatBan, tenDauSach, isbn, namXuatBan, ngonNgu, soTrang,
                moTa, anhBia, triGia, trangThai, maTacGias, maTheLoais,
                null, List.of(), List.of(), null, null);
    }

    public DauSachResponse(
            String maDauSach,
            String maNhaXuatBan,
            String tenDauSach,
            String isbn,
            Integer namXuatBan,
            String ngonNgu,
            Integer soTrang,
            String moTa,
            String anhBia,
            BigDecimal triGia,
            String trangThai,
            List<String> maTacGias,
            List<String> maTheLoais,
            String tenNhaXuatBan,
            List<String> tenTacGias,
            List<String> tenTheLoais,
            Long tongSoBan,
            Long soBanSanCo
    ) {
        this.maDauSach = maDauSach;
        this.maNhaXuatBan = maNhaXuatBan;
        this.tenDauSach = tenDauSach;
        this.isbn = isbn;
        this.namXuatBan = namXuatBan;
        this.ngonNgu = ngonNgu;
        this.soTrang = soTrang;
        this.moTa = moTa;
        this.anhBia = anhBia;
        this.triGia = triGia;
        this.trangThai = trangThai;
        this.maTacGias = maTacGias;
        this.maTheLoais = maTheLoais;
        this.tenNhaXuatBan = tenNhaXuatBan;
        this.tenTacGias = tenTacGias;
        this.tenTheLoais = tenTheLoais;
        this.tongSoBan = tongSoBan;
        this.soBanSanCo = soBanSanCo;
    }

    public String getMaDauSach() {
        return maDauSach;
    }

    public String getMaNhaXuatBan() {
        return maNhaXuatBan;
    }

    public String getTenDauSach() {
        return tenDauSach;
    }

    public String getIsbn() {
        return isbn;
    }

    public Integer getNamXuatBan() {
        return namXuatBan;
    }

    public String getNgonNgu() {
        return ngonNgu;
    }

    public Integer getSoTrang() {
        return soTrang;
    }

    public String getMoTa() {
        return moTa;
    }

    public String getAnhBia() {
        return anhBia;
    }

    public BigDecimal getTriGia() {
        return triGia;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public List<String> getMaTacGias() {
        return maTacGias;
    }

    public List<String> getMaTheLoais() {
        return maTheLoais;
    }

    public String getTenNhaXuatBan() { return tenNhaXuatBan; }

    public List<String> getTenTacGias() { return tenTacGias; }

    public List<String> getTenTheLoais() { return tenTheLoais; }

    public Long getTongSoBan() { return tongSoBan; }

    public Long getSoBanSanCo() { return soBanSanCo; }
}
