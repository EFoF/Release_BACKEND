package com.service.releasenote.domain.member.dao;

import com.service.releasenote.domain.member.model.MemberProject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProjectRepository extends JpaRepository<MemberProject, Long> {

}
