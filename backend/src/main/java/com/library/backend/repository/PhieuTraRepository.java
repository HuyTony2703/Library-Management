package com.library.backend.repository;

import com.library.backend.entity.PhieuTra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhieuTraRepository extends JpaRepository<PhieuTra, String> {
    Optional<PhieuTra> findByIdempotencyKey(String idempotencyKey);
}
