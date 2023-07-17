package com.service.releasenote.domain.project.application;

import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.model.Role;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.CompanyNotFoundException;
import com.service.releasenote.domain.project.exception.exceptions.DuplicatedProjectTitleException;
import com.service.releasenote.domain.project.exception.exceptions.NotOwnerProjectException;
import com.service.releasenote.domain.project.exception.exceptions.ProjectNotFoundException;
import com.service.releasenote.domain.project.model.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.service.releasenote.domain.project.dto.ProjectDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public CreateProjectResponseDto createProject
            (CreateProjectRequestDto createProjectRequestDto, Long company_id) {

        // 회사가 존재하지 않는 경우 예외 처리
        Company company = companyRepository.findById(company_id)
                .orElseThrow(CompanyNotFoundException::new);

        Project newProject = createProjectRequestDto.toEntity(company);

        // 해당 회사 프로젝트들 중 프로젝트의 이름이 이미 존재하는 경우 예외 처리
        // equal 메소드에서 NullPointerException이 발생하는 것을 막기 위해
        // A.equals(B) 에서 A는 반드시 null 값이 아닌 것으로 넣어주는 것이 좋다!! (NPE를 다룰 게 아니라면)
        List<String> TitleByCompanyId = projectRepository.findTitleByCompanyId(company_id);
        for (String s : TitleByCompanyId) {
            if(s.equals(newProject.getTitle())){
                throw new DuplicatedProjectTitleException();
            }
        }

        Project saveProject = projectRepository.save(newProject);

        return new CreateProjectResponseDto().toResponseDto(saveProject);
    }

    @Transactional
    public void deleteProject(Long companyId, Long projectId, Long currentMemberId) {
        // 프로젝트가 존재하지 않는 경우 예외 처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        // member_project 테이블에서 currentMemberId와 companyId를 이용해서 role 찾기
        Role roleByMemberIdAndProjectId = projectRepository.findRoleByMemberIdAndProjectId(projectId, currentMemberId);

        // OWNER가 아닐 경우 예외 처리 (프로젝트를 삭제할 권한이 없습니다)
        if (!roleByMemberIdAndProjectId.equals(Role.OWNER)) {
            throw new NotOwnerProjectException();
        }

        // OWNER일 경우 프로젝트 삭제
        // 1. 회사의 프로젝트 리스트에서 삭제 되어야 한다.
        // 2. 내가 속한 프로젝트 리스트에서 삭제 되어야 한다.
        // 3. member_project에서 삭제 되어야 하나?
        // 4. 프로젝트의 하위 카테고리도 (자동으로? cascade 어쩌구..) 삭제되어야 한다.
        projectRepository.delete(project);

    }
}
