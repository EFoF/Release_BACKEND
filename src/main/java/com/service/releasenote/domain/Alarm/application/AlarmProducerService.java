package com.service.releasenote.domain.Alarm.application;

import com.service.releasenote.domain.Alarm.model.Message;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlarmProducerService {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.binding_key}")
    private String bindingKey;

    private final RabbitTemplate rabbitTemplate;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;

    public void produceMessage(Long projectId, String content) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        Member author = memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);
        Message message = Message.builder()
                .content(author.getUserName() + " 님이 " + content)
                .memberName(author.getUserName())
                .projectId(project.getId())
                .memberId(author.getId())
                .build();
        rabbitTemplate.convertAndSend(exchange, bindingKey, message);
    }
}
