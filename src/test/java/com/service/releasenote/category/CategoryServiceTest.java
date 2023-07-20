package com.service.releasenote.category;

import com.service.releasenote.domain.category.application.CategoryService;
import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.global.annotations.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.service.releasenote.domain.category.dto.CategoryDto.CategorySaveRequest;
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

    @MockBean
    CategoryService categoryService;

//    private void mockAuthentication() {
//        Authentication auth = mock(Authentication.class);
//
//        when(auth.getPrincipal()).thenReturn(buildLoggedInUser());
//
//        SecurityContext securityContext = mock(SecurityContext.class);
//        when(securityContext.getAuthentication()).thenReturn(auth);
//        SecurityContextHolder.setContext(securityContext);
//    }

    public Company buildCompany() {
        return Company.builder()
                .ImageURL("test image url")
                .name("teset company name")
                .id(1L)
                .build();
    }

    public Project buildProject(Company company) {
        return Project.builder()
                .description("test project description")
                .title("test project title")
                .company(company)
                .scope(true)
                .id(1L)
                .build();
    }

    public Category buildCategory(Project project) {
        return Category.builder()
                .description("test category description")
                .detail("test category detail")
                .title("test category title")
                .project(project)
                .id(1L)
                .build();
    }

    public CategorySaveRequest createCategorySaveRequest() {
        return CategorySaveRequest.builder()
                .description("test category description")
                .detail("test category detail")
                .title("test category title")
                .build();
    }

    @Test
    @DisplayName("카테고리 생성 테스트 - 성공")
    @WithMockCustomUser
    public void categorySaveTest() throws Exception {
        //given
        Long currentMemberId = 1L;
        List<Long> preparedMemberList = new ArrayList<>();
        preparedMemberList.add(currentMemberId);

        Company company = buildCompany();
        Project project = buildProject(company);
        Category category = buildCategory(project);
        CategorySaveRequest categorySaveRequest = createCategorySaveRequest();

        //when
        when(memberProjectRepository.findMemberListByProjectId(any())).thenReturn(preparedMemberList);
        when(projectRepository.findById(project.getId())).thenReturn(Optional.ofNullable(project));
        when(categoryRepository.save(category)).thenReturn(category);

        //then
        Long categoryId = categoryService.saveCategory(categorySaveRequest, project.getId());
        assertThat(categoryId).isEqualTo(category.getId());
    }
}
