package com.service.releasenote.memberProject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.releasenote.domain.category.dto.CategoryDto;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.api.MemberProjectController;
import com.service.releasenote.domain.member.application.MemberProjectService;
import com.service.releasenote.domain.member.dto.MemberDTO;
import com.service.releasenote.domain.member.dto.MemberProjectDTO.*;
import com.service.releasenote.domain.member.model.*;
import com.service.releasenote.domain.project.api.ProjectController;
import com.service.releasenote.domain.project.application.ProjectService;
import com.service.releasenote.domain.project.dto.ProjectDto;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.global.annotations.WithMockCustomUser;
import com.service.releasenote.global.jwt.JwtFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@WebMvcTest(MemberProjectController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class memberProjectControllerTest {
    @MockBean
    PasswordEncoder passwordEncoder;
    @MockBean
    ProjectService projectService;
    @MockBean
    MemberProjectService memberProjectService;
    @MockBean
    JwtFilter jwtFilter;
    @Autowired
    ObjectMapper objectMapper;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(sharedHttpSession())
                .build();
    }

    public Company buildCompany(Long id) {
        return Company.builder()
                .ImageURL("test image url")
                .name("teset company name " + id)
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

    public Member buildMember(Long id) { // Test 용 멤버 생성
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

    public MemberProject buildMemberProject(Long id, Project project, Member member) {
        return MemberProject.builder()
                .id(id)
                .project(project)
                .member(member)
                .build();
    }

    public AddProjectMemberRequestDto SaveProjectMemberRequestDto() {
        return AddProjectMemberRequestDto.builder()
                .email("test_email@test.com")
                .build();
    }

    public AddProjectMemberResponseDto SaveProjectMemberResponseDto() {
        return AddProjectMemberResponseDto.builder()
                .member_id(1L)
                .project_id(1L)
                .role(Role.MEMBER)
                .name("test_user_name")
                .build();
    }

    public MemberDTO.MemberListDTO getMemberEachDto(Long id) {
        return MemberDTO.MemberListDTO.builder()
                .id(id)
                .name("test_user_name " + id)
                .email("test_user_email " + id)
                .build();
    }

    public FindMemberListByProjectId getProjectMemberResponseDto(int number) {
        List<MemberDTO.MemberListDTO> list = new ArrayList<>();
        for(int i=1; i<=number; i++) {
            list.add(getMemberEachDto(Long.valueOf(i)));
        }
        return FindMemberListByProjectId.builder()
                .memberListDTOS(list)
                .build();
    }


    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 프로젝트 멤버 추가 테스트")
    public void saveProjectMemberForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member = buildMember(1L);
        MemberProject memberProject = buildMemberProject(1L, project, member);

        AddProjectMemberRequestDto addProjectMemberRequestDto = SaveProjectMemberRequestDto();
        AddProjectMemberResponseDto addProjectMemberResponseDto = SaveProjectMemberResponseDto();

        //when
        when(memberProjectService.addProjectMember(any(), any(), any())).thenReturn(addProjectMemberResponseDto);

        //then
        mockMvc.perform(post("/api/companies/projects/{project_id}/members", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addProjectMemberRequestDto)))
                .andExpect(jsonPath("$.member_id").value(1L))
                .andExpect(jsonPath("$.project_id").value(1L))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.name").value("test_user_name"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("성공 - 프로젝트 멤버 모두 조회")
    public void getProjectMemberForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member = buildMember(1L);
        MemberProject memberProject = buildMemberProject(1L, project, member);
        FindMemberListByProjectId findMemberListByProjectId = getProjectMemberResponseDto(3);

        //when
        when(memberProjectService.findProjectMemberList(project.getId())).thenReturn(findMemberListByProjectId);

        //then
        mockMvc.perform(get("/api/companies/projects/{project_id}/members", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberListDTOS[0].name").value("test_user_name 1"))
                .andExpect(jsonPath("$.memberListDTOS[1].name").value("test_user_name 2"))
                .andExpect(jsonPath("$.memberListDTOS[2].name").value("test_user_name 3"))
                .andDo(print());
    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 프로젝트 멤버 삭제 테스트")
    public void deleteProjectMemberForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Member member = buildMember(1L);
        MemberProject memberProject = buildMemberProject(1L, project, member);

        //when & then
        mockMvc.perform(delete("/api/companies/projects/{project_id}/members", 1L)
                        .header("email", "test1@naver.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
