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
public class AuditorAwareImpl implements AuditorAware<String> {

    private final MemberRepository memberRepository;

    public AuditorAwareImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) { // authentication 에 인증 정보가 없는 경우
            throw new UnAuthorizedException();
        }
        if(authentication.getPrincipal().equals("anonymousUser")) { // 로그인 하지 않은 경우
            throw new NotSignInException();
        }
        Long memberId = Long.valueOf(authentication.getName());
        Member member = memberRepository.findById(memberId).orElseThrow(RuntimeException::new);
        // 조회때 N+1이 발생하는 것 보다는 수정이나 생성처럼 상대적으로 수요가 적은 요청을 처리할때 id -> name으로 바꾸기 위한 쿼리를 작성한다.
        return Optional.ofNullable(member.getUserName());
    }
}
