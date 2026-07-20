package com.library.backend.service;

import com.library.backend.dto.ReaderPasswordResetRequest;
import com.library.backend.dto.ReaderPasswordResetResponse;
import com.library.backend.exception.CatalogValidationException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReaderPasswordResetServiceTest {
    @Mock JdbcTemplate jdbcTemplate;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ActivityLogService activityLogService;
    private ReaderPasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new ReaderPasswordResetService(jdbcTemplate, passwordEncoder, activityLogService,
                new java.security.SecureRandom(), 60, 3, 10);
    }

    @Test
    void rejectsReaderRoleBeforeReadingTarget() {
        assertThatThrownBy(() -> service.reset("DG001", request("reader@example.com", LocalDate.of(2000, 1, 2)),
                user("TK_READER", RoleConstants.READER)))
                .isInstanceOf(AccessDeniedException.class);
        verify(jdbcTemplate, never()).query(anyString(), ArgumentMatchers.<RowMapper<Object>>any(), any(Object[].class));
    }

    @Test
    void rejectsLibrarianWithoutStaffProfileBeforeReadingTarget() {
        AuthUser librarianWithoutStaff = new AuthUser("TK_LIB", "staff", "VT", RoleConstants.LIBRARIAN,
                null, null, "Thá»§ thÆ°");

        assertThatThrownBy(() -> service.reset("DG001", request("reader@example.com", LocalDate.of(2000, 1, 2)),
                librarianWithoutStaff))
                .isInstanceOf(AccessDeniedException.class);
        verify(jdbcTemplate, never()).query(anyString(), ArgumentMatchers.<RowMapper<Object>>any(), any(Object[].class));
    }

    @Test
    void mismatchedVerificationIsRecordedButDoesNotChangePassword() throws Exception {
        prepareReaderAndLimits();
        when(jdbcTemplate.queryForObject(argThat(sql -> sql != null && sql.contains("SCOPE_IDENTITY")), eq(Long.class)))
                .thenReturn(42L);

        assertThatThrownBy(() -> service.reset("DG001", request("wrong@example.com", LocalDate.of(2000, 1, 2)),
                user("TK_LIB", RoleConstants.LIBRARIAN)))
                .isInstanceOf(CatalogValidationException.class)
                .hasMessageContaining("không khớp");

        verify(jdbcTemplate).update(argThat(sql -> sql.contains("INSERT INTO DOCGIA_PASSWORD_RESET_EVENT")), any(Object[].class));
        verify(jdbcTemplate, never()).update(argThat(sql -> sql.contains("UPDATE TAIKHOAN")), any(Object[].class));
    }

    @Test
    void generatesStrongOneTimeSecretRevokesSessionsAndKeepsAuditClean() throws Exception {
        prepareReaderAndLimits();
        when(jdbcTemplate.queryForObject(argThat(sql -> sql != null && sql.contains("SCOPE_IDENTITY")), eq(Long.class)))
                .thenReturn(43L);
        when(passwordEncoder.encode(anyString())).thenReturn("BCRYPT_HASH_ONLY");
        doReturn(1).when(jdbcTemplate).update(anyString(), any(Object[].class));

        ReaderPasswordResetResponse response = service.reset("DG001",
                request("reader@example.com", LocalDate.of(2000, 1, 2)), user("TK_ADMIN", RoleConstants.ADMIN));

        assertThat(response.temporaryPassword()).hasSize(16)
                .containsPattern("[A-Z]").containsPattern("[a-z]").containsPattern("[0-9]")
                .containsPattern("[!@#$%*\\-_]");
        assertThat(response.mustChangePassword()).isTrue();
        assertThat(response.sessionsRevoked()).isTrue();
        verify(passwordEncoder).encode(response.temporaryPassword());
        verify(jdbcTemplate).update(argThat(sql -> sql.contains("TokenVersion = TokenVersion + 1")), any(Object[].class));
        verify(activityLogService).logAsAccountSafe(eq("TK_ADMIN"), eq("Reset mật khẩu độc giả"), eq("TAIKHOAN"),
                eq("TK_READER"), argThat(detail -> !detail.contains(response.temporaryPassword())
                        && !detail.contains("BCRYPT_HASH_ONLY")));
    }

    @Test
    void enforcesConfiguredReaderRateLimitBeforeCreatingAnotherAttempt() throws Exception {
        prepareReader();
        when(jdbcTemplate.queryForObject(argThat(sql -> sql != null && sql.contains("WHERE MaDocGia")),
                eq(Integer.class), any(Object[].class))).thenReturn(3);
        when(jdbcTemplate.queryForObject(argThat(sql -> sql != null && sql.contains("WHERE MaTaiKhoanThucHien")),
                eq(Integer.class), any(Object[].class))).thenReturn(0);

        assertThatThrownBy(() -> service.reset("DG001", request("reader@example.com", LocalDate.of(2000, 1, 2)),
                user("TK_LIB", RoleConstants.LIBRARIAN)))
                .isInstanceOf(CatalogValidationException.class)
                .extracting(ex -> ((CatalogValidationException) ex).getStatus().value()).isEqualTo(429);
        verify(jdbcTemplate, never()).update(argThat(sql -> sql.contains("INSERT INTO DOCGIA_PASSWORD_RESET_EVENT")), any(Object[].class));
    }

    private void prepareReaderAndLimits() throws Exception {
        prepareReader();
        when(jdbcTemplate.queryForObject(argThat(sql -> sql != null && sql.contains("WHERE MaDocGia")),
                eq(Integer.class), any(Object[].class))).thenReturn(0);
        when(jdbcTemplate.queryForObject(argThat(sql -> sql != null && sql.contains("WHERE MaTaiKhoanThucHien")),
                eq(Integer.class), any(Object[].class))).thenReturn(0);
    }

    private void prepareReader() throws Exception {
        ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
        when(rs.getString("MaDocGia")).thenReturn("DG001"); when(rs.getString("MaTaiKhoan")).thenReturn("TK_READER");
        when(rs.getString("Email")).thenReturn("reader@example.com");
        when(rs.getDate("NgaySinh")).thenReturn(Date.valueOf(LocalDate.of(2000, 1, 2)));
        when(jdbcTemplate.query(argThat(sql -> sql != null && sql.contains("FROM DOCGIA")),
                ArgumentMatchers.<RowMapper<Object>>any(), any(Object[].class))).thenAnswer(invocation -> {
            RowMapper<?> mapper = invocation.getArgument(1); return List.of(mapper.mapRow(rs, 0));
        });
    }

    private ReaderPasswordResetRequest request(String email, LocalDate dob) {
        return new ReaderPasswordResetRequest(ReaderPasswordResetRequest.Mode.GENERATE_TEMPORARY,
                true, true, "Độc giả đã xác minh tại quầy", email, dob);
    }
    private AuthUser user(String accountId, String role) {
        return new AuthUser(accountId, accountId.toLowerCase(), "VT", role, null,
                RoleConstants.LIBRARIAN.equals(role) ? "NV001" : null, "Actor");
    }
}
