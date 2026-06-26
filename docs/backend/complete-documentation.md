# TÀI LIỆU TOÀN DIỆN CẤU TRÚC BACKEND

> **Phạm vi:** Tài liệu snapshot theo lớp, hữu ích để học luồng xử lý nhưng có thể chậm hơn source code sau các lần phát triển. Không sao chép secret hoặc cấu hình mẫu trong tài liệu sang production.

**Dự án**: Hệ thống quản lý thư viện desktop  
**Framework**: Spring Boot 3.5.14 + Spring Security + JPA/Hibernate  
**Database**: SQL Server  
**Java Version**: 21  
**Kiến trúc**: 3-tier layered (Controller → Service → Repository)  
**Authentication**: JWT Token (Custom implementation)  

---

## PHẦN I: ENTRY POINT

### 1. BackendApplication.java
```java
package com.library.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```
**Chức năng**: Điểm vào của ứng dụng Spring Boot. Khi chạy file này, Spring sẽ tự động thực hiện:
- Scan tất cả @Component, @Controller, @Service, @Repository
- Cấu hình database connection
- Khởi động Tomcat server trên port 8080

---

## PHẦN II: CONTROLLER (14 files) - REST API Endpoints

### 1. AuthController.java
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // GET /api/auth/me
    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return authService.me(user);
    }
}
```

**Endpoints**:
- `POST /api/auth/login` - Đăng nhập, trả về JWT token
- `GET /api/auth/me` - Lấy thông tin user hiện tại (yêu cầu token)

---

### 2. DauSachController.java
```java
@RestController
@RequestMapping("/api/books")
public class DauSachController {
    private final DauSachService dauSachService;

    public DauSachController(DauSachService dauSachService) {
        this.dauSachService = dauSachService;
    }

    // GET /api/books
    @GetMapping
    public List<DauSachResponse> getAll() {
        return dauSachService.getAll();
    }

    // GET /api/books/{maDauSach}
    @GetMapping("/{maDauSach}")
    public DauSachResponse getById(@PathVariable String maDauSach) {
        return dauSachService.getById(maDauSach);
    }

    // POST /api/books
    @PostMapping
    public DauSachResponse create(@Valid @RequestBody DauSachRequest request) {
        return dauSachService.create(request);
    }

    // PUT /api/books/{maDauSach}
    @PutMapping("/{maDauSach}")
    public DauSachResponse update(
            @PathVariable String maDauSach,
            @Valid @RequestBody DauSachRequest request
    ) {
        return dauSachService.update(maDauSach, request);
    }

    // DELETE /api/books/{maDauSach}
    @DeleteMapping("/{maDauSach}")
    public String disable(@PathVariable String maDauSach) {
        dauSachService.disable(maDauSach);
        return "Ngừng hiển thị đầu sách thành công";
    }
}
```

**Endpoints**: CRUD operations cho Đầu Sách
- `GET /api/books` - Lấy tất cả sách
- `GET /api/books/{maDauSach}` - Lấy 1 sách
- `POST /api/books` - Tạo mới sách
- `PUT /api/books/{maDauSach}` - Cập nhật sách
- `DELETE /api/books/{maDauSach}` - Ngừng hiển thị sách

---

### 3. CuonSachController.java
```java
@RestController
@RequestMapping("/api/book-copies")
public class CuonSachController {
    private final CuonSachService cuonSachService;

    // GET /api/book-copies
    @GetMapping
    public List<CuonSachResponse> getAll() {
        return cuonSachService.getAll();
    }

    // GET /api/book-copies/{maCuonSach}
    @GetMapping("/{maCuonSach}")
    public CuonSachResponse getById(@PathVariable String maCuonSach) {
        return cuonSachService.getById(maCuonSach);
    }

    // POST /api/book-copies
    @PostMapping
    public CuonSachResponse create(@Valid @RequestBody CuonSachRequest request) {
        return cuonSachService.create(request);
    }

    // PUT /api/book-copies/{maCuonSach}
    @PutMapping("/{maCuonSach}")
    public CuonSachResponse update(
            @PathVariable String maCuonSach,
            @Valid @RequestBody CuonSachRequest request
    ) {
        return cuonSachService.update(maCuonSach, request);
    }

    // DELETE /api/book-copies/{maCuonSach}
    @DeleteMapping("/{maCuonSach}")
    public String disable(@PathVariable String maCuonSach) {
        cuonSachService.disable(maCuonSach);
        return "Ngừng lưu thông cuốn sách thành công";
    }
}
```

**Endpoints**: CRUD operations cho Cuốn Sách (bản vật lý)
- Quản lý từng cuốn sách (bản copy cụ thể)
- Lưu vị trí, mã vạch, QR code

---

### 4. DocGiaController.java
```java
@RestController
@RequestMapping("/api/readers")
public class DocGiaController {
    private final DocGiaService docGiaService;

    @GetMapping
    public List<DocGiaResponse> getAll() {
        return docGiaService.getAll();
    }

    @GetMapping("/{maDocGia}")
    public DocGiaResponse getById(@PathVariable String maDocGia) {
        return docGiaService.getById(maDocGia);
    }

    @PostMapping
    public DocGiaResponse create(@Valid @RequestBody DocGiaRequest request) {
        return docGiaService.create(request);
    }

    @PutMapping("/{maDocGia}")
    public DocGiaResponse update(
            @PathVariable String maDocGia,
            @Valid @RequestBody DocGiaRequest request
    ) {
        return docGiaService.update(maDocGia, request);
    }

    @DeleteMapping("/{maDocGia}")
    public String disable(@PathVariable String maDocGia) {
        docGiaService.disable(maDocGia);
        return "Ngừng hoạt động độc giả thành công";
    }
}
```

**Endpoints**: CRUD operations cho Độc Giả (Readers)
- `GET /api/readers` - Danh sách độc giả
- `GET /api/readers/{maDocGia}` - Chi tiết độc giả
- `POST /api/readers` - Đăng ký độc giả mới
- `PUT /api/readers/{maDocGia}` - Cập nhật thông tin
- `DELETE /api/readers/{maDocGia}` - Ngừng hoạt động

---

### 5. MuonSachController.java
```java
@RestController
@RequestMapping("/api")
public class MuonSachController {
    private final MuonSachService muonSachService;

    @PostMapping("/loans")
    public MuonSachResponse create(@Valid @RequestBody MuonSachRequest request) {
        return muonSachService.create(request);
    }

    @GetMapping("/loans/{maPhieuMuon}")
    public MuonSachResponse getById(@PathVariable String maPhieuMuon) {
        return muonSachService.getById(maPhieuMuon);
    }

    @GetMapping("/readers/{maDocGia}/current-loans")
    public List<MuonSachResponse.ChiTietMuonResponse> getCurrentLoans(
            @PathVariable String maDocGia
    ) {
        return muonSachService.getCurrentLoansByReader(maDocGia);
    }
}
```

**Endpoints**: Quản lý phiếu mượn sách
- `POST /api/loans` - Tạo phiếu mượn mới
- `GET /api/loans/{maPhieuMuon}` - Chi tiết phiếu mượn
- `GET /api/readers/{maDocGia}/current-loans` - Sách đang mượn

---

### 6. TraSachController.java
```java
@RestController
@RequestMapping("/api/returns")
public class TraSachController {
    private final TraSachService traSachService;

    @PostMapping
    public TraSachResponse create(@Valid @RequestBody TraSachRequest request) {
        return traSachService.create(request);
    }

    @GetMapping("/{maPhieuTra}")
    public TraSachResponse getById(@PathVariable String maPhieuTra) {
        return traSachService.getById(maPhieuTra);
    }
}
```

**Endpoints**: Quản lý phiếu trả sách
- `POST /api/returns` - Tạo phiếu trả
- `GET /api/returns/{maPhieuTra}` - Chi tiết phiếu trả

---

### 7. ThanhToanController.java
```java
@RestController
@RequestMapping("/api")
public class ThanhToanController {
    private final ThanhToanService thanhToanService;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;

    @GetMapping("/readers/{maDocGia}/debts")
    public List<KhoanNoResponse> getDebtsByReader(@PathVariable String maDocGia) {
        return thanhToanService.getDebtsByReader(maDocGia);
    }

    @GetMapping("/payment-methods")
    public List<PhuongThucThanhToan> getPaymentMethods() {
        return phuongThucThanhToanRepository.findAll();
    }

    @PostMapping("/payments")
    public PhieuThuResponse createPayment(@Valid @RequestBody PhieuThuRequest request) {
        return thanhToanService.createPayment(request);
    }

    @GetMapping("/payments/{maPhieuThu}")
    public PhieuThuResponse getPaymentById(@PathVariable String maPhieuThu) {
        return thanhToanService.getPaymentById(maPhieuThu);
    }

    @GetMapping("/readers/{maDocGia}/payments")
    public List<PhieuThuResponse> getPaymentsByReader(@PathVariable String maDocGia) {
        return thanhToanService.getPaymentsByReader(maDocGia);
    }
}
```

**Endpoints**: Quản lý thanh toán & khoản nợ
- `GET /api/readers/{maDocGia}/debts` - Danh sách khoản nợ
- `GET /api/payment-methods` - Các phương thức thanh toán
- `POST /api/payments` - Tạo phiếu thu tiền
- `GET /api/payments/{maPhieuThu}` - Chi tiết phiếu thu
- `GET /api/readers/{maDocGia}/payments` - Lịch sử thanh toán

---

### 8. TacGiaController.java
```java
@RestController
@RequestMapping("/api/authors")
public class TacGiaController {
    private final TacGiaService tacGiaService;

    @GetMapping
    public List<TacGiaResponse> getAll() {
        return tacGiaService.getAll();
    }

    @GetMapping("/{maTacGia}")
    public TacGiaResponse getById(@PathVariable String maTacGia) {
        return tacGiaService.getById(maTacGia);
    }

    @PostMapping
    public TacGiaResponse create(@Valid @RequestBody TacGiaRequest request) {
        return tacGiaService.create(request);
    }

    @PutMapping("/{maTacGia}")
    public TacGiaResponse update(
            @PathVariable String maTacGia,
            @Valid @RequestBody TacGiaRequest request
    ) {
        return tacGiaService.update(maTacGia, request);
    }

    @DeleteMapping("/{maTacGia}")
    public String delete(@PathVariable String maTacGia) {
        tacGiaService.delete(maTacGia);
        return "Xóa tác giả thành công";
    }
}
```

**Endpoints**: CRUD operations cho Tác Giả (Authors)

---

### 9. TheLoaiController.java
```java
@RestController
@RequestMapping("/api/categories")
public class TheLoaiController {
    private final TheLoaiService theLoaiService;

    @GetMapping
    public List<TheLoaiResponse> getAll() {
        return theLoaiService.getAll();
    }

    @GetMapping("/{maTheLoai}")
    public TheLoaiResponse getById(@PathVariable String maTheLoai) {
        return theLoaiService.getById(maTheLoai);
    }

    @PostMapping
    public TheLoaiResponse create(@Valid @RequestBody TheLoaiRequest request) {
        return theLoaiService.create(request);
    }

    @PutMapping("/{maTheLoai}")
    public TheLoaiResponse update(
            @PathVariable String maTheLoai,
            @Valid @RequestBody TheLoaiRequest request
    ) {
        return theLoaiService.update(maTheLoai, request);
    }

    @DeleteMapping("/{maTheLoai}")
    public String disable(@PathVariable String maTheLoai) {
        theLoaiService.disable(maTheLoai);
        return "Ngừng áp dụng thể loại thành công";
    }
}
```

**Endpoints**: CRUD operations cho Thể Loại (Categories)

---

### 10. NhaXuatBanController.java
```java
@RestController
@RequestMapping("/api/publishers")
public class NhaXuatBanController {
    private final NhaXuatBanService nhaXuatBanService;

    @GetMapping
    public List<NhaXuatBanResponse> getAll() {
        return nhaXuatBanService.getAll();
    }

    @GetMapping("/{maNhaXuatBan}")
    public NhaXuatBanResponse getById(@PathVariable String maNhaXuatBan) {
        return nhaXuatBanService.getById(maNhaXuatBan);
    }

    @PostMapping
    public NhaXuatBanResponse create(@Valid @RequestBody NhaXuatBanRequest request) {
        return nhaXuatBanService.create(request);
    }

    @PutMapping("/{maNhaXuatBan}")
    public NhaXuatBanResponse update(
            @PathVariable String maNhaXuatBan,
            @Valid @RequestBody NhaXuatBanRequest request
    ) {
        return nhaXuatBanService.update(maNhaXuatBan, request);
    }

    @DeleteMapping("/{maNhaXuatBan}")
    public String delete(@PathVariable String maNhaXuatBan) {
        nhaXuatBanService.delete(maNhaXuatBan);
        return "Xóa nhà xuất bản thành công";
    }
}
```

**Endpoints**: CRUD operations cho Nhà Xuất Bản (Publishers)

---

### 11. BaoCaoController.java
```java
@RestController
@RequestMapping("/api/reports")
public class BaoCaoController {
    private final BaoCaoService baoCaoService;

    @GetMapping("/debts")
    public List<TongNoDocGiaReportResponse> getTongNoDocGia() {
        return baoCaoService.getTongNoDocGia();
    }

    @GetMapping("/current-loans")
    public List<SachDangMuonReportResponse> getSoSachDangMuon() {
        return baoCaoService.getSoSachDangMuon();
    }

    @GetMapping("/borrow-by-category")
    public List<BaoCaoMuonTheLoaiResponse> getBaoCaoMuonTheoTheLoai(
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        return baoCaoService.getBaoCaoMuonTheoTheLoai(month, year);
    }

    @GetMapping("/late-returns")
    public List<BaoCaoTraTreResponse> getBaoCaoTraTre(
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        return baoCaoService.getBaoCaoTraTre(month, year);
    }
}
```

**Endpoints**: Báo cáo & thống kê
- `GET /api/reports/debts` - Tổng nợ độc giả
- `GET /api/reports/current-loans` - Sách đang mượn
- `GET /api/reports/borrow-by-category?month=1&year=2024` - Thống kê mượn theo thể loại
- `GET /api/reports/late-returns?month=1&year=2024` - Báo cáo trả trễ

---

### 12. HealthController.java
```java
@RestController
public class HealthController {
    @GetMapping("/api/health")
    public String health() {
        return "Backend is running";
    }
}
```

**Endpoints**: Health check
- `GET /api/health` - Kiểm tra server chạy (không cần auth)

---

### 13. ReaderOptionController.java
```java
@RestController
@RequestMapping("/api")
public class ReaderOptionController {
    private final NhomDocGiaRepository nhomDocGiaRepository;
    private final GoiThanhVienRepository goiThanhVienRepository;

    @GetMapping("/reader-groups")
    public List<NhomDocGia> getReaderGroups() {
        return nhomDocGiaRepository.findAll();
    }

    @GetMapping("/membership-plans")
    public List<GoiThanhVien> getMembershipPlans() {
        return goiThanhVienRepository.findAll();
    }
}
```

**Endpoints**: Lấy danh sách các tùy chọn
- `GET /api/reader-groups` - Danh sách nhóm độc giả
- `GET /api/membership-plans` - Danh sách gói thành viên

---

### 14. DevPasswordController.java
```java
@RestController
@RequestMapping("/api/dev")
@Profile("dev")
public class DevPasswordController {
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/hash")
    public String hash(@RequestParam String password) {
        return passwordEncoder.encode(password);
    }
}
```

**Endpoints**: Dev only (chỉ khi chạy profile "dev")
- `GET /api/dev/hash?password=abc123` - Mã hóa mật khẩu

---

## PHẦN III: DTO (27 files) - Request/Response Objects

### 1. LoginRequest.java
```java
public class LoginRequest {
    @NotBlank(message = "Tên đăng nhập hoặc email không được để trống")
    private String usernameOrEmail;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    // Getters & Setters
}
```

**Mục đích**: Nhận dữ liệu đăng nhập từ client

---

### 2. AuthResponse.java
```java
public class AuthResponse {
    private String token;              // JWT Token
    private String tokenType;          // "Bearer"
    private String maTaiKhoan;
    private String tenDangNhap;
    private String maVaiTro;
    private String tenVaiTro;
    private String maDocGia;           // Null nếu là nhân viên
    private String maNhanVien;         // Null nếu là độc giả

    public AuthResponse(
            String token, String tokenType, String maTaiKhoan,
            String tenDangNhap, String maVaiTro, String tenVaiTro,
            String maDocGia, String maNhanVien
    ) {
        this.token = token;
        this.tokenType = tokenType;
        this.maTaiKhoan = maTaiKhoan;
        this.tenDangNhap = tenDangNhap;
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
        this.maDocGia = maDocGia;
        this.maNhanVien = maNhanVien;
    }

    // Getters
}
```

**Mục đích**: Trả về thông tin đăng nhập thành công + JWT token

---

### 3. DauSachRequest.java
```java
public class DauSachRequest {
    @NotBlank(message = "Mã đầu sách không được để trống")
    @Size(max = 30, message = "Mã đầu sách tối đa 30 ký tự")
    private String maDauSach;

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

    @Min(value = 1, message = "Số trang phải lớn hơn 0")
    private Integer soTrang;

    private String moTa;

    @Size(max = 500, message = "Ảnh bìa tối đa 500 ký tự")
    private String anhBia;

    @NotNull(message = "Trị giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Trị giá không được âm")
    private BigDecimal triGia;

    @NotEmpty(message = "Đầu sách phải có ít nhất một tác giả")
    private List<String> maTacGias;

    @NotEmpty(message = "Đầu sách phải có ít nhất một thể loại")
    private List<String> maTheLoais;

    // Getters & Setters
}
```

**Mục đích**: Nhận dữ liệu tạo/cập nhật Đầu Sách từ client

---

### 4. CuonSachRequest.java
```java
public class CuonSachRequest {
    @NotBlank(message = "Mã cuốn sách không được để trống")
    @Size(max = 40, message = "Mã cuốn sách tối đa 40 ký tự")
    private String maCuonSach;

    @NotBlank(message = "Mã đầu sách không được để trống")
    @Size(max = 30, message = "Mã đầu sách tối đa 30 ký tự")
    private String maDauSach;

    @NotBlank(message = "Mã chi nhánh không được để trống")
    @Size(max = 30, message = "Mã chi nhánh tối đa 30 ký tự")
    private String maChiNhanh;

    @NotBlank(message = "Mã vị trí không được để trống")
    @Size(max = 30, message = "Mã vị trí tối đa 30 ký tự")
    private String maViTri;

    @Size(max = 30, message = "Mã trạng thái tối đa 30 ký tự")
    private String maTrangThai;

    @Size(max = 100, message = "Mã vạch tối đa 100 ký tự")
    private String maVach;

    @Size(max = 100, message = "Mã QR tối đa 100 ký tự")
    private String maQrCode;

    @PastOrPresent(message = "Ngày nhập sách không được lớn hơn ngày hiện tại")
    private LocalDate ngayNhapSach;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự")
    private String ghiChu;

    // Getters & Setters
}
```

**Mục đích**: Nhận dữ liệu tạo/cập nhật Cuốn Sách (bản vật lý)

---

### 5. DocGiaRequest.java
```java
public class DocGiaRequest {
    @NotBlank(message = "Mã độc giả không được để trống")
    private String maDocGia;

    @NotBlank(message = "Mã tài khoản không được để trống")
    private String maTaiKhoan;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    private String tenDangNhap;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String matKhau;

    @NotBlank(message = "Mã nhóm độc giả không được để trống")
    private String maNhomDocGia;

    private String maGoiThanhVien;

    @NotBlank(message = "Họ tên không được để trống")
    private String hoTen;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate ngaySinh;

    private String diaChi;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String soDienThoai;

    private LocalDate ngayLapThe;

    // Getters & Setters
}
```

**Mục đích**: Nhận dữ liệu đăng ký/cập nhật Độc Giả

---

Các Response classes có cấu trúc tương tự Request nhưng dùng để trả dữ liệu từ server.

---

## PHẦN IV: ENTITY (23 files) - JPA Entities (Mapping bảng database)

### 1. DauSach.java
```java
@Entity
@Table(name = "DAUSACH")
public class DauSach {
    @Id
    @Column(name = "MaDauSach", length = 30)
    private String maDauSach;          // PRIMARY KEY

    @Column(name = "MaNhaXuatBan", length = 30)
    private String maNhaXuatBan;       // FOREIGN KEY

    @Column(name = "TenDauSach", nullable = false, length = 200)
    private String tenDauSach;

    @Column(name = "ISBN", length = 30)
    private String isbn;

    @Column(name = "NamXuatBan", nullable = false)
    private Integer namXuatBan;

    @Column(name = "NgonNgu", length = 50)
    private String ngonNgu;

    @Column(name = "SoTrang")
    private Integer soTrang;

    @Column(name = "MoTa", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "AnhBia", length = 500)
    private String anhBia;

    @Column(name = "TriGia", nullable = false, precision = 18, scale = 2)
    private BigDecimal triGia;

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;          // "Hoạt động" hoặc "Ngừng"

    // Getters & Setters
}
```

**Mapping**: Bảng DAUSACH - Thông tin đầu sách (tên sách, tác giả, nhà xuất bản, v.v)

---

### 2. CuonSach.java
```java
@Entity
@Table(name = "CUONSACH")
public class CuonSach {
    @Id
    @Column(name = "MaCuonSach", length = 40)
    private String maCuonSach;        // PRIMARY KEY - ID từng cuốn cụ thể

    @Column(name = "MaDauSach", nullable = false, length = 30)
    private String maDauSach;          // FK → DauSach

    @Column(name = "MaChiNhanh", nullable = false, length = 30)
    private String maChiNhanh;         // FK → ChiNhanh

    @Column(name = "MaViTri", nullable = false, length = 30)
    private String maViTri;            // FK → ViTriSach (vị trí kệ)

    @Column(name = "MaTrangThai", nullable = false, length = 30)
    private String maTrangThai;        // FK → TrangThaiCuonSach

    @Column(name = "MaVach", length = 100)
    private String maVach;             // Mã vạch

    @Column(name = "MaQRCode", length = 100)
    private String maQrCode;           // Mã QR code

    @Column(name = "NgayNhapSach", nullable = false)
    private LocalDate ngayNhapSach;

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    // Getters & Setters
}
```

**Mapping**: Bảng CUONSACH - Từng cuốn sách cụ thể (1 DauSach có nhiều CuonSach)

---

### 3. DocGia.java
```java
@Entity
@Table(name = "DOCGIA")
public class DocGia {
    @Id
    @Column(name = "MaDocGia", length = 30)
    private String maDocGia;

    @Column(name = "MaTaiKhoan", nullable = false, length = 30)
    private String maTaiKhoan;         // FK → TaiKhoan

    @Column(name = "MaNhomDocGia", nullable = false, length = 30)
    private String maNhomDocGia;       // FK → NhomDocGia

    @Column(name = "HoTen", nullable = false, length = 150)
    private String hoTen;

    @Column(name = "NgaySinh", nullable = false)
    private LocalDate ngaySinh;

    @Column(name = "DiaChi", length = 255)
    private String diaChi;

    @Column(name = "Email", nullable = false, length = 255)
    private String email;

    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "NgayLapThe", nullable = false)
    private LocalDate ngayLapThe;      // Ngày lập thẻ độc giả

    @Column(name = "NgayHetHanThe", nullable = false)
    private LocalDate ngayHetHanThe;   // Ngày hết hạn thẻ

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;          // "Hoạt động" hoặc "Ngừng"

    // Getters & Setters
}
```

**Mapping**: Bảng DOCGIA - Thông tin độc giả (bạn đọc)

---

### 4. PhieuMuon.java
```java
@Entity
@Table(name = "PHIEUMUON")
public class PhieuMuon {
    @Id
    @Column(name = "MaPhieuMuon", length = 30)
    private String maPhieuMuon;        // PRIMARY KEY

    @Column(name = "MaDocGia", nullable = false, length = 30)
    private String maDocGia;           // FK → DocGia

    @Column(name = "MaNhanVienLap", nullable = false, length = 30)
    private String maNhanVienLap;      // FK → NhanVien

    @Column(name = "MaChiNhanh", nullable = false, length = 30)
    private String maChiNhanh;         // FK → ChiNhanh

    @Column(name = "MaPhienBanQuyDinh", nullable = false, length = 30)
    private String maPhienBanQuyDinh;  // Phiên bản quy định mượn

    @Column(name = "NgayMuon", nullable = false)
    private LocalDateTime ngayMuon;

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;          // "Mở" hoặc "Đóng"

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    // Getters & Setters
}
```

**Mapping**: Bảng PHIEUMUON - Phiếu mượn sách (1 phiếu có nhiều cuốn sách)

---

### 5. PhieuTra.java
```java
@Entity
@Table(name = "PHIEUTRA")
public class PhieuTra {
    @Id
    @Column(name = "MaPhieuTra", length = 30)
    private String maPhieuTra;        // PRIMARY KEY

    @Column(name = "MaDocGia", nullable = false, length = 30)
    private String maDocGia;          // FK → DocGia

    @Column(name = "MaNhanVienNhan", nullable = false, length = 30)
    private String maNhanVienNhan;    // FK → NhanVien

    @Column(name = "MaChiNhanh", nullable = false, length = 30)
    private String maChiNhanh;        // FK → ChiNhanh

    @Column(name = "NgayTra", nullable = false)
    private LocalDateTime ngayTra;

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    // Getters & Setters
}
```

**Mapping**: Bảng PHIEUTRA - Phiếu trả sách

---

### 6. PhieuThu.java
```java
@Entity
@Table(name = "PHIEUTHU")
public class PhieuThu {
    @Id
    @Column(name = "MaPhieuThu", length = 30)
    private String maPhieuThu;        // PRIMARY KEY

    @Column(name = "MaDocGia", nullable = false, length = 30)
    private String maDocGia;          // FK → DocGia

    @Column(name = "MaNhanVienThu", length = 30)
    private String maNhanVienThu;     // FK → NhanVien (nhân viên thu tiền)

    @Column(name = "MaPhuongThuc", nullable = false, length = 30)
    private String maPhuongThuc;      // FK → PhuongThucThanhToan

    @Column(name = "LoaiThu", nullable = false, length = 50)
    private String loaiThu;           // "Lệ phí", "Phạt trễ", "Phạt mất", v.v

    @Column(name = "SoTienThu", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienThu;

    @Column(name = "NgayThu", nullable = false)
    private LocalDateTime ngayThu;

    @Column(name = "MaGiaoDichNgoai", length = 100)
    private String maGiaoDichNgoai;   // Mã giao dịch bên ngoài (ngân hàng, v.v)

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;

    // Getters & Setters
}
```

**Mapping**: Bảng PHIEUTHU - Phiếu thu tiền thanh toán

---

### 7. KhoanNo.java
```java
@Entity
@Table(name = "KHOANNO")
public class KhoanNo {
    @Id
    @Column(name = "MaKhoanNo", length = 30)
    private String maKhoanNo;         // PRIMARY KEY

    @Column(name = "MaDocGia", nullable = false, length = 30)
    private String maDocGia;          // FK → DocGia

    @Column(name = "MaLoaiKhoanNo", nullable = false, length = 30)
    private String maLoaiKhoanNo;     // Loại nợ (phạt trễ, phạt mất)

    @Column(name = "MaChiTietTra", length = 30)
    private String maChiTietTra;      // FK → ChiTietPhieuTra

    @Column(name = "SoTienPhatSinh", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienPhatSinh; // Số tiền phát sinh

    @Column(name = "SoTienDaThanhToan", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTienDaThanhToan; // Số tiền đã thanh toán

    @Column(name = "SoTienConLai", insertable = false, updatable = false, precision = 18, scale = 2)
    private BigDecimal soTienConLai;   // Computed: SoTienPhatSinh - SoTienDaThanhToan

    @Column(name = "NgayPhatSinh", nullable = false)
    private LocalDateTime ngayPhatSinh;

    @Column(name = "LyDo", length = 255)
    private String lyDo;              // Lý do phát sinh nợ

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;         // "Chưa thanh toán", "Đã thanh toán"

    // Getters & Setters
}
```

**Mapping**: Bảng KHOANNO - Khoản nợ độc giả (phạt trễ, phạt mất, v.v)

---

### 8. TaiKhoan.java
```java
@Entity
@Table(name = "TAIKHOAN")
public class TaiKhoan {
    @Id
    @Column(name = "MaTaiKhoan", length = 30)
    private String maTaiKhoan;

    @Column(name = "TenDangNhap", nullable = false, length = 100)
    private String tenDangNhap;       // Username

    @Column(name = "MatKhauHash", nullable = false, length = 255)
    private String matKhauHash;       // Mật khẩu đã mã hóa (BCrypt)

    @Column(name = "EmailDangNhap", nullable = false, length = 255)
    private String emailDangNhap;

    @Column(name = "MaVaiTro", nullable = false, length = 20)
    private String maVaiTro;          // FK → VaiTro

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;         // "Hoạt động" hoặc "Khóa"

    @Column(name = "NgayTao", nullable = false)
    private LocalDateTime ngayTao;

    @Column(name = "LanDangNhapCuoi")
    private LocalDateTime lanDangNhapCuoi;

    // Getters & Setters
}
```

**Mapping**: Bảng TAIKHOAN - Tài khoản đăng nhập

---

### 9. VaiTro.java
```java
@Entity
@Table(name = "VAITRO")
public class VaiTro {
    @Id
    @Column(name = "MaVaiTro", length = 20)
    private String maVaiTro;          // "VT_ADMIN", "VT_THU_THU", "VT_DOC_GIA"

    @Column(name = "TenVaiTro", nullable = false, length = 50)
    private String tenVaiTro;         // "Quản trị viên", "Thủ thư", "Độc giả"

    @Column(name = "MoTa", length = 255)
    private String moTa;

    // Getters & Setters
}
```

**Mapping**: Bảng VAITRO - Vai trò (Admin, Thủ thư, Độc giả)

---

### 10. ChiNhanh.java
```java
@Entity
@Table(name = "CHINHANH")
public class ChiNhanh {
    @Id
    @Column(name = "MaChiNhanh", length = 30)
    private String maChiNhanh;

    @Column(name = "TenChiNhanh", nullable = false, length = 150)
    private String tenChiNhanh;

    @Column(name = "DiaChi", nullable = false, length = 255)
    private String diaChi;

    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "Email", length = 255)
    private String email;

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    // Getters & Setters
}
```

**Mapping**: Bảng CHINHANH - Chi nhánh thư viện

---

### 11. NhanVien.java
```java
@Entity
@Table(name = "NHANVIEN")
public class NhanVien {
    @Id
    @Column(name = "MaNhanVien", length = 30)
    private String maNhanVien;

    @Column(name = "MaTaiKhoan", nullable = false, length = 30)
    private String maTaiKhoan;        // FK → TaiKhoan

    @Column(name = "MaChiNhanh", length = 30)
    private String maChiNhanh;        // FK → ChiNhanh

    @Column(name = "HoTen", nullable = false, length = 150)
    private String hoTen;

    @Column(name = "NgaySinh")
    private LocalDate ngaySinh;

    @Column(name = "Email", length = 255)
    private String email;

    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;

    @Column(name = "DiaChi", length = 255)
    private String diaChi;

    @Column(name = "NgayVaoLam", nullable = false)
    private LocalDate ngayVaoLam;

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;

    // Getters & Setters
}
```

**Mapping**: Bảng NHANVIEN - Nhân viên thư viện

---

### 12-23. Các Entity khác:
- `TacGia.java` - Tác giả sách
- `TheLoai.java` - Thể loại/Danh mục sách
- `NhaXuatBan.java` - Nhà xuất bản
- `ViTriSach.java` - Vị trí kệ sách
- `ChiTietPhieuMuon.java` - Chi tiết mượn (1 phiếu mượn có nhiều cuốn)
- `ChiTietPhieuTra.java` - Chi tiết trả (tính tiền phạt, v.v)
- `GoiThanhVien.java` - Gói thành viên (lệ phí độc giả)
- `NhomDocGia.java` - Nhóm độc giả (Trẻ em, Học sinh, Sinh viên, v.v)
- `TrangThaiCuonSach.java` - Trạng thái cuốn sách (Có sẵn, Đang mượn, Hỏng, v.v)
- `PhuongThucThanhToan.java` - Phương thức thanh toán (Tiền mặt, Chuyển khoản, v.v)
- `ChiTietPhieuThuNo.java` - Chi tiết phiếu thu nợ
- `LichSuGoiThanhVien.java` - Lịch sử gói thành viên

---

## PHẦN V: SERVICE (8+ files) - Business Logic

### 1. AuthService.java
```java
@Service
public class AuthService {
    private final TaiKhoanRepository taiKhoanRepository;
    private final VaiTroRepository vaiTroRepository;
    private final DocGiaRepository docGiaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(
            TaiKhoanRepository taiKhoanRepository,
            VaiTroRepository vaiTroRepository,
            DocGiaRepository docGiaRepository,
            NhanVienRepository nhanVienRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService
    ) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.vaiTroRepository = vaiTroRepository;
        this.docGiaRepository = docGiaRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    // Đăng nhập
    public AuthResponse login(LoginRequest request) {
        // 1. Tìm tài khoản theo username hoặc email
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(request.getUsernameOrEmail())
                .or(() -> taiKhoanRepository.findByEmailDangNhap(request.getUsernameOrEmail()))
                .orElseThrow(() -> new RuntimeException("Tên đăng nhập/email hoặc mật khẩu không đúng"));

        // 2. Kiểm tra trạng thái tài khoản
        if (!"Hoạt động".equals(taiKhoan.getTrangThai())) {
            throw new RuntimeException("Tài khoản không ở trạng thái hoạt động");
        }

        // 3. So sánh mật khẩu (BCrypt)
        if (!passwordEncoder.matches(request.getPassword(), taiKhoan.getMatKhauHash())) {
            throw new RuntimeException("Tên đăng nhập/email hoặc mật khẩu không đúng");
        }

        // 4. Lấy thông tin vai trò
        VaiTro vaiTro = vaiTroRepository.findById(taiKhoan.getMaVaiTro())
                .orElseThrow(() -> new RuntimeException("Vai trò tài khoản không tồn tại"));

        // 5. Xây dựng AuthUser object
        AuthUser user = buildAuthUser(taiKhoan, vaiTro);

        // 6. Tạo JWT token
        String token = tokenService.generateToken(user);

        // 7. Cập nhật lần đăng nhập cuối cùng
        taiKhoan.setLanDangNhapCuoi(LocalDateTime.now());
        taiKhoanRepository.save(taiKhoan);

        // 8. Trả về response
        return new AuthResponse(
                token, "Bearer",
                user.getMaTaiKhoan(), user.getTenDangNhap(),
                user.getMaVaiTro(), user.getTenVaiTro(),
                user.getMaDocGia(), user.getMaNhanVien()
        );
    }

    // Lấy thông tin user hiện tại
    public AuthResponse me(AuthUser user) {
        return new AuthResponse(
                null, "Bearer",
                user.getMaTaiKhoan(), user.getTenDangNhap(),
                user.getMaVaiTro(), user.getTenVaiTro(),
                user.getMaDocGia(), user.getMaNhanVien()
        );
    }

    // Xây dựng AuthUser object
    private AuthUser buildAuthUser(TaiKhoan taiKhoan, VaiTro vaiTro) {
        String maDocGia = docGiaRepository.findByMaTaiKhoan(taiKhoan.getMaTaiKhoan())
                .map(DocGia::getMaDocGia)
                .orElse(null);

        String maNhanVien = nhanVienRepository.findByMaTaiKhoan(taiKhoan.getMaTaiKhoan())
                .map(NhanVien::getMaNhanVien)
                .orElse(null);

        return new AuthUser(
                taiKhoan.getMaTaiKhoan(),
                taiKhoan.getTenDangNhap(),
                taiKhoan.getMaVaiTro(),
                vaiTro.getTenVaiTro(),
                maDocGia,
                maNhanVien
        );
    }
}
```

**Chức năng**: Xử lý đăng nhập, xác thực, lấy thông tin user

---

### 2. DauSachService.java
```java
@Service
public class DauSachService {
    private final DauSachRepository dauSachRepository;
    private final NhaXuatBanRepository nhaXuatBanRepository;
    private final TacGiaRepository tacGiaRepository;
    private final TheLoaiRepository theLoaiRepository;
    private final JdbcTemplate jdbcTemplate;

    // Lấy tất cả đầu sách
    public List<DauSachResponse> getAll() {
        return dauSachRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Lấy 1 đầu sách
    public DauSachResponse getById(String maDauSach) {
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));
        return toResponse(dauSach);
    }

    // Tạo mới đầu sách
    @Transactional
    public DauSachResponse create(DauSachRequest request) {
        // Kiểm tra trùng mã
        if (dauSachRepository.existsById(request.getMaDauSach())) {
            throw new RuntimeException("Mã đầu sách đã tồn tại");
        }

        // Validate dữ liệu
        validateRequest(request, null);

        // Tạo entity mới
        DauSach dauSach = new DauSach();
        dauSach.setMaDauSach(request.getMaDauSach());
        dauSach.setMaNhaXuatBan(emptyToNull(request.getMaNhaXuatBan()));
        dauSach.setTenDauSach(request.getTenDauSach());
        dauSach.setIsbn(emptyToNull(request.getIsbn()));
        dauSach.setNamXuatBan(request.getNamXuatBan());
        dauSach.setNgonNgu(request.getNgonNgu());
        dauSach.setSoTrang(request.getSoTrang());
        dauSach.setMoTa(request.getMoTa());
        dauSach.setAnhBia(request.getAnhBia());
        dauSach.setTriGia(request.getTriGia());
        dauSach.setTrangThai("Hoạt động");

        // Lưu vào DB
        DauSach saved = dauSachRepository.saveAndFlush(dauSach);

        // Lưu tác giả (many-to-many)
        saveTacGias(saved.getMaDauSach(), request.getMaTacGias());

        // Lưu thể loại (many-to-many)
        saveTheLoais(saved.getMaDauSach(), request.getMaTheLoais());

        return toResponse(saved);
    }

    // Cập nhật đầu sách
    @Transactional
    public DauSachResponse update(String maDauSach, DauSachRequest request) {
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));

        validateRequest(request, maDauSach);

        // Cập nhật các field
        dauSach.setMaNhaXuatBan(emptyToNull(request.getMaNhaXuatBan()));
        dauSach.setTenDauSach(request.getTenDauSach());
        dauSach.setIsbn(emptyToNull(request.getIsbn()));
        dauSach.setNamXuatBan(request.getNamXuatBan());
        dauSach.setNgonNgu(request.getNgonNgu());
        dauSach.setSoTrang(request.getSoTrang());
        dauSach.setMoTa(request.getMoTa());
        dauSach.setAnhBia(request.getAnhBia());
        dauSach.setTriGia(request.getTriGia());

        DauSach updated = dauSachRepository.saveAndFlush(dauSach);
        return toResponse(updated);
    }

    // Ngừng hiển thị (soft delete)
    @Transactional
    public void disable(String maDauSach) {
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));

        dauSach.setTrangThai("Ngừng");
        dauSachRepository.saveAndFlush(dauSach);
    }

    // Chuyển Entity → Response DTO
    private DauSachResponse toResponse(DauSach dauSach) {
        DauSachResponse response = new DauSachResponse();
        response.setMaDauSach(dauSach.getMaDauSach());
        response.setTenDauSach(dauSach.getTenDauSach());
        response.setIsbn(dauSach.getIsbn());
        response.setNamXuatBan(dauSach.getNamXuatBan());
        response.setTriGia(dauSach.getTriGia());
        response.setTrangThai(dauSach.getTrangThai());
        return response;
    }

    // Các method helper khác...
}
```

**Chức năng**: CRUD + Business logic cho Đầu Sách

---

## PHẦN VI: REPOSITORY (23 files) - Data Access Layer

### Ví dụ: DauSachRepository.java
```java
public interface DauSachRepository extends JpaRepository<DauSach, String> {
    // Kế thừa từ JpaRepository:
    // - findAll(), findById(), save(), delete(), etc.

    // Custom queries
    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndMaDauSachNot(String isbn, String maDauSach);
}
```

**Chức năng**: Truy cập dữ liệu từ database - Spring Data JPA tự động implement

---

## PHẦN VII: SECURITY (4 files)

### 1. SecurityConfig.java
```java
@Configuration
public class SecurityConfig {
    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tắt CSRF (vì là API)
                .csrf(csrf -> csrf.disable())

                // Bật CORS
                .cors(Customizer.withDefaults())

                // Stateless session (JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Cấu hình quyền truy cập
                .authorizeHttpRequests(auth -> auth
                        // Preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                        // Public endpoints
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()

                        // Dev endpoints
                        .requestMatchers("/api/dev/**").hasRole("QUAN_TRI_VIEN")

                        // GET endpoints - require auth
                        .requestMatchers(HttpMethod.GET,
                                "/api/categories/**",
                                "/api/authors/**",
                                "/api/publishers/**",
                                "/api/books/**",
                                "/api/book-copies/**"
                        ).authenticated()

                        // POST/PUT/DELETE - require thủ thư or admin
                        .requestMatchers(HttpMethod.POST,
                                "/api/categories/**",
                                "/api/authors/**",
                                "/api/publishers/**",
                                "/api/books/**",
                                "/api/book-copies/**"
                        ).hasAnyRole("THU_THU", "QUAN_TRI_VIEN")

                        // Nghiệp vụ thư viện - thủ thư & admin
                        .requestMatchers(
                                "/api/readers/**",
                                "/api/loans/**",
                                "/api/returns/**",
                                "/api/payments/**",
                                "/api/reports/**"
                        ).hasAnyRole("THU_THU", "QUAN_TRI_VIEN")

                        // Các API khác
                        .anyRequest().authenticated()
                )

                // Thêm filter xác thực JWT
                .addFilterBefore(
                        tokenAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow origins
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://localhost:3000",
                "http://127.0.0.1:3000"
        ));

        // Allow methods
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // Allow headers
        configuration.setAllowedHeaders(List.of("*"));

        // Expose headers
        configuration.setExposedHeaders(List.of("Authorization"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
```

**Chức năng**: Cấu hình bảo mật, phân quyền, CORS

---

### 2. TokenService.java
```java
@Service
public class TokenService {
    private final String secret;
    private final long expirationMinutes;
    private final ObjectMapper objectMapper;

    public TokenService(
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${app.security.jwt-expiration-minutes}") long expirationMinutes,
            ObjectMapper objectMapper
    ) {
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
        this.objectMapper = objectMapper;
    }

    // Tạo JWT token
    public String generateToken(AuthUser user) {
        try {
            Map<String, Object> header = Map.of(
                    "alg", "HS256",
                    "typ", "JWT"
            );

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", user.getTenDangNhap());
            payload.put("maTaiKhoan", user.getMaTaiKhoan());
            payload.put("maVaiTro", user.getMaVaiTro());
            payload.put("tenVaiTro", user.getTenVaiTro());
            payload.put("maDocGia", user.getMaDocGia());
            payload.put("maNhanVien", user.getMaNhanVien());
            payload.put("exp", Instant.now().plusSeconds(expirationMinutes * 60).getEpochSecond());

            String headerPart = base64UrlEncode(objectMapper.writeValueAsBytes(header));
            String payloadPart = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
            String unsignedToken = headerPart + "." + payloadPart;
            String signature = sign(unsignedToken);

            return unsignedToken + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo token");
        }
    }

    // Xác minh & parse token
    public AuthUser parseToken(String token) {
        try {
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                throw new RuntimeException("Token không hợp lệ");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);

            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8)
            )) {
                throw new RuntimeException("Chữ ký token không hợp lệ");
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(
                    payloadBytes,
                    new TypeReference<Map<String, Object>>() {}
            );

            long exp = ((Number) payload.get("exp")).longValue();

            if (Instant.now().getEpochSecond() > exp) {
                throw new RuntimeException("Token đã hết hạn");
            }

            return new AuthUser(
                    (String) payload.get("maTaiKhoan"),
                    (String) payload.get("sub"),
                    (String) payload.get("maVaiTro"),
                    (String) payload.get("tenVaiTro"),
                    (String) payload.get("maDocGia"),
                    (String) payload.get("maNhanVien")
            );
        } catch (Exception e) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        mac.init(key);
        return base64UrlEncode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
```

**Chức năng**: Tạo & xác minh JWT token (custom implementation)

---

### 3. TokenAuthenticationFilter.java
```java
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService tokenService;

    public TokenAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);

            try {
                AuthUser user = tokenService.parseToken(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(new SimpleGrantedAuthority(user.getRoleAuthority()))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (RuntimeException ex) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

**Chức năng**: Intercept request, kiểm tra JWT token, set SecurityContext

---

### 4. AuthUser.java
```java
public class AuthUser {
    private final String maTaiKhoan;
    private final String tenDangNhap;
    private final String maVaiTro;
    private final String tenVaiTro;
    private final String maDocGia;
    private final String maNhanVien;

    public AuthUser(
            String maTaiKhoan, String tenDangNhap,
            String maVaiTro, String tenVaiTro,
            String maDocGia, String maNhanVien
    ) {
        this.maTaiKhoan = maTaiKhoan;
        this.tenDangNhap = tenDangNhap;
        this.maVaiTro = maVaiTro;
        this.tenVaiTro = tenVaiTro;
        this.maDocGia = maDocGia;
        this.maNhanVien = maNhanVien;
    }

    public String getRoleAuthority() {
        if ("VT_ADMIN".equals(maVaiTro)) {
            return "ROLE_QUAN_TRI_VIEN";
        }
        if ("VT_THU_THU".equals(maVaiTro)) {
            return "ROLE_THU_THU";
        }
        if ("VT_DOC_GIA".equals(maVaiTro)) {
            return "ROLE_DOC_GIA";
        }
        return "ROLE_UNKNOWN";
    }

    // Getters...
}
```

**Chức năng**: Đối tượng user đã xác thực

---

## PHẦN VIII: EXCEPTION HANDLING (4 files)

### 1. GlobalExceptionHandler.java
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "BUSINESS_ERROR",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ErrorResponse response = new ErrorResponse(
                "Dữ liệu gửi lên không hợp lệ",
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Bạn không có quyền truy cập tài nguyên này",
                request
        );
    }
}
```

**Chức năng**: Bắt tất cả exception, trả về error response thống nhất

---

### 2. ErrorResponse.java
```java
public class ErrorResponse {
    private final boolean success;
    private final String message;
    private final String errorCode;
    private final int status;
    private final String path;
    private final LocalDateTime timestamp;
    private final Map<String, String> fieldErrors;

    public ErrorResponse(
            String message, String errorCode,
            int status, String path
    ) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = null;
    }

    public ErrorResponse(
            String message, String errorCode,
            int status, String path,
            Map<String, String> fieldErrors
    ) {
        this.success = false;
        this.message = message;
        this.errorCode = errorCode;
        this.status = status;
        this.path = path;
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = fieldErrors;
    }

    // Getters...
}
```

**Mục đích**: Cấu trúc lỗi thống nhất

---

### 3. BusinessException.java
```java
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

---

### 4. ResourceNotFoundException.java
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

---

## PHẦN IX: APPLICATION PROPERTIES

### application.properties
```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=QuanLyThuVien;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=huy123567908
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
spring.jpa.hibernate.naming.implicit-strategy=org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl

# Timezone
spring.jackson.time-zone=Asia/Ho_Chi_Minh

# JWT Security
app.security.jwt-secret=library-desktop-app-secret-key-change-this-later
app.security.jwt-expiration-minutes=480
```

---

## PHẦN X: POM.XML - Dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.14</version>
    </parent>

    <groupId>com.library</groupId>
    <artifactId>backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>backend</name>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Spring Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Spring Web (REST API) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- SQL Server Driver -->
        <dependency>
            <groupId>com.microsoft.sqlserver</groupId>
            <artifactId>mssql-jdbc</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## PHẦN XI: REQUEST/RESPONSE FLOW

### Ví dụ: Tạo mới Đầu Sách

```
1. Client (React)
   POST /api/books
   Headers: Authorization: Bearer <JWT_TOKEN>
   Body: {
     "maDauSach": "SACH001",
     "tenDauSach": "Lập trình Java",
     "namXuatBan": 2024,
     "triGia": 150000,
     "maTacGias": ["TG001"],
     "maTheLoais": ["TL001"]
   }

2. TokenAuthenticationFilter
   ✓ Extract token từ header
   ✓ Validate token signature & expiration
   ✓ Parse token → AuthUser object
   ✓ Set SecurityContext

3. Spring Security Authorization
   ✓ Check role (THU_THU hoặc QUAN_TRI_VIEN)

4. DauSachController.create()
   ✓ Validate @Valid @RequestBody (DauSachRequest)
   ✓ Call dauSachService.create()

5. DauSachService.create()
   ✓ Check mã trùng
   ✓ Validate business logic
   ✓ Save entity to database
   ✓ Save relationships (tác giả, thể loại)

6. DauSachRepository.save()
   → Hibernate/JPA execute INSERT SQL

7. Return DauSachResponse
   {
     "maDauSach": "SACH001",
     "tenDauSach": "Lập trình Java",
     "namXuatBan": 2024,
     "triGia": 150000,
     "trangThai": "Hoạt động"
   }

8. Client receives response
```

### Ví dụ: Lỗi Validation

```
POST /api/books
Body: {
  "maDauSach": "",  // Lỗi: empty
  "tenDauSach": "Lập trình Java",
  "namXuatBan": 2024,
  "triGia": -1000   // Lỗi: âm
}

Response (400 Bad Request):
{
  "success": false,
  "message": "Dữ liệu gửi lên không hợp lệ",
  "errorCode": "VALIDATION_ERROR",
  "status": 400,
  "path": "/api/books",
  "timestamp": "2024-06-01T10:30:00",
  "fieldErrors": {
    "maDauSach": "Mã đầu sách không được để trống",
    "triGia": "Trị giá không được âm"
  }
}
```

---

## PHẦN XII: TÓM TẮT KIẾN TRÚC

| Lớp | Số file | Chức năng |
|-----|---------|---------|
| **Controller** | 14 | REST API endpoints - nhận request, gọi Service, trả response |
| **Service** | 8+ | Business logic - validate, process, access repository |
| **Repository** | 23 | Data access layer - JpaRepository (auto-impl queries) |
| **Entity** | 23 | JPA entities - mapping bảng database |
| **DTO** | 27 | Request/Response objects - transfer dữ liệu |
| **Security** | 4 | JWT token, authentication, authorization |
| **Exception** | 4 | Global exception handling, error response |

**Lưu ý**: Service classes trong DTO folder (DocGiaService, ThanhToanService, TraSachService) có thể là helpers được lưu ở vị trí nhầm.

---

## PHẦN XIII: CHẠY BACKEND

```bash
# Build
mvn clean package

# Run
java -jar target/backend-0.0.1-SNAPSHOT.jar

# Hoặc chạy từ IDE
# Chạy BackendApplication.java
```

Server sẽ chạy trên: `http://localhost:8080`

---

**Tài liệu hoàn chỉnh!** 📚
