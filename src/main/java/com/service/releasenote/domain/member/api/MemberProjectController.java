package com.service.releasenote.domain.member.api;

import com.service.releasenote.domain.member.application.MemberProjectService;
import com.service.releasenote.domain.member.dto.MemberProjectDTO;
import com.service.releasenote.domain.project.application.ProjectService;
import com.service.releasenote.domain.project.dto.ProjectDto;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.service.releasenote.domain.member.dto.MemberProjectDTO.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MemberProjectController {
    private final ProjectService projectService;
    private final MemberProjectService memberProjectService;

    /**
     * 프로젝트 멤버 추가
     */
    @PostMapping("/project/{project_id}/member")
    public ResponseEntity<MemberProjectDTO.AddProjectMemberResponseDto> addMemberProject(
            @RequestBody MemberProjectDTO.AddProjectMemberRequestDto addProjectMemberRequestDto,
            @PathVariable Long project_id) {

        // 현재 멤버의 아이디를 가져옴
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // currentMemberId: 초대하는 사람
        // member_id: 초대되는 사람
        AddProjectMemberResponseDto addProjectMember = memberProjectService.addProjectMember(addProjectMemberRequestDto, project_id, currentMemberId);

        return new ResponseEntity<>(addProjectMember, HttpStatus.CREATED);

//        ProjectDto.CreateProjectResponseDto project = projectService.createProject(addProjectMemberRequestDto, company_id, currentMemberId);
//        return new ResponseEntity<>(project, HttpStatus.CREATED);
    }
}
