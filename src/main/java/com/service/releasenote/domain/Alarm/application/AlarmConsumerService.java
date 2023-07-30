package com.service.releasenote.domain.Alarm.application;

import com.service.releasenote.domain.Alarm.dao.AlarmRepository;
import com.service.releasenote.domain.Alarm.model.Alarm;
import com.service.releasenote.domain.Alarm.model.Message;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.MemberProjectNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.model.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmConsumerService {

    private final MemberProjectRepository memberProjectRepository;
    private final MemberRepository memberRepository;
    private final AlarmRepository alarmRepository;
    @Transactional
    @RabbitListener(queues = {"doklib-queue-01"})
    public void consumeMessage(Message message) {
        Long projectId = message.getProjectId();
        Long memberId = message.getMemberId();
        Member author = memberRepository.findById(memberId).orElseThrow(UserNotFoundException::new);
        List<MemberProject> memberProjectList = memberProjectRepository.findMemberProjectByProjectId(projectId);
        for (MemberProject memberProject : memberProjectList) {
            Alarm alarm = Alarm.builder()
                    .message(message.getContent())
                    .memberProject(memberProject)
                    .member(author)
                    .isChecked(false)
                    .build();
            alarmRepository.save(alarm);
        }
        // 이후에 필요하면 웹소켓을 열어서 푸시알람을 보낼 수도 있음
    }
}
