package com.service.releasenote.domain.category.api;

import com.service.releasenote.domain.category.application.CategoryService;
import io.swagger.annotations.*;
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
     * @param categorySaveRequest
     * @return String
     */
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("api for save category")
    @PostMapping("/companies/projects/{project_id}/categories")
    @ApiResponses({
            @ApiResponse(code=201, message="생성 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자"),
            @ApiResponse(code=404, message = "존재하지 않는 프로젝트"),
            @ApiResponse(code=409, message = "프로젝트에 속하지 않은 사용자")
    })
    public Long categoryAdd(
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody CategorySaveRequest categorySaveRequest
            ) {
        return categoryService.saveCategory(categorySaveRequest, projectId);
    }

    /**
     * 특정 프로젝트 하위의 카테고리를 모두 조회하는 api
     * @param projectId
     * @return CategoryInfoDto
     */
    @ApiOperation("api for get categories by project id")
    @GetMapping("/companies/projects/{project_id}/categories")
    @ApiResponses({ @ApiResponse(code = 200, message = "요청 성공"), @ApiResponse(code = 404, message = "존재하지 않는 프로젝트")})
    public CategoryInfoDto categoryList(@PathVariable(name = "project_id") Long projectId) {
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
    @ApiImplicitParam(name = "developer", value = "개발자 모드", required = true, dataType = "boolean", paramType = "query", defaultValue = "false")
    @ApiResponses({ @ApiResponse(code=200, message = "요청 성공"), @ApiResponse(code=404, message = "존재하지 않는 카테고리")})
    @GetMapping("/companies/{company_id}/projects/{project_id}/categories/{category_id}")
    public CategoryResponseDto categoryDetailsWithCondition (
            @PathVariable(name = "company_id") Long companyId,
            @PathVariable(name = "project_id") Long projectId,
            @PathVariable(name = "category_id") Long categoryId,
            @RequestParam(value = "developer", required = true, defaultValue = "false") Boolean isDeveloper) {
        return categoryService.findCategoryByIds(companyId, projectId, categoryId, isDeveloper);
    }

    /**
     * 카테고리 아이디만을 고려하여 카테고리 세부 데이터를 조회하는 api
     * @param categoryId
     * @return CategoryResponseDto
     */
    @ApiOperation("api for get specific category by category id only")
    @ApiImplicitParam(name = "developer", value = "개발자 모드", required = true, dataType = "boolean", paramType = "query", defaultValue = "false")
    @ApiResponses({ @ApiResponse(code=200, message = "요청 성공"), @ApiResponse(code=404, message = "존재하지 않는 카테고리")})
    @GetMapping("/categories/{category_id}")
    public CategoryResponseDto categoryDetails (
            @PathVariable(name = "category_id") Long categoryId,
            @RequestParam(value = "developer", required = true, defaultValue = "false") Boolean isDeveloper) {
        return categoryService.findCategoryByCategoryId(categoryId, isDeveloper);
    }

    /**
     * 카테고리 삭제 api
     * @param projectId
     * @param categoryId
     * @return String
     */
    @ApiOperation("api for delete category and releases under category")
    @ApiResponses({
            @ApiResponse(code=200, message = "요청 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자"),
            @ApiResponse(code=404, message = "존재하지 않는 카테고리"),
            @ApiResponse(code=409, message = "프로젝트에 속하지 않은 사용자")
    })
    @DeleteMapping("/companies/projects/{project_id}/categories/{category_id}")
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
    @ApiResponses({
            @ApiResponse(code=200, message = "요청 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자"),
            @ApiResponse(code=404, message = "존재하지 않는 카테고리"),
            @ApiResponse(code=409, message = "프로젝트에 속하지 않은 사용자")
    })
    @PutMapping("/companies/projects/{project_id}/categories/{category_id}")
    public CategoryModifyResponseDto categoryModify(
            @PathVariable(name = "project_id")Long projectId,
            @PathVariable(name = "category_id")Long categoryId,
            @RequestBody CategoryModifyRequestDto modifyRequestDto
    ) {
        categoryService.modifyCategory(modifyRequestDto, categoryId, projectId);
        return categoryService.findCategoryAndConvert(categoryId);
    }
}
