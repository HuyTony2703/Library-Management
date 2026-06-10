package com.library.backend.dto.reader;

import java.math.BigDecimal;
import java.util.List;

public class ReaderBookDetailResponse {

    private final String maDauSach;
    private final String tenDauSach;
    private final String isbn;
    private final Integer namXuatBan;
    private final String ngonNgu;
    private final Integer soTrang;
    private final String moTa;
    private final String anhBia;
    private final BigDecimal triGia;
    private final String trangThai;
    private final String nhaXuatBan;
    private final List<String> tacGia;
    private final List<String> theLoai;
    private final List<ReaderBookCopyResponse> cuonSach;

    public ReaderBookDetailResponse(
            String maDauSach,
            String tenDauSach,
            String isbn,
            Integer namXuatBan,
            String ngonNgu,
            Integer soTrang,
            String moTa,
            String anhBia,
            BigDecimal triGia,
            String trangThai,
            String nhaXuatBan,
            List<String> tacGia,
            List<String> theLoai,
            List<ReaderBookCopyResponse> cuonSach
    ) {
        this.maDauSach = maDauSach;
        this.tenDauSach = tenDauSach;
        this.isbn = isbn;
        this.namXuatBan = namXuatBan;
        this.ngonNgu = ngonNgu;
        this.soTrang = soTrang;
        this.moTa = moTa;
        this.anhBia = anhBia;
        this.triGia = triGia;
        this.trangThai = trangThai;
        this.nhaXuatBan = nhaXuatBan;
        this.tacGia = tacGia;
        this.theLoai = theLoai;
        this.cuonSach = cuonSach;
    }

    public String getMaDauSach() { return maDauSach; }
    public String getTenDauSach() { return tenDauSach; }
    public String getIsbn() { return isbn; }
    public Integer getNamXuatBan() { return namXuatBan; }
    public String getNgonNgu() { return ngonNgu; }
    public Integer getSoTrang() { return soTrang; }
    public String getMoTa() { return moTa; }
    public String getAnhBia() { return anhBia; }
    public BigDecimal getTriGia() { return triGia; }
    public String getTrangThai() { return trangThai; }
    public String getNhaXuatBan() { return nhaXuatBan; }
    public List<String> getTacGia() { return tacGia; }
    public List<String> getTheLoai() { return theLoai; }
    public List<ReaderBookCopyResponse> getCuonSach() { return cuonSach; }
}

