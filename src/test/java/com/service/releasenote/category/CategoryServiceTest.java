package com.service.releasenote.category;

import com.service.releasenote.domain.category.application.CategoryService;
import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.exception.CategoryNotFoundException;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectPermissionDeniedException;
import com.service.releasenote.domain.project.model.Project;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.service.releasenote.domain.category.dto.CategoryDto.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @MockBean
    MemberProjectRepository memberProjectRepository;

    @MockBean
    CategoryRepository categoryRepository;

    @MockBean
    ProjectRepository projectRepository;

    @Autowired
    CategoryService categoryService;

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

    public CategorySaveRequest createCategorySaveRequest() {
        return CategorySaveRequest.builder()
                .description("test category description")
                .detail("test category detail")
                .title("test category title")
                .build();
    }

    public CategoryModifyRequestDto createCategoryModifyRequest() {
        return CategoryModifyRequestDto.builder()
                .description("modified category description")
                .detail("modified category detail")
                .title("modified category title")
                .build();
    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 카테고리 생성 테스트")
    public void saveCategoryForSuccess() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        CategorySaveRequest categorySaveRequest = createCategorySaveRequest();

        //when
        when(memberProjectRepository.findMembersByProjectId(any())).thenReturn(preparedMemberList);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));
        when(categoryRepository.save(any())).thenReturn(category);

        //then
        Long categoryId = categoryService.saveCategory(categorySaveRequest, project.getId());
        assertThat(categoryId).isEqualTo(category.getId());
    }

    @Test
    @DisplayName("실패 - 카테고리 생성 테스트 - 인증되지 않은 사용자")
    public void saveCategoryForFailureByUnAuthorizedUser() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        CategorySaveRequest categorySaveRequest = createCategorySaveRequest();

        //when
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));
        when(categoryRepository.save(any())).thenReturn(category);

        //then
        Assertions.assertThrows(UnAuthorizedException.class,
                () -> categoryService.saveCategory(categorySaveRequest, project.getId()));

    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 카테고리 생성 테스트 - 프로젝트에 속하지 않은 사용자")
    public void saveCategoryForFailureByNonProjectMember() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        CategorySaveRequest categorySaveRequest = createCategorySaveRequest();

        //when
        when(memberProjectRepository.findMembersByProjectId(any())).thenReturn(new ArrayList<>());
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));
        when(categoryRepository.save(any())).thenReturn(category);

        //then
        Assertions.assertThrows(ProjectPermissionDeniedException.class,
                () -> categoryService.saveCategory(categorySaveRequest, project.getId()));

    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 카테고리 생성 테스트 - 존재하지 않는 프로젝트")
    public void saveCategoryForFailureByNotExistsProject() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        CategorySaveRequest categorySaveRequest = createCategorySaveRequest();

        //when
        when(memberProjectRepository.findMembersByProjectId(any())).thenReturn(preparedMemberList);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenReturn(category);

        //then
        Assertions.assertThrows(ProjectNotFoundException.class,
                () -> categoryService.saveCategory(categorySaveRequest, project.getId()));

    }

    @Test
    @WithMockCustomUser
    @DisplayName("성공 - 카테고리 조회 테스트 - 프로젝트로 조회하기")
    public void getCategoriesWithProjectForSuccess () throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category1 = buildCategory(project, 1L);
        Category category2 = buildCategory(project, 2L);
        Category category3 = buildCategory(project, 3L);
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(category1);
        categoryList.add(category2);
        categoryList.add(category3);

        //when
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));
        when(categoryRepository.findByProject(project.getId())).thenReturn(categoryList);

        //then
        CategoryInfoDto categoryInfoDto = categoryService.findCategoryByProjectId(project.getId());
        assertThat(categoryInfoDto.getCategoryEachDtoList()).extracting("title")
                .contains("test category title 1", "test category title 2", "test category title 3");
    }

    @Test
    @DisplayName("실패 - 카테고리 조회 테스트 - 존재하지 않는 프로젝트")
    public void getCategoriesForFailureByNotExistsProject() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);

        //when
        when(projectRepository.findById(project.getId())).thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(ProjectNotFoundException.class,
                () -> categoryService.findCategoryByProjectId(project.getId()));
    }

    @Test
    @DisplayName("성공 - 카테고리 구체 조회 테스트")
    public void getCategoryForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);

        //when
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.ofNullable(category));

        //then
        CategoryResponseDto resultDto = categoryService.findCategoryByCategoryId(category.getId());
        assertThat(resultDto.getTitle()).isEqualTo("test category title 1");
        assertThat(resultDto.getDescription()).isEqualTo("test category description 1");
        assertThat(resultDto.getDetail()).isEqualTo("test category detail 1");

    }

    @Test
    @DisplayName("실패 - 카테고리 구체 조회 테스트 - 존재하지 않는 카테고리")
    public void getCategoryForFailureByNotExistsCategory() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);

        //when
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(CategoryNotFoundException.class,
                () -> categoryService.findCategoryByCategoryId(category.getId()));

    }

    @Test
    @DisplayName("성공 - 카테고리 세부 조건 조회 테스트")
    public void getCategoryWithDetailConditionForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);

        //when
        when(categoryRepository.findByIntersectionId(company.getId(), project.getId(), category.getId()))
                .thenReturn(Optional.ofNullable(category));

        //then
        CategoryResponseDto resultDto = categoryService.findCategoryByIds(company.getId(), project.getId(), category.getId());
        assertThat(resultDto.getTitle()).isEqualTo("test category title 1");
        assertThat(resultDto.getDescription()).isEqualTo("test category description 1");
        assertThat(resultDto.getDetail()).isEqualTo("test category detail 1");
    }

    @Test
    @DisplayName("실패 - 카테고리 세부 조건 조회 테스트 - 존재하지 않는 회사, 프로젝트, 카테고리")
    public void getCategoryWithDetailConditionForFailureByNotExistsCompany() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);

        //when
        when(categoryRepository.findByIntersectionId(company.getId(), project.getId(), category.getId()))
                .thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(CategoryNotFoundException.class,
                () -> categoryService.findCategoryByIds(company.getId(), project.getId(), category.getId()));

    }

    @Test
    @DisplayName("실패 - 카테고리 수정 테스트 - 인증되지 않은 사용자")
    public void updateCategoryForFailureByUnAuthorizedUser() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        CategoryModifyRequestDto categoryModifyRequest = createCategoryModifyRequest();

        //when
        when(memberProjectRepository.findMembersByProjectId(any())).thenReturn(new ArrayList<>());
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.ofNullable(category));

        //then
        Assertions.assertThrows(UnAuthorizedException.class,
                () -> categoryService.modifyCategory(categoryModifyRequest, category.getId(), project.getId()));
    }
    
    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 카테고리 수정 테스트 - 프로젝트에 속하지 않은 사용자")
    public void updateCategoryForFailureByNonProjectMember() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        CategoryModifyRequestDto categoryModifyRequest = createCategoryModifyRequest();

        //when
        when(memberProjectRepository.findMembersByProjectId(any())).thenReturn(new ArrayList<>());
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.ofNullable(category));
        
        //then
        Assertions.assertThrows(ProjectPermissionDeniedException.class,
                () -> categoryService.modifyCategory(categoryModifyRequest, category.getId(), project.getId()));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 카테고리 수정 테스트 - 존재하지 않는 카테고리")
    public void updateCategoryForFailureByNotExistsCategory() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        CategoryModifyRequestDto categoryModifyRequest = createCategoryModifyRequest();

        //when
        when(memberProjectRepository.findMembersByProjectId(currentMemberId)).thenReturn(preparedMemberList);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(CategoryNotFoundException.class,
                () -> categoryService.modifyCategory(categoryModifyRequest, category.getId(), project.getId()));
    }

    @Test
    @DisplayName("실패 - 카테고리 삭제 테스트 - 인증되지 않은 사용자")
    public void deleteCategoryForFailureByUnAuthorizedUser() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);

        //when & then
        Assertions.assertThrows(UnAuthorizedException.class,
                () -> categoryService.deleteCategory(category.getId(), project.getId()));
    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 카테고리 삭제 테스트 - 프로젝트에 속하지 않은 사용자")
    public void deleteCategoryForFailureByNonProjectMember() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);

        //when
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.ofNullable(category));

        //then
        Assertions.assertThrows(ProjectPermissionDeniedException.class,
                () -> categoryService.deleteCategory(category.getId(), project.getId()));

    }

    @Test
    @WithMockCustomUser
    @DisplayName("실패 - 카테고리 삭제 테스트 - 존재하지 않는 카테고리")
    public void deleteCategoryForFailureByNotExistsCategory() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        CategoryModifyRequestDto categoryModifyRequest = createCategoryModifyRequest();

        //when
        when(memberProjectRepository.findMembersByProjectId(currentMemberId)).thenReturn(preparedMemberList);
        when(categoryRepository.findById(category.getId())).thenReturn(Optional.empty());

        //then
        Assertions.assertThrows(CategoryNotFoundException.class,
                () -> categoryService.deleteCategory(category.getId(), project.getId()));

    }

}
