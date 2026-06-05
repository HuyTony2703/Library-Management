# Mô Tả Chi Tiết Cấu Trúc Backend - Hoàn Chỉnh

**Dự án**: Hệ thống quản lý thư viện desktop app
**Framework**: Spring Boot 3.5.14  
**Database**: SQL Server  
**Kiến trúc**: 3-tier layered (Controller → Service → Repository)  
**Authentication**: JWT Token  

---

## I. CẤU TRÚC THƯ MỤC HOÀN CHỈNH

```
backend/
├── pom.xml                              # Maven configuration - Spring Boot dependencies
├── mvnw, mvnw.cmd                       # Maven wrapper scripts
├── HELP.md                              # Maven help file
├── .gitignore, .gitattributes           # Git configuration
├── .idea/                               # IntelliJ IDEA workspace settings
├── .mvn/wrapper/                        # Maven wrapper configuration
├── src/
│   ├── main/
│   │   ├── java/com/library/backend/
│   │   │   ├── BackendApplication.java                    # Spring Boot entry point
│   │   │   ├── controller/                                # 14 REST API controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── DauSachController.java
│   │   │   │   ├── CuonSachController.java
│   │   │   │   ├── DocGiaController.java
│   │   │   │   ├── MuonSachController.java
│   │   │   │   ├── TraSachController.java
│   │   │   │   ├── ThanhToanController.java
│   │   │   │   ├── TacGiaController.java
│   │   │   │   ├── TheLoaiController.java
│   │   │   │   ├── NhaXuatBanController.java
│   │   │   │   ├── BaoCaoController.java
│   │   │   │   ├── HealthController.java
│   │   │   │   ├── ReaderOptionController.java
│   │   │   │   └── DevPasswordController.java
│   │   │   ├── dto/                                       # 27 Request/Response classes
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── DauSachRequest.java
│   │   │   │   ├── DauSachResponse.java
│   │   │   │   ├── CuonSachRequest.java
│   │   │   │   ├── CuonSachResponse.java
│   │   │   │   ├── DocGiaRequest.java
│   │   │   │   ├── DocGiaResponse.java
│   │   │   │   ├── MuonSachRequest.java
│   │   │   │   ├── MuonSachResponse.java
│   │   │   │   ├── TraSachRequest.java
│   │   │   │   ├── TraSachResponse.java
│   │   │   │   ├── PhieuThuRequest.java
│   │   │   │   ├── PhieuThuResponse.java
│   │   │   │   ├── KhoanNoResponse.java
│   │   │   │   ├── TacGiaRequest.java
│   │   │   │   ├── TacGiaResponse.java
│   │   │   │   ├── TheLoaiRequest.java
│   │   │   │   ├── TheLoaiResponse.java
│   │   │   │   ├── NhaXuatBanRequest.java
│   │   │   │   ├── NhaXuatBanResponse.java
│   │   │   │   ├── BaoCaoMuonTheLoaiResponse.java
│   │   │   │   ├── BaoCaoTraTreResponse.java
│   │   │   │   ├── SachDangMuonReportResponse.java
│   │   │   │   ├── TongNoDocGiaReportResponse.java
│   │   │   │   ├── DocGiaService.java (Service in DTO folder)
│   │   │   │   ├── ThanhToanService.java (Service in DTO folder)
│   │   │   │   └── TraSachService.java (Service in DTO folder)
│   │   │   ├── entity/                                    # 23 JPA Entity classes
│   │   │   │   ├── DauSach.java
│   │   │   │   ├── CuonSach.java
│   │   │   │   ├── DocGia.java
│   │   │   │   ├── NhaXuatBan.java
│   │   │   │   ├── TacGia.java
│   │   │   │   ├── TheLoai.java
│   │   │   │   ├── PhieuMuon.java
│   │   │   │   ├── PhieuTra.java
│   │   │   │   ├── PhieuThu.java
│   │   │   │   ├── KhoanNo.java
│   │   │   │   ├── TaiKhoan.java
│   │   │   │   ├── VaiTro.java
│   │   │   │   ├── ChiNhanh.java
│   │   │   │   ├── ViTriSach.java
│   │   │   │   ├── NhanVien.java
│   │   │   │   ├── GoiThanhVien.java
│   │   │   │   ├── NhomDocGia.java
│   │   │   │   ├── TrangThaiCuonSach.java
│   │   │   │   ├── PhuongThucThanhToan.java
│   │   │   │   ├── ChiTietPhieuMuon.java
│   │   │   │   ├── ChiTietPhieuTra.java
│   │   │   │   ├── ChiTietPhieuThuNo.java
│   │   │   │   └── LichSuGoiThanhVien.java
│   │   │   ├── service/                                   # 8+ Service classes
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── DauSachService.java
│   │   │   │   ├── CuonSachService.java
│   │   │   │   ├── DocGiaService.java
│   │   │   │   ├── MuonSachService.java
│   │   │   │   ├── TraSachService.java
│   │   │   │   ├── TheLoaiService.java
│   │   │   │   ├── TacGiaService.java
│   │   │   │   ├── NhaXuatBanService.java
│   │   │   │   ├── ThanhToanService.java
│   │   │   │   └── BaoCaoService.java
│   │   │   ├── repository/                                # 23 JPA Repository interfaces
│   │   │   │   ├── DauSachRepository.java
│   │   │   │   ├── CuonSachRepository.java
│   │   │   │   ├── DocGiaRepository.java
│   │   │   │   ├── NhaXuatBanRepository.java
│   │   │   │   ├── TacGiaRepository.java
│   │   │   │   ├── TheLoaiRepository.java
│   │   │   │   ├── PhieuMuonRepository.java
│   │   │   │   ├── PhieuTraRepository.java
│   │   │   │   ├── PhieuThuRepository.java
│   │   │   │   ├── KhoanNoRepository.java
│   │   │   │   ├── TaiKhoanRepository.java
│   │   │   │   ├── VaiTroRepository.java
│   │   │   │   ├── ChiNhanhRepository.java
│   │   │   │   ├── ViTriSachRepository.java
│   │   │   │   ├── NhanVienRepository.java
│   │   │   │   ├── GoiThanhVienRepository.java
│   │   │   │   ├── NhomDocGiaRepository.java
│   │   │   │   ├── TrangThaiCuonSachRepository.java
│   │   │   │   ├── PhuongThucThanhToanRepository.java
│   │   │   │   ├── ChiTietPhieuMuonRepository.java
│   │   │   │   ├── ChiTietPhieuTraRepository.java
│   │   │   │   ├── ChiTietPhieuThuNoRepository.java
│   │   │   │   └── LichSuGoiThanhVienRepository.java
│   │   │   ├── security/                                  # 4 Security classes
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── TokenService.java
│   │   │   │   ├── TokenAuthenticationFilter.java
│   │   │   │   └── AuthUser.java
│   │   │   └── exception/                                 # 4 Exception handling classes
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── ErrorResponse.java
│   │   │       ├── BusinessException.java
│   │   │       └── ResourceNotFoundException.java
│   │   └── resources/
│   │       ├── application.properties                     # Database & JWT configuration
│   │       ├── static/                                    # Static files (images, CSS, JS)
│   │       └── templates/                                 # HTML templates
│   └── test/java/...                                     # Unit tests
└── target/                                               # Compiled JAR/classes
    ├── classes/
    ├── generated-sources/
    └── ...
```

---

## II. CHI TIẾT TỪng FOLDER VÀ FILE

### 1. **BackendApplication.java** (Root package)
- **Vị trí**: `src/main/java/com/library/backend/BackendApplication.java`
- **Mục đích**: Điểm vào của ứng dụng Spring Boot
- **Code**:
```java
@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
```
- **Chức năng**: Khởi động ứng dụng Spring Boot khi chạy

---

### 2. **CONTROLLER** Folder (`controller/`)
**Mục đích**: Xử lý HTTP requests và trả về responses

#### 14 Controller files:

| File | Mục đích | Endpoints |
|------|---------|-----------|
| **AuthController.java** | Xử lý đăng nhập & xác thực | POST `/api/auth/login`, GET `/api/auth/me` |
| **DauSachController.java** | Quản lý đầu sách (sách) | GET, POST, PUT, DELETE `/api/books` |
| **CuonSachController.java** | Quản lý cuốn sách (bản vật lý) | GET, POST, PUT `/api/volumes` |
| **DocGiaController.java** | Quản lý độc giả | GET, POST, PUT `/api/readers` |
| **MuonSachController.java** | Quản lý phiếu mượn sách | GET, POST, PUT `/api/borrowings` |
| **TraSachController.java** | Quản lý phiếu trả sách | GET, POST `/api/returns` |
| **ThanhToanController.java** | Quản lý thanh toán | GET, POST `/api/payments` |
| **TacGiaController.java** | Quản lý tác giả | GET, POST, PUT `/api/authors` |
| **TheLoaiController.java** | Quản lý thể loại sách | GET, POST, PUT `/api/categories` |
| **NhaXuatBanController.java** | Quản lý nhà xuất bản | GET, POST, PUT `/api/publishers` |
| **BaoCaoController.java** | Báo cáo thống kê | GET `/api/reports` |
| **HealthController.java** | Health check | GET `/api/health` |
| **ReaderOptionController.java** | Cấu hình độc giả | GET, POST `/api/reader-options` |
| **DevPasswordController.java** | Dev endpoint (dev only) | POST `/api/dev/password` |

**Ví dụ chi tiết - DauSachController**:
```java
@RestController
@RequestMapping("/api/books")
public class DauSachController {
    private final DauSachService dauSachService;

    @GetMapping
    public List<DauSachResponse> getAll() {
        return dauSachService.getAll();  // Lấy tất cả đầu sách
    }

    @GetMapping("/{maDauSach}")
    public DauSachResponse getById(@PathVariable String maDauSach) {
        return dauSachService.getById(maDauSach);  // Lấy 1 đầu sách
    }

    @PostMapping
    public DauSachResponse create(@Valid @RequestBody DauSachRequest request) {
        return dauSachService.create(request);  // Thêm mới đầu sách
    }

    @PutMapping("/{maDauSach}")
    public DauSachResponse update(
            @PathVariable String maDauSach,
            @Valid @RequestBody DauSachRequest request
    ) {
        return dauSachService.update(maDauSach, request);  // Cập nhật đầu sách
    }

    @DeleteMapping("/{maDauSach}")
    public String disable(@PathVariable String maDauSach) {
        dauSachService.disable(maDauSach);  // Ngừng hiển thị
        return "Ngừng hiển thị đầu sách thành công";
    }
}
```

---

### 3. **DTO** Folder (`dto/`)
**Mục đích**: Chứa Request/Response objects để trao đổi dữ liệu

#### 27 DTO files:

| File | Loại | Mục đích |
|------|------|---------|
| **LoginRequest.java** | Request | Chứa username + password đăng nhập |
| **AuthResponse.java** | Response | Trả về token & thông tin user |
| **DauSachRequest.java** | Request | Dữ liệu tạo/cập nhật đầu sách |
| **DauSachResponse.java** | Response | Dữ liệu trả về của đầu sách |
| **CuonSachRequest.java** | Request | Dữ liệu tạo/cập nhật cuốn sách |
| **CuonSachResponse.java** | Response | Dữ liệu trả về của cuốn sách |
| **DocGiaRequest.java** | Request | Dữ liệu tạo/cập nhật độc giả |
| **DocGiaResponse.java** | Response | Dữ liệu trả về của độc giả |
| **MuonSachRequest.java** | Request | Dữ liệu tạo phiếu mượn |
| **MuonSachResponse.java** | Response | Dữ liệu trả về của phiếu mượn |
| **TraSachRequest.java** | Request | Dữ liệu tạo phiếu trả |
| **TraSachResponse.java** | Response | Dữ liệu trả về của phiếu trả |
| **ThanhToanService.java** | Service | Service xử lý thanh toán |
| **TraSachService.java** | Service | Service xử lý trả sách |
| **DocGiaService.java** | Service | Service xử lý độc giả |
| **PhieuThuRequest.java** | Request | Dữ liệu phiếu thu tiền |
| **PhieuThuResponse.java** | Response | Dữ liệu trả về phiếu thu |
| Các Response khác | Response | Dữ liệu báo cáo: BaoCaoMuonTheLoaiResponse, BaoCaoTraTreResponse, v.v |

**Ví dụ chi tiết - DauSachRequest**:
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

    // Getters & Setters...
}
```
**Chú ý**: Các @Annotation là validation rules cho dữ liệu đầu vào

---

### 4. **ENTITY** Folder (`entity/`)
**Mục đích**: Chứa các class đại diện cho bảng trong database (JPA Entities)

#### 23 Entity files:

| File | Đại Diện Cho | Các Trường Chính |
|------|-------------|------------------|
| **DauSach.java** | Bảng DAUSACH | maDauSach, tenDauSach, maNhaXuatBan, ISBN, namXuatBan, triGia, trangThai |
| **CuonSach.java** | Bảng CUONSACH | maCuonSach, maDauSach, maChiNhanh, maViTri, maTrangThai, ngayNhapSach |
| **DocGia.java** | Bảng DOCGIA | maDocGia, hoTen, ngaySinh, diaChi, email, soDienThoai, ngayLapThe, ngayHetHanThe |
| **NhaXuatBan.java** | Bảng NHAXUATBAN | maNhaXuatBan, tenNhaXuatBan, diaChi, soDienThoai, email |
| **TacGia.java** | Bảng TACGIA | maTacGia, tenTacGia, ngaySinh, quocTich |
| **TheLoai.java** | Bảng THELOAI | maTheLoai, tenTheLoai |
| **PhieuMuon.java** | Bảng PHIEUMUON | maPhieuMuon, maDocGia, ngayMuon, ngayHetHan, trangThai |
| **PhieuTra.java** | Bảng PHIEUTRA | maPhieuTra, maPhieuMuon, ngayTra, trangThai |
| **PhieuThu.java** | Bảng PHIEUTHU | maPhieuThu, maDocGia, soTienThu, ngayThu |
| **KhoanNo.java** | Bảng KHOANNO | maKhoanNo, maDocGia, soTien, ngayPhatSinh, trangThai |
| **TaiKhoan.java** | Bảng TAIKHOAN | maTaiKhoan, tenDangNhap, matKhau, maVaiTro |
| **VaiTro.java** | Bảng VAITRO | maVaiTro, tenVaiTro |
| **ChiNhanh.java** | Bảng CHINHANH | maChiNhanh, tenChiNhanh, diaChi |
| **ViTriSach.java** | Bảng VITRISACH | maViTri, tenViTri, maChiNhanh |
| **NhanVien.java** | Bảng NHANVIEN | maNhanVien, hoTen, soDienThoai, diaChi, maTaiKhoan |
| **GoiThanhVien.java** | Bảng GOITHANHVIEN | maGoi, tenGoi, hoanGia, ngayHetHan |
| **LichSuGoiThanhVien.java** | Bảng LICHSUGOITHANHVIEN | maLichSu, maDocGia, maGoi, ngayApDung |
| **ChiTietPhieuMuon.java** | Bảng CHITIETPHIEUMUON | maChiTiet, maPhieuMuon, maCuonSach |
| **ChiTietPhieuTra.java** | Bảng CHITIETPHIETRA | maChiTiet, maPhieuTra, maCuonSach |
| **ChiTietPhieuThuNo.java** | Bảng CHITIETPHIEUTHU | maChiTiet, maPhieuThu, maKhoanNo |
| **PhuongThucThanhToan.java** | Bảng PHUONGTHUCTHANHTOAN | maPhuongThuc, tenPhuongThuc |
| **TrangThaiCuonSach.java** | Bảng TRANGTHAICUONSACH | maTrangThai, tenTrangThai |
| **NhomDocGia.java** | Bảng NHOMDOCGIA | maNhomDocGia, tenNhomDocGia |

**Ví dụ chi tiết - DauSach Entity**:
```java
@Entity
@Table(name = "DAUSACH")
public class DauSach {
    @Id
    @Column(name = "MaDauSach", length = 30)
    private String maDauSach;  // Khóa chính

    @Column(name = "MaNhaXuatBan", length = 30)
    private String maNhaXuatBan;  // Liên kết tới NhaXuatBan

    @Column(name = "TenDauSach", nullable = false, length = 200)
    private String tenDauSach;  // Tên sách

    @Column(name = "ISBN", length = 30)
    private String isbn;  // Mã ISBN

    @Column(name = "NamXuatBan", nullable = false)
    private Integer namXuatBan;  // Năm xuất bản

    @Column(name = "NgonNgu", length = 50)
    private String ngonNgu;  // Ngôn ngữ

    @Column(name = "SoTrang")
    private Integer soTrang;  // Số trang

    @Column(name = "MoTa", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;  // Mô tả

    @Column(name = "AnhBia", length = 500)
    private String anhBia;  // URL ảnh bìa

    @Column(name = "TriGia", nullable = false, precision = 18, scale = 2)
    private BigDecimal triGia;  // Giá trị sách

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;  // Trạng thái (Hoạt động, Ngừng)

    // Getters & Setters...
}
```

**Ví dụ chi tiết - CuonSach Entity**:
```java
@Entity
@Table(name = "CUONSACH")
public class CuonSach {
    @Id
    @Column(name = "MaCuonSach", length = 40)
    private String maCuonSach;  // Khóa chính (ID cuốn sách)

    @Column(name = "MaDauSach", nullable = false, length = 30)
    private String maDauSach;  // FK: Liên kết tới DauSach

    @Column(name = "MaChiNhanh", nullable = false, length = 30)
    private String maChiNhanh;  // FK: Chi nhánh thư viện

    @Column(name = "MaViTri", nullable = false, length = 30)
    private String maViTri;  // FK: Vị trí kệ sách

    @Column(name = "MaTrangThai", nullable = false, length = 30)
    private String maTrangThai;  // FK: Trạng thái (Có sẵn, Đang mượn, Hỏng, v.v)

    @Column(name = "MaVach", length = 100)
    private String maVach;  // Mã vạch

    @Column(name = "MaQRCode", length = 100)
    private String maQrCode;  // Mã QR code

    @Column(name = "NgayNhapSach", nullable = false)
    private LocalDate ngayNhapSach;  // Ngày nhập kho

    @Column(name = "GhiChu", length = 255)
    private String ghiChu;  // Ghi chú

    // Getters & Setters...
}
```

**Ví dụ chi tiết - DocGia Entity**:
```java
@Entity
@Table(name = "DOCGIA")
public class DocGia {
    @Id
    @Column(name = "MaDocGia", length = 30)
    private String maDocGia;  // Khóa chính

    @Column(name = "MaTaiKhoan", nullable = false, length = 30)
    private String maTaiKhoan;  // FK: Tài khoản login

    @Column(name = "MaNhomDocGia", nullable = false, length = 30)
    private String maNhomDocGia;  // FK: Nhóm độc giả (Trẻ em, Học sinh, v.v)

    @Column(name = "HoTen", nullable = false, length = 150)
    private String hoTen;  // Họ tên độc giả

    @Column(name = "NgaySinh", nullable = false)
    private LocalDate ngaySinh;  // Ngày sinh

    @Column(name = "DiaChi", length = 255)
    private String diaChi;  // Địa chỉ

    @Column(name = "Email", nullable = false, length = 255)
    private String email;  // Email

    @Column(name = "SoDienThoai", length = 20)
    private String soDienThoai;  // Số điện thoại

    @Column(name = "NgayLapThe", nullable = false)
    private LocalDate ngayLapThe;  // Ngày lập thẻ

    @Column(name = "NgayHetHanThe", nullable = false)
    private LocalDate ngayHetHanThe;  // Ngày hết hạn thẻ

    @Column(name = "TrangThai", nullable = false, length = 30)
    private String trangThai;  // Trạng thái (Hoạt động, Ngừng, v.v)

    // Getters & Setters...
}
```

---

### 5. **SERVICE** Folder (`service/`)
**Mục đích**: Chứa logic xử lý business logic (tính toán, kiểm tra, gọi repository)

#### 8 Service files:

| File | Chức Năng |
|------|---------|
| **AuthService.java** | Xử lý đăng nhập, xác thực, JWT |
| **DauSachService.java** | CRUD + Business logic cho đầu sách |
| **CuonSachService.java** | CRUD + Logic cho cuốn sách |
| **DocGiaService.java** | CRUD + Logic cho độc giả |
| **MuonSachService.java** | Logic mượn sách, kiểm tra hạn mượn |
| **TheLoaiService.java** | CRUD thể loại sách |
| **NhaXuatBanService.java** | CRUD nhà xuất bản |
| **TacGiaService.java** | CRUD tác giả |
| **BaoCaoService.java** | Tính toán & trả về các báo cáo thống kê |

**Ví dụ chi tiết - DauSachService**:
```java
@Service
public class DauSachService {
    private final DauSachRepository dauSachRepository;
    private final NhaXuatBanRepository nhaXuatBanRepository;
    private final TacGiaRepository tacGiaRepository;
    private final TheLoaiRepository theLoaiRepository;
    private final JdbcTemplate jdbcTemplate;

    // Constructor injection của các dependencies

    // Lấy tất cả đầu sách
    public List<DauSachResponse> getAll() {
        return dauSachRepository.findAll()
                .stream()
                .map(this::toResponse)  // Chuyển Entity -> Response
                .toList();
    }

    // Lấy 1 đầu sách theo mã
    public DauSachResponse getById(String maDauSach) {
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));
        return toResponse(dauSach);
    }

    // Tạo mới đầu sách
    @Transactional
    public DauSachResponse create(DauSachRequest request) {
        // Kiểm tra xem mã đã tồn tại chưa
        if (dauSachRepository.existsById(request.getMaDauSach())) {
            throw new RuntimeException("Mã đầu sách đã tồn tại");
        }

        validateRequest(request, null);  // Validate request

        // Tạo entity mới
        DauSach dauSach = new DauSach();
        dauSach.setMaDauSach(request.getMaDauSach());
        dauSach.setMaNhaXuatBan(request.getMaNhaXuatBan());
        dauSach.setTenDauSach(request.getTenDauSach());
        dauSach.setIsbn(request.getIsbn());
        dauSach.setNamXuatBan(request.getNamXuatBan());
        dauSach.setTriGia(request.getTriGia());
        dauSach.setTrangThai("Hoạt động");

        // Lưu vào database
        DauSach saved = dauSachRepository.saveAndFlush(dauSach);

        // Lưu tác giả & thể loại (mối quan hệ many-to-many)
        saveTacGias(saved.getMaDauSach(), request.getMaTacGias());
        saveTheLoais(saved.getMaDauSach(), request.getMaTheLoais());

        return toResponse(saved);
    }

    // Cập nhật đầu sách
    @Transactional
    public DauSachResponse update(String maDauSach, DauSachRequest request) {
        // Tìm entity cũ
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));

        validateRequest(request, maDauSach);

        // Cập nhật các trường
        dauSach.setMaNhaXuatBan(request.getMaNhaXuatBan());
        dauSach.setTenDauSach(request.getTenDauSach());
        // ... cập nhật các trường khác

        DauSach updated = dauSachRepository.saveAndFlush(dauSach);
        return toResponse(updated);
    }

    // Ngừng hiển thị đầu sách (soft delete)
    @Transactional
    public void disable(String maDauSach) {
        DauSach dauSach = dauSachRepository.findById(maDauSach)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đầu sách"));
        
        dauSach.setTrangThai("Ngừng");  // Thay đổi trạng thái
        dauSachRepository.saveAndFlush(dauSach);
    }

    // Chuyển Entity -> Response DTO
    private DauSachResponse toResponse(DauSach dauSach) {
        DauSachResponse response = new DauSachResponse();
        response.setMaDauSach(dauSach.getMaDauSach());
        response.setTenDauSach(dauSach.getTenDauSach());
        // ... map các trường khác
        return response;
    }
}
```

---

### 6. **REPOSITORY** Folder (`repository/`)
**Mục đích**: Lớp truy cập dữ liệu (DAO - Data Access Object)

#### 23 Repository files:
Mỗi Repository là interface kế thừa `JpaRepository<Entity, KeyType>`, cho phép thực hiện các thao tác CRUD cơ bản và custom queries

**Ví dụ chi tiết - DauSachRepository**:
```java
public interface DauSachRepository extends JpaRepository<DauSach, String> {
    // Kế thừa từ JpaRepository: findAll(), findById(), save(), delete(), v.v

    // Custom queries
    boolean existsByIsbn(String isbn);  // Kiểm tra ISBN có tồn tại
    
    boolean existsByIsbnAndMaDauSachNot(String isbn, String maDauSach);  
    // Kiểm tra ISBN có tồn tại nhưng không phải của cuốn này
}
```

**Danh sách tất cả Repository**:
- DauSachRepository
- CuonSachRepository
- DocGiaRepository
- NhaXuatBanRepository
- TacGiaRepository
- TheLoaiRepository
- PhieuMuonRepository
- PhieuTraRepository
- PhieuThuRepository
- KhoanNoRepository
- TaiKhoanRepository
- VaiTroRepository
- ChiNhanhRepository
- ViTriSachRepository
- NhanVienRepository
- GoiThanhVienRepository
- LichSuGoiThanhVienRepository
- ChiTietPhieuMuonRepository
- ChiTietPhieuTraRepository
- ChiTietPhieuThuNoRepository
- PhuongThucThanhToanRepository
- TrangThaiCuonSachRepository
- NhomDocGiaRepository

---

### 7. **SECURITY** Folder (`security/`)
**Mục đích**: Xử lý bảo mật, xác thực, phân quyền

#### 4 Security files:

**1. SecurityConfig.java** - Cấu hình Spring Security
```java
@Configuration
public class SecurityConfig {
    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Tắt CSRF vì là API
                .cors(Customizer.withDefaults())  // Bật CORS
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Stateless (JWT)
                )
                .authorizeHttpRequests(auth -> auth
                        // Cho phép React gửi preflight request
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

                        // API công khai
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()

                        // Dev endpoint: chỉ admin được dùng
                        .requestMatchers("/api/dev/**").hasRole("QUAN_TRI_VIEN")

                        // Các API khác cần xác thực
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Mã hóa mật khẩu
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Cấu hình CORS cho React frontend
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));  // React dev server
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

**2. TokenService.java** - Xử lý JWT token
```java
@Service
public class TokenService {
    @Value("${app.security.jwt-secret}")
    private String jwtSecret;

    @Value("${app.security.jwt-expiration-minutes}")
    private int jwtExpirationMinutes;

    // Tạo token JWT
    public String generateToken(String tenDangNhap, String maVaiTro) {
        // Encode + sign token bằng secret key
    }

    // Xác minh token
    public boolean validateToken(String token) {
        // Kiểm tra signature & expiration
    }

    // Lấy username từ token
    public String getUsernameFromToken(String token) {
        // Decode token & extract username
    }
}
```

**3. TokenAuthenticationFilter.java** - Bộ lọc xác thực
```java
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractTokenFromHeader(request);
        if (token != null && tokenService.validateToken(token)) {
            // Token hợp lệ -> xác thực request
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    authUser, null, authUser.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
```

**4. AuthUser.java** - Thông tin user đã xác thực
```java
public class AuthUser extends User {  // Kế thừa Spring UserDetails
    private String maTaiKhoan;
    private String maVaiTro;
    private String tenVaiTro;

    // Chứa thông tin user hiện tại sau khi đăng nhập
    // Được sử dụng trong @Authentication context
}
```

---

### 8. **EXCEPTION** Folder (`exception/`)
**Mục đích**: Xử lý các exception của ứng dụng

#### 4 Exception files:

**1. GlobalExceptionHandler.java** - Bắt tất cả exception
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
            MethodArgumentNotValidException ex
    ) {
        // Trả về lỗi validation từ DTO
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        // Trả về errors cho client
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Bạn không có quyền truy cập tài nguyên này",
                null
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Xác thực không thành công",
                null
        );
    }
}
```

**2. ErrorResponse.java** - Cấu trúc lỗi trả về
```java
public class ErrorResponse {
    private String code;  // Mã lỗi (BUSINESS_ERROR, NOT_FOUND, v.v)
    private String message;  // Thông báo lỗi
    private String path;  // Đường dẫn request
    private LocalDateTime timestamp;  // Thời gian lỗi xảy ra
    private Map<String, String> validationErrors;  // Nếu là validation error
}
```

**3. BusinessException.java**
```java
public class BusinessException extends RuntimeException {
    // Exception cho các lỗi logic business
    public BusinessException(String message) {
        super(message);
    }
}
```

**4. ResourceNotFoundException.java**
```java
public class ResourceNotFoundException extends RuntimeException {
    // Exception khi không tìm thấy resource
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

---

### 9. **application.properties** - Cấu hình ứng dụng

```properties
# Port chạy server
server.port=8080

# Database (SQL Server)
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=QuanLyThuVien;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=huy123567908
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate  # Chỉ validate schema, không tạo table
spring.jpa.show-sql=true  # In SQL log ra console
spring.jpa.properties.hibernate.format_sql=true  # Format SQL đẹp

# Naming strategy (tên column không đổi case)
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
spring.jpa.hibernate.naming.implicit-strategy=org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl

# Timezone (múi giờ Việt Nam)
spring.jackson.time-zone=Asia/Ho_Chi_Minh

# JWT Security
app.security.jwt-secret=library-desktop-app-secret-key-change-this-later
app.security.jwt-expiration-minutes=480  # Token hết hạn sau 8 giờ
```

---

### 10. **pom.xml** - Maven Dependencies

```xml
<dependencies>
    <!-- Spring Boot Data JPA (ORM framework) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Validation (kiểm tra DTO) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Spring Security (xác thực & phân quyền) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Spring Web (REST API) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- SQL Server JDBC Driver -->
    <dependency>
        <groupId>com.microsoft.sqlserver</groupId>
        <artifactId>mssql-jdbc</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok (Giảm boilerplate code) -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## III. FLOW XỬ LÝ REQUEST ĐIỂN HÌNH

### Ví dụ: Tạo mới đầu sách

```
1. React Frontend (POST /api/books)
   ↓
2. DauSachController.create()
   ↓
3. Spring Validation (@Valid @RequestBody)
   - Kiểm tra DauSachRequest có hợp lệ không
   - Nếu lỗi → GlobalExceptionHandler trả về lỗi
   ↓
4. DauSachService.create()
   - Kiểm tra mã sách có tồn tại không
   - Validate business logic
   - Tạo entity DauSach
   ↓
5. DauSachRepository.saveAndFlush()
   - Lưu vào database qua Hibernate/JPA
   ↓
6. Chuyển Entity → DauSachResponse DTO
   ↓
7. Trả về Response cho React Frontend
```

### Ví dụ: Đăng nhập

```
1. React Frontend (POST /api/auth/login với LoginRequest)
   ↓
2. AuthController.login()
   ↓
3. AuthService.login()
   - Tìm TaiKhoan từ database
   - So sánh mật khẩu (BCrypt)
   - Tạo JWT token (TokenService.generateToken())
   - Trả về AuthResponse (token + user info)
   ↓
4. React lưu token vào localStorage
   ↓
5. Request tiếp theo: gửi token trong header "Authorization: Bearer <token>"
   ↓
6. TokenAuthenticationFilter.doFilterInternal()
   - Extract token từ header
   - Validate token (kiểm tra signature & expiration)
   - Nếu hợp lệ → set Authentication vào SecurityContext
   ↓
7. Request được phép đi tới endpoint
```

---

## IV. TÓNG KẾT CẤU TRÚC

| Lớp | Thành Phần | Chức Năng |
|-----|-----------|---------|
| **Controller** | 14 files | Nhận request HTTP, gọi Service, trả response |
| **DTO** | 27 files | Chứa dữ liệu request/response |
| **Entity** | 23 files | Đại diện bảng database |
| **Service** | 8+ files | Xử lý business logic, gọi Repository |
| **Repository** | 23 files | Truy cập database |
| **Security** | 4 files | JWT, xác thực, phân quyền |
| **Exception** | 4 files | Xử lý lỗi, trả về error response |

**Kiến trúc**: **3-tier layered architecture** (Controller → Service → Repository)
**Framework**: Spring Boot 3.5.14 + Spring Security + JPA/Hibernate
**Database**: SQL Server
**Authentication**: JWT Token

