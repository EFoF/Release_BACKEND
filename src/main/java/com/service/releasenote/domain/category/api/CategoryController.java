package com.service.releasenote.domain.category.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.releasenote.domain.category.application.CategoryService;
import com.service.releasenote.domain.project.api.ProjectController;
import com.service.releasenote.global.log.CommonLog;
import com.service.releasenote.global.util.SecurityUtil;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.service.releasenote.domain.category.dto.CategoryDto.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"category"})
public class CategoryController {

    private final CategoryService categoryService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    /**
     * 카테고리 생성 api
     * @param projectId
     * @param categorySaveRequest
     * @return String
     */
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("api for save category")
    @PostMapping("/api/companies/projects/{project_id}/categories")
    @ApiResponses({
            @ApiResponse(code=201, message="생성 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자"),
            @ApiResponse(code=404, message = "존재하지 않는 프로젝트"),
            @ApiResponse(code=409, message = "프로젝트에 속하지 않은 사용자")
    })
    public Long categoryAdd(
            @PathVariable(name = "project_id") Long projectId,
            @RequestBody CategorySaveRequest categorySaveRequest) throws JsonProcessingException {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        CommonLog commonLog = new CommonLog(objectMapper.writeValueAsString(categorySaveRequest), "Post", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
        return categoryService.saveCategory(categorySaveRequest, projectId, currentMemberId);
    }

    /**
     * 특정 프로젝트 하위의 카테고리를 모두 조회하는 api
     * @param projectId
     * @return CategoryInfoDto
     */
    @ApiOperation("api for get categories by project id")
    @GetMapping("/api/companies/projects/{project_id}/categories")
    @ApiResponses({ @ApiResponse(code = 200, message = "요청 성공"), @ApiResponse(code = 404, message = "존재하지 않는 프로젝트")})
    public CategoryInfoDto categoryList(@PathVariable(name = "project_id") Long projectId) throws JsonProcessingException {
        CommonLog commonLog = new CommonLog(projectId + " 번 프로젝트 하위 카테고리 모두 조회", "Get", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
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
    @GetMapping("/api/companies/{company_id}/projects/{project_id}/categories/{category_id}")
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
    @GetMapping("/api/categories/{category_id}")
    public CategoryResponseDto categoryDetails (
            @PathVariable(name = "category_id") Long categoryId,
            @RequestParam(value = "developer", required = true, defaultValue = "false") Boolean isDeveloper) throws JsonProcessingException {
        String msg;
        if(isDeveloper) {
            msg = "개발자 - " + categoryId + " 번 카테고리 조회";
        } else {
            msg = "일반인 - " + categoryId + " 번 카테고리 조회";
        }
        CommonLog commonLog = new CommonLog(msg, "Post", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
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
    @DeleteMapping("/api/companies/projects/{project_id}/categories/{category_id}")
    public String categoryRemove(
            @PathVariable(name = "project_id") Long projectId,
            @PathVariable(name = "category_id") Long categoryId) throws JsonProcessingException {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        CommonLog commonLog = new CommonLog(categoryId + " 번 카테고리 삭제", "Delete", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
        return categoryService.deleteCategory(categoryId, projectId, currentMemberId);
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
    @PutMapping("/api/companies/projects/{project_id}/categories/{category_id}")
    public CategoryModifyResponseDto categoryModify(
            @PathVariable(name = "project_id")Long projectId,
            @PathVariable(name = "category_id")Long categoryId,
            @RequestBody CategoryModifyRequestDto modifyRequestDto) throws JsonProcessingException {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        categoryService.modifyCategory(modifyRequestDto, categoryId, projectId, currentMemberId);
        CommonLog commonLog = new CommonLog(objectMapper.writeValueAsString(modifyRequestDto), "Put", LocalDateTime.now());
        logger.info(objectMapper.writeValueAsString(commonLog));
        return categoryService.findCategoryAndConvert(categoryId);
    }


    @ApiOperation("api for update image")
    @PostMapping("/api/categories")
    public String uploadImage(@RequestPart(value="image", required=false)MultipartFile image) throws IOException {
        String imageUrl = categoryService.uploadImage(image);
        log.info(imageUrl);
        return imageUrl;
    }
}
