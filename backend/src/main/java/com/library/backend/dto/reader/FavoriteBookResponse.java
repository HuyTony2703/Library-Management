package com.library.backend.dto.reader;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FavoriteBookResponse {

    private final String maYeuThich;
    private final String maDocGia;
    private final String maDauSach;
    private final String tenDauSach;
    private final String anhBia;
    private final Integer namXuatBan;
    private final BigDecimal triGia;
    private final String trangThaiDauSach;
    private final Integer soCuonSanCo;
    private final LocalDateTime ngayThem;

    public FavoriteBookResponse(
            String maYeuThich,
            String maDocGia,
            String maDauSach,
            String tenDauSach,
            String anhBia,
            Integer namXuatBan,
            BigDecimal triGia,
            String trangThaiDauSach,
            Integer soCuonSanCo,
            LocalDateTime ngayThem
    ) {
        this.maYeuThich = maYeuThich;
        this.maDocGia = maDocGia;
        this.maDauSach = maDauSach;
        this.tenDauSach = tenDauSach;
        this.anhBia = anhBia;
        this.namXuatBan = namXuatBan;
        this.triGia = triGia;
        this.trangThaiDauSach = trangThaiDauSach;
        this.soCuonSanCo = soCuonSanCo;
        this.ngayThem = ngayThem;
    }

    public String getMaYeuThich() { return maYeuThich; }
    public String getMaDocGia() { return maDocGia; }
    public String getMaDauSach() { return maDauSach; }
    public String getTenDauSach() { return tenDauSach; }
    public String getAnhBia() { return anhBia; }
    public Integer getNamXuatBan() { return namXuatBan; }
    public BigDecimal getTriGia() { return triGia; }
    public String getTrangThaiDauSach() { return trangThaiDauSach; }
    public Integer getSoCuonSanCo() { return soCuonSanCo; }
    public LocalDateTime getNgayThem() { return ngayThem; }
}
