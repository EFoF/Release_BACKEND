package com.service.releasenote.releases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.releasenote.domain.category.dto.CategoryDto;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectPermissionDeniedException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.domain.release.api.ReleaseController;
import com.service.releasenote.domain.release.application.ReleaseService;
import com.service.releasenote.domain.release.dto.ReleaseDto;
import com.service.releasenote.domain.release.exception.ReleasesNotFoundException;
import com.service.releasenote.domain.release.model.Releases;
import com.service.releasenote.domain.release.model.Tag;
import com.service.releasenote.global.error.exception.UnAuthorizedException;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.service.releasenote.domain.category.dto.CategoryDto.*;
import static com.service.releasenote.domain.release.dto.ReleaseDto.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ReleaseController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class ReleaseControllerTest {

    @MockBean
    ReleaseService releaseService;

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

    public SaveReleaseRequest createSaveReleaseRequest() {
        return SaveReleaseRequest.builder()
                .releaseDate(LocalDateTime.now())
                .message("test release message")
                .version("1.0.0")
                .tag(Tag.NEW)
                .build();
    }

    public ReleaseModifyRequestDto createModifyRequest() {
        return ReleaseModifyRequestDto.builder()
                .message("changed release message")
                .releaseDate(LocalDateTime.now())
                .tag(Tag.UPDATED)
                .version("1.0.1")
                .build();
    }

    public ReleaseDtoEach createReleaseDtoEach(int number) {
        return ReleaseDtoEach.builder()
                .lastModifiedTime(LocalDateTime.now())
                .content("test release " + number)
                .lastModifierName("tester")
                .version("1.0.0")
                .tag(Tag.NEW)
                .build();
    }
    public ReleaseInfoDto createReleaseInfoDto(int iter) {
        List<ReleaseDtoEach> list = new ArrayList<>();
        for(int i=1; i<=iter; i++) {
            ReleaseDtoEach releaseDtoEach = createReleaseDtoEach(i);
            list.add(releaseDtoEach);
        }
        return ReleaseInfoDto.builder()
                .releaseDtoList(list)
                .build();
    }

    public ProjectReleasesDtoEach createProjectReleaseDtoEach(int number, int each) {
        CategoryResponseDto categoryResponseDto = CategoryResponseDto.builder()
                .description("test category description " + number)
                .title("test category title " + number)
                .detail("## test category detail " + number)
                .lastModifiedTime(LocalDateTime.now())
                .lastModifierName("tester")
                .build();
        List<ReleaseDtoEach> list = new ArrayList<>();
        for(int i=1; i<=each; i++) {
            ReleaseDtoEach releaseDtoEach = createReleaseDtoEach(i);
            list.add(releaseDtoEach);
        }
        return ProjectReleasesDtoEach.builder()
                .categoryResponseDto(categoryResponseDto)
                .releaseDtoList(list)
                .build();
    }

    public ProjectReleasesDto createProjectReleaseDto(int number, int each) {
        List<ProjectReleasesDtoEach> list = new ArrayList<>();
        for (int i=1; i<=number; i++) {
            list.add(createProjectReleaseDtoEach(i, each));
        }
        return new ProjectReleasesDto(list);
    }

    public ReleaseModifyRequestDto createReleaseModifyRequestDto() {
        return ReleaseModifyRequestDto.builder()
                .releaseDate(LocalDateTime.now())
                .message("modified release")
                .version("1.0.1")
                .tag(Tag.UPDATED)
                .build();
    }

    public ReleaseModifyResponseDto createReleaseModifyResponseDto(ReleaseModifyRequestDto requestDto, Releases releases) {
        return ReleaseModifyResponseDto.builder()
                .lastModifierName(releases.getModifierName())
                .lastModifiedTime(releases.getModifiedDate())
                .releaseDate(releases.getReleaseDate())
                .message(requestDto.getMessage())
                .version(requestDto.getVersion())
                .tag(releases.getTag())
                .build();
    }

    @Test
    @DisplayName("성공 - 릴리즈 생성 테스트")
    public void saveReleaseForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        SaveReleaseRequest saveReleaseRequest = createSaveReleaseRequest();

        //when
        when(releaseService.saveRelease(saveReleaseRequest, project.getId(), category.getId()))
                .thenReturn(1L);

        //then
        mockMvc.perform(
                post("/companies/projects/{projectId}/categories/{categoryId}/releases", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveReleaseRequest)))
                .andDo(print())
                .andExpect(status().isCreated());

    }

    @Test
    @DisplayName("실패 - 릴리즈 생성 테스트 - 인증되지 않은 사용자")
    public void saveReleaseForFailureByUnAuthorizedUser() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        SaveReleaseRequest saveReleaseRequest = createSaveReleaseRequest();

        //when
        when(releaseService.saveRelease(saveReleaseRequest, project.getId(), category.getId()))
                .thenThrow(UnAuthorizedException.class);

        //then
        mockMvc.perform(
                        post("/companies/projects/{projectId}/categories/{categoryId}/releases", 1L, 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(saveReleaseRequest)))
                .andExpect(status().is2xxSuccessful())
//                .andExpect(content().string("Security Context에 인증 정보가 없습니다."))
                .andDo(print());
    }

//    @Test
//    @DisplayName("실패 - 릴리즈 생성 테스트 - 존재하지 않는 프로젝트")
//    public void saveReleaseForFailureByNotExistsProject() throws Exception {
//        //given
//        Company company = buildCompany(1L);
//        Project project = buildProject(company, 1L);
//        Category category = buildCategory(project, 1L);
//        Releases releases = buildReleases(category, 1L);
//        SaveReleaseRequest saveReleaseRequest = createSaveReleaseRequest();
//
//        //when
//        when(releaseService.saveRelease(saveReleaseRequest, project.getId(), category.getId()))
//                .thenThrow(ProjectNotFoundException.class);
//
//        //then
//        mockMvc.perform(post("/companies/projects/{projectId}/categories/{categoryId}/releases",
//                        1L, 1L)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(saveReleaseRequest)))
//                .andExpect(status().isConflict())
//                .andExpect(content().string("프로젝트를 찾을 수 없습니다."))
//                .andDo(print());
//
//    }
//
//    @Test
//    @DisplayName("실패 - 릴리즈 생성 테스트 - 프로젝트에 속하지 않은 사용자")
//    public void saveProjectForFailureByNonProjectMember() throws Exception {
//        //given
//        Company company = buildCompany(1L);
//        Project project = buildProject(company, 1L);
//        Category category = buildCategory(project, 1L);
//        Releases releases = buildReleases(category, 1L);
//        SaveReleaseRequest saveReleaseRequest = createSaveReleaseRequest();
//
//        //when
//        when(releaseService.saveRelease(saveReleaseRequest, project.getId(), category.getId()))
//                .thenThrow(ProjectPermissionDeniedException.class);
//
//        //then
//        mockMvc.perform(post("/companies/projects/{projectId}/categories/{categoryId}/releases",
//                        1L, 1L)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(saveReleaseRequest)))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("프로젝트 수정 권한이 없습니다."))
//                .andDo(print());
//
//    }

    @Test
    @DisplayName("성공 - 카테고리 하위 릴리즈 모두 조회 테스트")
    public void getReleasesWithCategoryForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        // 카테고리 하위에 3개의 릴리즈가 있다고 가정
        ReleaseInfoDto releaseInfoDto = createReleaseInfoDto(3);

        //when
        when(releaseService.findReleasesByCategoryId(category.getId())).thenReturn(releaseInfoDto);

        //then
        mockMvc.perform(get("/companies/projects/categories/{category_id}/releases", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseDtoList[0].content")
                        .value("test release 1"))
                .andExpect(jsonPath("$.releaseDtoList[1].content")
                        .value("test release 2"))
                .andExpect(jsonPath("$.releaseDtoList[2].content")
                        .value("test release 3"))
                .andDo(print());

    }

    @Test
    @DisplayName("성공 - 프로젝트 하위 릴리즈 모두 조회 테스트")
    public void getReleasesWithProjectForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        // 프로젝트 하위에 3개의 카테고리가 있고, 각각 4개의 릴리즈가 있다고 가정
        ProjectReleasesDto projectReleaseDto = createProjectReleaseDto(3, 4);

        //when
        when(releaseService.findReleasesByProjectId(project.getId())).thenReturn(projectReleaseDto);

        //then
        mockMvc.perform(get("/companies/projects/{project_id}/categories/releases", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectReleasesDto[0].categoryResponseDto.title")
                        .value("test category title 1"))
                .andExpect(jsonPath("$.projectReleasesDto[0].releaseDtoList[0].content")
                        .value("test release 1"))
                .andExpect(jsonPath("$.projectReleasesDto[0].releaseDtoList[1].content")
                        .value("test release 2"))
                .andExpect(jsonPath("$.projectReleasesDto[0].releaseDtoList[2].content")
                        .value("test release 3"))
                .andExpect(jsonPath("$.projectReleasesDto[0].releaseDtoList[3].content")
                        .value("test release 4"))
                .andExpect(jsonPath("$.projectReleasesDto[1].categoryResponseDto.title")
                        .value("test category title 2"))
                .andExpect(jsonPath("$.projectReleasesDto[2].categoryResponseDto.title")
                        .value("test category title 3"))
                .andDo(print());

    }

    @Test
    @DisplayName("성공 - 릴리즈 수정 테스트")
    public void modifyReleaseForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        ReleaseModifyRequestDto requestDto = createReleaseModifyRequestDto();
        ReleaseModifyResponseDto responseDto = createReleaseModifyResponseDto(requestDto, releases);

        //when
        when(releaseService.findReleaseAndConvert(category.getId())).thenReturn(responseDto);

        //then
        mockMvc.perform(put("/companies/projects/{project_id}/categories/{category_id}/releases/{release_id}",
                1L, 1L,1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("modified release"))
                .andExpect(jsonPath("$.version").value("1.0.1"))
                .andDo(print());
    }

    @Test
    @DisplayName("실패 - 릴리즈 수정 테스트 - 존재하지 않는 릴리즈")
    public void modifyReleaseForFailureByNotExistsRelease() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        ReleaseModifyRequestDto requestDto = createReleaseModifyRequestDto();
        ReleaseModifyResponseDto responseDto = createReleaseModifyResponseDto(requestDto, releases);
        ReleasesNotFoundException exception = new ReleasesNotFoundException();
        //when
        when(releaseService.findReleaseAndConvert(category.getId())).thenThrow(exception);

        //then
        mockMvc.perform(put("/companies/projects/{project_id}/categories/{category_id}/releases/{release_id}",
                        1L, 1L,1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(content().string("해당 릴리즈를 찾을 수 없습니다."))
//                .andExpect(jsonPath("$.message").value("해당 릴리즈를 찾을 수 없습니다."))
//                .andExpect(jsonPath("$.exceptionName").value("ReleasesNotFoundException"))
                .andDo(print());

    }

    @Test
    @DisplayName("성공 - 릴리즈 삭제 테스트")
    public void deleteReleaseForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);

        //when
        when(releaseService.deleteRelease(project.getId(), category.getId(), releases.getId()))
                .thenReturn("deleted");

        //then
        mockMvc.perform(delete("/companies/projects/{project_id}/categories/{category_id}/releases/{release_id}",
                1L,1L,1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("deleted"))
                .andDo(print());

    }

}
