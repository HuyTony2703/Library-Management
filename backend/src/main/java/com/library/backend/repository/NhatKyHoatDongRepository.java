package com.library.backend.repository;

import com.library.backend.entity.NhatKyHoatDong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NhatKyHoatDongRepository extends JpaRepository<NhatKyHoatDong, Long> {

    Page<NhatKyHoatDong> findAllByOrderByThoiGianDesc(Pageable pageable);

    Page<NhatKyHoatDong> findByDoiTuongTacDongAndMaDoiTuongTacDongOrderByThoiGianDesc(
            String doiTuongTacDong,
            String maDoiTuongTacDong,
            Pageable pageable
    );
}
