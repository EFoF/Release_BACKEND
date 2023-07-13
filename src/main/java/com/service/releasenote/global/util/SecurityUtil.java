package com.service.releasenote.global.util;

import com.service.releasenote.global.error.exception.NotSignInException;
import com.service.releasenote.global.error.exception.UnAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public class SecurityUtil {
    // 인증된 멤버의 pk 값을 확인하는 메서드
    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//        log.info("authentication:{}", authentication);

        if (authentication == null) { // authentication 에 인증 정보가 없는 경우
            throw new UnAuthorizedException();
        }

        if(authentication.getPrincipal().equals("anonymousUser")) { // 로그인 하지 않은 경우
            throw new NotSignInException();
        }

        String name = authentication.getName();
        Long memberId = Long.valueOf(name);

        return memberId;
    }
}
