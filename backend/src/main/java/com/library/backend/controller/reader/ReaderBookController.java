package com.library.backend.controller.reader;

import com.library.backend.dto.reader.ReaderBookCopyResponse;
import com.library.backend.dto.reader.ReaderBookDetailResponse;
import com.library.backend.dto.reader.ReaderBookListResponse;
import com.library.backend.service.reader.ReaderBookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reader/books")
public class ReaderBookController {

    private final ReaderBookService readerBookService;

    public ReaderBookController(ReaderBookService readerBookService) {
        this.readerBookService = readerBookService;
    }

    @GetMapping
    public List<ReaderBookListResponse> getBooks(
            @RequestParam(required = false) String keyword
    ) {
        return readerBookService.getBooks(keyword);
    }

    @GetMapping("/{maDauSach}")
    public ReaderBookDetailResponse getBookDetail(
            @PathVariable String maDauSach
    ) {
        return readerBookService.getBookDetail(maDauSach);
    }

    @GetMapping("/{maDauSach}/copies")
    public List<ReaderBookCopyResponse> getBookCopies(
            @PathVariable String maDauSach
    ) {
        return readerBookService.getBookCopies(maDauSach);
    }
}
