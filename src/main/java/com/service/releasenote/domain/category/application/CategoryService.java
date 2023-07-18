package com.service.releasenote.domain.category.application;

import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.exception.CategoryNotFoundException;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectPermissionDeniedException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.domain.release.dao.ReleaseRepository;
import com.service.releasenote.domain.release.model.Releases;
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

    private final MemberProjectRepository memberProjectRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;
    private final ReleaseRepository releaseRepository;
    private final MemberRepository memberRepository;

    /**
     * 카테고리 저장 서비스 로직
     * @param saveCategoryRequest
     * @param projectId
     * @return String
     */
    @Transactional
    public String saveCategory(SaveCategoryRequest saveCategoryRequest, Long projectId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        List<Long> memberListByProjectId = memberProjectRepository.findMemberListByProjectId(projectId);
        if(!memberListByProjectId.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        Category category = saveCategoryRequest.toEntity(project);
        categoryRepository.save(category);
        return "saved";
    }

    /**
     * 특정 프로젝트에 속한 카테고리 모두 조회
     * @param projectId
     * @return CategoryInfoDto
     */
    public CategoryInfoDto findCategoryByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
//        List<Category> categoryList = categoryRepository.findByProjectId(projectId);
        List<Category> categoryList = categoryRepository.findByProject(project.getId());
        List<CategoryEachDto> categoryEachDtoList = categoryList.stream()
                .map(c -> mapCategoryEntityToCategoryEachDto(c))
                .collect(Collectors.toList());
        return CategoryInfoDto.builder().categoryEachDtoList(categoryEachDtoList).build();
    }

    /**
     * 카테고리 아이디로 카테고리 구체 조회 (디테일 등 모든 정보 포함)
     * @param categoryId
     * @return CategoryResponseDto
     */
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

    /**
     * 회사, 프로젝트, 카테고리 아이디를 모두 고려해서, 조건에 전부 해당하는 카테고리만 조회
     * @param companyId
     * @param projectId
     * @param categoryId
     * @return CategoryResponseDto
     */
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

    /**
     * 카테고리 및 하위 릴리즈 삭제
     * @param categoryId
     * @param projectId
     * @return String
     */
    @Transactional
    public String deleteCategory(Long categoryId, Long projectId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        List<Long> members = memberProjectRepository.findMemberListByProjectId(projectId);
        if(!members.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }
        List<Releases> releaseList = releaseRepository.findByCategoryId(categoryId);
        releaseRepository.deleteAll(releaseList);
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        categoryRepository.delete(category);
        return "deleted";
    }


    @Transactional
    public void modifyCategory(CategoryModifyRequestDto modifyRequestDto, Long categoryId, Long projectId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        List<Long> members = memberProjectRepository.findMemberListByProjectId(projectId);
        if(!members.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        category.setTitle(modifyRequestDto.getTitle());
        category.setDescription(modifyRequestDto.getDescription());
        category.setDetail(modifyRequestDto.getDetail());
        // 커맨드와 쿼리를 분리
    }

    public CategoryModifyResponseDto findCategoryAndConvert(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        Member modifier = memberRepository.findById(category.getModifierId()).orElseThrow(UserNotFoundException::new);
        return CategoryModifyResponseDto.builder()
                .lastModifiedTime(category.getModifiedDate())
                .lastModifierName(modifier.getUserName())
                .description(category.getDescription())
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