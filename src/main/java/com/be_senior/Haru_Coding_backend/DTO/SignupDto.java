package com.be_senior.Haru_Coding_backend.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupDto {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String nickname;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    private String passwordConfirm;
}