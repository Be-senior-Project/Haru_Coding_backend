package com.besenior.harucoding.controller;

import com.besenior.harucoding.DTO.RecommendationFilterDto;
import com.besenior.harucoding.DTO.UserProfileDto;
import com.besenior.harucoding.global.jwt.JwtProvider;
import com.besenior.harucoding.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final JwtProvider jwtProvider;

    /**
     * 신규 유저 온보딩 추천
     * 가입 완료 시 호출 — coding_level, cote_prepared 기반
     *
     * 대상 유저는 바디의 userId가 아니라 access token에서 정한다.
     * 바디 값을 그대로 믿으면 남의 온보딩 결과를 덮어쓸 수 있다.
     */
    @PostMapping("/onboarding")
    public ResponseEntity<RecommendationFilterDto> onboarding(
            @RequestHeader("Authorization") String token,
            @RequestBody UserProfileDto profile) {
        Long userId = jwtProvider.getUserId(token.replace("Bearer ", ""));
        return ResponseEntity.ok(recommendationService.recommendOnboarding(userId, profile));
    }
}
