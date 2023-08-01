package com.service.releasenote.domain.alarm.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Message {
    private Long domainId;
    private Long memberId;
    private Long projectId;
    private String content;
    private String memberName;
    private AlarmDomain alarmDomain;

    @Builder
    public Message(Long domainId, Long memberId, Long projectId, String memberName, String content, AlarmDomain alarmDomain) {
        this.content = content;
        this.domainId = domainId;
        this.memberId = memberId;
        this.projectId = projectId;
        this.memberName = memberName;
        this.alarmDomain = alarmDomain;
    }
}
