package com.be_senior.Haru_Coding_backend.domain.user.repository;

import com.be_senior.Haru_Coding_backend.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByGoogleId(String googleId);

    boolean existsByGoogleId(String googleId);
}