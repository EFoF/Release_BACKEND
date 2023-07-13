package com.service.releasenote.domain.member.dao;

import com.service.releasenote.domain.member.model.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 쿼리가 수행될 때, Lazy 가 아닌 Eager 조회로 authority 정보를 같이 가져온다.
    @EntityGraph(attributePaths = "authority")
    // email 을 기준으로 Member 정보 가져올 때, 권한 정보도 같이 가져온다.
    Optional<Member> findOneWithAuthorityByEmail(String email);

    Optional<Member> findById(Long id);
}
