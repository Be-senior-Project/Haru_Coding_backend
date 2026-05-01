package com.be_senior.Haru_Coding_backend.domain.auth.service;

import com.be_senior.Haru_Coding_backend.domain.auth.dto.LoginResponse;
import com.be_senior.Haru_Coding_backend.domain.auth.entity.RefreshTokenEntity;
import com.be_senior.Haru_Coding_backend.domain.auth.repository.RefreshTokenRepository;
import com.be_senior.Haru_Coding_backend.domain.auth.service.GoogleTokenVerifier.GoogleUserInfo;
import com.be_senior.Haru_Coding_backend.domain.user.entity.UserEntity;
import com.be_senior.Haru_Coding_backend.domain.user.repository.UserRepository;
import com.be_senior.Haru_Coding_backend.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Transactional
    public LoginResponse googleLogin(String idToken) {
        GoogleUserInfo userInfo = googleTokenVerifier.verify(idToken);

        UserEntity user = userRepository.findByGoogleId(userInfo.googleId())
                .orElseGet(() -> userRepository.save(
                        UserEntity.builder()
                                .googleId(userInfo.googleId())
                                .email(userInfo.email())
                                .nickname(userInfo.name())
                                .profileImageUrl(userInfo.picture())
                                .build()
                ));

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(
                RefreshTokenEntity.builder()
                        .userId(user.getId())
                        .token(refreshToken)
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .build()
        );

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}