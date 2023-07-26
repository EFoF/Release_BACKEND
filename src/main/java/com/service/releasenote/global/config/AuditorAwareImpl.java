package com.service.releasenote.global.config;

import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.global.error.exception.NotSignInException;
import com.service.releasenote.global.error.exception.UnAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Slf4j
public class AuditorAwareImpl implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) { // authentication 에 인증 정보가 없는 경우
            throw new UnAuthorizedException();
        }
        if(authentication.getPrincipal().equals("anonymousUser")) { // 로그인 하지 않은 경우
//            throw new NotSignInException();
            return Optional.empty();
        }
        return Optional.ofNullable(Long.parseLong(authentication.getName()));
    }
}
