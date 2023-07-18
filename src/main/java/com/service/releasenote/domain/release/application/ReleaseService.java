package com.service.releasenote.domain.release.application;

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
import com.service.releasenote.domain.release.dao.ReleaseRepository;
import com.service.releasenote.domain.release.model.Releases;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    public ReleaseInfoDto findReleasesByCategoryId(Long categoryId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);
        List<Releases> releasesList = releaseRepository.findByCategoryId(categoryId);
        List<ReleaseDtoEach> dtoList = releasesList.stream()
                .map(r -> mapReleaseToDto(r, r.getModifierId()))
                .collect(Collectors.toList());
        return ReleaseInfoDto.builder().releaseDtoList(dtoList).build();
    }

    private ReleaseDtoEach mapReleaseToDto(Releases releases, Long lastModifierId) {
        Member member = memberRepository.findById(lastModifierId).orElseThrow(UserNotFoundException::new);
        return ReleaseDtoEach.builder()
                .lastModifiedTime(releases.getModifiedDate())
                .lastModifierName(member.getUserName())
                .version(releases.getVersion())
                .content(releases.getMessage())
                .tag(releases.getTag())
                .build();
    }

}