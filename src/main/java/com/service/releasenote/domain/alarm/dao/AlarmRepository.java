package com.service.releasenote.domain.alarm.dao;

import com.service.releasenote.domain.alarm.model.Alarm;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long>, AlarmCustomRepository {

    @EntityGraph(attributePaths = {"member"})
    List<Alarm> findByMemberProjectId(Long memberProjectId);

    @EntityGraph(attributePaths = {"member"})
    List<Alarm> findByMemberProjectIdAndIsCheckedFalse(Long memberProjectId);
}
