package com.library.backend.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class TokenService {

    private final String secret;
    private final long expirationMinutes;
    private final ObjectMapper objectMapper;

    public TokenService(
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${app.security.jwt-expiration-minutes}") long expirationMinutes,
            ObjectMapper objectMapper
    ) {
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
        this.objectMapper = objectMapper;
    }

    public String generateToken(AuthUser user) {
        try {
            Map<String, Object> header = Map.of(
                    "alg", "HS256",
                    "typ", "JWT"
            );

            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", user.getTenDangNhap());
            payload.put("maTaiKhoan", user.getMaTaiKhoan());
            payload.put("maVaiTro", user.getMaVaiTro());
            payload.put("tenVaiTro", user.getTenVaiTro());
            payload.put("maDocGia", user.getMaDocGia());
            payload.put("maNhanVien", user.getMaNhanVien());
            payload.put("hoTen", user.getHoTen());
            payload.put("tokenVersion", user.getTokenVersion());
            payload.put("exp", Instant.now().plusSeconds(expirationMinutes * 60).getEpochSecond());

            String headerPart = base64UrlEncode(objectMapper.writeValueAsBytes(header));
            String payloadPart = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
            String unsignedToken = headerPart + "." + payloadPart;
            String signature = sign(unsignedToken);

            return unsignedToken + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo token");
        }
    }

    public AuthUser parseToken(String token) {
        try {
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                throw new RuntimeException("Token không hợp lệ");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);

            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8)
            )) {
                throw new RuntimeException("Chữ ký token không hợp lệ");
            }

            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            Map<String, Object> payload = objectMapper.readValue(
                    payloadBytes,
                    new TypeReference<Map<String, Object>>() {}
            );

            long exp = ((Number) payload.get("exp")).longValue();

            if (Instant.now().getEpochSecond() > exp) {
                throw new RuntimeException("Token đã hết hạn");
            }

            Number tokenVersion = (Number) payload.get("tokenVersion");
            return new AuthUser(
                    (String) payload.get("maTaiKhoan"),
                    (String) payload.get("sub"),
                    (String) payload.get("maVaiTro"),
                    (String) payload.get("tenVaiTro"),
                    (String) payload.get("maDocGia"),
                    (String) payload.get("maNhanVien"),
                    (String) payload.get("hoTen"),
                    tokenVersion == null ? -1 : tokenVersion.longValue(),
                    false
            );
        } catch (Exception e) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        mac.init(key);

        return base64UrlEncode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
