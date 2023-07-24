package com.service.releasenote.domain.project.application;

import com.service.releasenote.domain.category.application.CategoryService;
import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.dao.MemberCompanyRepository;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.error.exception.UserNotFoundException;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberCompany;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final CategoryRepository categoryRepository;
    private final MemberCompanyRepository memberCompanyRepository;
    private final CategoryService categoryService;

    /**
     * 프로젝트 저장 서비스 로직
     * @param createProjectRequestDto
     * @param company_id
     * @return CreateProjectResponseDto
     * */
    @Transactional
    public CreateProjectResponseDto createProject
            (CreateProjectRequestDto createProjectRequestDto, Long company_id) {

        // 현재 멤버의 아이디를 가져옴
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

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

        return new CreateProjectResponseDto().toResponseDto(saveProject, company);
    }

    /**
     * 특정 회사의 프로젝트 리스트 조회 서비스 로직
     * @param companyId
     * @return List<FindProjectListResponseDto>
     * */
    public List<FindProjectListResponseDto> findProjectListByCompany(Long companyId) {

        // 회사가 없는 경우 예외 처리
        Company company = companyRepository.findById(companyId)
                .orElseThrow(CompanyNotFoundException::new);

        // 회사의 프로젝트가 없는 경우 예외 처리
        List<Project> projectList = projectRepository.findByCompany(company)
                .orElseThrow(ProjectNotFoundException::new);

        // 프로젝트를 dto에 담아 리스트화
        List<FindProjectListResponseDto> collect = projectList.stream()
                .map(project -> new FindProjectListResponseDto().toResponseDto(project))
                .collect(Collectors.toList());

        return collect;
    }

    /**
     * 회사에 따라 내가 속한 프로젝트를 모두 조회 서비스 로직
     * @return MyProjectByCompanyDto
     * */
    public MyProjectByCompanyDto findMyProjectListByCompany() {
        // 현재 멤버의 아이디를 가져옴
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 내가 속한 회사 리스트 (없으면 예외 처리)
        List<MemberCompany> memberCompanyList = memberCompanyRepository.findByMemberId(currentMemberId);
        List<Company> companyList = memberCompanyList.stream()
                .map(mc -> mc.getCompany())
                .collect(Collectors.toList());

        // 내가 속한 프로젝트 리스트 (전체)
        List<MemberProject> memberProjectList = memberProjectRepository.findByMemberId(currentMemberId);
        List<Project> projectList = memberProjectList.stream()
                .map(mp -> mp.getProject())
                .collect(Collectors.toList());

        return mapMyProjectByCompanyToDto(companyList, projectList);
    }

    private ProjectDtoEach mapProjectToDto(Project project) {
        return ProjectDtoEach.builder()
                .project_id(project.getId())
                .title(project.getTitle())
                .build();
    }

    private MyProjectByCompanyDto mapMyProjectByCompanyToDto(List<Company> companyList, List<Project> projectList) {
        // 회사 id로 project를 묶어서 정리
        Map<Long, List<Project>> projectGroupByCompany = projectList.stream()
                .collect(Collectors.groupingBy(p -> p.getCompany().getId()));

        // ID로 회사 접근에 용이한 구조로 변경?
        Map<Long, Company> companyMap = companyList.stream().collect(Collectors.toMap(c -> c.getId(), c -> c));
        List<MyProjectByCompanyDtoEach> result = new ArrayList<>();

        projectGroupByCompany.forEach((companyId, project) -> {
            Company company = companyMap.get(companyId);
            CompanyResponseDto companyResponseDto = CompanyResponseDto.builder()
                    .name(company.getName())
                    .img_url(company.getImageURL())
                    .build();

            List<ProjectDtoEach> projectDtoEachList = project.stream()
                    .map(p -> mapProjectToDto(p))
                    .collect(Collectors.toList());

            MyProjectByCompanyDtoEach resultEach = MyProjectByCompanyDtoEach.builder()
                    .companyResponseDto(companyResponseDto)
                    .projectDtoList(projectDtoEachList)
                    .build();

            result.add(resultEach);
        });

        return new MyProjectByCompanyDto(result);
    }

    /**
     * 프로젝트 수정 서비스 로직
     * @param updateProjectRequestDto
     * @param project_id
     * @return UpdateProjectResponseDto
     * */
    @Transactional
    public UpdateProjectResponseDto updateProject
            (UpdateProjectRequestDto updateProjectRequestDto, Long project_id) {
        // 현재 멤버의 아이디를 가져옴
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 로그인 하지 않은 경우 예외 처리
        Member member = memberRepository.findById(currentMemberId)
                .orElseThrow(UserNotFoundException::new);

        // 프로젝트가 없는 경우 예외 처리
        Project project = projectRepository.findById(project_id)
                .orElseThrow(ProjectNotFoundException::new);

        // 프로젝트 정보를 수정할 권한이 없으면 예외 처리
        List<Long> memberListByProjectId = memberProjectRepository.findMembersByProjectId(project_id);
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
    public void deleteProject(Long companyId, Long projectId) {
        // 현재 멤버의 아이디를 가져옴
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 프로젝트가 존재하지 않는 경우 예외 처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // 해당 프로젝트가 회사 내에 속하지 않을 경우 예외 처리
        Company company = projectRepository.findCompanyById(projectId)
                .orElseThrow(CompanyNotFoundException::new);

        // 속한 회사가 다를 경우 예외 처리
        if(companyId.equals(company.getId())){
            throw new CompanyNotFoundException();
        }

        // member_project 테이블에서 currentMemberId와 companyId를 이용해서 role 찾기
        Role roleByMemberIdAndProjectId =
                memberProjectRepository.findRoleByMemberIdAndProjectId(projectId, currentMemberId);

        // OWNER가 아닐 경우 예외 처리 (프로젝트를 삭제할 권한이 없습니다)
        if (!roleByMemberIdAndProjectId.equals(Role.OWNER)) {
            throw new NotOwnerProjectException();
        }

        // 프로젝트에 속한 하위 카테고리 삭제
        List<Category> categoryIdByProjectId = categoryRepository.findCategoryByProjectId(projectId);
        for (Category category : categoryIdByProjectId) {
            categoryService.deleteCategory(category.getId(), projectId);
        }

        // member_project에서 삭제
        List<MemberProject> memberProjectByProjectId
                = memberProjectRepository.findMemberProjectByProjectId(projectId);

        if(memberProjectByProjectId.isEmpty()){
            throw new UserNotFoundException();
        } else {
            for (MemberProject memberProject : memberProjectByProjectId) {
                memberProjectRepository.delete(memberProject);
            }
        }

        // 프로젝트 삭제
        projectRepository.delete(project);

    }
}
