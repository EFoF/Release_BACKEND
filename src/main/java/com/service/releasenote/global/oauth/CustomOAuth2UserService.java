package com.service.releasenote.global.oauth;

import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.model.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> service = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = service.loadUser(userRequest);  // OAuth2로 부터 유저 정보를 가져옴.

        // 소셜 정보를 가져옴. (Google, Kakao, Naver 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        /** userNameAttributeName
         * OAuth2 로그인 진행 시 키가 되는 값 (=Primary Key)
         * 구글 기본 코드: sub
         */
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 소셜 로그인에서 API 가 제공하는 유저 정보들 Json 값
        Map<String, Object> originAttributes = oAuth2User.getAttributes();
        OAuthAttributes attributes = OAuthAttributes.ofGoogle(userNameAttributeName, originAttributes);

        Member member = saveOrUpdate(attributes);

        // Member 객체를 사용하여 리턴할 사용자 정보에 추가
        Map<String, Object> userAttributes = new HashMap<>(attributes.getAttributes());
        userAttributes.put("member", member);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getAuthority().toString())),
                userAttributes,
                attributes.getNameAttributeKey()
        );
    }

    /**
     * 이미 존재하는 회원이라면 이름 업데이트
     *  처음 가입하는 회원이라면 Member 테이블을 생성
     */
    @Transactional
    public Member saveOrUpdate (OAuthAttributes attributes){
        Member member = memberRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.updateUsername(attributes.getName()))
                .orElse(attributes.toEntity());

        return memberRepository.save(member);
    }
}
