package com.service.releasenote.alarm;

import com.service.releasenote.domain.alarm.application.AlarmService;
import com.service.releasenote.domain.alarm.dao.AlarmRepository;
import com.service.releasenote.domain.alarm.dto.AlarmDto;
import com.service.releasenote.domain.alarm.exception.AlarmNotFoundException;
import com.service.releasenote.domain.alarm.model.Alarm;
import com.service.releasenote.domain.alarm.model.AlarmDomain;
import com.service.releasenote.domain.alarm.model.Message;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.model.Authority;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberLoginType;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.MemberProjectNotFoundException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.domain.release.model.Releases;
import com.service.releasenote.domain.release.model.Tag;
import com.service.releasenote.global.annotations.WithMockCustomUser;
import com.service.releasenote.global.error.exception.UnAuthorizedException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.service.releasenote.domain.alarm.dto.AlarmDto.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class AlarmServiceTest {

    @MockBean
    MemberProjectRepository memberProjectRepository;
    @MockBean
    ProjectRepository projectRepository;
    @MockBean
    MemberRepository memberRepository;
    @MockBean
    AlarmRepository alarmRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AlarmService alarmService;

    public Message buildMessage(Long alarmId, Long domainId, Long memberId, Long projectId, AlarmDomain alarmDomain) {
        return Message.builder()
                .memberName("test user " + memberId)
                .content("test alarm" + alarmId)
                .alarmDomain(alarmDomain)
                .projectId(projectId)
                .domainId(domainId)
                .memberId(memberId)
                .build();
    }

    public Alarm buildAlarm(Long id, MemberProject memberProject, Member member, Message message) {
        return Alarm.builder()
                .id(id)
                .alarmDomain(message.getAlarmDomain())
                .domainId(message.getDomainId())
                .memberProject(memberProject)
                .message("test alarm " + id)
                .isChecked(false)
                .member(member)
                .build();
    }

    public List<Alarm> buildAlarmList(int number, MemberProject memberProject, Member member, Message message) {
        List<Alarm> list = new ArrayList<>();
        for(int i=1; i<=number; i++) {
            list.add(buildAlarm(Long.valueOf(i), memberProject, member, message));
        }
        return list;
    }

    public Member buildMember(Long id) { // Test 용 멤버 생성
        return Member.builder()
                .id(id)
                .password(passwordEncoder.encode("test_password"))
                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
                .authority(Authority.ROLE_USER)
                .email("test_email@test.com")
                .userName("test_user_name")
                .isDeleted(false)
                .build();
    }

    public Company buildCompany(Long id) {
        return Company.builder()
                .name("teset company name " + id)
                .ImageURL("test image url")
                .id(id)
                .build();
    }

    public Project buildProject(Company company, Long id) {
        return Project.builder()
                .description("test project description " + id)
                .title("test project title " + id)
                .company(company)
                .scope(true)
                .id(id)
                .build();
    }

    public Category buildCategory(Project project, Long id) {
        return Category.builder()
                .description("test category description " + id)
                .detail("test category detail " + id)
                .title("test category title " + id)
                .project(project)
                .id(id)
                .build();
    }

    public Releases buildReleases(Category category, Long id) {
        return Releases.builder()
                .message("test release message " + id)
                .releaseDate(LocalDateTime.now())
                .category(category)
                .version("1.0.0")
                .tag(Tag.NEW)
                .id(id)
                .build();
    }

    public MemberProject buildMemberProject(Long id, Member member, Project project) {
        return MemberProject.builder()
                .project(project)
                .member(member)
                .id(id)
                .build();
    }

    @Test
    @DisplayName("성공 - 프로젝트 아이디로 알람 조회")
    public void getAlarmDetailByProjectIdForSuccess() throws Exception {
        //given
        Long currentMemberId = 1L;
        Member member = buildMember(currentMemberId);
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        MemberProject memberProject = buildMemberProject(1L, member, project);
        Category category = buildCategory(project, 1L);
        Message message = buildMessage(1L, category.getId(), member.getId(), project.getId(), AlarmDomain.CATEGORY);
        List<Alarm> alarms = buildAlarmList(5, memberProject, member, message);

        //when
        when(memberProjectRepository.findByMemberIdAndProjectId(member.getId(), project.getId()))
                .thenReturn(Optional.ofNullable(memberProject));
        when(alarmRepository.findByMemberProjectId(memberProject.getId())).thenReturn(alarms);

        //then
        AlarmInfoDto alarmInfoDto = alarmService.getAlarmDetailByProjectId(project.getId(), currentMemberId);
        assertThat(alarmInfoDto.getAlarmInfoDtoList()).extracting("message")
                .contains("test alarm 1", "test alarm 2", "test alarm 3", "test alarm 4", "test alarm 5");
    }

    @Test
    @DisplayName("성공 - 프로젝트 아이디로 알람 조회 - 비어있는 알람 리스트")
    public void getAlarmDetailByProjectIdForSuccessWithEmptyResult() throws Exception {
        //given
        Long currentMemberId = 1L;
        Member member = buildMember(currentMemberId);
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        MemberProject memberProject = buildMemberProject(1L, member, project);
        Category category = buildCategory(project, 1L);
        Message message = buildMessage(1L, category.getId(), member.getId(), project.getId(), AlarmDomain.CATEGORY);
        List<Alarm> alarms = buildAlarmList(5, memberProject, member, message);

        //when
        when(memberProjectRepository.findByMemberIdAndProjectId(member.getId(), project.getId()))
                .thenReturn(Optional.ofNullable(memberProject));
        when(alarmRepository.findByMemberProjectId(memberProject.getId())).thenReturn(new ArrayList<>());

        //then
        AlarmInfoDto alarmInfoDto = alarmService.getAlarmDetailByProjectId(project.getId(), currentMemberId);
        assertThat(alarmInfoDto.getAlarmInfoDtoList()).isEmpty();
    }


    @Test
    @DisplayName("실패 - 프로젝트 아이디로 알람 조회 - 프로젝트에 속하지 않은 사용자")
    public void getAlarmDetailByProjectIdForFailureByNonProjectMember() throws Exception {
        //given
        Long currentMemberId = 1L;
        Member member = buildMember(currentMemberId);
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        MemberProject memberProject = buildMemberProject(1L, member, project);
        Category category = buildCategory(project, 1L);
        Message message = buildMessage(1L, category.getId(), member.getId(), project.getId(), AlarmDomain.CATEGORY);
        List<Alarm> alarms = buildAlarmList(5, memberProject, member, message);

        //when
        when(memberProjectRepository.findByMemberIdAndProjectId(member.getId(), project.getId()))
                .thenReturn(Optional.empty());
        when(alarmRepository.findByMemberProjectId(memberProject.getId())).thenReturn(alarms);

        //then
        assertThrows(MemberProjectNotFoundException.class, () -> alarmService.getAlarmDetailByProjectId(project.getId(), currentMemberId));
    }

    @Test
    @DisplayName("성공 - 알람 읽음 처리")
    public void readAlarmForSuccess() throws Exception {
        //given
        Long currentMemberId = 1L;
        Member member = buildMember(currentMemberId);
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        MemberProject memberProject = buildMemberProject(1L, member, project);
        Category category = buildCategory(project, 1L);
        Message message = buildMessage(1L, category.getId(), member.getId(), project.getId(), AlarmDomain.CATEGORY);
        List<Alarm> alarms = buildAlarmList(5, memberProject, member, message);

        //when
        when(memberProjectRepository.findByMemberIdAndProjectId(member.getId(), project.getId()))
                .thenReturn(Optional.ofNullable(memberProject));
        when(alarmRepository.findByMemberProjectIdAndIsCheckedFalse(memberProject.getId())).thenReturn(alarms);

        //then
        alarmService.readAlarm(project.getId(), currentMemberId);
        // 데이터가 정상적으로 들어가있을 때, 예외가 발생하지 않는 것을 확인
    }

    @Test
    @DisplayName("실패 - 알람 읽음 처리 - 프로젝트에 속하지 않은 사용자")
    public void readAlarmForFailureByNonProjectMember() throws Exception {
        //given
        Long currentMemberId = 1L;
        Member member = buildMember(currentMemberId);
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        MemberProject memberProject = buildMemberProject(1L, member, project);
        Category category = buildCategory(project, 1L);
        Message message = buildMessage(1L, category.getId(), member.getId(), project.getId(), AlarmDomain.CATEGORY);
        List<Alarm> alarms = buildAlarmList(5, memberProject, member, message);

        //when
        when(memberProjectRepository.findByMemberIdAndProjectId(member.getId(), project.getId()))
                .thenReturn(Optional.empty());
        when(alarmRepository.findByMemberProjectIdAndIsCheckedFalse(memberProject.getId())).thenReturn(alarms);

        //then
        assertThrows(MemberProjectNotFoundException.class, () -> alarmService.readAlarm(project.getId(), currentMemberId));
    }

    @Test
    @DisplayName("성공 - 알람 삭제")
    public void deleteAlarmForSuccess() throws Exception {
        //given
        Long currentMemberId = 1L;
        Member member = buildMember(currentMemberId);
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        MemberProject memberProject = buildMemberProject(1L, member, project);
        Category category = buildCategory(project, 1L);
        Message message = buildMessage(1L, category.getId(), member.getId(), project.getId(), AlarmDomain.CATEGORY);
        List<Alarm> alarms = buildAlarmList(5, memberProject, member, message);
        Alarm deleteTarget = alarms.get(2);

        //when
        when(memberProjectRepository.findByMemberIdAndProjectId(member.getId(), project.getId()))
                .thenReturn(Optional.ofNullable(memberProject));
        when(alarmRepository.findByMemberProjectId(memberProject.getId())).thenReturn(alarms);
        when(alarmRepository.findById(deleteTarget.getId())).thenReturn(Optional.ofNullable(deleteTarget));

        //then
        alarmService.deleteAlarm(project.getId(), deleteTarget.getId(), currentMemberId);
    }


    @Test
    @DisplayName("실패 - 알람 삭제 - 프로젝트에 속하지 않은 사용자")
    public void deleteAlarmForFailureByNonProjectMember() throws Exception {
        //given
        Long currentMemberId = 1L;
        Member member = buildMember(currentMemberId);
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        MemberProject memberProject = buildMemberProject(1L, member, project);
        Category category = buildCategory(project, 1L);
        Message message = buildMessage(1L, category.getId(), member.getId(), project.getId(), AlarmDomain.CATEGORY);
        List<Alarm> alarms = buildAlarmList(5, memberProject, member, message);
        Alarm deleteTarget = alarms.get(2);

        //when
        when(memberProjectRepository.findByMemberIdAndProjectId(member.getId(), project.getId()))
                .thenReturn(Optional.empty());
        when(alarmRepository.findByMemberProjectId(memberProject.getId())).thenReturn(alarms);
        when(alarmRepository.findById(deleteTarget.getId())).thenReturn(Optional.ofNullable(deleteTarget));

        //then
        assertThrows(MemberProjectNotFoundException.class, () -> alarmService.deleteAlarm(project.getId(), deleteTarget.getId(), currentMemberId));
    }

    @Test
    @DisplayName("실패 - 알람 삭제 - 존재하지 않는 알람")
    public void deleteAlarmForFailureByNotExistsAlarm() throws Exception {
        //given
        Long currentMemberId = 1L;
        Member member = buildMember(currentMemberId);
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        MemberProject memberProject = buildMemberProject(1L, member, project);
        Category category = buildCategory(project, 1L);
        Message message = buildMessage(1L, category.getId(), member.getId(), project.getId(), AlarmDomain.CATEGORY);
        List<Alarm> alarms = buildAlarmList(5, memberProject, member, message);
        Alarm deleteTarget = alarms.get(2);

        //when
        when(memberProjectRepository.findByMemberIdAndProjectId(member.getId(), project.getId()))
                .thenReturn(Optional.ofNullable(memberProject));
        when(alarmRepository.findByMemberProjectId(memberProject.getId())).thenReturn(alarms);
        when(alarmRepository.findById(deleteTarget.getId())).thenReturn(Optional.empty());

        //then
        assertThrows(AlarmNotFoundException.class, () -> alarmService.deleteAlarm(project.getId(), deleteTarget.getId(), currentMemberId));
    }
}
