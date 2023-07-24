package com.service.releasenote.domain.company.application;

import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.dao.CompanyRepositoryImpl;
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
import com.service.releasenote.domain.project.exception.exceptions.CompanyNotFoundException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.domain.release.dao.ReleaseRepository;
import com.service.releasenote.domain.release.model.Releases;
import com.service.releasenote.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.service.releasenote.domain.company.dto.CompanyDTO.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompanyService {

    private final MemberRepository memberRepository;

    private final CompanyRepository companyRepository;

    private final CompanyRepositoryImpl companyRepositoryImpl;

    private final MemberCompanyRepository memberCompanyRepository;

    private final ProjectRepository projectRepository;

    private final CategoryRepository categoryRepository;

    private final ReleaseRepository releaseRepository;

    private final MemberProjectRepository memberProjectRepository;

    @Transactional
    public Long createCompany(CreateCompanyRequestDTO createCompanyRequestDTO) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 로그인 되지 않은 경우
        // TODO: exception 추가 후 수정
        Member member = memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);
        Company company = companyRepository.save(createCompanyRequestDTO.toEntity());
        MemberCompany memberCompany = MemberCompany.builder()
                .role(Role.OWNER)
                .company(company)
                .member(member)
                .build();
        memberCompanyRepository.save(memberCompany);
        return company.getId();
    }

    public Page<CompanyResponseDTO> findCompaniesByName(String name, Pageable pageable) {
        Page<Company> companyList = companyRepositoryImpl.findCompaniesByName(name, pageable);
        Page<CompanyResponseDTO> collect = companyList.map(company -> {
            CompanyResponseDTO companyListDTO = new CompanyResponseDTO();
            companyListDTO.setId(company.getId());
            companyListDTO.setName(company.getName());
            companyListDTO.setImageUrl(company.getImageURL());
            return companyListDTO;
        });
        return collect;
    }

    @Transactional
    public UpdateCompanyResponseDTO updateCompany(Long company_id, UpdateCompanyRequestDTO updateCompanyRequestDTO) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 로그인 되지 않은 경우
        // TODO: exception 추가 후 수정
        memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);

        // 회사가 존재하지 않는 경우
        // TODO: exception
        Company company = companyRepository.findById(company_id).orElseThrow(CompanyNotFoundException::new);

        // 멤버가 속한 회사가 아닌 경우
        // TODO: exception
        List<Long> memberListByCompanyId = memberCompanyRepository.findMemberListByCompanyId(company_id);
        if(!memberListByCompanyId.contains(currentMemberId)) {
            throw new UserNotFoundException();
        }

        company.setName(updateCompanyRequestDTO.getName());
        company.setImageUrl(updateCompanyRequestDTO.getImageUrl());

        return new UpdateCompanyResponseDTO().toResponseDTO(company);
    }

    @Transactional
    public Long deleteCompany(Long company_id) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        // TODO: exception 추가 후 수정
        memberRepository.findById(currentMemberId).orElseThrow(UserNotFoundException::new);

        // TODO: exception
        Company company = companyRepository.findById(company_id).orElseThrow(CompanyNotFoundException::new);

        // TODO: exception
        Role memberRoleByCompanyId = memberCompanyRepository.findRoleByMemberIdAndCompanyId(company_id, currentMemberId).orElseThrow(UserNotFoundException::new);

        // TODO: exception
        if(!memberRoleByCompanyId.equals(Role.OWNER)) {
            throw new UserNotFoundException();
        }

        // TODO: 확인 필요..
        List<Project> projectList = projectRepository.findByCompanyId(company_id);
        for (Project project : projectList) {
            List<Category> categoryList = categoryRepository.findByProject(project.getId());
            for(Category category: categoryList) {
                List<Releases> releaseList = releaseRepository.findByCategoryId(category.getId());
                releaseRepository.deleteAll(releaseList);
                categoryRepository.delete(category);
            }
            List<MemberProject> memberProjectList = memberProjectRepository.findMemberProjectByProjectId(project.getId());
            memberProjectRepository.deleteAll(memberProjectList);
            projectRepository.delete(project);
        }
        List<MemberCompany> memberCompanyList = memberCompanyRepository.findMemberCompanyByCompanyId(company_id);
        memberCompanyRepository.deleteAll(memberCompanyList);

        companyRepository.delete(company);

        return company_id;
    }

}
