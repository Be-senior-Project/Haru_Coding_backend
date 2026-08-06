package com.besenior.harucoding.controller;

import com.besenior.harucoding.DTO.GoogleLoginRequest;
import com.besenior.harucoding.DTO.LoginRequest;
import com.besenior.harucoding.DTO.LoginResponse;
import com.besenior.harucoding.DTO.LogoutRequest;
import com.besenior.harucoding.DTO.RefreshTokenRequest;
import com.besenior.harucoding.DTO.SignupRequest;
import com.besenior.harucoding.service.AuthService;
import com.besenior.harucoding.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ApiResponse<LoginResponse> googleLogin(@RequestBody @Valid GoogleLoginRequest request) {
        LoginResponse response = authService.googleLogin(request.getIdToken());
        return ApiResponse.success(response);
    }

    @PostMapping("/signup")
    public ApiResponse<LoginResponse> signup(@RequestBody @Valid SignupRequest dto) {
        return ApiResponse.success(authService.signup(dto));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest dto) {
        return ApiResponse.success(authService.login(dto));
    }

    /** refresh token은 Authorization(access token 자리)이 아니라 바디로 받는다. */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request.getRefreshToken()));
    }

    /**
     * 로그아웃 — 서버에 저장된 refresh token을 삭제한다.
     * 헤더·바디 모두 선택이며, 토큰이 없거나 만료됐어도 200을 돌려준다(멱등).
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) LogoutRequest request) {

        String accessToken  = (authorization != null) ? authorization.replace("Bearer ", "") : null;
        String refreshToken = (request != null) ? request.getRefreshToken() : null;

        authService.logout(accessToken, refreshToken);
        return ApiResponse.success("로그아웃 성공", null);
    }
}
