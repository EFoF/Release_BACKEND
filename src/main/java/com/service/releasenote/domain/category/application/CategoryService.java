package com.service.releasenote.domain.category.application;

import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.dto.CategoryDto;
import com.service.releasenote.domain.category.exception.CategoryNotFoundException;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.service.releasenote.domain.category.dto.CategoryDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    @Transactional
    public String saveCategory(SaveCategoryRequest saveCategoryRequest, Long projectId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        Category category = saveCategoryRequest.toEntity(project);
        categoryRepository.save(category);
        return "saved";
    }

    public CategoryInfoDto findCategoryByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
//        List<Category> categoryList = categoryRepository.findByProjectId(projectId);
        List<Category> categoryList = categoryRepository.findByProject(projectId);
        List<CategoryEachDto> categoryEachDtoList = categoryList.stream()
                .map(c -> mapCategoryEntityToCategoryEachDto(c))
                .collect(Collectors.toList());
        return CategoryInfoDto.builder().categoryEachDtoList(categoryEachDtoList).build();
    }

    public CategoryResponseDto findCategoryByCategoryId(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        Member member = memberRepository.findById(category.getModifierId()).orElseThrow(UserNotFoundException::new);
        return CategoryResponseDto.builder()
                .lastModifiedTime(category.getModifiedDate())
                .description(category.getDescription())
                .lastModifierName(member.getUserName())
                .detail(category.getDetail())
                .title(category.getTitle())
                .build();
    }

    public CategoryResponseDto findCategoryByIds(Long companyId, Long projectId, Long categoryId) {
        Category category = categoryRepository.findByIntersectionId(companyId, projectId, categoryId).orElseThrow(CategoryNotFoundException::new);
        Member member = memberRepository.findById(category.getModifierId()).orElseThrow(UserNotFoundException::new);
        return CategoryResponseDto.builder()
                .lastModifiedTime(category.getModifiedDate())
                .description(category.getDescription())
                .lastModifierName(member.getUserName())
                .detail(category.getDetail())
                .title(category.getTitle())
                .build();
    }


    private CategoryEachDto mapCategoryEntityToCategoryEachDto(Category category) {
        return CategoryEachDto.builder()
                .title(category.getTitle())
                .description(category.getDescription())
                .build();
    }
}