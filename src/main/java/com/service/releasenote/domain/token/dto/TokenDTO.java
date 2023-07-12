package com.service.releasenote.domain.token.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class TokenDTO {
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenInfoDTO {
        private String grantType; // OAuth2 프로토콜에서 사용되는 필드
        private String accessToken;
        private Long accessTokenExpiresIn;
        private Long refreshTokenExpiresIn;
        private String refreshToken;
    }
}

