package com.service.releasenote.domain.alarm.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Message {
    private Long memberId;
    private Long projectId;
    private String memberName;
    private String content;

    @Builder
    public Message(Long memberId, Long projectId, String memberName, String content) {
        this.memberId = memberId;
        this.projectId = projectId;
        this.memberName = memberName;
        this.content = content;
    }
}
