package com.besenior.harucoding.service;

import com.besenior.harucoding.DTO.LoginRequest;
import com.besenior.harucoding.DTO.LoginResponse;
import com.besenior.harucoding.DTO.SignupRequest;
import com.besenior.harucoding.entity.RefreshToken;
import com.besenior.harucoding.repository.RefreshTokenRepository;
import com.besenior.harucoding.service.GoogleTokenVerifier.GoogleUserInfo;
import com.besenior.harucoding.entity.User;
import com.besenior.harucoding.repository.UserRepository;
import com.besenior.harucoding.global.crypto.AesGcmEncryptor;
import com.besenior.harucoding.global.exception.CustomException;
import com.besenior.harucoding.global.exception.ErrorCode;
import com.besenior.harucoding.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AesGcmEncryptor encryptor;

    @Transactional
    public LoginResponse googleLogin(String idToken) {
        GoogleUserInfo userInfo = googleTokenVerifier.verify(idToken);

        User user = userRepository.findByGoogleId(userInfo.googleId())
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .googleId(userInfo.googleId())
                                .email(userInfo.email())
                                .emailHash(encryptor.hash(userInfo.email()))
                                .nickname(userInfo.name())
                                .profileImageUrl(userInfo.picture())
                                .build()
                ));

        return issueTokens(user);
    }

    @Transactional
    public LoginResponse signup(SignupRequest dto) {
        if (userRepository.existsByEmailHash(encryptor.hash(dto.getEmail()))) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        User user = userRepository.save(
                User.builder()
                        .email(dto.getEmail())
                        .emailHash(encryptor.hash(dto.getEmail()))
                        .nickname(dto.getNickname())
                        .password(passwordEncoder.encode(dto.getPassword()))
                        .build()
        );

        return issueTokens(user);
    }

    @Transactional
    public LoginResponse login(LoginRequest dto) {
        User user = userRepository.findByEmailHash(encryptor.hash(dto.getEmail()))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        return issueTokens(user);
    }

    @Transactional
    public LoginResponse refresh(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));

        if (tokenEntity.isExpired()) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = userRepository.findById(tokenEntity.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return issueTokens(user);
    }

    /**
     * 로그아웃 — 서버에 남아 있는 refresh token을 제거한다.
     *
     * 로그아웃은 멱등해야 한다(이미 로그아웃했거나 토큰이 만료돼도 실패로 만들지 않는다).
     * 지우지 못하면 그 refresh token으로 계속 재발급이 가능해지므로
     * refreshToken → accessToken 순으로 최대한 시도한다.
     */
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // 1순위: refresh token 자체로 삭제. access token이 만료된 뒤에도 동작한다.
        if (StringUtils.hasText(refreshToken)) {
            var found = refreshTokenRepository.findByToken(refreshToken);
            if (found.isPresent()) {
                refreshTokenRepository.delete(found.get());
                return;
            }
        }

        // 2순위: access token이 아직 유효하면 그 유저의 refresh token을 전부 삭제.
        if (StringUtils.hasText(accessToken) && jwtProvider.validateToken(accessToken)) {
            refreshTokenRepository.deleteByUserId(jwtProvider.getUserId(accessToken));
        }

        // 둘 다 실패해도 예외를 던지지 않는다 — 클라이언트는 어차피 로컬 토큰을 지우고 나간다.
    }

    private LoginResponse issueTokens(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());

        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.flush();
        refreshTokenRepository.save(
                RefreshToken.builder()
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
