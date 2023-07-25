package com.service.releasenote.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class MailDTO {
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmailCodeRequestDTO {
        @NotBlank(message = "이메일을 입력해주세요.")
        @Pattern(regexp = "^\\s*$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "이메일 형식이 유효하지 않습니다.")
        private String email;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmailVerificationRequestDTO {
        @NotBlank(message = "이메일을 입력해주세요.")
        @Pattern(regexp = "^\\s*$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "이메일 형식이 유효하지 않습니다.")
        private String email;

        private String inputCode;
    }
}
