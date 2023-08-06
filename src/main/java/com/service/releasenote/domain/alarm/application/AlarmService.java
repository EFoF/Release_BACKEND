package com.service.releasenote.domain.alarm.application;

import com.service.releasenote.domain.alarm.dao.AlarmRepository;
import com.service.releasenote.domain.alarm.exception.AlarmNotFoundException;
import com.service.releasenote.domain.alarm.model.Alarm;
import com.service.releasenote.domain.alarm.model.AlarmDomain;
import com.service.releasenote.domain.alarm.model.Message;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.MemberProjectNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.service.releasenote.domain.alarm.dto.AlarmDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmService {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.binding_key}")
    private String bindingKey;

    private final MemberProjectRepository memberProjectRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final AlarmRepository alarmRepository;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 메세지 produce 서비스 로직
     * @param projectId
     * @param content
     */
    public void produceMessage(Long projectId, Long domainId, String content, AlarmDomain alarmDomain) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        Member author = memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);
        Message message = Message.builder()
                .content(author.getUserName() + " 님이 " + content)
                .memberName(author.getUserName())
                .projectId(project.getId())
                .memberId(author.getId())
                .alarmDomain(alarmDomain)
                .domainId(domainId)
                .build();
        rabbitTemplate.convertAndSend(exchange, bindingKey, message);
    }

    /**
     * 메세지 consume 서비스 로직
     * @param message
     */
    @Transactional
    @RabbitListener(queues = {"doklib-queue-01"})
    public void consumeMessage(Message message) {
        Long projectId = message.getProjectId();
        Long memberId = message.getMemberId();
//        Member author = memberRepository.findById(memberId).orElseThrow(UserNotFoundException::new);
        Optional<Member> memberOptional = memberRepository.findById(memberId);
        if(memberOptional.isPresent()) {
            List<MemberProject> memberProjectList = memberProjectRepository.findMemberProjectByProjectId(projectId);
            for (MemberProject memberProject : memberProjectList) {
                Alarm alarm = Alarm.builder()
                        .alarmDomain(message.getAlarmDomain())
                        .domainId(message.getDomainId())
                        .message(message.getContent())
                        .memberProject(memberProject)
                        .member(memberOptional.get())
                        .isChecked(false)
                        .build();
                alarmRepository.save(alarm);
            }
        }
        // 이후에 필요하면 웹소켓을 열어서 푸시알람을 보낼 수도 있음
    }

    /**
     * 그룹에 속한 사용자별 알람 반환 (전체)
     * @param projectId
     * @return AlarmInfoDto
     */
    public AlarmInfoDto getAlarmDetailByProjectId(Long projectId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        MemberProject memberProject = memberProjectRepository.findByMemberIdAndProjectId(currentMemberId, projectId)
                .orElseThrow(MemberProjectNotFoundException::new);
        List<Alarm> alarmList = alarmRepository.findByMemberProjectId(memberProject.getId());
        List<AlarmInfoDtoEach> resultList = alarmList.stream().map(alarm -> convertToDto(alarm))
                .collect(Collectors.toList());
        return AlarmInfoDto.builder().alarmInfoDtoList(resultList).build();
    }

    /**
     * 그룹에 속한 사용자별 알람 반환 (읽지 않은 메세지만)
     * @param projectId
     * @return AlarmInfoDto
     */
    public AlarmInfoDto getAlarmDetailWithNotReadByProjectId(Long projectId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        MemberProject memberProject = memberProjectRepository.findByMemberIdAndProjectId(currentMemberId, projectId)
                .orElseThrow(MemberProjectNotFoundException::new);
        List<Alarm> alarmList = alarmRepository.findByMemberProjectIdAndIsCheckedFalse(memberProject.getId());
        List<AlarmInfoDtoEach> resultList = alarmList.stream().map(alarm -> convertToDto(alarm))
                .collect(Collectors.toList());
        return AlarmInfoDto.builder().alarmInfoDtoList(resultList).build();
    }


    /**
     * 알람 읽음 처리 서비스 로직
     * @param projectId
     */
    @Transactional
    public void readAlarm(Long projectId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        MemberProject memberProject = memberProjectRepository.findByMemberIdAndProjectId(currentMemberId, projectId)
                .orElseThrow(MemberProjectNotFoundException::new);
        List<Alarm> alarmList = alarmRepository.findByMemberProjectIdAndIsCheckedFalse(memberProject.getId());
        for (Alarm alarm : alarmList) {
            alarm.updateIsChecked(true);
        }
    }

    @Transactional
    public void deleteAlarm(Long projectId, Long alarmId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        MemberProject memberProject = memberProjectRepository.findByMemberIdAndProjectId(currentMemberId, projectId)
                .orElseThrow(MemberProjectNotFoundException::new);
        List<Alarm> alarmList = alarmRepository.findByMemberProjectId(memberProject.getId());
        if(!alarmList.stream().anyMatch(alarm -> alarm.getId().equals(alarmId))) {
            throw new AlarmNotFoundException("현재 멤버의 알람 리스트에 해당 알람이 존재하지 않습니다.");
        }
        Alarm alarm = alarmRepository.findById(alarmId).orElseThrow(AlarmNotFoundException::new);
        alarmRepository.delete(alarm);
    }

    private AlarmInfoDtoEach convertToDto(Alarm alarm) {
        return AlarmInfoDtoEach.builder()
                .authorEmail(alarm.getMember().getEmail())
                .authorId(alarm.getMember().getId())
                .message(alarm.getMessage())
                .id(alarm.getId())
                .build();
    }
}
