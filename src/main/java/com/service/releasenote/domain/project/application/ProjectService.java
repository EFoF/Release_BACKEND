package com.service.releasenote.domain.project.application;

import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.dto.MemberProjectDTO;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.member.model.Role;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.*;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.service.releasenote.domain.project.dto.ProjectDto.*;
import static com.service.releasenote.domain.member.dto.MemberProjectDTO.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final MemberProjectRepository memberProjectRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public CreateProjectResponseDto createProject
            (CreateProjectRequestDto createProjectRequestDto, Long company_id, Long currentMemberId) {

        // 회사가 존재하지 않는 경우 예외 처리
        Company company = companyRepository.findById(company_id)
                .orElseThrow(CompanyNotFoundException::new);

        // 로그인 하지 않은 경우 예외 처리
        Member member = memberRepository.findById(currentMemberId)
                .orElseThrow(UserNotFoundException::new);

        // 프로젝트 객체 생성
        Project newProject = createProjectRequestDto.toEntity(company);

        // 해당 회사 프로젝트들 중 프로젝트의 이름이 이미 존재하는 경우 예외 처리
        List<String> TitleByCompanyId = projectRepository.findTitleByCompanyId(company_id);
        for (String s : TitleByCompanyId) {
            if(s.equals(newProject.getTitle())){
                // equal 메소드에서 NullPointerException이 발생하는 것을 막기 위해
                // A.equals(B) 에서 A는 반드시 null 값이 아닌 것으로 넣어주는 것이 좋다!! (NPE를 다룰 게 아니라면)
                throw new DuplicatedProjectTitleException();
            }
        }

        // 프로젝트 저장
        Project saveProject = projectRepository.save(newProject);

        // member_project에 OWNER 지정
        RoleDto roleDto = new RoleDto();
        MemberProject memberProject = roleDto.toEntity(member, newProject, Role.OWNER);
        memberProjectRepository.save(memberProject);    // 멤버 프로젝트에 role 저장

        return new CreateProjectResponseDto().toResponseDto(saveProject);
    }

    public FindMyProjectListByCompanyResponseDto getMyProjectList() {
        // 현재 멤버의 아이디를 가져옴
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        return new FindMyProjectListByCompanyResponseDto().toResponseDto();
    }

    @Transactional
    public UpdateProjectResponseDto updateProject
            (UpdateProjectRequestDto updateProjectRequestDto, Long project_id, Long currentMemberId) {

        // 로그인 하지 않은 경우 예외 처리
        Member member = memberRepository.findById(currentMemberId)
                .orElseThrow(UserNotFoundException::new);
        log.info("member: " + member.getId());

        // 프로젝트가 없는 경우 예외 처리
        Project project = projectRepository.findById(project_id)
                .orElseThrow(ProjectNotFoundException::new);
        log.info("project: " + project_id);

        // 프로젝트 정보를 수정할 권한이 없으면 예외 처리
        List<Long> memberListByProjectId = memberProjectRepository.findMemberListByProjectId(project_id);
        if(!memberListByProjectId.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }
        log.info("project member list: " + memberListByProjectId.toString());

        // 정보 수정
        project.setTitle(updateProjectRequestDto.getTitle());
        project.setScope(updateProjectRequestDto.isScope());
        project.setDescription(updateProjectRequestDto.getDescription());

        return new UpdateProjectResponseDto().toResponseDto(project);
    }

    @Transactional
    public void deleteProject(Long companyId, Long projectId, Long currentMemberId) {
        // 프로젝트가 존재하지 않는 경우 예외 처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // member_project 테이블에서 currentMemberId와 companyId를 이용해서 role 찾기
        Role roleByMemberIdAndProjectId = projectRepository.findRoleByMemberIdAndProjectId(projectId, currentMemberId);
//        log.info(roleByMemberIdAndProjectId.toString());    // OWNER

        // OWNER가 아닐 경우 예외 처리 (프로젝트를 삭제할 권한이 없습니다)
        if (!roleByMemberIdAndProjectId.equals(Role.OWNER)) {
            throw new NotOwnerProjectException();
        }

        // OWNER일 경우 프로젝트 삭제
        // 1. 회사의 프로젝트 리스트에서 삭제 되어야 한다.
        // 2. 내가 속한 프로젝트 리스트에서 삭제 되어야 한다.
        // 3. member_project에서 삭제 되어야 하나?
        // 4. 프로젝트의 하위 카테고리도 (자동으로? cascade 어쩌구..) 삭제되어야 한다.
        // 5. 프로젝트 삭제

        // 3
        List<MemberProject> memberProjectByProjectId = projectRepository.findMemberProjectByProjectId(projectId);
        for (MemberProject memberProject : memberProjectByProjectId) {
            memberProjectRepository.delete(memberProject);
        }

        // 4
//        List<Category> categoryIdByProjectId = projectRepository.findCategoryByProjectId(projectId);
//        for (Category category : categoryIdByProjectId) {
//            categoryRepository.delete(category);
//        }

        // 프로젝트 삭제
        projectRepository.delete(project);

    }


}
