package com.library.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public abstract class DauSachUpsertRequest {

    @Size(max = 30, message = "Mã nhà xuất bản tối đa 30 ký tự")
    private String maNhaXuatBan;

    @NotBlank(message = "Tên đầu sách không được để trống")
    @Size(max = 200, message = "Tên đầu sách tối đa 200 ký tự")
    private String tenDauSach;

    @Size(max = 30, message = "ISBN tối đa 30 ký tự")
    private String isbn;

    @NotNull(message = "Năm xuất bản không được để trống")
    @Min(value = 1, message = "Năm xuất bản không hợp lệ")
    private Integer namXuatBan;

    @Size(max = 50, message = "Ngôn ngữ tối đa 50 ký tự")
    private String ngonNgu;

    @NotNull(message = "Số trang không được để trống")
    @Min(value = 1, message = "Số trang phải lớn hơn 0")
    private Integer soTrang;

    @Size(max = 4000, message = "Mô tả tối đa 4000 ký tự")
    private String moTa;

    @Size(max = 500, message = "Ảnh bìa tối đa 500 ký tự")
    private String anhBia;

    @NotNull(message = "Trị giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Trị giá không được âm")
    private BigDecimal triGia;

    @NotEmpty(message = "Đầu sách phải có ít nhất một tác giả")
    private List<@NotBlank(message = "Mã tác giả không hợp lệ") String> maTacGias;

    @NotEmpty(message = "Đầu sách phải có ít nhất một thể loại")
    private List<@NotBlank(message = "Mã thể loại không hợp lệ") String> maTheLoais;

    public String getMaNhaXuatBan() { return maNhaXuatBan; }
    public void setMaNhaXuatBan(String maNhaXuatBan) { this.maNhaXuatBan = maNhaXuatBan; }
    public String getTenDauSach() { return tenDauSach; }
    public void setTenDauSach(String tenDauSach) { this.tenDauSach = tenDauSach; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getNamXuatBan() { return namXuatBan; }
    public void setNamXuatBan(Integer namXuatBan) { this.namXuatBan = namXuatBan; }
    public String getNgonNgu() { return ngonNgu; }
    public void setNgonNgu(String ngonNgu) { this.ngonNgu = ngonNgu; }
    public Integer getSoTrang() { return soTrang; }
    public void setSoTrang(Integer soTrang) { this.soTrang = soTrang; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getAnhBia() { return anhBia; }
    public void setAnhBia(String anhBia) { this.anhBia = anhBia; }
    public BigDecimal getTriGia() { return triGia; }
    public void setTriGia(BigDecimal triGia) { this.triGia = triGia; }
    public List<String> getMaTacGias() { return maTacGias; }
    public void setMaTacGias(List<String> maTacGias) { this.maTacGias = maTacGias; }
    public List<String> getMaTheLoais() { return maTheLoais; }
    public void setMaTheLoais(List<String> maTheLoais) { this.maTheLoais = maTheLoais; }
}
