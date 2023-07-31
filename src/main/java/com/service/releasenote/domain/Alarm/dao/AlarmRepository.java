package com.service.releasenote.domain.Alarm.dao;

import com.service.releasenote.domain.Alarm.model.Alarm;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    @EntityGraph(attributePaths = {"member"})
    List<Alarm> findByMemberProjectId(Long memberProjectId);

    @EntityGraph(attributePaths = {"member"})
    List<Alarm> findByMemberProjectIdAndIsCheckedFalse(Long memberProjectId);
}
