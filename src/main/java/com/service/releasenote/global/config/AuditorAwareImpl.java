package com.service.releasenote.global.config;

import com.service.releasenote.global.error.exception.NotSignInException;
import com.service.releasenote.global.error.exception.UnAuthorizedException;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) { // authentication 에 인증 정보가 없는 경우
            throw new UnAuthorizedException();
        }
        if(authentication.getPrincipal().equals("anonymousUser")) { // 로그인 하지 않은 경우
            throw new NotSignInException();
        }
        Long memberId = Long.valueOf(authentication.getName());
        return Optional.ofNullable(memberId);
    }
}
