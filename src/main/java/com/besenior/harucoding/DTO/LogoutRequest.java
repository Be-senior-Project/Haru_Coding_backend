package com.besenior.harucoding.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * POST /api/auth/logout 요청 바디(선택).
 *
 * refreshToken을 함께 보내면 access token이 이미 만료된 상황에서도 서버 세션을
 * 확실히 정리할 수 있다. 생략하면 Authorization 헤더의 access token으로 처리한다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {

    private String refreshToken;
}
