package com.service.releasenote.domain.Alarm.dao;

import com.service.releasenote.domain.Alarm.model.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

}
