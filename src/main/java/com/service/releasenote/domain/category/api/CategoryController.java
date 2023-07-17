package com.service.releasenote.domain.category.api;

import com.service.releasenote.domain.category.application.CategoryService;
import com.service.releasenote.domain.category.dto.CategoryDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.service.releasenote.domain.category.dto.CategoryDto.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"category"})
public class CategoryController {

    private final CategoryService categoryService;

    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("api for save category")
    @PostMapping("/company/{company_id}/project/{project_id}/category")
    public String saveCategory(
            @PathVariable(name = "company_id") Long companyId,
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody SaveCategoryRequest saveCategoryRequest
            ) {
        return categoryService.saveCategory(saveCategoryRequest, projectId);
    }

    @ApiOperation("api for get categories by project id")
    @GetMapping("/company/{company_id}/project/{project_id}/category")
    public CategoryInfoDto getCategoryByProject(
            @PathVariable(name = "company_id") Long companyId,
            @PathVariable(name = "project_id") Long projectId
    ) {
        return categoryService.findCategoryByProjectId(projectId);
    }

    @ApiOperation("api for get specific category by combination of companyId, projectId, categoryId")
    @GetMapping("/company/{company_id}/project/{project_id}/category/{category_id}")
    public CategoryResponseDto getCategoryByIds (
            @PathVariable(name = "company_id") Long companyId,
            @PathVariable(name = "project_id") Long projectId,
            @PathVariable(name = "category_id") Long categoryId
    ) {
        return categoryService.findCategoryByIds(companyId, projectId, categoryId);
    }

    @ApiOperation("api for get specific category by category id only")
    @GetMapping("/category/{category_id}")
    public CategoryResponseDto getCategoryByCategoryId (@PathVariable(name = "category_id") Long categoryId) {
        return categoryService.findCategoryByCategoryId(categoryId);
    }
}
