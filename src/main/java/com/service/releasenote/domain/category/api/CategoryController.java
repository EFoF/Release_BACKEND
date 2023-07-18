package com.service.releasenote.domain.category.api;

import com.service.releasenote.domain.category.application.CategoryService;
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

    /**
     * 카테고리 생성 api
     * @param projectId
     * @param saveCategoryRequest
     * @return String
     */
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("api for save category")
    @PostMapping("/company/project/{project_id}/category")
    public String categoryAdd(
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody SaveCategoryRequest saveCategoryRequest
            ) {
        return categoryService.saveCategory(saveCategoryRequest, projectId);
    }

    /**
     * 특정 프로젝트 하위의 카테고리를 모두 조회하는 api
     * @param projectId
     * @return CategoryInfoDto
     */
    @ApiOperation("api for get categories by project id")
    @GetMapping("/company/project/{project_id}/category")
    public CategoryInfoDto categoryList(
            @PathVariable(name = "project_id") Long projectId
    ) {
        return categoryService.findCategoryByProjectId(projectId);
    }

    /**
     * 회사, 프로젝트, 카테고리 3개의 아이디를 모두 고려하여 카테고리 세부 데이터를 조회하는 api
     * @param companyId
     * @param projectId
     * @param categoryId
     * @return CategoryResponseDto
     */
    @ApiOperation("api for get specific category by combination of companyId, projectId, categoryId")
    @GetMapping("/company/{company_id}/project/{project_id}/category/{category_id}")
    public CategoryResponseDto categoryDetailsWithCondition (
            @PathVariable(name = "company_id") Long companyId,
            @PathVariable(name = "project_id") Long projectId,
            @PathVariable(name = "category_id") Long categoryId
    ) {
        return categoryService.findCategoryByIds(companyId, projectId, categoryId);
    }

    /**
     * 카테고리 아이디만을 고려하여 카테고리 세부 데이터를 조회하는 api
     * @param categoryId
     * @return CategoryResponseDto
     */
    @ApiOperation("api for get specific category by category id only")
    @GetMapping("/category/{category_id}")
    public CategoryResponseDto categoryDetails (@PathVariable(name = "category_id") Long categoryId) {
        return categoryService.findCategoryByCategoryId(categoryId);
    }

    /**
     * 카테고리 삭제 api
     * @param projectId
     * @param categoryId
     * @return String
     */
    @ApiOperation("api for delete category and releases under category")
    @DeleteMapping("/company/project/{project_id}/category/{category_id}")
    public String categoryRemove(
            @PathVariable(name = "project_id") Long projectId,
            @PathVariable(name = "category_id") Long categoryId
    ) {
        return categoryService.deleteCategory(categoryId, projectId);
    }

    /**
     * 카테고리 수정 api
     * @param projectId
     * @param categoryId
     * @param modifyRequestDto
     * @return CategoryModifyResponseDto
     */
    @ApiOperation("api for update category")
    @PutMapping("/company/project/{project_id}/category/{category_id}")
    public CategoryModifyResponseDto categoryModify(
            @PathVariable(name = "project_id")Long projectId,
            @PathVariable(name = "category_id")Long categoryId,
            @RequestBody CategoryModifyRequestDto modifyRequestDto
    ) {
        categoryService.modifyCategory(modifyRequestDto, categoryId, projectId);
        return categoryService.findCategoryAndConvert(categoryId);
    }
}
