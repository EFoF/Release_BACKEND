package com.service.releasenote.category;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.releasenote.domain.category.api.CategoryController;
import com.service.releasenote.domain.category.application.CategoryService;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.model.Company;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.service.releasenote.domain.category.dto.CategoryDto.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@ExtendWith(SpringExtension.class)
@WebMvcTest(CategoryController.class)
@MockBean(JpaMetamodelMappingContext.class)
public class CategoryControllerTest {

    @MockBean
    CategoryService categoryService;

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

    public CategoryEachDto createCategoryEachDto(Long id) {
        return CategoryEachDto.builder()
                .id(id)
                .description("test category description " + id)
                .title("test category title " + id)
                .build();
    }

    public CategoryInfoDto createCategoryInfoDto(int number) {
        List<CategoryEachDto> list = new ArrayList<>();
        for(int i=1; i<=number; i++) {
            list.add(createCategoryEachDto(Long.valueOf(i)));
        }
        return CategoryInfoDto.builder()
                .categoryEachDtoList(list)
                .build();
    }

    public CategoryResponseDto createCategoryResponseDto() {
        return CategoryResponseDto.builder()
                .description("test category description")
                .lastModifiedTime(LocalDateTime.now())
                .detail("test category detail")
                .title("test category title")
                .lastModifierName("tester")
                .id(1L)
                .build();
    }

    @Test
    @DisplayName("성공 - 카테고리 생성 테스트")
    public void saveCategoryForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        CategorySaveRequest categorySaveRequest = createCategorySaveRequest();

        //when
        when(categoryService.saveCategory(any(), any())).thenReturn(category.getId());

        //then
        mockMvc.perform(post("/companies/projects/{project_id}/categories", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categorySaveRequest)))
                .andExpect(content().string(category.getId().toString()))
                .andExpect(status().isCreated());
    }
    
    @Test
    @DisplayName("성공 - 프로젝트 하위 카테고리 모두 조회")
    public void getCategoryWithProjectForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        CategoryInfoDto categoryInfoDto = createCategoryInfoDto(3);

        //when
        when(categoryService.findCategoryByProjectId(project.getId())).thenReturn(categoryInfoDto);
        
        //then
        mockMvc.perform(get("/companies/projects/{project_id}/categories", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryEachDtoList[0].title").value("test category title 1"))
                .andExpect(jsonPath("$.categoryEachDtoList[1].title").value("test category title 2"))
                .andExpect(jsonPath("$.categoryEachDtoList[2].title").value("test category title 3"))
                .andDo(print());
    }

    @Test
    @DisplayName("성공 - 세부 조건으로 카테고리 조회")
    public void getCategoryWithIdsForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        CategoryResponseDto categoryResponseDto = createCategoryResponseDto();
        //when
        when(categoryService.findCategoryByIds(company.getId(), project.getId(), category.getId(), false))
                .thenReturn(categoryResponseDto);

        //then
        mockMvc.perform(get("/companies/{company_id}/projects/{project_id}/categories/{category_id}",
                1L,1L,1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("test category title"))
                .andExpect(jsonPath("$.detail").value("test category detail"))
                .andExpect(jsonPath("$.description").value("test category description"))
                .andDo(print());

    }

    @Test
    @DisplayName("성공 - 카테고리 세부 조회")
    public void getCategoryDetailForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        CategoryResponseDto categoryResponseDto = createCategoryResponseDto();

        //when
        when(categoryService.findCategoryByCategoryId(category.getId(), false)).thenReturn(categoryResponseDto);

        //then
        mockMvc.perform(get("/categories/{category_id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("test category title"))
                .andExpect(jsonPath("$.detail").value("test category detail"))
                .andExpect(jsonPath("$.description").value("test category description"))
                .andDo(print());
    }

    @Test
    @DisplayName("성공 - 카테고리 수정")
    public void modifyCategoryForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);
        CategoryModifyRequestDto categoryModifyRequest = createCategoryModifyRequest();

        //when & then
        mockMvc.perform(put("/companies/projects/{project_id}/categories/{category_id}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryModifyRequest)))
                .andExpect(status().isOk())
                .andDo(print());

    }

    @Test
    @DisplayName("성공 - 카테고리 삭제")
    public void deleteCategoryForSuccess() throws Exception {
        //given
        Company company = buildCompany(1L);
        Project project = buildProject(company, 1L);
        Category category = buildCategory(project, 1L);

        //when
        when(categoryService.deleteCategory(project.getId(), category.getId()))
                .thenReturn("deleted");

        //then
        mockMvc.perform(delete("/companies/projects/{project_id}/categories/{category_id}", 1L, 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("deleted"));

    }
}
