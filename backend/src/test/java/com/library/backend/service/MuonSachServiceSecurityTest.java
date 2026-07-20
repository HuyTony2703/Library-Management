package com.library.backend.service;

import com.library.backend.dto.MuonSachRequest;
import com.library.backend.dto.StaffContextResponse;
import com.library.backend.entity.ChiTietPhieuMuon;
import com.library.backend.entity.CuonSach;
import com.library.backend.entity.DocGia;
import com.library.backend.entity.PhieuMuon;
import com.library.backend.exception.BusinessException;
import com.library.backend.repository.ChiTietPhieuMuonRepository;
import com.library.backend.repository.CuonSachRepository;
import com.library.backend.repository.DocGiaRepository;
import com.library.backend.repository.PhieuMuonRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MuonSachServiceSecurityTest {

    private static final String BRANCH_A = "CN_A";
    private static final String STAFF_ID = "NV001";
    private static final String IDEMPOTENCY_KEY = "loan-test-key";

    @Mock
    private PhieuMuonRepository phieuMuonRepository;
    @Mock
    private ChiTietPhieuMuonRepository chiTietPhieuMuonRepository;
    @Mock
    private DocGiaRepository docGiaRepository;
    @Mock
    private CuonSachRepository cuonSachRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    private MuonSachService service;
    private AuthUser authenticatedUser;

    @BeforeEach
    void setUp() {
        service = new MuonSachService(
                phieuMuonRepository,
                chiTietPhieuMuonRepository,
                docGiaRepository,
                cuonSachRepository,
                jdbcTemplate,
                activityLogService,
                branchAuthorizationService
        );
        authenticatedUser = new AuthUser(
                "TK_LIB",
                "thuthu",
                "VT_THU_THU",
                RoleConstants.LIBRARIAN,
                null,
                STAFF_ID,
                "Thủ thư A"
        );
    }

    @Test
    void rejectsSpoofedLegacyStaffIdBeforeWritingLoan() {
        MuonSachRequest request = request("NV_KHAC", BRANCH_A);
        when(branchAuthorizationService.requireAllowedBranch(authenticatedUser, BRANCH_A))
                .thenReturn(staffContext());

        assertThatThrownBy(() -> service.create(request, authenticatedUser, IDEMPOTENCY_KEY))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("khong khop");

        verify(phieuMuonRepository, never()).save(any());
        verify(cuonSachRepository, never()).findByIdForUpdate(anyString());
    }

    @Test
    void rejectsLoanAtUnauthorizedBranchBeforeBusinessWrites() {
        MuonSachRequest request = request(null, "CN_KHONG_DUOC_PHEP");
        when(branchAuthorizationService.requireAllowedBranch(
                authenticatedUser,
                "CN_KHONG_DUOC_PHEP"
        )).thenThrow(new AccessDeniedException("Không có quyền thao tác tại chi nhánh"));

        assertThatThrownBy(() -> service.create(request, authenticatedUser, IDEMPOTENCY_KEY))
                .isInstanceOf(AccessDeniedException.class);

        verify(phieuMuonRepository, never()).save(any());
        verify(cuonSachRepository, never()).findByIdForUpdate(anyString());
    }

    @Test
    void rejectsCopyFromAnotherBranchAfterAcquiringRowLock() {
        MuonSachRequest request = request(null, BRANCH_A);
        prepareUntilCopyLock(request);
        when(cuonSachRepository.findByIdForUpdate("CS001"))
                .thenReturn(Optional.of(copy("CS001", "CN_B", "TT_SANCO")));

        assertThatThrownBy(() -> service.create(request, authenticatedUser, IDEMPOTENCY_KEY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("khong thuoc chi nhanh");

        verify(cuonSachRepository).findByIdForUpdate("CS001");
        verify(chiTietPhieuMuonRepository, never()).save(any());
    }

    @Test
    void rejectsCopyBorrowedByConcurrentTransactionAfterRowLock() {
        MuonSachRequest request = request(STAFF_ID, BRANCH_A);
        prepareUntilCopyLock(request);
        when(cuonSachRepository.findByIdForUpdate("CS001"))
                .thenReturn(Optional.of(copy("CS001", BRANCH_A, "TT_DANGMUON")));

        assertThatThrownBy(() -> service.create(request, authenticatedUser, IDEMPOTENCY_KEY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("khong o trang thai");

        verify(cuonSachRepository).findByIdForUpdate("CS001");
        verify(chiTietPhieuMuonRepository, never()).save(any());
    }

    @Test
    void rejectsReaderWithActiveBorrowingLockBeforeCreatingLoan() {
        MuonSachRequest request = request(STAFF_ID, BRANCH_A);
        when(branchAuthorizationService.requireAllowedBranch(authenticatedUser, BRANCH_A)).thenReturn(staffContext());
        when(phieuMuonRepository.existsById(request.getMaPhieuMuon())).thenReturn(false);
        when(jdbcTemplate.queryForObject(argThat(sql -> sql != null && sql.contains("FROM CHINHANH")), eq(Integer.class), any(Object[].class))).thenReturn(1);
        DocGia reader = new DocGia();
        reader.setMaDocGia("DG001");
        reader.setTrangThai("Hoạt động");
        reader.setNgayHetHanThe(LocalDate.now().plusMonths(1));
        when(docGiaRepository.findById("DG001")).thenReturn(Optional.of(reader));
        when(jdbcTemplate.queryForObject(argThat(sql -> sql != null && sql.contains("FROM DOCGIA_KHOA")), eq(Integer.class), any(Object[].class))).thenReturn(1);

        assertThatThrownBy(() -> service.create(request, authenticatedUser, IDEMPOTENCY_KEY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("khoa quyen muon");

        verify(phieuMuonRepository, never()).save(any());
    }

    @Test
    void cardExpiringTodayIsNotTreatedAsManuallyExpired() {
        MuonSachRequest request = request(STAFF_ID, BRANCH_A);
        prepareUntilCopyLock(request);
        DocGia reader = new DocGia();
        reader.setMaDocGia("DG001");
        reader.setTrangThai("Hoạt động");
        reader.setNgayHetHanThe(LocalDate.now());
        when(docGiaRepository.findById("DG001")).thenReturn(Optional.of(reader));
        when(cuonSachRepository.findByIdForUpdate("CS001"))
                .thenReturn(Optional.of(copy("CS001", "CN_B", "TT_SANCO")));

        assertThatThrownBy(() -> service.create(request, authenticatedUser, IDEMPOTENCY_KEY))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("khong thuoc chi nhanh");
    }

    @Test
    void storesAndAuditsAuthenticatedStaffWhenLegacyFieldMatches() throws Exception {
        MuonSachRequest request = request(STAFF_ID, BRANCH_A);
        prepareUntilCopyLock(request);
        CuonSach copy = copy("CS001", BRANCH_A, "TT_SANCO");
        when(cuonSachRepository.findByIdForUpdate("CS001")).thenReturn(Optional.of(copy));

        ResultSet ruleRow = mock(ResultSet.class);
        when(ruleRow.getString("MaQuyDinhMuon")).thenReturn("QDM001");
        when(ruleRow.getInt("SoNgayMuon")).thenReturn(7);
        when(jdbcTemplate.query(
                argThat(sql -> sql != null && sql.contains("FROM CUONSACH cs")),
                ArgumentMatchers.<RowMapper<Object>>any(),
                any(Object[].class)
        )).thenAnswer(invocation -> {
            RowMapper<?> mapper = invocation.getArgument(1);
            return List.of(mapper.mapRow(ruleRow, 0));
        });

        AtomicReference<PhieuMuon> savedLoan = new AtomicReference<>();
        List<ChiTietPhieuMuon> savedDetails = new ArrayList<>();
        when(phieuMuonRepository.save(any(PhieuMuon.class))).thenAnswer(invocation -> {
            PhieuMuon loan = invocation.getArgument(0);
            savedLoan.set(loan);
            return loan;
        });
        when(chiTietPhieuMuonRepository.save(any(ChiTietPhieuMuon.class))).thenAnswer(invocation -> {
            ChiTietPhieuMuon detail = invocation.getArgument(0);
            savedDetails.add(detail);
            return detail;
        });
        when(phieuMuonRepository.findById(request.getMaPhieuMuon()))
                .thenAnswer(invocation -> Optional.ofNullable(savedLoan.get()));
        when(chiTietPhieuMuonRepository.findByMaPhieuMuon(request.getMaPhieuMuon()))
                .thenAnswer(invocation -> List.copyOf(savedDetails));

        service.create(request, authenticatedUser, IDEMPOTENCY_KEY);

        assertThat(savedLoan.get().getMaNhanVienLap()).isEqualTo(STAFF_ID);
        assertThat(copy.getMaTrangThai()).isEqualTo("TT_DANGMUON");
        verify(activityLogService).logAsAccountSafe(
                eq("TK_LIB"),
                eq("Tao phieu muon"),
                eq("PHIEUMUON"),
                eq(request.getMaPhieuMuon()),
                argThat(detail -> detail.contains("Nhan vien " + STAFF_ID))
        );
    }

    private void prepareUntilCopyLock(MuonSachRequest request) {
        when(branchAuthorizationService.requireAllowedBranch(authenticatedUser, BRANCH_A))
                .thenReturn(staffContext());
        when(phieuMuonRepository.existsById(request.getMaPhieuMuon())).thenReturn(false);
        when(jdbcTemplate.queryForObject(
                argThat(sql -> sql != null && sql.contains("FROM CHINHANH")),
                eq(Integer.class),
                any(Object[].class)
        )).thenReturn(1);

        DocGia reader = new DocGia();
        reader.setMaDocGia("DG001");
        reader.setTrangThai("Hoạt động");
        reader.setNgayHetHanThe(LocalDate.now().plusMonths(3));
        when(docGiaRepository.findById("DG001")).thenReturn(Optional.of(reader));

        when(jdbcTemplate.queryForObject(
                argThat(sql -> sql != null && sql.contains("FROM KHOANNO")),
                eq(BigDecimal.class),
                any(Object[].class)
        )).thenReturn(BigDecimal.ZERO);
        when(jdbcTemplate.queryForObject(
                argThat(sql -> sql != null && sql.contains("FROM DOCGIA_KHOA")),
                eq(Integer.class),
                any(Object[].class)
        )).thenReturn(0);
        when(jdbcTemplate.query(
                ArgumentMatchers.<String>argThat(
                        sql -> sql != null && sql.contains("FROM PHIENBANQUYDINH")
                ),
                (RowMapper<String>) ArgumentMatchers.any(RowMapper.class)
        )).thenReturn(List.of("QD_V1"));
        when(jdbcTemplate.query(
                argThat(sql -> sql != null && sql.contains("FROM LICHSUGOITHANHVIEN")),
                ArgumentMatchers.<RowMapper<String>>any(),
                any(Object[].class)
        )).thenReturn(List.of("GOI_THUONG"));
        when(jdbcTemplate.queryForObject(
                argThat(sql -> sql != null
                        && sql.contains("SELECT COUNT(*)")
                        && sql.contains("PHIEUMUON pm")),
                eq(Integer.class),
                any(Object[].class)
        )).thenReturn(0);
        when(jdbcTemplate.query(
                argThat(sql -> sql != null && sql.contains("FROM QUYDINHGOI")),
                ArgumentMatchers.<RowMapper<Integer>>any(),
                any(Object[].class)
        )).thenReturn(List.of(5));
    }

    private MuonSachRequest request(String legacyStaffId, String branchId) {
        MuonSachRequest request = new MuonSachRequest();
        request.setMaPhieuMuon("PM_TEST_001");
        request.setMaDocGia("DG001");
        request.setMaNhanVienLap(legacyStaffId);
        request.setMaChiNhanh(branchId);
        request.setMaCuonSachs(List.of("CS001"));
        return request;
    }

    private StaffContextResponse staffContext() {
        StaffContextResponse.BranchSummary branch = new StaffContextResponse.BranchSummary(
                BRANCH_A,
                "Chi nhánh A"
        );
        return new StaffContextResponse(
                "TK_LIB",
                STAFF_ID,
                "Thủ thư A",
                new StaffContextResponse.RoleSummary("VT_THU_THU", RoleConstants.LIBRARIAN),
                branch,
                List.of(branch),
                List.of("LOAN_CREATE"),
                true,
                null
        );
    }

    private CuonSach copy(String id, String branchId, String status) {
        CuonSach copy = new CuonSach();
        copy.setMaCuonSach(id);
        copy.setMaChiNhanh(branchId);
        copy.setMaTrangThai(status);
        return copy;
    }
}
