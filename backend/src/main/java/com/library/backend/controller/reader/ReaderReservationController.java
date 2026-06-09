package com.library.backend.controller.reader;

import com.library.backend.dto.reader.ReaderReservationRequest;
import com.library.backend.dto.reader.ReaderReservationResponse;
import com.library.backend.security.AuthUser;
import com.library.backend.service.reader.ReaderReservationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reader/reservations")
public class ReaderReservationController {

    private final ReaderReservationService readerReservationService;

    public ReaderReservationController(ReaderReservationService readerReservationService) {
        this.readerReservationService = readerReservationService;
    }

    @GetMapping
    public List<ReaderReservationResponse> getMyReservations(
            @AuthenticationPrincipal AuthUser user
    ) {
        return readerReservationService.getMyReservations(user);
    }

    @PostMapping("/by-title")
    public ReaderReservationResponse reserveByTitle(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody ReaderReservationRequest request
    ) {
        return readerReservationService.reserveByTitle(user, request);
    }

    @PostMapping("/by-copy")
    public ReaderReservationResponse reserveByCopy(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody ReaderReservationRequest request
    ) {
        return readerReservationService.reserveByCopy(user, request);
    }

    @DeleteMapping("/{maPhieuDatTruoc}")
    public Map<String, Object> cancelReservation(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable String maPhieuDatTruoc
    ) {
        readerReservationService.cancelReservation(user, maPhieuDatTruoc);

        return Map.of(
                "message", "Hủy đặt trước thành công",
                "maPhieuDatTruoc", maPhieuDatTruoc
        );
    }
}
