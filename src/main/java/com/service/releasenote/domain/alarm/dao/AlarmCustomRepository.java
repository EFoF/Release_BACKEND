package com.service.releasenote.domain.alarm.dao;

import com.service.releasenote.domain.alarm.model.Alarm;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface AlarmCustomRepository {

    Slice<Alarm> findMyAlarmsAsSlice(Pageable pageable, Long currentMemberId);
    List<Alarm> findMyAlarm(Long currentMemberId);
}
