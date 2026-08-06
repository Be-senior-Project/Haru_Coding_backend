package com.besenior.harucoding.service;

import com.besenior.harucoding.DTO.RecommendationFilterDto;
import com.besenior.harucoding.DTO.UserProfileDto;
import com.besenior.harucoding.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 온보딩(신규 유저) 추천 전용.
 *
 * 온보딩 질문이 coding_level(NONE/SOME/LOTS) × cote_prepared(false/true) 두 개뿐이라
 * 나올 수 있는 결과가 6가지로 고정된다. 매번 GPT를 부를 이유가 없어 사전 정의한
 * 프리셋 6개에서 바로 꺼내 쓴다. (토큰 비용 0 / 응답 지연 0 / 문구 일관성 확보)
 *
 * 기존 유저 개인화 추천은 ProblemRecommendationService(/api/recommendations) 담당.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserRepository userRepository;

    /** 온보딩 결과 한 벌. language는 유저 선호 언어를 따르므로 여기 두지 않는다. */
    private record OnboardingPreset(
            String difficulty,
            List<Integer> topicIds,
            String type,
            String style,
            String reason,
            String focusPoint
    ) {}

    /**
     * 온보딩 결과 6가지. key = codingLevel + "|" + cotePrepared
     *
     * topicIds: 1=배열/문자열 2=스택/큐 3=해시맵 4=정렬 5=탐욕법
     *           6=이진탐색 7=DFS/BFS 8=동적프로그래밍 9=그래프 10=수학
     * type   : ProblemType  (IMPLEMENTATION / DEBUGGING / FILL_IN_THE_BLANK)
     * style  : ProblemStyle (CONCEPT / DEBUG / BLANK / IMPLEMENTATION / MATCH)
     */
    private static final Map<String, OnboardingPreset> PRESETS = Map.of(
            "NONE|false", new OnboardingPreset(
                    "입문", List.of(1), "FILL_IN_THE_BLANK", "CONCEPT",
                    "코딩이 처음이시군요! 부담 없는 입문 난이도 빈칸 채우기 문제로 차근차근 시작해봐요.",
                    "변수·조건문·반복문 기본 문법 익히기"),

            "NONE|true", new OnboardingPreset(
                    "초급", List.of(1, 4), "FILL_IN_THE_BLANK", "CONCEPT",
                    "코딩 테스트를 준비 중이시네요! 기초 문법을 다지면서 초급 문제로 감을 잡아봐요.",
                    "배열·문자열 다루기와 기본 정렬 이해하기"),

            "SOME|false", new OnboardingPreset(
                    "초급", List.of(1, 2), "IMPLEMENTATION", "IMPLEMENTATION",
                    "기초 문법을 알고 계시니 초급 구현 문제로 직접 코드를 써보는 연습을 해봐요.",
                    "자료구조 기초와 반복문 응용력 기르기"),

            "SOME|true", new OnboardingPreset(
                    "중급", List.of(3, 4, 6), "IMPLEMENTATION", "IMPLEMENTATION",
                    "코테 준비 경험이 있으시군요! 중급 난이도로 실전 감각을 끌어올려봐요.",
                    "해시맵·이진탐색으로 시간복잡도 줄이기"),

            "LOTS|false", new OnboardingPreset(
                    "중급", List.of(2, 3, 7), "DEBUGGING", "DEBUG",
                    "개발 경험이 충분하시네요! 알고리즘 풀이에 익숙해지도록 중급 디버깅 문제부터 시작해봐요.",
                    "탐색 알고리즘 익히고 코드 흐름 정확히 읽기"),

            "LOTS|true", new OnboardingPreset(
                    "고급", List.of(7, 8, 9), "IMPLEMENTATION", "IMPLEMENTATION",
                    "이미 준비가 잘 되어 있으시네요! 고급 난이도로 바로 실전 문제에 도전해봐요.",
                    "DP·그래프 등 고난도 유형 집중 공략")
    );

    // ── 온보딩 추천 (신규 유저) ────────────────────────────────────
    /** userId는 access token에서 온 값을 쓴다. 바디의 profile.userId는 신뢰하지 않는다. */
    public RecommendationFilterDto recommendOnboarding(Long userId, UserProfileDto profile) {
        String codingLevel = normalizeCodingLevel(profile.getCodingLevel());
        OnboardingPreset preset = PRESETS.get(codingLevel + "|" + profile.isCotePrepared());

        RecommendationFilterDto result = RecommendationFilterDto.builder()
                .difficulty(preset.difficulty())
                .topicIds(preset.topicIds())
                .type(preset.type())
                .style(preset.style())
                .language(resolveLanguage(profile.getPreferredLanguage()))
                .reason(preset.reason())
                .focusPoint(preset.focusPoint())
                .method("preset")
                .build();

        // users 테이블에 온보딩 결과 저장.
        // recommendedDifficulty가 채워지는 시점 = 온보딩 완료 시점이며,
        // GET /api/users/me 의 onboardingCompleted 가 이 값을 근거로 계산된다.
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                user.updateOnboarding(codingLevel, profile.isCotePrepared(), result.getDifficulty());
                userRepository.save(user);
            });
        }

        return result;
    }

    /** 알 수 없는/누락된 값은 가장 보수적인 NONE으로 취급해 프리셋 조회 실패를 막는다. */
    private String normalizeCodingLevel(String level) {
        return ("LOTS".equals(level) || "SOME".equals(level)) ? level : "NONE";
    }

    private String resolveLanguage(String preferredLanguage) {
        return (preferredLanguage == null || preferredLanguage.isBlank())
                ? "COMMON" : preferredLanguage;
    }
}
