package com.service.releasenote.domain.member.api;

import com.service.releasenote.domain.member.application.MemberProjectService;
import com.service.releasenote.domain.member.dto.MemberDTO;
import com.service.releasenote.domain.member.dto.MemberProjectDTO;
import com.service.releasenote.global.util.SecurityUtil;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.service.releasenote.domain.member.dto.MemberProjectDTO.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = {"member_project"})
public class MemberProjectController {
    private final MemberProjectService memberProjectService;

    /**
     * 프로젝트 멤버 추가
     */
    @PostMapping(value = "companies/projects/{project_id}/members")
    public ResponseEntity<MemberProjectDTO.AddProjectMemberResponseDto> addMemberProject(
            @RequestBody MemberProjectDTO.AddProjectMemberRequestDto addProjectMemberRequestDto,
            @PathVariable Long project_id) {

        // currentMemberId: 초대하는 사람
        // member_id: 초대되는 사람
        AddProjectMemberResponseDto addProjectMember
                = memberProjectService.addProjectMember(addProjectMemberRequestDto, project_id);

        return new ResponseEntity<>(addProjectMember, HttpStatus.CREATED);
    }

    @GetMapping(value = "companies/projects/{project_id}/members")
    public ResponseEntity<FindMemberListByProjectId> findProjectMember(
            @PathVariable Long project_id) {

        FindMemberListByProjectId projectMemberList = memberProjectService.findProjectMemberList(project_id);
        return new ResponseEntity<>(projectMemberList, HttpStatus.OK);
    }



    /**
     * 프로젝트 멤버 삭제
     * */
    @DeleteMapping(value = "companies/projects/{project_id}/members")
    public ResponseEntity deleteMemberProject(
            @PathVariable Long project_id,
            @RequestHeader("email") String memberEmail) {

        memberProjectService.deleteProjectMember(project_id, memberEmail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
