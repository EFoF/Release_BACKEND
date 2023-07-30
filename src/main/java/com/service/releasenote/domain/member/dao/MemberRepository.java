package com.service.releasenote.domain.member.dao;

import com.service.releasenote.domain.member.model.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    String findUserNameById(Long id);

    @Query(value = "select * from member m " +
            "join member_project mp on m.member_id = mp.member_id " +
            "where project_id = :project_id", nativeQuery = true)
    List<Member> findByProjectId(@Param("project_id") Long project_id);


}
