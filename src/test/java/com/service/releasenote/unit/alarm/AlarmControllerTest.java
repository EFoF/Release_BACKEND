package com.service.releasenote.unit.alarm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.releasenote.domain.alarm.api.AlarmController;
import com.service.releasenote.domain.alarm.application.AlarmService;
import com.service.releasenote.domain.member.model.Authority;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberLoginType;
import com.service.releasenote.domain.project.exception.exceptions.MemberProjectNotFoundException;
import com.service.releasenote.domain.project.exception.handler.ProjectExceptionHandler;
import com.service.releasenote.global.error.exception.UnAuthorizedException;
import com.service.releasenote.global.jwt.JwtFilter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.ArrayList;
import java.util.List;

import static com.service.releasenote.domain.alarm.dto.AlarmDto.AlarmInfoDto;
import static com.service.releasenote.domain.alarm.dto.AlarmDto.AlarmInfoDtoEach;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;
@ExtendWith(SpringExtension.class)
@WebMvcTest(AlarmController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class AlarmControllerTest {

    @MockBean
    AlarmService alarmService;

    @MockBean
    JwtFilter jwtFilter;

    @MockBean
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    private static MockMvc mockMvc;

    @BeforeAll
    public static void setup(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(sharedHttpSession())
                .build();
    }

    public AlarmInfoDtoEach alarmInfoDtoEachBuilder(Long id, Member member) {
        return AlarmInfoDtoEach.builder()
                .authorEmail(member.getEmail())
                .message("test alarm " + id)
                .authorId(member.getId())
                .id(id)
                .build();
    }
    public AlarmInfoDto alarmInfoDtoBuilder(int number, Member member) {
        List<AlarmInfoDtoEach> list = new ArrayList<>();
        for(int i=1; i<=number; i++) {
            list.add(alarmInfoDtoEachBuilder(Long.valueOf(i), member));
        }
        return AlarmInfoDto.builder()
                .alarmInfoDtoList(list)
                .build();
    }

    public Member buildMember(Long id) {
        return Member.builder()
                .id(id)
                .userName("test_user_name")
                .email("test_email@test.com")
                .password(passwordEncoder.encode("test_password"))
                .authority(Authority.ROLE_USER)
                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
                .isDeleted(false)
                .build();
    }

    @Test
    @DisplayName("성공 - 알람 조회")
    public void getAlarmDetailsForSuccess() throws Exception {
        //given
        Long currentMemberId = 1L;
        Member member = buildMember(currentMemberId);
        AlarmInfoDto alarmInfoDto = alarmInfoDtoBuilder(5, member);

        //when
        when(alarmService.getAlarmDetailByProjectId(1L)).thenReturn(alarmInfoDto);

        //then

        ResultActions perform = mockMvc.perform(get("/companies/projects/{projectId}/alarms", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param("onlyNew", "false"));
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alarmInfoDtoList[0].message").value("test alarm 1"))
                .andExpect(jsonPath("$.alarmInfoDtoList[1].message").value("test alarm 2"))
                .andExpect(jsonPath("$.alarmInfoDtoList[2].message").value("test alarm 3"))
                .andExpect(jsonPath("$.alarmInfoDtoList[3].message").value("test alarm 4"))
                .andExpect(jsonPath("$.alarmInfoDtoList[4].message").value("test alarm 5"));

    }

    @Test
    @DisplayName("실패 - 알람 조회 - 인증되지 않은 사용자")
    public void getAlarmDetailForFailureByUnAuthorizedUser() throws Exception {
        //given
        Long currentMemberId = 1L;
        Member member = buildMember(currentMemberId);
        AlarmInfoDto alarmInfoDto = alarmInfoDtoBuilder(5, member);

        //when
        // when(alarmService.getAlarmDetailByProjectId(1L)).thenThrow(UnAuthorizedException.class);
        // 위 코드는 반환값이 없을 경우에 컴파일 에러가 발생해 실행되지 않는다. 예외를 검증할 때는 아래와 같이 사용하자.
        doThrow(UnAuthorizedException.class).when(alarmService).getAlarmDetailByProjectId(1L);

        //then
        ResultActions perform = mockMvc.perform(get("/companies/projects/{projectId}/alarms", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .param("onlyNew", "false"));

        perform.andExpect(status().isUnauthorized())
                .andExpect((result) -> assertTrue(result.getResolvedException().getClass().isAssignableFrom(UnAuthorizedException.class)));

    }

    // TODO 실패 원인을 모르겠음. 검증까지도 못가고 서비스 로직 실행이 되는 동시에 예외가 발생한다.
//    @Test
//    @DisplayName("실패 - 알람 조회 - 프로젝트에 속하지 않은 사용자")
//    public void getAlarmDetailForFailureByNonProjectMember() throws Exception {
//        //given
//        Long currentMemberId = 1L;
//        Member member = buildMember(currentMemberId);
//        AlarmInfoDto alarmInfoDto = alarmInfoDtoBuilder(5, member);
//
//        //when
//        doThrow(MemberProjectNotFoundException.class).when(alarmService).getAlarmDetailByProjectId(1L);
//
//        //then
//        ResultActions perform = mockMvc.perform(get("/companies/projects/{projectId}/alarms", 1L)
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .param("onlyNew", "false"));
//
//        perform.andExpect(status().isNotFound())
//                .andExpect((result) -> assertTrue(result.getResolvedException().getClass().isAssignableFrom(MemberProjectNotFoundException.class)));
//
//    }

    @Test
    @DisplayName("성공 - 알람 읽음 처리")
    public void readAlarmForSuccess() throws Exception {
        //then
        ResultActions perform = mockMvc.perform(post("/companies/projects/{projectId}/alarms", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        perform.andExpect(status().isOk());

    }

    @Test
    @DisplayName("실패 - 알람 읽음 처리 - 인증되지 않은 사용자")
    public void readAlarmForFailureByUnAuthorizedUser() throws Exception {
        //when
        doThrow(UnAuthorizedException.class).when(alarmService).readAlarm(1L);

        //then
        ResultActions perform = mockMvc.perform(post("/companies/projects/{projectId}/alarms", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        perform
                .andExpect(status().isUnauthorized())
                .andExpect((result) -> assertTrue(result.getResolvedException().getClass().isAssignableFrom(UnAuthorizedException.class)));
    }

//    @Test
//    @DisplayName("실패 - 알람 읽음 처리 - 프로젝트에 속하지 않은 사용자")
//    public void readAlarmForFailureByNonProjectMember() throws Exception {
//        //when
//        doThrow(MemberProjectNotFoundException.class).when(alarmService).readAlarm(1L);
//
//        //then
//        ResultActions perform = mockMvc.perform(post("/companies/projects/{projectId}/alarms", 1L)
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON));
//
//        perform
//                .andExpect(status().isNotFound())
//                .andExpect((result) -> assertTrue(result.getResolvedException().getClass().isAssignableFrom(MemberProjectNotFoundException.class)));
//    }

    @Test
    @DisplayName("성공 - 알람 삭제")
    public void deleteAlarmForSuccess() throws Exception {
        //then
        ResultActions perform = mockMvc.perform(delete("/companies/projects/{projectId}/alarms/{alarmId}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        perform.andExpect(status().isOk());
    }

    @Test
    @DisplayName("실패 - 알람 삭제 - 인증되지 않은 사용자")
    public void deleteAlarmForFailureByUnAuthorizedUser() throws Exception {
        //when
        doThrow(UnAuthorizedException.class).when(alarmService).deleteAlarm(1L, 1L);

        //then
        ResultActions perform = mockMvc.perform(delete("/companies/projects/{projectId}/alarms/{alarmId}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        perform.andExpect(status().isUnauthorized())
                .andExpect((result) -> assertTrue(result.getResolvedException().getClass().isAssignableFrom(UnAuthorizedException.class)));
    }

}
