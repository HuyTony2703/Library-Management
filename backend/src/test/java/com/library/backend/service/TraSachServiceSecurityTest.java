package com.library.backend.service;

import com.library.backend.dto.StaffContextResponse;
import com.library.backend.dto.TraSachRequest;
import com.library.backend.entity.ChiTietPhieuMuon;
import com.library.backend.entity.ChiTietPhieuTra;
import com.library.backend.entity.CuonSach;
import com.library.backend.entity.PhieuMuon;
import com.library.backend.entity.PhieuTra;
import com.library.backend.exception.BusinessException;
import com.library.backend.repository.ChiTietPhieuMuonRepository;
import com.library.backend.repository.ChiTietPhieuTraRepository;
import com.library.backend.repository.CuonSachRepository;
import com.library.backend.repository.KhoanNoRepository;
import com.library.backend.repository.PhieuMuonRepository;
import com.library.backend.repository.PhieuTraRepository;
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
import java.time.LocalDateTime;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraSachServiceSecurityTest {

    private static final String BRANCH_A = "CN_A";
    private static final String STAFF_ID = "NV001";

    @Mock
    private PhieuTraRepository phieuTraRepository;
    @Mock
    private ChiTietPhieuTraRepository chiTietPhieuTraRepository;
    @Mock
    private ChiTietPhieuMuonRepository chiTietPhieuMuonRepository;
    @Mock
    private PhieuMuonRepository phieuMuonRepository;
    @Mock
    private CuonSachRepository cuonSachRepository;
    @Mock
    private KhoanNoRepository khoanNoRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private BranchAuthorizationService branchAuthorizationService;

    private TraSachService service;
    private AuthUser authenticatedUser;

    @BeforeEach
    void setUp() {
        service = new TraSachService(
                phieuTraRepository,
                chiTietPhieuTraRepository,
                chiTietPhieuMuonRepository,
                phieuMuonRepository,
                cuonSachRepository,
                khoanNoRepository,
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
                "Thu thu A"
        );
    }

    @Test
    void rejectsSpoofedLegacyReceiverBeforeWritingReturn() {
        TraSachRequest request = request("NV_KHAC", BRANCH_A);
        allowBranch(BRANCH_A);

        assertThatThrownBy(() -> service.create(request, authenticatedUser))
                .isInstanceOf(AccessDeniedException.class);

        verify(phieuTraRepository, never()).save(any());
        verify(chiTietPhieuMuonRepository, never()).findByIdForUpdate(anyString());
    }

    @Test
    void rejectsUnauthorizedReceivingBranchBeforeBusinessWrites() {
        TraSachRequest request = request(null, "CN_KHONG_DUOC_PHEP");
        when(branchAuthorizationService.requireAllowedBranch(
                authenticatedUser,
                "CN_KHONG_DUOC_PHEP"
        )).thenThrow(new AccessDeniedException("branch denied"));

        assertThatThrownBy(() -> service.create(request, authenticatedUser))
                .isInstanceOf(AccessDeniedException.class);

        verify(phieuTraRepository, never()).save(any());
        verify(chiTietPhieuMuonRepository, never()).findByIdForUpdate(anyString());
    }

    @Test
    void rejectsReturnAtDifferentBranchFromLoan() {
        TraSachRequest request = request(null, BRANCH_A);
        prepareUntilDetailValidation(request, loan("DG001", "CN_B"));

        assertThatThrownBy(() -> service.create(request, authenticatedUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("chi nh");

        verify(phieuTraRepository, never()).save(any());
    }

    @Test
    void rejectsDetailAlreadyReturnedAfterAcquiringRowLock() {
        TraSachRequest request = request(null, BRANCH_A);
        prepareCommon(request);
        when(chiTietPhieuMuonRepository.findByIdForUpdate("CTM001"))
                .thenReturn(Optional.of(detail()));
        when(chiTietPhieuTraRepository.existsByMaChiTietMuon("CTM001")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request, authenticatedUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CTM001");

        verify(chiTietPhieuMuonRepository).findByIdForUpdate("CTM001");
        verify(phieuTraRepository, never()).save(any());
    }

    @Test
    void rejectsDetailOwnedByAnotherReader() {
        TraSachRequest request = request(null, BRANCH_A);
        prepareUntilDetailValidation(request, loan("DG_KHAC", BRANCH_A));

        assertThatThrownBy(() -> service.create(request, authenticatedUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Chi ti");

        verify(phieuTraRepository, never()).save(any());
    }

    @Test
    void concurrentReturnIsRecheckedOnlyAfterPessimisticLock() {
        TraSachRequest request = request(null, BRANCH_A);
        prepareCommon(request);
        when(chiTietPhieuMuonRepository.findByIdForUpdate("CTM001"))
                .thenReturn(Optional.of(detail()));
        when(chiTietPhieuTraRepository.existsByMaChiTietMuon("CTM001"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(request, authenticatedUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CTM001");

        var inOrder = org.mockito.Mockito.inOrder(
                chiTietPhieuMuonRepository,
                chiTietPhieuTraRepository
        );
        inOrder.verify(chiTietPhieuMuonRepository).findByIdForUpdate("CTM001");
        inOrder.verify(chiTietPhieuTraRepository).existsByMaChiTietMuon("CTM001");
    }

    @Test
    void rejectsNegativeDamageFineBeforeWritingReturn() {
        TraSachRequest request = request(null, BRANCH_A);
        request.getChiTiet().get(0).setTinhTrangKhiTra("Hong");
        request.getChiTiet().get(0).setTienPhatHongMat(new BigDecimal("-1"));
        prepareUntilDetailValidation(request, loan("DG001", BRANCH_A));

        assertThatThrownBy(() -> service.create(request, authenticatedUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("khong duoc am");

        verify(phieuTraRepository, never()).save(any());
        verify(chiTietPhieuTraRepository, never()).save(any());
    }

    @Test
    void storesAndAuditsAuthenticatedReceiverWhenLegacyFieldIsOmitted() {
        TraSachRequest request = request(null, BRANCH_A);
        prepareUntilDetailValidation(request, loan("DG001", BRANCH_A));
        when(cuonSachRepository.findById("CS001")).thenReturn(Optional.of(copy()));
        when(jdbcTemplate.query(
                argThat(sql -> sql != null && sql.contains("FROM THAMSOQUYDINH")),
                ArgumentMatchers.<RowMapper<BigDecimal>>any(),
                any(Object[].class)
        )).thenReturn(List.of(BigDecimal.ZERO));
        when(jdbcTemplate.queryForObject(
                argThat(sql -> sql != null && sql.contains("FROM CHITIETPHIEUMUON")),
                eq(Integer.class),
                any(Object[].class)
        )).thenReturn(0);

        AtomicReference<PhieuTra> savedReturn = new AtomicReference<>();
        List<ChiTietPhieuTra> savedDetails = new ArrayList<>();
        when(phieuTraRepository.save(any(PhieuTra.class))).thenAnswer(invocation -> {
            PhieuTra value = invocation.getArgument(0);
            savedReturn.set(value);
            return value;
        });
        when(chiTietPhieuTraRepository.save(any(ChiTietPhieuTra.class))).thenAnswer(invocation -> {
            ChiTietPhieuTra value = invocation.getArgument(0);
            savedDetails.add(value);
            return value;
        });
        when(phieuTraRepository.findById(request.getMaPhieuTra()))
                .thenAnswer(invocation -> Optional.ofNullable(savedReturn.get()));
        when(chiTietPhieuTraRepository.findByMaPhieuTra(request.getMaPhieuTra()))
                .thenAnswer(invocation -> List.copyOf(savedDetails));

        service.create(request, authenticatedUser);

        assertThat(savedReturn.get().getMaNhanVienNhan()).isEqualTo(STAFF_ID);
        verify(activityLogService).logAsAccountSafe(
                eq("TK_LIB"),
                anyString(),
                eq("PHIEUTRA"),
                eq(request.getMaPhieuTra()),
                argThat(detail -> detail.contains(STAFF_ID))
        );
    }

    private void prepareCommon(TraSachRequest request) {
        allowBranch(request.getMaChiNhanh());
        when(phieuTraRepository.existsById(request.getMaPhieuTra())).thenReturn(false);
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Integer.class),
                any(Object[].class)
        )).thenReturn(1);
    }

    private void prepareUntilDetailValidation(TraSachRequest request, PhieuMuon loan) {
        prepareCommon(request);
        when(chiTietPhieuMuonRepository.findByIdForUpdate("CTM001"))
                .thenReturn(Optional.of(detail()));
        when(chiTietPhieuTraRepository.existsByMaChiTietMuon("CTM001")).thenReturn(false);
        when(phieuMuonRepository.findById("PM001")).thenReturn(Optional.of(loan));
    }

    private void allowBranch(String branchId) {
        when(branchAuthorizationService.requireAllowedBranch(authenticatedUser, branchId))
                .thenReturn(staffContext());
    }

    private TraSachRequest request(String legacyStaffId, String branchId) {
        TraSachRequest.ChiTietTraRequest item = new TraSachRequest.ChiTietTraRequest();
        item.setMaChiTietMuon("CTM001");
        item.setTinhTrangKhiTra("Binh thuong");
        item.setTienPhatHongMat(BigDecimal.ZERO);

        TraSachRequest request = new TraSachRequest();
        request.setMaPhieuTra("PTR_TEST_001");
        request.setMaDocGia("DG001");
        request.setMaNhanVienNhan(legacyStaffId);
        request.setMaChiNhanh(branchId);
        request.setChiTiet(List.of(item));
        return request;
    }

    private StaffContextResponse staffContext() {
        StaffContextResponse.BranchSummary branch = new StaffContextResponse.BranchSummary(
                BRANCH_A,
                "Chi nhanh A"
        );
        return new StaffContextResponse(
                "TK_LIB",
                STAFF_ID,
                "Thu thu A",
                new StaffContextResponse.RoleSummary("VT_THU_THU", RoleConstants.LIBRARIAN),
                branch,
                List.of(branch),
                List.of("RETURN_CREATE"),
                true,
                null
        );
    }

    private ChiTietPhieuMuon detail() {
        ChiTietPhieuMuon detail = new ChiTietPhieuMuon();
        detail.setMaChiTietMuon("CTM001");
        detail.setMaPhieuMuon("PM001");
        detail.setMaCuonSach("CS001");
        detail.setHanTra(LocalDateTime.now().plusDays(1));
        detail.setTrangThai("Dang muon");
        return detail;
    }

    private PhieuMuon loan(String readerId, String branchId) {
        PhieuMuon loan = new PhieuMuon();
        loan.setMaPhieuMuon("PM001");
        loan.setMaDocGia(readerId);
        loan.setMaChiNhanh(branchId);
        loan.setMaPhienBanQuyDinh("QD_V1");
        return loan;
    }

    private CuonSach copy() {
        CuonSach copy = new CuonSach();
        copy.setMaCuonSach("CS001");
        copy.setMaChiNhanh(BRANCH_A);
        copy.setMaTrangThai("TT_DANGMUON");
        return copy;
    }
}
