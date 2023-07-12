package com.service.releasenote.domain.member.application;

import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.model.Authority;
import com.service.releasenote.domain.member.model.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    /** 로그인 시 DB 에서 유저정보, 권한정보 가져옴. */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) {
        log.info("Load By Username : " + username);
        return memberRepository.findOneWithAuthorityByEmail(username)
                .map(member -> createUser(member))
                .orElseThrow(() -> new UsernameNotFoundException(username + " -> 존재하지 않는 사용자입니다."));
    }

    /** Security User 객체를 생성한다. */
    private User createUser(Member member) {
        // 권한 정보와 id, password를 가지고 User 객체를 리턴해줌.
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(member.getAuthority().toString());

        return new User(
                member.getId().toString(), // getName() 으로 Username을 받는 게 아니라 pk 값을 받기 위함.
//                member.getUsername(),
                member.getPassword(),
                Collections.singleton(grantedAuthority)
        );
    }
}
