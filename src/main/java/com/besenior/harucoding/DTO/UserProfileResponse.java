package com.besenior.harucoding.DTO;

import com.besenior.harucoding.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {
    private Long id;
    private String nickname;
    private String email;
    private String profileImageUrl;
    private int level;
    private int xp;
    private int streakDays;
    private long totalSolved;
    private long correctCount;
    private double accuracyRate;
    private String preferredLanguage;

    // ── 온보딩 상태 ────────────────────────────────────────────────
    // 온보딩을 건너뛴 채로 앱을 쓰는 것을 막으려면 클라이언트가 "이 유저가 온보딩을
    // 마쳤는지"를 알아야 한다. 서버는 값을 갖고 있었지만 내려주지 않고 있었다.
    private String codingLevel;             // NONE / SOME / LOTS
    private boolean cotePrepared;
    private String recommendedDifficulty;   // 온보딩 미완료면 null
    private boolean onboardingCompleted;    // recommendedDifficulty 존재 여부

    public static UserProfileResponse from(User user, long totalSolved, long correctCount) {
        double accuracy = totalSolved == 0 ? 0.0
                : Math.round((double) correctCount / totalSolved * 1000) / 10.0;
        return UserProfileResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .level(user.getLevel())
                .xp(user.getXp())
                .streakDays(user.getStreakDays())
                .totalSolved(totalSolved)
                .correctCount(correctCount)
                .accuracyRate(accuracy)
                .preferredLanguage(user.getPreferredLanguage())
                .codingLevel(user.getCodingLevel())
                .cotePrepared(user.isCotePrepared())
                .recommendedDifficulty(user.getRecommendedDifficulty())
                .onboardingCompleted(user.getRecommendedDifficulty() != null)
                .build();
    }
}