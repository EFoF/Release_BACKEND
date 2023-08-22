package com.service.releasenote.domain.category.application;

import com.service.releasenote.domain.alarm.application.AlarmService;
import com.service.releasenote.domain.alarm.model.AlarmDomain;
import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.exception.CategoryNotFoundException;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectPermissionDeniedException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.domain.release.dao.ReleaseRepository;
import com.service.releasenote.domain.release.model.Releases;
import com.service.releasenote.global.config.S3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.service.releasenote.domain.category.dto.CategoryDto.*;
import static com.service.releasenote.global.constants.S3Constants.CATEGORY_DIRECTORY;

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
    private final AlarmService alarmService;
    private final S3Config s3Uploader;

    /**
     * 카테고리 저장 서비스 로직
     * @param categorySaveRequest
     * @param projectId
     * @return String
     */
    @Transactional
    public Long saveCategory(CategorySaveRequest categorySaveRequest, Long projectId, Long currentMemberId) {
//        List<Long> memberListByProjectId = memberProjectRepository.findMemberIdByProjectId(projectId);
        List<Long> memberListByProjectId = memberProjectRepository.findMemberIdByProjectId(projectId);
        if(!memberListByProjectId.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        Category category = categorySaveRequest.toEntity(project);
        Category savedCategory = categoryRepository.save(category);

        alarmService.produceMessage(project.getId(), category.getId(), "카테고리를 생성하셨습니다.", AlarmDomain.CATEGORY, currentMemberId);

        return savedCategory.getId();
    }

    /**
     * 특정 프로젝트에 속한 카테고리 모두 조회
     * @param projectId
     * @return CategoryInfoDto
     */
    public CategoryInfoDto findCategoryByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
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
    public CategoryResponseDto findCategoryByCategoryId(Long categoryId, Boolean isDeveloper) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        if(!isDeveloper) {
            return CategoryResponseDto.builder()
                    .lastModifiedTime(category.getModifiedDate())
                    .description(category.getDescription())
                    .detail(category.getDetail())
                    .title(category.getTitle())
                    .id(category.getId())
                    .build();
        }

        Optional<Member> memberOptional = memberRepository.findById(category.getModifierId());
        return CategoryResponseDto.builder()
                .lastModifierName(memberOptional.isEmpty() ? "anonymous user" : memberOptional.get().getUserName())
                .lastModifierEmail(memberOptional.isEmpty() ? "anonymous email" : memberOptional.get().getEmail())
                .lastModifiedTime(category.getModifiedDate())
                .description(category.getDescription())
                .detail(category.getDetail())
                .title(category.getTitle())
                .id(category.getId())
                .build();
    }

    /**
     * 회사, 프로젝트, 카테고리 아이디를 모두 고려해서, 조건에 전부 해당하는 카테고리만 조회
     * @param companyId
     * @param projectId
     * @param categoryId
     * @return CategoryResponseDto
     */
    public CategoryResponseDto findCategoryByIds(Long companyId, Long projectId, Long categoryId, Boolean isDeveloper) {
        Category category = categoryRepository.findByIntersectionId(companyId, projectId, categoryId).orElseThrow(CategoryNotFoundException::new);
        if(!isDeveloper) {
            return CategoryResponseDto.builder()
                    .lastModifiedTime(category.getModifiedDate())
                    .description(category.getDescription())
                    .detail(category.getDetail())
                    .title(category.getTitle())
                    .id(category.getId())
                    .build();
        }

        Optional<Member> memberOptional = memberRepository.findById(category.getModifierId());
        return CategoryResponseDto.builder()
                .lastModifierName(memberOptional.isEmpty() ? "anonymous user" : memberOptional.get().getUserName())
                .lastModifiedTime(category.getModifiedDate())
                .description(category.getDescription())
                .detail(category.getDetail())
                .title(category.getTitle())
                .id(category.getId())
                .build();
    }

    /**
     * 카테고리 및 하위 릴리즈 삭제
     * @param categoryId
     * @param projectId
     * @return String
     */
    @Transactional
    public String deleteCategory(Long categoryId, Long projectId, Long currentMemberId) {
        List<Long> members = memberProjectRepository.findMemberIdByProjectId(projectId);
        if(!members.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }
        List<Releases> releaseList = releaseRepository.findByCategoryId(categoryId);
        releaseRepository.deleteAll(releaseList);
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        categoryRepository.delete(category);
        alarmService.produceMessage(projectId, 0L,"카테고리를 삭제하셨습니다.", AlarmDomain.CATEGORY, currentMemberId);
        return "deleted";
    }


    @Transactional
    public void modifyCategory(CategoryModifyRequestDto modifyRequestDto, Long categoryId, Long projectId, Long currentMemberId) {
        List<Long> members = memberProjectRepository.findMemberIdByProjectId(projectId);
        if(!members.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        category.setTitle(modifyRequestDto.getTitle());
        category.setDescription(modifyRequestDto.getDescription());
        category.setDetail(modifyRequestDto.getDetail());
        alarmService.produceMessage(projectId,  category.getId(), "카테고리를 수정하셨습니다.", AlarmDomain.CATEGORY, currentMemberId);
        // 커맨드와 쿼리를 분리
    }

    public CategoryModifyResponseDto findCategoryAndConvert(Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        Optional<Member> memberOptional = memberRepository.findById(category.getModifierId());
        return CategoryModifyResponseDto.builder()
                .lastModifierName(memberOptional.isEmpty() ? "anonymous user" : memberOptional.get().getUserName())
                .lastModifiedTime(category.getModifiedDate())
                .description(category.getDescription())
                .detail(category.getDetail())
                .title(category.getTitle())
                .build();
    }

    private CategoryEachDto mapCategoryEntityToCategoryEachDto(Category category) {
        return CategoryEachDto.builder()
                .id(category.getId())
                .title(category.getTitle())
                .description(category.getDescription())
                .build();
    }

    public String uploadImage(MultipartFile inputImage) throws IOException {
        // TODO: 고도화
        return s3Uploader.upload(inputImage, CATEGORY_DIRECTORY);
    }
}