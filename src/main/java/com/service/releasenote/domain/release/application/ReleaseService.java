package com.service.releasenote.domain.release.application;

import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.dto.CategoryDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.service.releasenote.domain.category.dto.CategoryDto.*;
import static com.service.releasenote.domain.release.dto.ReleaseDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReleaseService {

    private final MemberProjectRepository memberProjectRepository;
    private final CategoryRepository categoryRepository;
    private final ReleaseRepository releaseRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;

    /**
     * release 저장 서비스 로직
     * @param saveReleaseRequest
     * @param projectId
     * @param categoryId
     * @return Long
     */
    @Transactional
    public Long saveRelease(SaveReleaseRequest saveReleaseRequest, Long projectId, Long categoryId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if(!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException();
        }
        List<Long> members = memberProjectRepository.findMemberListByProjectId(projectId);
        if(!members.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        Releases releases = saveReleaseRequest.toEntity(category);
        releaseRepository.save(releases);
        return releases.getId();
    }

    /**
     * 카테고리로 릴리즈 조회 서비스 로직
     * @param categoryId
     * @return ReleaseInfoDto
     */
    public ReleaseInfoDto findReleasesByCategoryId(Long categoryId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);
        List<Releases> releasesList = releaseRepository.findByCategoryId(categoryId);
        List<ReleaseDtoEach> dtoList = releasesList.stream()
                .map(r -> mapReleaseToDto(r))
                .collect(Collectors.toList());
        return ReleaseInfoDto.builder().releaseDtoList(dtoList).build();
    }

    /**
     * 프로젝트로 릴리즈 조회 서비스 로직
     * @param projectId
     * @return ProjectReleasesDto
     */
    public ProjectReleasesDto findReleasesByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        List<Category> categoryList = categoryRepository.findByProjectId(project.getId());
        List<Long> categoryIdList = categoryList.stream().map(c -> c.getId()).collect(Collectors.toList());
        List<Releases> releasesList = releaseRepository.findByCategoryIdIn(categoryIdList);
        return mapProjectReleaseToDto(categoryList, releasesList);
    }

    private ReleaseDtoEach mapReleaseToDto(Releases releases) {
        return ReleaseDtoEach.builder()
                .lastModifiedTime(releases.getModifiedDate())
                .lastModifierName(releases.getModifierName())
                .version(releases.getVersion())
                .content(releases.getMessage())
                .tag(releases.getTag())
                .build();
    }

    private ProjectReleasesDto mapProjectReleaseToDto(List<Category> categoryList, List<Releases> releasesList) {
        // in 절로 한번에 조회 후 메모리에서 데이터 정제
        // 쿼리를 여러번 날리지 않아도 됨 -> 네트워크 최적화
        // 메모리에 로드가 발생 -> 트레이드 오프

        // 카테고리 ID로 release를 묶어서 정리
        Map<Long, List<Releases>> releasesGroupByCategory = releasesList.stream()
                .collect(Collectors.groupingBy(r -> r.getCategory().getId()));
        // ID로 카테고리 접근에 용이한 구조로 변경
        Map<Long, Category> categoryMap = categoryList.stream().collect(Collectors.toMap(c -> c.getId(), c -> c));
        List<ProjectReleasesDtoEach> result = new ArrayList<>();

        releasesGroupByCategory.forEach((categoryId, release) -> {
            Category category = categoryMap.get(categoryId);
            CategoryResponseDto categoryResponseDto = CategoryResponseDto.builder()
                    .title(category.getTitle())
                    .description(category.getDescription())
                    .build();
            List<ReleaseDtoEach> releaseDtoEachList = release.stream()
                    .map(r -> mapReleaseToDto(r)).collect(Collectors.toList());
            ProjectReleasesDtoEach resultEach = ProjectReleasesDtoEach.builder()
                    .categoryResponseDto(categoryResponseDto)
                    .releaseDtoList(releaseDtoEachList)
                    .build();
            result.add(resultEach);
        });

        return new ProjectReleasesDto(result);
    }

}