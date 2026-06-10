package com.library.backend.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "BUSINESS_ERROR",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "BUSINESS_ERROR",
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ErrorResponse response = new ErrorResponse(
                "Dữ liệu gửi lên không hợp lệ",
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Dữ liệu không hợp lệ: " + ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        String message = "Body JSON bị thiếu hoặc sai định dạng";

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {
            String fieldName = invalidFormatException.getPath().isEmpty()
                    ? "không xác định"
                    : invalidFormatException.getPath().get(0).getFieldName();

            message = "Sai kiểu dữ liệu tại field '" + fieldName + "'";
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_JSON",
                message,
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String message = "Tham số '" + ex.getName() + "' sai kiểu dữ liệu";

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "TYPE_MISMATCH",
                message,
                request
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        String message = "Thiếu tham số bắt buộc: " + ex.getParameterName();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "MISSING_PARAMETER",
                message,
                request
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                "Phương thức HTTP không được hỗ trợ cho API này",
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        String rootMessage = getRootMessage(ex);
        SqlErrorMessage sqlErrorMessage = mapSqlError(rootMessage);

        return buildResponse(
                sqlErrorMessage.status(),
                "DATA_INTEGRITY_VIOLATION",
                sqlErrorMessage.message(),
                request
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(
            DataAccessException ex,
            HttpServletRequest request
    ) {
        ex.printStackTrace();

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "DATABASE_SYNC_ERROR",
                buildDatabaseSyncMessage(ex),
                request
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Bạn cần đăng nhập để thực hiện thao tác này",
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "Bạn không có quyền thực hiện thao tác này",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            HttpServletRequest request
    ) {
        ex.printStackTrace();

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Lỗi hệ thống. Vui lòng thử lại sau",
                request
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                message,
                errorCode,
                status.value(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }

    private String getRootMessage(Throwable throwable) {
        Throwable root = throwable;

        while (root.getCause() != null) {
            root = root.getCause();
        }

        return root.getMessage() == null ? "" : root.getMessage();
    }

    private SqlErrorMessage mapSqlError(String rootMessage) {
        if (rootMessage == null) {
            return new SqlErrorMessage(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu không hợp lệ theo ràng buộc database"
            );
        }

        if (rootMessage.contains("UX_CTPM_CUONSACH_DANGMUON")) {
            return new SqlErrorMessage(
                    HttpStatus.CONFLICT,
                    "Cuốn sách này đang được mượn, không thể mượn lần nữa"
            );
        }

        if (rootMessage.contains("UX_CUONSACH_MAVACH")) {
            return new SqlErrorMessage(
                    HttpStatus.CONFLICT,
                    "Mã vạch cuốn sách đã tồn tại"
            );
        }

        if (rootMessage.contains("UX_CUONSACH_QRCODE")) {
            return new SqlErrorMessage(
                    HttpStatus.CONFLICT,
                    "Mã QR cuốn sách đã tồn tại"
            );
        }

        if (rootMessage.contains("UX_DAUSACH_ISBN")) {
            return new SqlErrorMessage(
                    HttpStatus.CONFLICT,
                    "ISBN đầu sách đã tồn tại"
            );
        }

        if (rootMessage.contains("CK_CTPM_NGAY")) {
            return new SqlErrorMessage(
                    HttpStatus.BAD_REQUEST,
                    "Hạn trả phải lớn hơn ngày mượn"
            );
        }

        if (rootMessage.contains("CK_CTPM_TRANGTHAI")) {
            return new SqlErrorMessage(
                    HttpStatus.BAD_REQUEST,
                    "Trạng thái chi tiết phiếu mượn không hợp lệ"
            );
        }

        if (rootMessage.contains("CK_PM_TRANGTHAI")) {
            return new SqlErrorMessage(
                    HttpStatus.BAD_REQUEST,
                    "Trạng thái phiếu mượn không hợp lệ"
            );
        }

        if (rootMessage.contains("CK_CTPT_TINHTRANG")) {
            return new SqlErrorMessage(
                    HttpStatus.BAD_REQUEST,
                    "Tình trạng khi trả chỉ được là Bình thường, Hỏng hoặc Mất"
            );
        }

        if (rootMessage.contains("CK_CTPT_TIEN")) {
            return new SqlErrorMessage(
                    HttpStatus.BAD_REQUEST,
                    "Tiền phạt hoặc số ngày trễ không hợp lệ"
            );
        }

        if (rootMessage.contains("CK_KHOANNO_TIEN")) {
            return new SqlErrorMessage(
                    HttpStatus.BAD_REQUEST,
                    "Số tiền nợ không hợp lệ. Số tiền đã thanh toán không được vượt quá số tiền phát sinh"
            );
        }

        if (rootMessage.contains("CK_KHOANNO_TRANGTHAI")) {
            return new SqlErrorMessage(
                    HttpStatus.BAD_REQUEST,
                    "Trạng thái khoản nợ không hợp lệ"
            );
        }

        if (rootMessage.contains("CK_PHIEUTHU_LOAITHU")) {
            return new SqlErrorMessage(
                    HttpStatus.BAD_REQUEST,
                    "Loại thu chỉ được là Thu tiền phạt hoặc Thu tiền mua gói"
            );
        }

        if (rootMessage.contains("CK_PHIEUTHU_TIEN")) {
            return new SqlErrorMessage(
                    HttpStatus.BAD_REQUEST,
                    "Số tiền thu không hợp lệ"
            );
        }

        if (rootMessage.contains("CK_CTPTN_TIEN")) {
            return new SqlErrorMessage(
                    HttpStatus.BAD_REQUEST,
                    "Số tiền áp dụng cho khoản nợ phải lớn hơn 0"
            );
        }

        if (rootMessage.contains("FOREIGN KEY")
                || rootMessage.contains("REFERENCE constraint")
                || rootMessage.contains("FK_")) {
            return new SqlErrorMessage(
                    HttpStatus.BAD_REQUEST,
                    "Dữ liệu liên kết không tồn tại. Hãy kiểm tra lại mã độc giả, nhân viên, chi nhánh, đầu sách, cuốn sách hoặc quy định"
            );
        }

        if (rootMessage.contains("duplicate")
                || rootMessage.contains("UNIQUE")
                || rootMessage.contains("PRIMARY KEY")) {
            return new SqlErrorMessage(
                    HttpStatus.CONFLICT,
                    "Dữ liệu đã tồn tại, vui lòng kiểm tra lại mã hoặc thông tin duy nhất"
            );
        }

        return new SqlErrorMessage(
                HttpStatus.BAD_REQUEST,
                "Dữ liệu không hợp lệ theo ràng buộc database"
        );
    }

    private String buildDatabaseSyncMessage(Throwable throwable) {
        String rootMessage = getRootMessage(throwable);
        String message = "Database chưa đồng bộ với backend. Hãy chạy lại các script database mới nhất.";

        if (rootMessage.contains("Invalid object name")
                || rootMessage.contains("Invalid column name")
                || rootMessage.contains("Could not find stored procedure")) {
            return message + " Chi tiết: " + rootMessage;
        }

        return message;
    }

    private record SqlErrorMessage(HttpStatus status, String message) {
    }
}
