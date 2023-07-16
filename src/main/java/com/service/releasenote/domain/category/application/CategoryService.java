package com.service.releasenote.domain.category.application;

import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.dto.CategoryDto;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.project.dao.ProjectRepository;
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
    @Transactional
    public void saveCategory(SaveCategoryRequest saveCategoryRequest, Long projectId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
    }

    public CategoryInfoDto findCategoryByProjectId(Long projectId) {
        // TODO 프로젝트 존재하는지 검증하기
        List<Category> categoryList = categoryRepository.findByProjectId(projectId);
        List<CategoryEachDto> categoryEachDtoList = categoryList.stream()
                .map(c -> mapCategoryToDto(c))
                .collect(Collectors.toList());
        return CategoryInfoDto.builder().categoryEachDtoList(categoryEachDtoList).build();
    }

    private CategoryEachDto mapCategoryToDto(Category category) {
        return CategoryEachDto.builder()
                .title(category.getTitle())
                .description(category.getDescription())
                .build();
    }
}
