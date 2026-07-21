package com.library.backend.service;

import com.library.backend.dto.PaymentPreviewResponse;
import com.library.backend.dto.PaymentReversalRequest;
import com.library.backend.dto.PaymentReversalResponse;
import com.library.backend.dto.PhieuThuRequest;
import com.library.backend.dto.PhieuThuResponse;
import com.library.backend.dto.StaffContextResponse;
import com.library.backend.entity.ChiTietPhieuThuNo;
import com.library.backend.entity.KhoanNo;
import com.library.backend.entity.PhieuThu;
import com.library.backend.entity.PhieuThuReversal;
import com.library.backend.exception.BusinessException;
import com.library.backend.repository.ChiTietPhieuThuNoRepository;
import com.library.backend.repository.DocGiaRepository;
import com.library.backend.repository.KhoanNoRepository;
import com.library.backend.repository.PhieuThuRepository;
import com.library.backend.repository.PhieuThuReversalRepository;
import com.library.backend.repository.PhuongThucThanhToanRepository;
import com.library.backend.security.AuthUser;
import com.library.backend.security.RoleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThanhToanServiceSecurityTest {

    private static final String STAFF_ID = "NV001";
    private static final String BRANCH_ID = "CN_A";

    @Mock
    private KhoanNoRepository khoanNoRepository;
    @Mock
    private PhieuThuRepository phieuThuRepository;
    @Mock
    private PhieuThuReversalRepository phieuThuReversalRepository;
    @Mock
    private ChiTietPhieuThuNoRepository chiTietPhieuThuNoRepository;
    @Mock
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    @Mock
    private DocGiaRepository docGiaRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private StaffContextService staffContextService;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private ThanhToanService service;
    private AuthUser authenticatedUser;

    @BeforeEach
    void setUp() {
        service = new ThanhToanService(
                khoanNoRepository,
                phieuThuRepository,
                phieuThuReversalRepository,
                chiTietPhieuThuNoRepository,
                phuongThucThanhToanRepository,
                docGiaRepository,
                activityLogService,
                staffContextService,
                jdbcTemplate
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
    void rejectsSpoofedLegacyCollectorBeforeLockingDebt() {
        PhieuThuRequest request = manualRequest("NV_KHAC", money("50"), money("50"));
        when(staffContextService.getContext(authenticatedUser)).thenReturn(staffContext());

        assertThatThrownBy(() -> service.createPayment(request, authenticatedUser, "idem-1"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("khong khop");

        verify(khoanNoRepository, never()).findByIdForUpdate(anyString());
        verify(phieuThuRepository, never()).save(any());
    }

    @Test
    void previewAutoAllocatesOldestDebtFirst() {
        PhieuThuRequest request = autoRequest(money("70"));
        preparePreview(request);
        KhoanNo newerDebt = debt("NO_NEW", "DG001", money("100"), money("0"), LocalDateTime.now().minusDays(1));
        KhoanNo olderDebt = debt("NO_OLD", "DG001", money("50"), money("0"), LocalDateTime.now().minusDays(3));
        when(khoanNoRepository.findOutstandingByReaderForPreview("DG001", "Đã thanh toán"))
                .thenReturn(List.of(newerDebt, olderDebt));

        PaymentPreviewResponse preview = service.previewPayment(request, authenticatedUser);

        assertThat(preview.allocations()).extracting(PaymentPreviewResponse.AllocationPreview::debtId)
                .containsExactly("NO_OLD", "NO_NEW");
        assertThat(preview.allocations().get(0).appliedAmount()).isEqualByComparingTo("50");
        assertThat(preview.allocations().get(1).appliedAmount()).isEqualByComparingTo("20");
        assertThat(preview.balanceAfter()).isEqualByComparingTo("80");
    }

    @Test
    void rejectsAmountGreaterThanLockedDebtRemaining() {
        PhieuThuRequest request = manualRequest(null, money("20"), money("20"));
        prepareBeforeLock(request);
        when(khoanNoRepository.findByIdForUpdate("NO001"))
                .thenReturn(Optional.of(debt("NO001", "DG001", money("100"), money("90"), LocalDateTime.now())));

        assertThatThrownBy(() -> service.createPayment(request, authenticatedUser, "idem-over"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vuot qua khoan no con lai");

        verify(khoanNoRepository).findByIdForUpdate("NO001");
        verify(phieuThuRepository, never()).save(any());
    }

    @Test
    void rejectsDebtOwnedByAnotherReaderAfterLock() {
        PhieuThuRequest request = manualRequest(null, money("20"), money("20"));
        prepareBeforeLock(request);
        when(khoanNoRepository.findByIdForUpdate("NO001"))
                .thenReturn(Optional.of(debt("NO001", "DG_KHAC", money("100"), money("0"), LocalDateTime.now())));

        assertThatThrownBy(() -> service.createPayment(request, authenticatedUser, "idem-reader"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("khong thuoc doc gia");

        verify(phieuThuRepository, never()).save(any());
    }

    @Test
    void rejectsAllocationTotalDifferentFromReceiptTotal() {
        PhieuThuRequest request = manualRequest(null, money("60"), money("50"));
        prepareBeforeLock(request);
        when(khoanNoRepository.findByIdForUpdate("NO001"))
                .thenReturn(Optional.of(debt("NO001", "DG001", money("100"), money("0"), LocalDateTime.now())));

        assertThatThrownBy(() -> service.createPayment(request, authenticatedUser, "idem-total"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("phai bang so tien thu");

        verify(phieuThuRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateExternalTransactionBeforeLockingDebt() {
        PhieuThuRequest request = manualRequest(null, money("50"), money("50"));
        request.setMaGiaoDichNgoai("BANK-123");
        prepareBeforeLock(request);
        when(phieuThuRepository.existsByMaGiaoDichNgoai("BANK-123")).thenReturn(true);

        assertThatThrownBy(() -> service.createPayment(request, authenticatedUser, "idem-ext"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ma giao dich ngoai da ton tai");

        verify(khoanNoRepository, never()).findByIdForUpdate(anyString());
        verify(phieuThuRepository, never()).save(any());
    }

    @Test
    void rejectsFinancialReadWhenAccountHasNoOperationalStaffContext() {
        AuthUser adminWithoutStaff = new AuthUser(
                "TK_ADMIN",
                "admin",
                "VT_ADMIN",
                RoleConstants.ADMIN,
                null,
                null,
                "Quan tri"
        );
        when(staffContextService.getContext(adminWithoutStaff)).thenReturn(new StaffContextResponse(
                "TK_ADMIN",
                null,
                null,
                new StaffContextResponse.RoleSummary("VT_ADMIN", RoleConstants.ADMIN),
                null,
                List.of(),
                List.of(),
                false,
                "NO_STAFF_PROFILE"
        ));

        assertThatThrownBy(() -> service.getDebtsByReader("DG001", adminWithoutStaff))
                .isInstanceOf(AccessDeniedException.class);

        verify(docGiaRepository, never()).existsById(anyString());
        verify(khoanNoRepository, never()).findByMaDocGiaOrderByNgayPhatSinhDesc(anyString());
    }

    @Test
    void repeatedRequestReturnsExistingReceiptWithoutCollectingTwice() {
        PhieuThuRequest request = manualRequest(null, money("50"), money("50"));
        AtomicReference<PhieuThu> savedReceipt = prepareSuccessfulPayment(request, "idem-repeat");

        PhieuThuResponse first = service.createPayment(request, authenticatedUser, "idem-repeat");
        PhieuThuResponse repeated = service.createPayment(request, authenticatedUser, "idem-repeat");

        assertThat(repeated.getMaPhieuThu()).isEqualTo(first.getMaPhieuThu());
        assertThat(savedReceipt.get().getIdempotencyKey()).isEqualTo("idem-repeat");
        verify(phieuThuRepository, times(1)).save(any(PhieuThu.class));
        verify(khoanNoRepository, times(1)).save(any(KhoanNo.class));
        verify(chiTietPhieuThuNoRepository, times(1)).save(any(ChiTietPhieuThuNo.class));
    }

    @Test
    void successfulPaymentStoresPrincipalGeneratedReceiptCashAndAuditsDebtBeforeAndAfter() {
        PhieuThuRequest request = manualRequest(STAFF_ID, money("50"), money("50"));
        request.setMaPhieuThu("PT_CLIENT_IGNORED");
        request.setCashReceived(money("100"));
        AtomicReference<PhieuThu> savedReceipt = prepareSuccessfulPayment(request, "idem-success");

        service.createPayment(request, authenticatedUser, "idem-success");

        assertThat(savedReceipt.get().getMaPhieuThu()).startsWith("PT");
        assertThat(savedReceipt.get().getMaPhieuThu()).isNotEqualTo("PT_CLIENT_IGNORED");
        assertThat(savedReceipt.get().getMaNhanVienThu()).isEqualTo(STAFF_ID);
        assertThat(savedReceipt.get().getTienKhachDua()).isEqualByComparingTo("100");
        assertThat(savedReceipt.get().getTienThua()).isEqualByComparingTo("50");
        verify(activityLogService).logAsAccount(
                eq("TK_LIB"),
                eq("Phan bo phieu thu vao khoan no"),
                eq("KHOANNO"),
                eq("NO001"),
                argThat(detail -> detail.contains("truoc {") && detail.contains("sau {"))
        );
    }

    @Test
    void reversePaymentRestoresDebtCreatesLedgerAndKeepsOriginalReceipt() {
        PhieuThu original = originalReceipt("PT_ORIGINAL");
        ChiTietPhieuThuNo detail = receiptDetail("PT_ORIGINAL", "NO001", money("50"));
        KhoanNo debt = debt("NO001", "DG001", money("100"), money("70"), LocalDateTime.now().minusDays(2));
        AtomicReference<PhieuThuReversal> savedReversal = new AtomicReference<>();

        when(staffContextService.getContext(authenticatedUser)).thenReturn(staffContext());
        when(phieuThuRepository.findByIdForUpdate("PT_ORIGINAL")).thenReturn(Optional.of(original));
        when(phieuThuReversalRepository.existsByMaPhieuThuGoc("PT_ORIGINAL")).thenReturn(false);
        when(chiTietPhieuThuNoRepository.findByMaPhieuThu("PT_ORIGINAL")).thenReturn(List.of(detail));
        when(khoanNoRepository.findByIdForUpdate("NO001")).thenReturn(Optional.of(debt));
        when(phieuThuReversalRepository.existsById(anyString())).thenReturn(false);
        when(phieuThuReversalRepository.save(any(PhieuThuReversal.class))).thenAnswer(invocation -> {
            PhieuThuReversal reversal = invocation.getArgument(0);
            savedReversal.set(reversal);
            return reversal;
        });

        PaymentReversalResponse response = service.reversePayment(
                "PT_ORIGINAL",
                new PaymentReversalRequest("Thu nham", "APPROVAL-1"),
                authenticatedUser
        );

        assertThat(original.getTrangThai()).isEqualTo("Thành công");
        assertThat(savedReversal.get().getMaPhieuThuGoc()).isEqualTo("PT_ORIGINAL");
        assertThat(savedReversal.get().getApprovalReference()).isEqualTo("APPROVAL-1");
        assertThat(debt.getSoTienDaThanhToan()).isEqualByComparingTo("20");
        assertThat(debt.getTrangThai()).isEqualTo("Thanh toán một phần");
        assertThat(response.restoredAmount()).isEqualByComparingTo("50");
        assertThat(response.restoredDebts()).hasSize(1);
        verify(phieuThuRepository, never()).save(any(PhieuThu.class));
        verify(activityLogService).logAsAccount(
                eq("TK_LIB"),
                eq("Dao phieu thu - hoan khoan no"),
                eq("KHOANNO"),
                eq("NO001"),
                argThat(text -> text.contains("truoc {") && text.contains("sau {"))
        );
    }

    @Test
    void reversePaymentRejectsSecondReversalBeforeChangingDebt() {
        PhieuThu original = originalReceipt("PT_ORIGINAL");

        when(staffContextService.getContext(authenticatedUser)).thenReturn(staffContext());
        when(phieuThuRepository.findByIdForUpdate("PT_ORIGINAL")).thenReturn(Optional.of(original));
        when(phieuThuReversalRepository.existsByMaPhieuThuGoc("PT_ORIGINAL")).thenReturn(true);

        assertThatThrownBy(() -> service.reversePayment(
                "PT_ORIGINAL",
                new PaymentReversalRequest("Thu nham", "APPROVAL-1"),
                authenticatedUser
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("da duoc dao");

        verify(khoanNoRepository, never()).findByIdForUpdate(anyString());
        verify(phieuThuReversalRepository, never()).save(any());
    }

    private AtomicReference<PhieuThu> prepareSuccessfulPayment(
            PhieuThuRequest request,
            String idempotencyKey
    ) {
        AtomicReference<PhieuThu> savedReceipt = new AtomicReference<>();
        List<ChiTietPhieuThuNo> savedDetails = new ArrayList<>();

        prepareBeforeLock(request);
        when(khoanNoRepository.findByIdForUpdate("NO001"))
                .thenReturn(Optional.of(debt("NO001", "DG001", money("100"), money("0"), LocalDateTime.now().minusDays(1))));
        when(phieuThuRepository.findByIdempotencyKey(idempotencyKey))
                .thenAnswer(invocation -> Optional.ofNullable(savedReceipt.get()));
        when(phieuThuRepository.save(any(PhieuThu.class))).thenAnswer(invocation -> {
            PhieuThu receipt = invocation.getArgument(0);
            savedReceipt.set(receipt);
            return receipt;
        });
        when(chiTietPhieuThuNoRepository.save(any(ChiTietPhieuThuNo.class)))
                .thenAnswer(invocation -> {
                    ChiTietPhieuThuNo detail = invocation.getArgument(0);
                    savedDetails.add(detail);
                    return detail;
                });
        when(phieuThuRepository.findById(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(savedReceipt.get()));
        when(chiTietPhieuThuNoRepository.findByMaPhieuThu(anyString()))
                .thenAnswer(invocation -> List.copyOf(savedDetails));

        return savedReceipt;
    }

    private void preparePreview(PhieuThuRequest request) {
        when(staffContextService.getContext(authenticatedUser)).thenReturn(staffContext());
        when(docGiaRepository.existsById(request.getMaDocGia())).thenReturn(true);
        when(phuongThucThanhToanRepository.existsById(request.getMaPhuongThuc())).thenReturn(true);
    }

    private void prepareBeforeLock(PhieuThuRequest request) {
        preparePreview(request);
        when(phieuThuRepository.findByIdempotencyKey(anyString())).thenReturn(Optional.empty());
        lenient().when(phieuThuRepository.existsById(anyString())).thenReturn(false);
    }

    private PhieuThuRequest manualRequest(
            String legacyStaffId,
            BigDecimal receiptAmount,
            BigDecimal allocationAmount
    ) {
        PhieuThuRequest.ChiTietThuNoRequest detail = new PhieuThuRequest.ChiTietThuNoRequest();
        detail.setMaKhoanNo("NO001");
        detail.setSoTienApDung(allocationAmount);

        PhieuThuRequest request = autoRequest(receiptAmount);
        request.setMaPhieuThu("PT_TEST_001");
        request.setMaNhanVienThu(legacyStaffId);
        request.setChiTietNo(List.of(detail));
        return request;
    }

    private PhieuThuRequest autoRequest(BigDecimal receiptAmount) {
        PhieuThuRequest request = new PhieuThuRequest();
        request.setMaDocGia("DG001");
        request.setMaPhuongThuc("PT_CASH");
        request.setSoTienThu(receiptAmount);
        return request;
    }

    private StaffContextResponse staffContext() {
        StaffContextResponse.BranchSummary branch = new StaffContextResponse.BranchSummary(
                BRANCH_ID,
                "Chi nhanh A"
        );
        return new StaffContextResponse(
                "TK_LIB",
                STAFF_ID,
                "Thu thu A",
                new StaffContextResponse.RoleSummary("VT_THU_THU", RoleConstants.LIBRARIAN),
                branch,
                List.of(branch),
                List.of("PAYMENT_CREATE"),
                true,
                null
        );
    }

    private KhoanNo debt(
            String debtId,
            String readerId,
            BigDecimal incurred,
            BigDecimal paid,
            LocalDateTime incurredAt
    ) {
        KhoanNo debt = new KhoanNo();
        debt.setMaKhoanNo(debtId);
        debt.setMaDocGia(readerId);
        debt.setMaLoaiKhoanNo("NO_TRA_TRE");
        debt.setSoTienPhatSinh(incurred);
        debt.setSoTienDaThanhToan(paid);
        debt.setNgayPhatSinh(incurredAt);
        debt.setTrangThai(paid.signum() == 0 ? "Chưa thanh toán" : "Thanh toán một phần");
        return debt;
    }

    private PhieuThu originalReceipt(String receiptId) {
        PhieuThu receipt = new PhieuThu();
        receipt.setMaPhieuThu(receiptId);
        receipt.setMaDocGia("DG001");
        receipt.setMaNhanVienThu(STAFF_ID);
        receipt.setMaPhuongThuc("PT_CASH");
        receipt.setLoaiThu("Thu tiền phạt");
        receipt.setSoTienThu(money("50"));
        receipt.setNgayThu(LocalDateTime.now().minusHours(1));
        receipt.setTrangThai("Thành công");
        return receipt;
    }

    private ChiTietPhieuThuNo receiptDetail(String receiptId, String debtId, BigDecimal amount) {
        ChiTietPhieuThuNo detail = new ChiTietPhieuThuNo();
        detail.setMaChiTietPhieuThu("CTPT_TEST_01");
        detail.setMaPhieuThu(receiptId);
        detail.setMaKhoanNo(debtId);
        detail.setSoTienApDung(amount);
        return detail;
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
