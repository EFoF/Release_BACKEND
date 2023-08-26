package com.service.releasenote.domain.project.application;

import com.service.releasenote.domain.alarm.dao.AlarmRepository;
import com.service.releasenote.domain.category.application.CategoryService;
import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberProject;
import com.service.releasenote.domain.member.model.Role;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.*;
import com.service.releasenote.domain.project.model.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.service.releasenote.domain.project.dto.ProjectDto.*;
import static com.service.releasenote.domain.company.dto.CompanyDTO.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final MemberProjectRepository memberProjectRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final AlarmRepository alarmRepository;

    /**
     * 프로젝트 저장 서비스 로직
     * @param createProjectRequestDto
     * @param company_id
     * @return CreateProjectResponseDto
     * */
    @Transactional
    public Long createProject
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
        MemberProject memberProject = MemberProject.builder()
                .member(member)
                .project(newProject)
                .role(Role.OWNER)
                .build();
        memberProjectRepository.save(memberProject);

        return saveProject.getId();
    }

    /**
     * 회사 단위로 내가 속한 프로젝트 리스트 조회 서비스 로직
     * @param companyId
     * @param pageable
     * @return FindProjectListByCompanyResponseDto
     * */
    public FindProjectListByCompanyResponseDto findProjectListByCompany(Long companyId, Pageable pageable, Long currentMemberId) {

        // company가 없으면 예외 처리
        Company company = companyRepository.findById(companyId)
                .orElseThrow(CompanyNotFoundException::new);

        Slice<Project> projectsByCompanyIdAndMemberId =
                projectRepository.findProjectsByCompanyIdAndMemberId(companyId, currentMemberId, pageable);

        // 1. companyId에 속한 프로젝트를 모두 가져온다.
        // 2. 해당 project에 currentMemberId가 속해 있다면 리스트에 포함한다.

        // 프로젝트를 dto에 담아 리스트화
        Slice<FindProjectListResponseDto> map = projectsByCompanyIdAndMemberId
                .map(project -> new FindProjectListResponseDto().toResponseDto(project));

        return new FindProjectListByCompanyResponseDto().toResponseDto(company, map);
    }

    public FindProjectListByCompanyIdResponseDto findProjectListByCompanyId(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(CompanyNotFoundException::new);
        List<Project> projectList = projectRepository.findByCompanyId(companyId);
        return new FindProjectListByCompanyIdResponseDto().toResponseDto(company, projectList);
    }

    /**
     * 내가 속한 프로젝트 조회 서비스 로직
     * */
    public ProjectPaginationDtoWrapper getProjectPage(Pageable pageable, Long currentMemberId) {
        Page<ProjectPaginationDtoEach> projects = projectRepository.findMyProjects(currentMemberId, pageable);
        return ProjectPaginationDtoWrapper.builder().list(projects).build();
    }

    public FindProjectListByCompanyIdResponseDto getMyProjectPageWithCompany(Long companyId, Long currentMemberId) {
        Member member = memberRepository.findById(currentMemberId)
                .orElseThrow(UserNotFoundException::new);
        Company company = companyRepository.findById(companyId)
                .orElseThrow(CompanyNotFoundException::new);
        List<MemberProject> result = projectRepository.findMyProjectsWithCompanyId(currentMemberId, companyId);
        List<Project> projectList = result.stream().map(mp -> mp.getProject()).collect(Collectors.toList());
        return new FindProjectListByCompanyIdResponseDto().toResponseDto(company, projectList);
    }

    /**
     * 프로젝트 수정 서비스 로직
     * @param updateProjectRequestDto
     * @param project_id
     * @return UpdateProjectResponseDto
     * */
    @Transactional
    public UpdateProjectResponseDto updateProject
    (UpdateProjectRequestDto updateProjectRequestDto, Long project_id, Long currentMemberId) {

        // 로그인 하지 않은 경우 예외 처리
        Member member = memberRepository.findById(currentMemberId)
                .orElseThrow(UserNotFoundException::new);

        // 프로젝트가 없는 경우 예외 처리
        Project project = projectRepository.findById(project_id)
                .orElseThrow(ProjectNotFoundException::new);

        // 프로젝트 정보를 수정할 권한이 없으면 예외 처리
        List<Long> memberListByProjectId = memberProjectRepository.findMemberIdByProjectId(project_id); // 수정 필요
        if(!memberListByProjectId.contains(currentMemberId)) {
            throw new ProjectPermissionDeniedException();
        }

        // 정보 수정
        project.setTitle(updateProjectRequestDto.getTitle());
        project.setScope(updateProjectRequestDto.isScope());
        project.setDescription(updateProjectRequestDto.getDescription());

        return new UpdateProjectResponseDto().toResponseDto(project);
    }

    /**
     * 프로젝트 삭제 서비스 로직
     * @param companyId
     * @param projectId
     * */
    @Transactional
    public void deleteProject(Long companyId, Long projectId, Long currentMemberId) {

        // 프로젝트가 존재하지 않는 경우 예외 처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        Company company = project.getCompany();

        // 속한 회사가 다를 경우 예외 처리
        if(!companyId.equals(company.getId())){
            throw new CompanyNotFoundException();
        }

        // member_project에 currentMemberId가 없을 경우 예외 처리
        List<MemberProject> memberProjectsByMember = memberProjectRepository.findByMemberId(currentMemberId);
        if(memberProjectsByMember.isEmpty()){
            throw new ProjectPermissionDeniedException();
        }

        // member_project에 project가 없을 경우 예외 처리
        List<MemberProject> memberProjectsByProject = memberProjectRepository.findByProjectId(projectId);
        if(memberProjectsByProject.isEmpty()){
            throw new ProjectNotFoundException();
        }

        // member_project 테이블에서 currentMemberId와 companyId를 이용해서 role 찾기
        MemberProject memberProject = memberProjectRepository.findByMemberIdAndProjectId(currentMemberId, projectId)
                .orElseThrow(UserNotFoundException::new);

        // OWNER가 아닐 경우 예외 처리 (프로젝트를 삭제할 권한이 없습니다)
        if (!memberProject.getRole().equals(Role.OWNER)) {
            throw new NotOwnerProjectException();
        }

        // 프로젝트에 속한 하위 카테고리 삭제
        List<Category> categoryIdByProjectId = categoryRepository.findCategoryByProjectId(projectId);
        for (Category category : categoryIdByProjectId) {
            categoryService.deleteCategory(category.getId(), projectId, currentMemberId);
        }

        // member_project에서 삭제
        List<MemberProject> memberProjectByProjectId
                = memberProjectRepository.findMemberProjectByProjectId(projectId);

        if(memberProjectByProjectId.isEmpty()){
            throw new UserNotFoundException();
        } else {
            for (MemberProject memberProject1 : memberProjectByProjectId) {
                memberProjectRepository.delete(memberProject1);
            }
        }

        // 프로젝트 삭제
        projectRepository.delete(project);
    }
}