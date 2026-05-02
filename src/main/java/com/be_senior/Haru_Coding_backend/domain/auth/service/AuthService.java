package com.be_senior.Haru_Coding_backend.domain.auth.service;

import com.be_senior.Haru_Coding_backend.domain.auth.dto.LoginDto;
import com.be_senior.Haru_Coding_backend.domain.auth.dto.LoginResponse;
import com.be_senior.Haru_Coding_backend.domain.auth.dto.SignupDto;
import com.be_senior.Haru_Coding_backend.domain.auth.entity.RefreshTokenEntity;
import com.be_senior.Haru_Coding_backend.domain.auth.repository.RefreshTokenRepository;
import com.be_senior.Haru_Coding_backend.domain.auth.service.GoogleTokenVerifier.GoogleUserInfo;
import com.be_senior.Haru_Coding_backend.domain.user.entity.UserEntity;
import com.be_senior.Haru_Coding_backend.domain.user.repository.UserRepository;
import com.be_senior.Haru_Coding_backend.global.exception.CustomException;
import com.be_senior.Haru_Coding_backend.global.exception.ErrorCode;
import com.be_senior.Haru_Coding_backend.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final BCryptPasswordEncoder passwordEncoder;

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

        return issueTokens(user);
    }

    @Transactional
    public LoginResponse signup(SignupDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        UserEntity user = userRepository.save(
                UserEntity.builder()
                        .email(dto.getEmail())
                        .nickname(dto.getNickname())
                        .password(passwordEncoder.encode(dto.getPassword()))
                        .build()
        );

        return issueTokens(user);
    }

    @Transactional
    public LoginResponse login(LoginDto dto) {
        UserEntity user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        return issueTokens(user);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private LoginResponse issueTokens(UserEntity user) {
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
}