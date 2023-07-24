package com.service.releasenote.global.oauth;

import com.service.releasenote.domain.member.model.Authority;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberLoginType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@Slf4j
@ToString
@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes; // OAuth2 반환하는 유저 정보
    private String nameAttributeKey;
    private String name;
    private String email;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
    }

    /* of()
     * OAuth2User 에서 반환하는 사용자 정보는 Map 이기 때문에 값 하나하나 변환
     */
    public static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .attributes(attributes) // 소셜 로그인에서 API 가 제공하는 유저 정보들 Json 값
                .nameAttributeKey(userNameAttributeName) // OAuth2 키 값
                .build();
    }

    /** toEntity()
     * Member 엔티티 생성
     */
    public Member toEntity() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedEmail = passwordEncoder.encode(email);
        return Member.builder()
                .userName(name)
                .email(email)
                .password(encodedEmail) // 인코딩된 멤버의 이메일 주소
                .authority(Authority.ROLE_USER)
                .memberLoginType(MemberLoginType.GOOGLE_LOGIN)
                .build();
    }
}