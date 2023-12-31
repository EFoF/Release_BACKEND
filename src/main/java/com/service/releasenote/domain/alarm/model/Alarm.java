package com.service.releasenote.domain.alarm.model;

import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberProject;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

import static javax.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor
public class Alarm {

    @Id
    @Column(name = "alarm_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_project_id")
    private MemberProject memberProject;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private boolean isChecked;

    @Enumerated(EnumType.STRING)
    private AlarmDomain alarmDomain;

    private Long domainId;

    public void updateIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    @Builder
    public Alarm(Long id, Long domainId, String message, MemberProject memberProject, Member member,  boolean isChecked, AlarmDomain alarmDomain) {
        this.memberProject = memberProject;
        this.alarmDomain = alarmDomain;
        this.isChecked = isChecked;
        this.domainId = domainId;
        this.message = message;
        this.member = member;
        this.id = id;
    }
}
