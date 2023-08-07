package com.service.releasenote.domain.member.api;

import com.service.releasenote.domain.member.application.MemberProjectService;
import com.service.releasenote.domain.member.dto.MemberDTO;
import com.service.releasenote.domain.member.dto.MemberProjectDTO.*;
import com.service.releasenote.global.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
     * 프로젝트 멤버 추가 API
     * @param addProjectMemberRequestDto
     * @param project_id
     * @return AddProjectMemberResponseDto
     */
    @ApiOperation("api for get specific category by category id only")
    @ApiResponses({
            @ApiResponse(code=200, message = "요청 성공"),
            @ApiResponse(code=404, message = "존재하지 않는 카테고리")
    })
    @PostMapping(value = "/companies/projects/{project_id}/members")
    public ResponseEntity<AddProjectMemberResponseDto> addMemberProject(
            @RequestBody AddProjectMemberRequestDto addProjectMemberRequestDto,
            @PathVariable Long project_id) {

        // currentMemberId: 초대하는 사람
        // member_id: 초대되는 사람
        AddProjectMemberResponseDto addProjectMember
                = memberProjectService.addProjectMember(addProjectMemberRequestDto, project_id);

        return new ResponseEntity<>(addProjectMember, HttpStatus.CREATED);
    }

    /**
     * 프로젝트 멤버 조회 API
     * @param project_id
     * @return FindMemberListByProjectId
     * */
    @ApiOperation("api for get member of project by project id only")
    @ApiResponses({
            @ApiResponse(code=200, message = "요청 성공"),
            @ApiResponse(code=404, message = "존재하지 않는 프로젝트 멤버")
    })
    @GetMapping(value = "/companies/projects/{project_id}/members")
    public ResponseEntity<FindMemberListByProjectId> findProjectMember(
            @PathVariable Long project_id) {

        FindMemberListByProjectId projectMemberList = memberProjectService.findProjectMemberList(project_id);
        return new ResponseEntity<>(projectMemberList, HttpStatus.OK);
    }

    /**
     * 프로젝트 멤버 삭제 API
     * @param project_id
     * @param memberEmail
     * */
    @ApiOperation("api for delete member of project")
    @ApiResponses({
            @ApiResponse(code=200, message = "요청 성공"),
            @ApiResponse(code=401, message = "인증되지 않은 사용자"),
            @ApiResponse(code=404, message = "존재하지 않는 프로젝트"),
            @ApiResponse(code=409, message = "프로젝트에 속하지 않은 사용자")
    })
    @DeleteMapping(value = "/companies/projects/{project_id}/members")
    public ResponseEntity deleteMemberProject(
            @PathVariable Long project_id,
            @RequestHeader("email") String memberEmail) {

        memberProjectService.deleteProjectMember(project_id, memberEmail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
