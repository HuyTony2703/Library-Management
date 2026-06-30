package com.library.backend.repository;

import com.library.backend.entity.PhieuMuon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhieuMuonRepository extends JpaRepository<PhieuMuon, String> {
    Optional<PhieuMuon> findByIdempotencyKey(String idempotencyKey);
}
