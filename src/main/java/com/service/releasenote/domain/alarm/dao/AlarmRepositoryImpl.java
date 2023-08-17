package com.service.releasenote.domain.alarm.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.service.releasenote.domain.alarm.model.Alarm;
import com.service.releasenote.domain.alarm.model.QAlarm;
import com.service.releasenote.domain.member.model.QMember;
import com.service.releasenote.domain.member.model.QMemberProject;
import com.service.releasenote.domain.project.model.QProject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static com.service.releasenote.domain.alarm.model.QAlarm.*;
import static com.service.releasenote.domain.member.model.QMember.*;
import static com.service.releasenote.domain.member.model.QMemberProject.*;
import static com.service.releasenote.domain.project.model.QProject.*;

@RequiredArgsConstructor
public class AlarmRepositoryImpl implements AlarmCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<Alarm> findMyAlarmsAsSlice(Pageable pageable, Long currentMemberId) {
        List<Alarm> alarmList = jpaQueryFactory
                .selectFrom(alarm)
                .join(alarm.memberProject, memberProject)
//                .join(memberProject.member, member)
                .where(memberProject.member.id.eq(currentMemberId))
                .fetch();
        boolean hasNext = false;
        if(alarmList.size() > pageable.getPageSize()) {
            alarmList.remove(pageable.getPageSize());
            hasNext = true;
        }
        return new SliceImpl<>(alarmList, pageable, hasNext);
    }

    @Override
    public List<Alarm> findMyAlarm(Long currentMemberId) {
        List<Alarm> alarmList = jpaQueryFactory
                .selectFrom(alarm)
                .join(alarm.memberProject, memberProject).fetchJoin()
                .join(memberProject.member, member).fetchJoin()
                .where(member.id.eq(currentMemberId))
                .fetch();
        return alarmList;
    }
}
