package com.service.releasenote.releases;

import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.dto.CategoryDto;
import com.service.releasenote.domain.category.exception.CategoryNotFoundException;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectPermissionDeniedException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.domain.release.application.ReleaseService;
import com.service.releasenote.domain.release.dao.ReleaseRepository;
import com.service.releasenote.domain.release.dto.ReleaseDto;
import com.service.releasenote.domain.release.exception.ReleasesNotFoundException;
import com.service.releasenote.domain.release.model.Releases;
import com.service.releasenote.domain.release.model.Tag;
import com.service.releasenote.global.annotations.WithMockCustomUser;
import com.service.releasenote.global.error.exception.UnAuthorizedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.service.releasenote.domain.category.dto.CategoryDto.*;
import static com.service.releasenote.domain.release.dto.ReleaseDto.*;
import static com.service.releasenote.domain.release.dto.ReleaseDto.SaveReleaseRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ReleaseServiceTest {
    @MockBean
    MemberProjectRepository memberProjectRepository;

    @MockBean
    CategoryRepository categoryRepository;

    @MockBean
    ReleaseRepository releaseRepository;

    @MockBean
    ProjectRepository projectRepository;

    @MockBean
    MemberRepository memberRepository;

    @Autowired
    ReleaseService releaseService;

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

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 릴리즈 생성 테스트")
    public void saveReleaseForSuccess() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        SaveReleaseRequest saveReleaseRequest = createSaveReleaseRequest();

        //when
        when(projectRepository.existsById(project.getId())).thenReturn(true);
        when(memberProjectRepository.findMemberIdByProjectId(project.getId())).thenReturn(preparedMemberList);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.ofNullable(category));
        when(releaseRepository.save(any())).thenReturn(releases);

        //then
        Long savedId = releaseService.saveRelease(saveReleaseRequest, project.getId(), category.getId());
        assertThat(savedId).isEqualTo(releases.getId());

    }

    @Test
    @DisplayName("실패 - 릴리즈 생성 테스트 - 인증되지 않은 사용자")
    public void saveReleaseForFailureByUnAuthorizedUser() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        SaveReleaseRequest saveReleaseRequest = createSaveReleaseRequest();

        //when
        when(projectRepository.existsById(project.getId())).thenReturn(true);
        when(memberProjectRepository.findMemberIdByProjectId(project.getId())).thenReturn(preparedMemberList);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.ofNullable(category));
        when(releaseRepository.save(any())).thenReturn(releases);

        //then
        Assertions.assertThrows(UnAuthorizedException.class,
                () -> releaseService.saveRelease(saveReleaseRequest, project.getId(), category.getId()));

    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 릴리즈 생성 테스트 - 프로젝트에 속하지 않은 사용자")
    public void saveReleaseForFailureByNonProjectMember() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        SaveReleaseRequest saveReleaseRequest = createSaveReleaseRequest();

        //when
        when(projectRepository.existsById(project.getId())).thenReturn(true);
        when(memberProjectRepository.findMemberIdByProjectId(project.getId())).thenReturn(new ArrayList<>());
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.ofNullable(category));
        when(releaseRepository.save(any())).thenReturn(releases);

        //then
        Assertions.assertThrows(ProjectPermissionDeniedException.class,
                () -> releaseService.saveRelease(saveReleaseRequest, project.getId(), category.getId()));

    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 릴리즈 생성 테스트 - 존재하지 않는 프로젝트")
    public void saveReleasesForFailureByNotExistsProject() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        SaveReleaseRequest saveReleaseRequest = createSaveReleaseRequest();

        //when
        when(projectRepository.existsById(project.getId())).thenReturn(false);
        when(memberProjectRepository.findMemberIdByProjectId(project.getId())).thenReturn(new ArrayList<>());
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.ofNullable(category));
        when(releaseRepository.save(any())).thenReturn(releases);

        //then
        Assertions.assertThrows(ProjectNotFoundException.class,
                () -> releaseService.saveRelease(saveReleaseRequest, project.getId(), category.getId()));

    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 릴리즈 생성 - 존재하지 않는 카테고리")
    public void saveReleaseForFailureByNotExistsCategory() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        SaveReleaseRequest saveReleaseRequest = createSaveReleaseRequest();

        //when
        when(projectRepository.existsById(project.getId())).thenReturn(true);
        when(memberProjectRepository.findMemberIdByProjectId(project.getId())).thenReturn(preparedMemberList);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());
        when(releaseRepository.save(any())).thenReturn(releases);

        //then
        Assertions.assertThrows(CategoryNotFoundException.class,
                () -> releaseService.saveRelease(saveReleaseRequest, project.getId(), category.getId()));

    }

    @Test
    @DisplayName("성공 - 카테고리 하위 릴리즈 모두 조회")
    public void getReleasesWithCategoryForSuccess() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);
        List<Releases> releaseList = new ArrayList<>();

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases1 = buildReleases(category, 1L);
        Releases releases2 = buildReleases(category, 2L);
        Releases releases3 = buildReleases(category, 3L);
        releaseList.add(releases1);
        releaseList.add(releases2);
        releaseList.add(releases3);
        SaveReleaseRequest saveReleaseRequest = createSaveReleaseRequest();

        //when
        when(projectRepository.existsById(project.getId())).thenReturn(true);
        when(memberProjectRepository.findMemberIdByProjectId(project.getId())).thenReturn(preparedMemberList);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.ofNullable(category));
        when(releaseRepository.findByCategoryId(category.getId())).thenReturn(releaseList);

        //then
        ReleaseInfoDto resultDto = releaseService.findReleasesByCategoryId(category.getId(), false);
        assertThat(resultDto.getReleaseDtoList()).extracting("content")
                .contains("test release message 1", "test release message 2", "test release message 3");

    }
    
    @Test
    @DisplayName("성공 - 프로젝트 하위 릴리즈 조회")
    public void getReleasesWithProjectForSuccess() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);
        List<Releases> releaseList = new ArrayList<>();

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category1 = buildCategory(project, 1L);
        Category category2 = buildCategory(project, 2L);
        Releases releases1 = buildReleases(category1, 1L);
        Releases releases2 = buildReleases(category1, 2L);
        Releases releases3 = buildReleases(category1, 3L);
        Releases releases4 = buildReleases(category2, 4L);
        Releases releases5 = buildReleases(category2, 5L);
        Releases releases6 = buildReleases(category2, 6L);
        releaseList.add(releases1);
        releaseList.add(releases2);
        releaseList.add(releases3);
        releaseList.add(releases4);
        releaseList.add(releases5);
        releaseList.add(releases6);

        //when
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));
        
        //then
        ProjectReleasesDto resultDto = releaseService.findReleasesByProjectId(project.getId(), false);
        List<ProjectReleasesDtoEach> resultEach = resultDto.getProjectReleasesDto();
        int iter = 0, iterEach;
        for (ProjectReleasesDtoEach each : resultEach) {
            iterEach = iter;
            CategoryResponseDto categoryResponseDto = each.getCategoryResponseDto();
            assertThat(categoryResponseDto).extracting("test category title " + (iter+1));
            List<ReleaseDtoEach> releaseDtoList = each.getReleaseDtoList();
            assertThat(releaseDtoList).extracting("content")
                    .contains("test release message " + (iterEach * 3) + 1,
                            "test release message " + (iterEach * 3) + 2,
                            "test release message " + (iterEach * 3) + 3);
            iter++;
        }
    }

    @Test
    @DisplayName("실패 - 프로젝트 하위 릴리즈 조회 - 존재하지 않는 프로젝트")
    public void getReleasesWithProjectForFailureBy() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);

        //when
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(ProjectNotFoundException.class,
                () -> releaseService.findReleasesByProjectId(project.getId(), false));

    }

    @Test
    @DisplayName("실패 - 릴리즈 수정 테스트 - 인증되지 않은 사용자")
    public void modifyReleaseForFailureByUnAuthorizedUser() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        ReleaseModifyRequestDto modifyRequest = createModifyRequest();

        //when
        when(categoryRepository.existsByProjectId(project.getId())).thenReturn(true);
        when(memberProjectRepository.findMemberIdByProjectId(currentMemberId)).thenReturn(preparedMemberList);
        when(releaseRepository.findByCategoryIdAndReleaseId(category.getId(), releases.getId()))
                .thenReturn(Optional.ofNullable(releases));

        //then
        Assertions.assertThrows(UnAuthorizedException.class,
                () -> releaseService.modifyReleases(modifyRequest, project.getId(), category.getId(), releases.getId()));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 릴리즈 수정 테스트 - 프로젝트에 속하지 않은 사용자")
    public void modifyReleaseForFailureByNonProjectMember() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        ReleaseModifyRequestDto modifyRequest = createModifyRequest();

        //when
        when(categoryRepository.existsByProjectId(project.getId())).thenReturn(true);
        when(memberProjectRepository.findMemberIdByProjectId(currentMemberId)).thenReturn(new ArrayList<>());
        when(releaseRepository.findByCategoryIdAndReleaseId(category.getId(), releases.getId()))
                .thenReturn(Optional.ofNullable(releases));

        //then
        Assertions.assertThrows(ProjectPermissionDeniedException.class,
                () -> releaseService.modifyReleases(modifyRequest, project.getId(), category.getId(), releases.getId()));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 릴리즈 수정 테스트 - 존재하지 않는 카테고리")
    public void modifyReleaseForFailureByNotExistsCategory() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        ReleaseModifyRequestDto modifyRequest = createModifyRequest();

        //when
        when(categoryRepository.existsByProjectId(project.getId())).thenReturn(false);
        when(memberProjectRepository.findMemberIdByProjectId(currentMemberId)).thenReturn(preparedMemberList);
        when(releaseRepository.findByCategoryIdAndReleaseId(category.getId(), releases.getId()))
                .thenReturn(Optional.ofNullable(releases));

        //then
        Assertions.assertThrows(CategoryNotFoundException.class,
                () -> releaseService.modifyReleases(modifyRequest, project.getId(), category.getId(), releases.getId()));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 릴리즈 수정 테스트 - 존재하지 않는 릴리즈")
    public void modifyReleaseForFailureByNotExistsRelease() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);
        ReleaseModifyRequestDto modifyRequest = createModifyRequest();

        //when
        when(categoryRepository.existsByProjectId(project.getId())).thenReturn(true);
        when(memberProjectRepository.findMemberIdByProjectId(currentMemberId)).thenReturn(preparedMemberList);
        when(releaseRepository.findByCategoryIdAndReleaseId(category.getId(), releases.getId()))
                .thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(ReleasesNotFoundException.class,
                () -> releaseService.modifyReleases(modifyRequest, project.getId(), category.getId(), releases.getId()));
    }


    @Test
    @DisplayName("실패 - 릴리즈 삭제 테스트 - 인증되지 않은 사용자")
    public void deletReleaseForFailureByUnAuthorizedUesr() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);

        //when
        when(categoryRepository.existsByProjectId(project.getId())).thenReturn(true);
        when(memberProjectRepository.findMemberIdByProjectId(currentMemberId)).thenReturn(preparedMemberList);
        when(releaseRepository.findByCategoryIdAndReleaseId(category.getId(), releases.getId()))
                .thenReturn(Optional.ofNullable(releases));

        //then
        Assertions.assertThrows(UnAuthorizedException.class,
                () -> releaseService.deleteRelease(project.getId(), category.getId(), releases.getId()));

    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 릴리즈 삭제 테스트 - 프로젝트에 속하지 않은 사용자")
    public void deletReleaseForFailureByNonProjectMember() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);

        //when
        when(categoryRepository.existsByProjectId(project.getId())).thenReturn(true);
        when(memberProjectRepository.findMemberIdByProjectId(currentMemberId)).thenReturn(new ArrayList<>());
        when(releaseRepository.findByCategoryIdAndReleaseId(category.getId(), releases.getId()))
                .thenReturn(Optional.ofNullable(releases));

        //then
        Assertions.assertThrows(ProjectPermissionDeniedException.class,
                () -> releaseService.deleteRelease(project.getId(), category.getId(), releases.getId()));

    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 릴리즈 삭제 테스트 - 존재하지 않는 카테고리")
    public void deletReleaseForFailureByNotExistsCategory() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);

        //when
        when(categoryRepository.existsByProjectId(project.getId())).thenReturn(false);
        when(memberProjectRepository.findMemberIdByProjectId(currentMemberId)).thenReturn(preparedMemberList);
        when(releaseRepository.findByCategoryIdAndReleaseId(category.getId(), releases.getId()))
                .thenReturn(Optional.ofNullable(releases));

        //then
        Assertions.assertThrows(CategoryNotFoundException.class,
                () -> releaseService.deleteRelease(project.getId(), category.getId(), releases.getId()));

    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 릴리즈 삭제 테스트 - 존재하지 않는 릴리즈")
    public void deletReleaseForFailureByNotExistsRelease() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        Releases releases = buildReleases(category, 1L);

        //when
        when(categoryRepository.existsByProjectId(project.getId())).thenReturn(true);
        when(memberProjectRepository.findMemberIdByProjectId(currentMemberId)).thenReturn(preparedMemberList);
        when(releaseRepository.findByCategoryIdAndReleaseId(category.getId(), releases.getId()))
                .thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(ReleasesNotFoundException.class,
                () -> releaseService.deleteRelease(project.getId(), category.getId(), releases.getId()));

    }

}
