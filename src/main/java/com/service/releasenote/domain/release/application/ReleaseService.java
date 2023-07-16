package com.service.releasenote.domain.release.application;

import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.model.Member;
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

    private final ReleaseRepository releaseRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void saveRelease(SaveReleaseRequest saveReleaseRequest) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
    }

    public ReleaseInfoDto findReleasesByCategoryId(Long categoryId) {
        List<Releases> releasesList = releaseRepository.findByCategoryId(categoryId);
        List<ReleaseDtoEach> dtoList = releasesList.stream()
                .map(r -> mapReleaseToDto(r))
                .collect(Collectors.toList());
        return ReleaseInfoDto.builder().releaseDtoList(dtoList).build();
    }

    private ReleaseDtoEach mapReleaseToDto(Releases releases) {
        return ReleaseDtoEach.builder()
                .lastModifiedTime(releases.getModifiedDate())
                .version(releases.getVersion())
                .content(releases.getMessage())
                .tag(releases.getTag())
                .build();
    }

}