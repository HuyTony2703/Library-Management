package com.library.backend.dto.reader;

import java.math.BigDecimal;

public class ReaderRecommendationBookResponse {

    private final String maDauSach;
    private final String tenDauSach;
    private final String anhBia;
    private final Integer namXuatBan;
    private final BigDecimal triGia;
    private final String trangThai;
    private final Integer soCuonSanCo;
    private final Integer tongSoCuon;
    private final boolean daYeuThich;
    private final String thongTinNoiBat;

    public ReaderRecommendationBookResponse(
            String maDauSach,
            String tenDauSach,
            String anhBia,
            Integer namXuatBan,
            BigDecimal triGia,
            String trangThai,
            Integer soCuonSanCo,
            Integer tongSoCuon,
            boolean daYeuThich,
            String thongTinNoiBat
    ) {
        this.maDauSach = maDauSach;
        this.tenDauSach = tenDauSach;
        this.anhBia = anhBia;
        this.namXuatBan = namXuatBan;
        this.triGia = triGia;
        this.trangThai = trangThai;
        this.soCuonSanCo = soCuonSanCo;
        this.tongSoCuon = tongSoCuon;
        this.daYeuThich = daYeuThich;
        this.thongTinNoiBat = thongTinNoiBat;
    }

    public String getMaDauSach() { return maDauSach; }
    public String getTenDauSach() { return tenDauSach; }
    public String getAnhBia() { return anhBia; }
    public Integer getNamXuatBan() { return namXuatBan; }
    public BigDecimal getTriGia() { return triGia; }
    public String getTrangThai() { return trangThai; }
    public Integer getSoCuonSanCo() { return soCuonSanCo; }
    public Integer getTongSoCuon() { return tongSoCuon; }
    public boolean isDaYeuThich() { return daYeuThich; }
    public String getThongTinNoiBat() { return thongTinNoiBat; }
}
