package com.be_senior.Haru_Coding_backend.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {

    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
