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
    @ApiOperation(value = "api for save category")
    @PostMapping("/company/{company_id}/project/{project_id}/category")
    public void saveCategory(
            @PathVariable(name = "company_id") Long companyId,
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody SaveCategoryRequest saveCategoryRequest
            ) {
        categoryService.saveCategory(saveCategoryRequest, projectId);
    }

    @ApiOperation("api for get categories by project id")
    @GetMapping("/company/{company_id}/project/{project_id}/category")
    public CategoryInfoDto getCategoryByProject(Long projectId) {
        return categoryService.findCategoryByProjectId(projectId);
    }

}
