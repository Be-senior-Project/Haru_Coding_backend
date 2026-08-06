package com.besenior.harucoding.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * POST /api/auth/refresh 요청 바디.
 *
 * 기존에는 refresh token을 Authorization 헤더로 받았는데, Authorization은 access token
 * 자리이므로 규격이 어긋났고 클라이언트 구현(바디 전송)과도 맞지 않았다. 바디로 통일한다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank
    private String refreshToken;
}
