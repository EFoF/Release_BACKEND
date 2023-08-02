package com.service.releasenote.project;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.releasenote.domain.category.dto.CategoryDto;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.project.api.ProjectController;
import com.service.releasenote.domain.project.application.ProjectService;
import com.service.releasenote.domain.project.dto.ProjectDto.*;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.global.jwt.JwtFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ProjectController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class ProjectControllerTest {

    @MockBean
    ProjectService projectService;

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

    public CreateProjectRequestDto createProjectSaveRequest() {
        return CreateProjectRequestDto.builder()
                .description("test project description")
                .title("test project title")
                .scope(true)
                .build();
    }

    public UpdateProjectRequestDto updateProjectRequest() {
        return UpdateProjectRequestDto.builder()
                .description("modified project description")
                .title("modified project title")
                .scope(false)
                .build();
    }

    @Test
    @DisplayName("성공 - 프로젝트 생성 테스트")
    public void saveProjectForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        CreateProjectRequestDto projectSaveRequest = createProjectSaveRequest();

        //when
        when(projectService.createProject(any(), any())).thenReturn(project.getId());

        //then
        mockMvc.perform(post("/companies/{company_id}/projects", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectSaveRequest)))
                .andExpect(content().string(project.getId().toString()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("성공 - 프로젝트 수정 테스트")
    public void modifyProjectForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        UpdateProjectRequestDto updateProjectRequestDto = updateProjectRequest();

        //when & then
        mockMvc.perform(put("/companies/projects/{project_id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProjectRequestDto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("성공 - 프로젝트 삭제 테스트")
    public void deleteProjectForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);

        //when & then
        mockMvc.perform(delete("/companies/{company_id}/projects/{project_id}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

}