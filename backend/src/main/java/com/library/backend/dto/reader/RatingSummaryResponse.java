package com.library.backend.dto.reader;

import java.math.BigDecimal;

public class RatingSummaryResponse {

    private final String maDauSach;
    private final BigDecimal diemTrungBinh;
    private final int tongSoDanhGia;
    private final int soSao1;
    private final int soSao2;
    private final int soSao3;
    private final int soSao4;
    private final int soSao5;
    private final String maDanhGiaCuaToi;
    private final Integer soSaoCuaToi;
    private final String noiDungCuaToi;

    public RatingSummaryResponse(
            String maDauSach,
            BigDecimal diemTrungBinh,
            int tongSoDanhGia,
            int soSao1,
            int soSao2,
            int soSao3,
            int soSao4,
            int soSao5,
            String maDanhGiaCuaToi,
            Integer soSaoCuaToi,
            String noiDungCuaToi
    ) {
        this.maDauSach = maDauSach;
        this.diemTrungBinh = diemTrungBinh;
        this.tongSoDanhGia = tongSoDanhGia;
        this.soSao1 = soSao1;
        this.soSao2 = soSao2;
        this.soSao3 = soSao3;
        this.soSao4 = soSao4;
        this.soSao5 = soSao5;
        this.maDanhGiaCuaToi = maDanhGiaCuaToi;
        this.soSaoCuaToi = soSaoCuaToi;
        this.noiDungCuaToi = noiDungCuaToi;
    }

    public String getMaDauSach() { return maDauSach; }
    public BigDecimal getDiemTrungBinh() { return diemTrungBinh; }
    public int getTongSoDanhGia() { return tongSoDanhGia; }
    public int getSoSao1() { return soSao1; }
    public int getSoSao2() { return soSao2; }
    public int getSoSao3() { return soSao3; }
    public int getSoSao4() { return soSao4; }
    public int getSoSao5() { return soSao5; }
    public String getMaDanhGiaCuaToi() { return maDanhGiaCuaToi; }
    public Integer getSoSaoCuaToi() { return soSaoCuaToi; }
    public String getNoiDungCuaToi() { return noiDungCuaToi; }
}
