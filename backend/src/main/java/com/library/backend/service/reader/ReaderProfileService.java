package com.library.backend.service.reader;

import com.library.backend.dto.reader.ReaderProfileResponse;
import com.library.backend.entity.DocGia;
import com.library.backend.exception.BusinessException;
import com.library.backend.exception.ResourceNotFoundException;
import com.library.backend.repository.DocGiaRepository;
import com.library.backend.security.AuthUser;
import org.springframework.stereotype.Service;

@Service
public class ReaderProfileService {

    private final DocGiaRepository docGiaRepository;

    public ReaderProfileService(DocGiaRepository docGiaRepository) {
        this.docGiaRepository = docGiaRepository;
    }

    public ReaderProfileResponse getCurrentReaderProfile(AuthUser user) {
        if (user == null) {
            throw new BusinessException("Bạn chưa đăng nhập");
        }

        if (user.getMaDocGia() == null || user.getMaDocGia().isBlank()) {
            throw new BusinessException("Tài khoản hiện tại không phải tài khoản độc giả");
        }

        DocGia docGia = docGiaRepository.findById(user.getMaDocGia())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy độc giả với mã: " + user.getMaDocGia()
                ));

        return ReaderProfileResponse.from(docGia);
    }
}
