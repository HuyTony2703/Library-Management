package com.library.backend.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibrarySchedulerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Test
    void expireHeldReservationsReleasesCopyAndNotifiesReader() {
        LibraryScheduler scheduler = new LibraryScheduler(jdbcTemplate);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of(
                        new LibraryScheduler.ExpiringReservation("PDT001", "CS001", "DG001")
                ));
        when(jdbcTemplate.query(contains("SELECT MaTaiKhoan FROM DOCGIA"), any(RowMapper.class), anyString()))
                .thenReturn(List.of("TK001"));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), anyString(), anyString()))
                .thenReturn(0);

        scheduler.expireHeldReservations();

        verify(jdbcTemplate).update(
                contains("UPDATE PHIEUDATTRUOC"),
                eq("PDT001")
        );
        verify(jdbcTemplate).update(
                contains("UPDATE CUONSACH"),
                eq("TT_SANCO"),
                eq("CS001")
        );
        verify(jdbcTemplate).update(
                contains("INSERT INTO THONGBAO"),
                anyString(),
                eq("TK001"),
                eq("TB_TAIKHOAN_THE_DOI_TRANGTHAI"),
                anyString(),
                anyString()
        );
    }

    @Test
    void expireHeldReservationsDoesNotReleaseWhenNoHeldCopy() {
        LibraryScheduler scheduler = new LibraryScheduler(jdbcTemplate);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of(
                        new LibraryScheduler.ExpiringReservation("PDT002", null, "DG001")
                ));
        when(jdbcTemplate.query(contains("SELECT MaTaiKhoan FROM DOCGIA"), any(RowMapper.class), anyString()))
                .thenReturn(List.of("TK001"));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), anyString(), anyString()))
                .thenReturn(0);

        scheduler.expireHeldReservations();

        verify(jdbcTemplate, never()).update(contains("UPDATE CUONSACH"), anyString(), anyString());
    }

    @Test
    void expireReaderCardsMarksExpiredReaders() {
        LibraryScheduler scheduler = new LibraryScheduler(jdbcTemplate);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of(
                        new LibraryScheduler.ExpiringReaderCard("DG001", "TK001")
                ));

        scheduler.expireReaderCards();

        verify(jdbcTemplate).update(
                contains("UPDATE DOCGIA"),
                eq("DG001")
        );
    }

    @Test
    void expireMembershipsMarksExpiredPlans() {
        LibraryScheduler scheduler = new LibraryScheduler(jdbcTemplate);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of(
                        new LibraryScheduler.ExpiringMembership("LSG001", "DG001")
                ));

        scheduler.expireMemberships();

        verify(jdbcTemplate).update(
                contains("UPDATE LICHSUGOITHANHVIEN"),
                eq("LSG001")
        );
    }

    @Test
    void sendDueRemindersInsertsNotificationForDueSoonLoans() {
        LibraryScheduler scheduler = new LibraryScheduler(jdbcTemplate);

        when(jdbcTemplate.query(contains("SELECT TOP 1 ts.SoNgayNhacTruocHan"), any(RowMapper.class)))
                .thenReturn(List.of(3));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(3)))
                .thenReturn(List.of(
                        new LibraryScheduler.LoanReminder(
                                "CTM001", "TK001", "Clean Code", LocalDateTime.now().plusDays(1)
                        )
                ));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), anyString(), anyString()))
                .thenReturn(0);

        scheduler.sendDueReminders();

        verify(jdbcTemplate).update(
                contains("INSERT INTO THONGBAO"),
                anyString(),
                eq("TK001"),
                eq("TB_SAP_DEN_HAN"),
                anyString(),
                anyString()
        );
    }

    @Test
    void sendDueRemindersSkipsAlreadyNotifiedLoans() {
        LibraryScheduler scheduler = new LibraryScheduler(jdbcTemplate);

        when(jdbcTemplate.query(contains("SELECT TOP 1 ts.SoNgayNhacTruocHan"), any(RowMapper.class)))
                .thenReturn(List.of(3));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(3)))
                .thenReturn(List.of(
                        new LibraryScheduler.LoanReminder(
                                "CTM001", "TK001", "Clean Code", LocalDateTime.now().plusDays(1)
                        )
                ));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), anyString(), anyString()))
                .thenReturn(1);

        scheduler.sendDueReminders();

        verify(jdbcTemplate, never()).update(contains("INSERT INTO THONGBAO"), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void sendOverdueNotificationsInsertsForOverdueLoans() {
        LibraryScheduler scheduler = new LibraryScheduler(jdbcTemplate);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of(
                        new LibraryScheduler.LoanReminder(
                                "CTM002", "TK001", "Clean Code", LocalDateTime.now().minusDays(2)
                        )
                ));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), anyString(), anyString()))
                .thenReturn(0);

        scheduler.sendOverdueNotifications();

        verify(jdbcTemplate).update(
                contains("INSERT INTO THONGBAO"),
                anyString(),
                eq("TK001"),
                eq("TB_QUA_HAN_TRA"),
                anyString(),
                anyString()
        );
    }

    @Test
    void sendMembershipExpiryRemindersInsertsNotification() {
        LibraryScheduler scheduler = new LibraryScheduler(jdbcTemplate);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(List.of(
                        new LibraryScheduler.MembershipReminder(
                                "LSG001", "TK001", "VIP", LocalDate.now().plusDays(3)
                        )
                ));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), anyString(), anyString()))
                .thenReturn(0);

        scheduler.sendMembershipExpiryReminders();

        verify(jdbcTemplate).update(
                contains("INSERT INTO THONGBAO"),
                anyString(),
                eq("TK001"),
                eq("TB_GOI_SAP_HET_HAN"),
                anyString(),
                anyString()
        );
    }

    @Test
    void sendCardExpiryRemindersInsertsNotification() {
        LibraryScheduler scheduler = new LibraryScheduler(jdbcTemplate);

        when(jdbcTemplate.query(contains("SELECT TOP 1 ts.SoNgayNhacTruocHan"), any(RowMapper.class)))
                .thenReturn(List.of(3));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(3)))
                .thenReturn(List.of(
                        new LibraryScheduler.CardReminder(
                                "DG001", "TK001", LocalDate.now().plusDays(2)
                        )
                ));
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), anyString(), anyString(), anyString()))
                .thenReturn(0);

        scheduler.sendCardExpiryReminders();

        verify(jdbcTemplate).update(
                contains("INSERT INTO THONGBAO"),
                anyString(),
                eq("TK001"),
                eq("TB_SAP_HET_HAN_THE"),
                anyString(),
                anyString()
        );
    }

    @Test
    void idGenerationIsUniquePerCall() {
        LibraryScheduler scheduler = new LibraryScheduler(jdbcTemplate);
        java.lang.reflect.Method method;
        try {
            method = LibraryScheduler.class.getDeclaredMethod("generateId", String.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
        method.setAccessible(true);
        String first;
        String second;
        try {
            first = (String) method.invoke(scheduler, "TB_SCH");
            second = (String) method.invoke(scheduler, "TB_SCH");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        assertThat(first).startsWith("TB_SCH_");
        assertThat(second).startsWith("TB_SCH_");
        assertThat(first).isNotEqualTo(second);
    }
}