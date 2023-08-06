package com.service.releasenote.domain.release.application;

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
import com.service.releasenote.domain.release.exception.ReleasesNotFoundException;
import com.service.releasenote.domain.release.model.Releases;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final AlarmService alarmService;

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
        List<Long> members = memberProjectRepository.findMemberIdByProjectId(projectId);
        if(!members.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);
        Releases releases = saveReleaseRequest.toEntity(category);
        Releases save = releaseRepository.save(releases);
        alarmService.produceMessage(projectId, releases.getId(), "새 릴리즈를 게시하셨습니다.", AlarmDomain.RELEASE);
        return save.getId();
    }

    /**
     * 카테고리로 릴리즈 조회 서비스 로직
     * @param categoryId
     * @return ReleaseInfoDto
     */
    public ReleaseInfoDto findReleasesByCategoryId(Long categoryId, Boolean isDeveloper) {
        List<Releases> releasesList = releaseRepository.findByCategoryId(categoryId);
        List<ReleaseDtoEach> dtoList = releasesList.stream()
                .map(r -> mapReleaseToDto(r, isDeveloper))
                .collect(Collectors.toList());
        return ReleaseInfoDto.builder().releaseDtoList(dtoList).build();
    }

    /**
     * 프로젝트로 릴리즈 조회 서비스 로직
     * @param projectId
     * @return ProjectReleasesDto
     */
    public ProjectReleasesDto findReleasesByProjectId(Long projectId, Boolean isDeveloper) {
        Project project = projectRepository.findById(projectId).orElseThrow(ProjectNotFoundException::new);
        List<Category> categoryList = categoryRepository.findByProjectId(project.getId());
        List<Long> categoryIdList = categoryList.stream().map(c -> c.getId()).collect(Collectors.toList());
        List<Releases> releasesList = releaseRepository.findByCategoryIdIn(categoryIdList);
        return mapProjectReleaseToDto(categoryList, releasesList, isDeveloper);
    }

    /**
     * 릴리즈 수정 api 서비스 로직 일부
     * CQS - Query
     * @param requestDto
     * @param projectId
     * @param categoryId
     * @param releaseId
     */
    @Transactional
    public void modifyReleases(ReleaseModifyRequestDto requestDto, Long projectId,
                                                   Long categoryId, Long releaseId) {
        if(!categoryRepository.existsByProjectId(projectId)) {
            throw new CategoryNotFoundException();
        }

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
//        List<Long> members = memberProjectRepository.findMemberIdByProjectId(projectId);
        List<Long> members = memberProjectRepository.findMemberIdByProjectId(projectId);
        if(!members.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }
        Releases releases = releaseRepository.findByCategoryIdAndReleaseId(categoryId, releaseId)
                .orElseThrow(() ->  new ReleasesNotFoundException("해당 카테고리에 속하는 릴리즈가 없습니다."));
        releases.setReleaseDate(requestDto.getReleaseDate());
        releases.setVersion(requestDto.getVersion());
        releases.setMessage(requestDto.getMessage());
        releases.setTag(requestDto.getTag());
        alarmService.produceMessage(projectId, releases.getId(), "릴리즈를 수정하셨습니다.", AlarmDomain.RELEASE);
        // CQS
    }

    /**
     * 릴리즈 수정 api 서비스 로직 일부
     * CQS - Query
     * @param categoryId
     * @return ReleaseModifyResponseDto
     */
    public ReleaseModifyResponseDto findReleaseAndConvert(Long categoryId) {
        Releases releases = releaseRepository.findById(categoryId).orElseThrow(ReleasesNotFoundException::new);
        Optional<Member> memberOptional = memberRepository.findById(releases.getModifierId());
        return ReleaseModifyResponseDto.builder()
                .lastModifierName(memberOptional.isEmpty() ? "anonymous user" : memberOptional.get().getUserName())
                .lastModifiedTime(releases.getModifiedDate())
                .releaseDate(releases.getReleaseDate())
                .version(releases.getVersion())
                .message(releases.getMessage())
                .tag(releases.getTag())
                .build();
    }

    /**
     * 릴리즈 삭제 api 서비스 로직
     * @param projectId
     * @param categoryId
     * @param releaseId
     * @return String
     */
    @Transactional
    public String deleteRelease(Long projectId, Long categoryId, Long releaseId) {
        if(!categoryRepository.existsByProjectId(projectId)) {
            throw new CategoryNotFoundException();
        }
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
//        List<Long> memberList = memberProjectRepository.findMemberIdByProjectId(projectId);
        List<Long> memberList = memberProjectRepository.findMemberIdByProjectId(projectId);
        if(!memberList.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }
        Releases releases = releaseRepository.findByCategoryIdAndReleaseId(categoryId, releaseId)
                .orElseThrow(() ->  new ReleasesNotFoundException("해당 카테고리에 속하는 릴리즈가 없습니다."));
        releaseRepository.delete(releases);
        alarmService.produceMessage(projectId, 0L, "릴리즈를 삭제하셨습니다.", AlarmDomain.RELEASE);
        return "deleted";
    }

    private ReleaseDtoEach mapReleaseToDto(Releases releases, Boolean isDeveloper) {
        if(!isDeveloper) {
            return ReleaseDtoEach.builder()
                    .lastModifiedTime(releases.getModifiedDate())
                    .releaseDate(releases.getReleaseDate())
                    .version(releases.getVersion())
                    .content(releases.getMessage())
                    .tag(releases.getTag())
                    .id(releases.getId())
                    .build();
        }
        Optional<Member> memberOptional = memberRepository.findById(releases.getModifierId());
        return ReleaseDtoEach.builder()
                .lastModifierName(memberOptional.isEmpty() ? "anonymous user" : memberOptional.get().getUserName())
                .lastModifierEmail(memberOptional.isEmpty() ? "anonymous user" : memberOptional.get().getEmail())
                .lastModifiedTime(releases.getModifiedDate())
                .releaseDate(releases.getReleaseDate())
                .version(releases.getVersion())
                .content(releases.getMessage())
                .tag(releases.getTag())
                .id(releases.getId())
                .build();
    }

    private ProjectReleasesDto mapProjectReleaseToDto(List<Category> categoryList, List<Releases> releasesList, Boolean isDeveloper) {
        // in 절로 한번에 조회 후 메모리에서 데이터 정제
        // 쿼리를 여러번 날리지 않아도 됨 -> 네트워크 최적화
        // 메모리에 로드가 발생 -> 트레이드 오프

        List<ProjectReleasesDtoEach> result = new ArrayList<>();

            // 카테고리 ID로 release를 묶어서 정리
            Map<Long, List<Releases>> releasesGroupByCategory = releasesList.stream()
                    .collect(Collectors.groupingBy(r -> r.getCategory().getId()));
            // ID로 카테고리 접근에 용이한 구조로 변경
            Map<Long, Category> categoryMap = categoryList.stream().collect(Collectors.toMap(c -> c.getId(), c -> c));

        categoryMap.forEach((categoryId, category) -> {
            List<Releases> releases = releasesGroupByCategory.get(categoryId);
            CategoryResponseDto categoryResponseDto = CategoryResponseDto.builder()
                    .id(category.getId())
                    .title(category.getTitle())
                    .description(category.getDescription())
                    .build();
            List<ReleaseDtoEach> releaseDtoEachList = releases == null ? new ArrayList<>() : releases.stream()
                    .map(r -> mapReleaseToDto(r, isDeveloper)).collect(Collectors.toList());
            ProjectReleasesDtoEach resultEach = ProjectReleasesDtoEach.builder()
                    .categoryResponseDto(categoryResponseDto)
                    .releaseDtoList(releaseDtoEachList)
                    .build();
            result.add(resultEach);
        });
        return new ProjectReleasesDto(result);
    }

}