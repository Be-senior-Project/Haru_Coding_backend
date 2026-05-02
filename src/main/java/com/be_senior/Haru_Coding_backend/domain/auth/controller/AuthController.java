package com.be_senior.Haru_Coding_backend.domain.auth.controller;

import com.be_senior.Haru_Coding_backend.domain.auth.dto.GoogleLoginRequest;
import com.be_senior.Haru_Coding_backend.domain.auth.dto.LoginDto;
import com.be_senior.Haru_Coding_backend.domain.auth.dto.LoginResponse;
import com.be_senior.Haru_Coding_backend.domain.auth.dto.SignupDto;
import com.be_senior.Haru_Coding_backend.domain.auth.service.AuthService;
import com.be_senior.Haru_Coding_backend.global.jwt.JwtProvider;
import com.be_senior.Haru_Coding_backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @PostMapping("/google")
    public ApiResponse<LoginResponse> googleLogin(@RequestBody @Valid GoogleLoginRequest request) {
        LoginResponse response = authService.googleLogin(request.getIdToken());
        return ApiResponse.success(response);
    }

    @PostMapping("/signup")
    public ApiResponse<LoginResponse> signup(@RequestBody @Valid SignupDto dto) {
        return ApiResponse.success(authService.signup(dto));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginDto dto) {
        return ApiResponse.success(authService.login(dto));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@RequestHeader("Authorization") String token) {
        String refreshToken = token.replace("Bearer ", "");
        return ApiResponse.success(authService.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String token) {
        String accessToken = token.replace("Bearer ", "");
        Long userId = jwtProvider.getUserId(accessToken);
        authService.logout(userId);
        return ApiResponse.success("로그아웃 성공", null);
    }
}