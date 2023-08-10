package com.service.releasenote;

import com.service.releasenote.domain.alarm.dao.AlarmRepository;
import com.service.releasenote.domain.category.application.CategoryService;
import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.category.model.Category;
import com.service.releasenote.domain.company.application.CompanyService;
import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.application.AuthService;
import com.service.releasenote.domain.member.application.MemberCompanyService;
import com.service.releasenote.domain.member.application.MemberProjectService;
import com.service.releasenote.domain.member.dao.MemberCompanyRepository;
import com.service.releasenote.domain.member.dao.MemberProjectRepository;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.model.*;
import com.service.releasenote.domain.project.application.ProjectService;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.project.exception.exceptions.CompanyNotFoundException;
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.domain.release.application.ReleaseService;
import com.service.releasenote.domain.release.dao.ReleaseRepository;
import com.service.releasenote.domain.release.model.Releases;
import com.service.releasenote.domain.release.model.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.service.releasenote.domain.member.dto.MemberCompanyDTO.AddMemberRequestDTO;
import static com.service.releasenote.domain.member.dto.MemberCompanyDTO.AddMemberResponseDTO;
import static com.service.releasenote.domain.member.dto.MemberDTO.LoginDTO;
import static com.service.releasenote.domain.member.dto.MemberDTO.SignUpRequest;
import static com.service.releasenote.domain.member.dto.MemberProjectDTO.AddProjectMemberRequestDto;
import static com.service.releasenote.domain.project.dto.ProjectDto.CreateProjectRequestDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureMockMvc
public class IntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    CompanyRepository companyRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ReleaseRepository releaseRepository;
    @Autowired
    AlarmRepository alarmRepository;
    @Autowired
    MemberCompanyRepository memberCompanyRepository;
    @Autowired
    MemberProjectRepository memberProjectRepository;
    @Autowired
    AuthService authService;
    @Autowired
    CompanyService companyService;
    @Autowired
    ProjectService projectService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ReleaseService releaseService;
    @Autowired
    MemberCompanyService memberCompanyService;
    @Autowired
    MemberProjectService memberProjectService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    AuthenticationManagerBuilder authenticationManagerBuilder;


    @BeforeEach
    private void set() {
        try {
            setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    private void clean() {
        alarmRepository.deleteAll();
        releaseRepository.deleteAll();
        categoryRepository.deleteAll();
        memberProjectRepository.deleteAll();
        projectRepository.deleteAll();
        memberCompanyRepository.deleteAll();
        companyRepository.deleteAll();
        memberRepository.deleteAll();
        // id 이슈 : 아래의 코드로 auto-increment를 초기화할 수 있지만, @Transactional 이 없는 환경에서는 불가능하다.
//        entityManager.createNativeQuery("ALERT TABLE alarm AUTO_INCREMENT 1").executeUpdate();
//        entityManager.createNativeQuery("ALERT TABLE category AUTO_INCREMENT 1").executeUpdate();
//        entityManager.createNativeQuery("ALERT TABLE company AUTO_INCREMENT 1").executeUpdate();
//        entityManager.createNativeQuery("ALERT TABLE member AUTO_INCREMENT 1").executeUpdate();
//        entityManager.createNativeQuery("ALERT TABLE member_company AUTO_INCREMENT 1").executeUpdate();
//        entityManager.createNativeQuery("ALERT TABLE member_project AUTO_INCREMENT 1").executeUpdate();
//        entityManager.createNativeQuery("ALERT TABLE project AUTO_INCREMENT 1").executeUpdate();
//        entityManager.createNativeQuery("ALERT TABLE releases AUTO_INCREMENT 1").executeUpdate();
    }

    // 사용자의 회원가입에 사용되는 dto를 생성하는 메서드
    public SignUpRequest buildSignUpRequest(int id) {
        return SignUpRequest.builder()
//                .password(passwordEncoder.encode("user_"+id+"password"))
                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
                .password("user_"+id+"password")
                .email("user"+id+"@doklib.com")
                .username("user"+id)
                .build();
    }

    // 사용자의 인증/인가를 위해 로그인을 수행해야 하는데, 이때 사용되는 dto를 생성하는 메서드
    public LoginDTO buildLoginDto(Long userId) {
        return LoginDTO.builder()
                .email("user"+userId+"@doklib.com")
                .password("user_"+userId+"password")
                .build();
    }


    public AddMemberResponseDTO buildAddMemberResponseDto(Long memberId, Long companyId, Role role) {
        return AddMemberResponseDTO.builder()
                .companyId(companyId)
                .memberId(memberId)
                .role(role)
                .build();
    }

    public AddMemberRequestDTO buildAddMemberRequestDto(Long userId) {
        return AddMemberRequestDTO.builder()
                .email("user"+userId+"@doklib.com")
                .build();
    }

    public CreateProjectRequestDto buildProjectRequestDto(String title, Long userId) {
        return CreateProjectRequestDto.builder()
                .title(title)
                .description("user " + userId + " 's " + title)
                .scope(true)
                .build();
    }

    public AddProjectMemberRequestDto buildAddProjectMemberRequestDto(Long userId) {
            return AddProjectMemberRequestDto.builder()
                    .email("user"+userId+"@doklib.com")
                    .build();
    }

    private Member saveMember(int i) {
        SignUpRequest signUpRequest = buildSignUpRequest(i);
        Member member = Member.builder()
                .userName(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .authority(Authority.ROLE_USER)
                .memberLoginType(MemberLoginType.RELEASE_LOGIN)
                .build();
        return memberRepository.save(member);
    }

    private Company saveCompany(String title, Member owner) {
        Company company = Company.builder()
                .name(title)
                .ImageURL("empty image")
                .build();
        Company savedCompany = companyRepository.save(company);
        MemberCompany memberCompany = MemberCompany.builder()
                .role(Role.OWNER)
                .company(company)
                .member(owner)
                .build();
        memberCompanyRepository.save(memberCompany);

        return savedCompany;
    }

    private MemberCompany participateCompany(Company company, Member member) {
        MemberCompany memberCompany = MemberCompany.builder()
                .role(Role.MEMBER)
                .company(company)
                .member(member)
                .build();
        return memberCompanyRepository.save(memberCompany);
    }

    private Project saveProject(Company company, Member member, String title) {
        Project project = Project.builder()
                .description(title + " description")
                .company(company)
                .title(title)
                .scope(true)
                .build();
        Project savedProject = projectRepository.save(project);
        MemberProject memberProject = MemberProject.builder()
                .member(member)
                .project(savedProject)
                .role(Role.OWNER)
                .build();
        memberProjectRepository.save(memberProject);
        return savedProject;
    }

    private MemberProject participateProject(Project project, Member member) {
        MemberProject memberProject = MemberProject.builder()
                .role(Role.MEMBER)
                .project(project)
                .member(member)
                .build();
        return memberProjectRepository.save(memberProject);
    }

    private Category saveCategory(Project project, String title) {
        Category category = Category.builder()
                .description(title + " 's description")
                .detail("### " + title + "'s detail")
                .project(project)
                .title(title)
                .build();
        return categoryRepository.save(category);
    }

    private Releases saveRelease(Category category, String version) {
        Releases releases = Releases.builder()
                .message(category.getTitle() + " release version :: " + version)
                .releaseDate(LocalDateTime.now())
                .category(category)
                .version(version)
                .tag(Tag.NEW)
                .build();
        return releaseRepository.save(releases);
    }

    private void setup() throws IOException {
        // 1. 20명의 멤버를 생성하여 데이터베이스에 저장
        List<Member> members = new ArrayList<>();
        for(int i=1; i<=20; i++) {
            members.add(saveMember(i));
        }
        List<Long> ownerList = new ArrayList<>();
        ownerList.add(1L); ownerList.add(5L); ownerList.add(8L); ownerList.add(13L); ownerList.add(16L);
        Map<Long, List<Member>> memberList = new HashMap<>();
        List<Member> ACompanyParticipants = new ArrayList<>();
        ACompanyParticipants.add(members.get(1)); ACompanyParticipants.add(members.get(2)); ACompanyParticipants.add(members.get(3));

        List<Member> BCompanyParticipants = new ArrayList<>();
        BCompanyParticipants.add(members.get(5)); BCompanyParticipants.add(members.get(6));

        List<Member> CCompanyParticipants = new ArrayList<>();
        CCompanyParticipants.add(members.get(8)); CCompanyParticipants.add(members.get(9));
        CCompanyParticipants.add(members.get(10)); CCompanyParticipants.add(members.get(11));

        List<Member> DCompanyParticipants = new ArrayList<>();
        DCompanyParticipants.add(members.get(13)); DCompanyParticipants.add(members.get(14));


        List<Member> ECompanyParticipants = new ArrayList<>();
        ECompanyParticipants.add(members.get(16)); ECompanyParticipants.add(members.get(17));
        ECompanyParticipants.add(members.get(18)); ECompanyParticipants.add(members.get(19));

        memberList.put(1L, ACompanyParticipants);
        memberList.put(5L, BCompanyParticipants);
        memberList.put(8L, CCompanyParticipants);
        memberList.put(13L, DCompanyParticipants);
        memberList.put(16L, ECompanyParticipants);

        // 2. 오너들이 회사를 생성
        Company A = saveCompany("A", members.get(0));
        Company B = saveCompany("B", members.get(4));
        Company C = saveCompany("C", members.get(7));
        Company D = saveCompany("D", members.get(12));
        Company E = saveCompany("E", members.get(15));

        List<Member> Amembers = memberList.get(ownerList.get(0));
        for (Member member : Amembers) {
            participateCompany(A, member);
        }

        List<Member> Bmembers = memberList.get(ownerList.get(1));
        for (Member member : Bmembers) {
            participateCompany(B, member);
        }

        List<Member> Cmembers = memberList.get(ownerList.get(2));
        for (Member member : Cmembers) {
            participateCompany(C, member);
        }

        List<Member> Dmembers = memberList.get(ownerList.get(3));
        for (Member member : Dmembers) {
            participateCompany(D, member);
        }

        List<Member> Emembers = memberList.get(ownerList.get(4));
        for (Member member : Emembers) {
            participateCompany(E, member);
        }


//        // 4. 초대된 멤버는 프로젝트를 생성한다.
        // A 1, 3, 4, 2
        Project AP1 = saveProject(A, members.get(0), "AP-1");
        Project AP2 = saveProject(A, members.get(2), "AP-2");
        Project AP3 = saveProject(A, members.get(3), "AP-3");
        Project AP4 = saveProject(A, members.get(1), "AP-4");
        // B 7, 5, 6
        Project BP1 = saveProject(B, members.get(6), "BP-1");
        Project BP2 = saveProject(B, members.get(4), "BP-2");
        Project BP3 = saveProject(B, members.get(5), "BP-3");
        // C 12, 9
        Project CP1 = saveProject(C, members.get(11), "CP-1");
        Project CP2 = saveProject(C, members.get(8), "CP-2");
        // D 13 15
        Project DP1 = saveProject(D, members.get(12), "DP-1");
        Project DP2 = saveProject(D, members.get(14), "DP-2");
        // E 17 20
        Project EP1 = saveProject(E, members.get(16), "EP-1");
        Project EP2 = saveProject(E, members.get(19), "EP-2");

        // 5. 프로젝트를 생성한 후 멤버들을 초대한다.
        // 5.1 A 회사의 프로젝트
        participateProject(AP1, members.get(1));participateProject(AP1, members.get(2));participateProject(AP1, members.get(3));
        participateProject(AP2, members.get(0));participateProject(AP3, members.get(1));participateProject(AP3, members.get(2));
        participateProject(AP4, members.get(0));participateProject(AP4, members.get(3));

        // 5.2 B 회사의 프로젝트
        participateProject(BP1, members.get(4));participateProject(BP2, members.get(5));participateProject(BP3, members.get(4));
        participateProject(BP3, members.get(6));

        // 5.3 C 회사의 프로젝트
        participateProject(CP1, members.get(8));participateProject(CP1, members.get(9));participateProject(CP1, members.get(10));
        participateProject(CP2, members.get(10));

        // 5.4 D 회사의 프로젝트
        participateProject(DP1, members.get(13));participateProject(DP1, members.get(14));participateProject(DP2, members.get(13));

        // 5.5 E 회사의 프로젝트
        participateProject(EP1, members.get(15));participateProject(EP1, members.get(17));participateProject(EP1, members.get(18));
        participateProject(EP1, members.get(19));participateProject(EP2, members.get(15));participateProject(EP2, members.get(18));

        // 6. 테스트 요구사항에 맞춰 담당자가 카테고리를 생성한다.
        Category AP1C1 = saveCategory(AP1, "APC1-1"); Category AP1C2 = saveCategory(AP1, "APC1-2");
        Category AP2C1 = saveCategory(AP2, "APC2-1"); Category AP2C2 = saveCategory(AP2, "APC2-2");
        Category AP2C3 = saveCategory(AP2, "APC2-3"); Category AP3C1 = saveCategory(AP3, "APC3-1");
        Category AP3C2 = saveCategory(AP3, "APC3-2"); Category AP3C3 = saveCategory(AP3, "APC3-3");
        Category AP4C1 = saveCategory(AP4, "APC4-1"); Category AP4C2 = saveCategory(AP4, "APC4-2");
        Category AP4C3 = saveCategory(AP4, "APC4-3");

        Category BP1C1 = saveCategory(BP1, "BP1-1"); Category BP1C2 = saveCategory(BP1, "BP1-2");
        Category BP1C3 = saveCategory(BP1, "BP1-3"); Category BP2C1 = saveCategory(BP2, "BP2-1");
        Category BP2C2 = saveCategory(BP2, "BP2-2"); Category BP2C3 = saveCategory(BP2, "BP2-3");
        Category BP2C4 = saveCategory(BP2, "BP2-4"); Category BP3C1 = saveCategory(BP3, "BP3-1");
        Category BP3C2 = saveCategory(BP3, "BP3-2"); Category BP3C3 = saveCategory(BP3, "BP3-3");

        Category CP1C1 = saveCategory(CP1, "CP1-1"); Category CP1C2 = saveCategory(CP1, "CP1-2");
        Category CP1C3 = saveCategory(CP1, "CP1-3");

        Category DP1C1 = saveCategory(DP1, "DP1-1"); Category DP1C2 = saveCategory(DP1, "DP1-2");
        Category DP1C3 = saveCategory(DP1, "DP1-3"); Category DP2C1 = saveCategory(DP2, "DP2-1");

        Category EP1C1 = saveCategory(EP1, "EP1-1"); Category EP1C2 = saveCategory(EP1, "EP1-2");
        Category EP1C3 = saveCategory(EP1, "EP1-3"); Category EP2C1 = saveCategory(EP2, "EP2-1");
        Category EP2C2 = saveCategory(EP2, "EP2-2"); Category EP2C3 = saveCategory(EP2, "EP2-3");

        // 7. 테스트 요구사항에 맞춰 담당자가 릴리즈를 생성한다.
        saveRelease(AP1C1,"1.0.0"); saveRelease(AP1C1,"1.0.1");
        saveRelease(AP1C2,"1.0.0"); saveRelease(AP1C2,"1.0.1"); saveRelease(AP1C2,"1.0.2");
        saveRelease(AP2C1,"1.0.0"); saveRelease(AP2C1,"1.0.1"); saveRelease(AP2C1,"1.0.2");
        saveRelease(AP2C2,"1.0.0"); saveRelease(AP2C2,"1.0.1");
        saveRelease(AP2C3,"1.0.0");
        saveRelease(AP3C1,"1.0.0");
        saveRelease(AP3C2,"1.0.0"); saveRelease(AP3C2,"1.0.1");
        saveRelease(AP3C3,"1.0.0"); saveRelease(AP3C3,"1.0.1");
        saveRelease(AP4C1,"1.0.0"); saveRelease(AP4C1,"1.0.1"); saveRelease(AP4C1,"1.0.2"); saveRelease(AP4C1,"1.0.3");
        saveRelease(AP4C2,"1.0.0"); saveRelease(AP4C2,"1.0.1");
        saveRelease(AP4C3,"1.0.0");

        saveRelease(BP1C1,"1.0.0"); saveRelease(BP1C1,"1.0.1"); saveRelease(BP1C1,"1.0.2");
        saveRelease(BP1C2,"1.0.0"); saveRelease(BP1C2,"1.0.1");
        saveRelease(BP1C3,"1.0.0"); saveRelease(BP1C3,"1.0.1");
        saveRelease(BP2C1, "1.0.0");
        saveRelease(BP2C2, "1.0.0"); saveRelease(BP2C2, "1.0.1");
        saveRelease(BP2C3, "1.0.0"); saveRelease(BP2C3, "1.0.1");
        saveRelease(BP2C4, "1.0.0");
        saveRelease(BP3C1, "1.0.0"); saveRelease(BP3C1, "1.0.1");
        saveRelease(BP3C2, "1.0.0"); saveRelease(BP3C2, "1.0.1");
        saveRelease(BP3C3, "1.0.0"); saveRelease(BP3C3, "1.0.1");

        saveRelease(CP1C1, "1.0.0"); saveRelease(CP1C1, "1.0.1"); saveRelease(CP1C1, "1.0.2");
        saveRelease(CP1C1, "1.0.3");
        saveRelease(CP1C2, "1.0.0"); saveRelease(CP1C2, "1.0.1");
        saveRelease(CP1C3, "1.0.0"); saveRelease(CP1C3, "1.0.1"); saveRelease(CP1C3, "1.0.2");


        saveRelease(DP1C1, "1.0.0"); saveRelease(DP1C1, "1.0.1"); saveRelease(DP1C1, "1.0.2");
        saveRelease(DP1C2, "1.0.0"); saveRelease(DP1C2, "1.0.1");
        saveRelease(DP1C3, "1.0.0"); saveRelease(DP1C3, "1.0.1"); saveRelease(DP1C3, "1.0.2");
        saveRelease(DP1C3, "1.0.3");
        saveRelease(DP2C1, "1.0.0"); saveRelease(DP2C1, "1.0.1");

        saveRelease(EP1C1, "1.0.0"); saveRelease(EP1C1, "1.0.1"); saveRelease(EP1C1, "1.0.2");
        saveRelease(EP1C2, "1.0.0"); saveRelease(EP1C2, "1.0.1"); saveRelease(EP1C2, "1.0.2");
        saveRelease(EP1C3, "1.0.0"); saveRelease(EP1C3, "1.0.1"); saveRelease(EP1C3, "1.0.2");
        saveRelease(EP2C1, "1.0.0");
        saveRelease(EP2C2, "1.0.0"); saveRelease(EP2C2, "1.0.1");
        saveRelease(EP2C3, "1.0.0");
        // 8. 테스트 환경설정이 끝났다.
    }

    // 직접 SecurityContext를 생성한 뒤 인증을 시키는 메서드이다.
    private void buildSecurityContext(SecurityContext securityContext, LoginDTO loginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        securityContext.setAuthentication(authentication);
    }

    /*
        모든 테스트 상황의 베이스는 다음과 같다.
        총 20명의 사용자가 있다.
        그 중 5명은 개인 회사를 보유한 Owner이고, 나머지 15명은 이 회사에 속해있다.
        A,B,C,D,E 총 5개의 회사가 존재하고, A회사는 프로젝트 4개, B회사는 3개, C,D,E 회사는 각각 2개의 프로젝트가 있다.
        각 회사의 오너는 다음과 같다.
        A회사 : 1번 멤버
        B회사 : 5번 멤버
        C회사 : 8번 멤버
        D회사 : 13번 멤버
        E회사 : 16번 멤버


        A회사는 4(1,2,3,4)명, B회사는 3(5,6,7)명, C회사는 5(8,9,10,11,12)명, D회사는 3(13,14,15)명, E회사는 5(16,17,18,19,20)명의 구성원이 있다.
        A회사의 A-1 프로젝트는 멤버 1,2,3,4 4명이 속해있고, A-2프로젝트는 멤버 1,3 2명이, A-3 프로젝트는 2,3,4 3명이, A-4 프로젝트는 1,2,4 3명이 속해있다.
        B회사의 B-1 프로젝트는 멤버 5,7 2명이 속해있고, B-2프로젝트는 멤버 5,6 2명이, B-3 프로젝트는 5,6,7 3명이 속해있다.
        C회사의 C-1 프로젝트는 멤버 9,10,11,12 4명이 속해있고, C-2프로젝트는 멤버 9,11 2명이 속해있다.
        D회사의 D-1 프로젝트는 멤버 13,14,15 3명이 속해있고, D-2프로젝트는 멤버 14,15 2명이 속해있다.
        E회사의 E-1 프로젝트는 멤버 16,17,18,19,20 5명이 속해있고, E-2프로젝트는 멤버 16,19,20 3명이 속해있다.

        또한 A-1, B-2, C-2, D-2 프로젝트를 제외한 모든 프로젝트에는 하위에 3개의 카테고리가 존재하며,
        A-1 프로젝트의 하위에는 2개의 카테고리가, B-2 프로젝트의 하위에는 4개의 카테고리가 있으며,
        C-2 프로젝트의 하위에는 카테고리가 존재하지 않고, D-2 프로젝트의 하위에는 1개의 카테고리만이 존재한다.

        A-1 프로젝트의 첫번째 카테고리는 1번 멤버에 의해 생성되었다.
        A-1 프로젝트의 두번째 카테고리는 4번 멤버에 의해 생성되었다.

        A-2 프로젝트의 첫번째 카테고리는 1번 멤버에 의해 생성되었다.
        A-2 프로젝트의 첫번째 카테고리는 1번 멤버에 의해 생성되었다.
        A-2 프로젝트의 첫번째 카테고리는 3번 멤버에 의해 생성되었다.

        A-3 프로젝트의 첫번째 카테고리는 2번 멤버에 의해 생성되었다.
        A-3 프로젝트의 첫번째 카테고리는 3번 멤버에 의해 생성되었다.
        A-3 프로젝트의 첫번째 카테고리는 4번 멤버에 의해 생성되었다.

        A-4 프로젝트의 첫번째 카테고리는 2번 멤버에 의해 생성되었다.
        A-4 프로젝트의 첫번째 카테고리는 4번 멤버에 의해 생성되었다.
        A-4 프로젝트의 첫번째 카테고리는 4번 멤버에 의해 생성되었다.

        B-1 프로젝트의 첫번째 카테고리는 5번 멤버에 의해 생성되었다.
        B-1 프로젝트의 첫번째 카테고리는 5번 멤버에 의해 생성되었다.
        B-1 프로젝트의 첫번째 카테고리는 7번 멤버에 의해 생성되었다.

        B-2 프로젝트의 첫번째 카테고리는 5번 멤버에 의해 생성되었다.
        B-2 프로젝트의 첫번째 카테고리는 5번 멤버에 의해 생성되었다.
        B-2 프로젝트의 첫번째 카테고리는 6번 멤버에 의해 생성되었다.
        B-2 프로젝트의 첫번째 카테고리는 6번 멤버에 의해 생성되었다.

        B-3 프로젝트의 첫번째 카테고리는 6번 멤버에 의해 생성되었다.
        B-3 프로젝트의 첫번째 카테고리는 7번 멤버에 의해 생성되었다.
        B-3 프로젝트의 첫번째 카테고리는 7번 멤버에 의해 생성되었다.


        A-1 프로젝트의 첫번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 1번, 4번 멤버에 의해 생성되었다.
        A-1 프로젝트의 두번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 1번, 2번, 4번 멤버에 의해 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        A-2 프로젝트의 첫번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 1번, 1번, 3번 멤버에 의해 생성되었다.
        A-2 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 모두 3번 멤버에 의해 생성되었다.
        A-2 프로젝트의 세번째 카테고리에는 1개의 릴리즈가 존재하며, 1번 멤버에 의해 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        A-3 프로젝트의 첫번째 카테고리에는 1개의 릴리즈가 존재하며, 2번 멤버에 의해서 생성되었다.
        A-3 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 3번, 4번멤버에 의해서 생성되었다.
        A-3 프로젝트의 세번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 2번, 4번 멤버에 의해서 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        A-4 프로젝트의 첫번째 카테고리에는 4개의 릴리즈가 존재하며, 각각 2번, 2번, 4번, 4번 멤버에 의해서 생성되었다.
        A-4 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 1번, 2번 멤버에 의해서 생성되었다.
        A-4 프로젝트의 세번째 카테고리에는 1개의 릴리즈가 존재하며, 4번 멤버에 의해서 생성되었다.

        B-1 프로젝트의 첫번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 각각 5번, 7번, 7번 멤버에 의해서 생성되었다.
        B-1 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 모두 5번 멤버에 의해서 생성되었다.
        B-1 프로젝트의 세번째 카테고리에는 2개의 릴리즈가 존재하며, 모두 7번 멤버에 의해서 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        B-2 프로젝트의 첫번째 카테고리에는 1개의 릴리즈가 존재하며, 6번 멤버에 의해서 생성되었다.
        B-2 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 5번, 6번 멤버에 의해서 생성되었다.
        B-2 프로젝트의 세번째 카테고리에는 2개의 릴리즈가 존재하며, 모두 6번 멤버에 의해서 생성되었다.
        B-2 프로젝트의 네번째 카테고리에는 1개의 릴리즈가 존재하며, 5번 멤버에 의해서 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        B-3 프로젝트의 첫번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 6번, 7번 멤버에 의해서 생성되었다.
        B-3 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 6번, 7번 멤버에 의해서 생성되었다.
        B-3 프로젝트의 세번째 카테고리에는 2개의 릴리즈가 존재하며, 모두 5번 멤버에 의해서 생성되었다.

        C-1 프로젝트의 첫번째 카테고리에는 4개의 릴리즈가 존재하며, 각각 9번, 10번, 11번, 12번 멤버에 의해서 생성되었다.
        C-1 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 10번, 12번 멤버에 의해서 생성되었다.
        C-1 프로젝트의 세번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 9번, 11번, 12번 멤버에 의해서 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        C-2 프로젝트에는 카테고리가 존재하지 않는다.

        D-1 프로젝트의 첫번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 13번, 13번, 14번 멤버에 의해서 생성되었다.
        D-1 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 14번, 14번 멤버에 의해서 생성되었다.
        D-1 프로젝트의 세번째 카테고리에는 4개의 릴리즈가 존재하며, 모두 15번 멤버에 의해서 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        D-2 프로젝트의 첫번째 카테고리에는 2개의 릴리즈가 존재하며, 14번, 15번 멤버에 의해서 생성되었다.

        E-1 프로젝트의 첫번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 16번, 17번, 20번 멤버에 의해서 생성되었다.
        E-1 프로젝트의 두번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 17번, 18번, 19번 멤버에 의해서 생성되었다.
        E-1 프로젝트의 세번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 16번, 19번, 20번 멤버에 의해서 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        E-2 프로젝트의 첫번째 카테고리에는 1개의 릴리즈가 존재하며, 20번 멤버에 의해서 생성되었다.
        E-2 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 모두 19번 멤버에 의해서 생성되었다.
        E-2 프로젝트의 세번째 카테고리에는 1개의 릴리즈가 존재하며, 16번 멤버에 의해서 생성되었다.

        모든 테스트는 위의 공통된 상황에서 시작된다.
         */

    @Test
    @DisplayName("통합테스트 - 공통 데이터 검증")
    public void environmentVerification() throws Exception {
        // given

        // 오너의 ID
        Long AOwner = 1L; Long BOwner = 5L; Long COwner = 8L; Long DOwner = 13L; Long EOwner = 16L;

        // 전체 회사 ID
        Long ACompanyId = 1L; Long BCompanyId = 2L; Long CCompanyId = 3L;
        Long DCompanyId = 4L; Long ECompanyId = 5L;
        // 전체 프로젝트 ID
        Long AP1 = 1L; Long AP2 = 2L; Long AP3 = 3L; Long AP4 = 4L;
        Long BP1 = 5L; Long BP2 = 6L; Long BP3 = 7L; Long CP1 = 8L;
        Long CP2 = 9L; Long DP1 = 10L; Long DP2 = 11L; Long EP1 = 12L; Long EP2 = 13L;

        Long AP1C1 = 1L; Long AP1C2 = 2L; Long AP2C1 = 3L; Long AP2C2 = 4L; Long AP2C3 = 5L;
        Long AP3C1 = 6L; Long AP3C2 = 7L; Long AP3C3 = 8L; Long AP4C1 = 9L; Long AP4C2 = 10L;
        Long AP4C3 = 11L;
        Long BP1C1 = 12L; Long BP1C2 = 13L; Long BP1C3 = 14L; Long BP2C1 = 15L; Long BP2C2 = 16L;
        Long BP2C3 = 17L; Long BP2C4 = 18L; Long BP3C1 = 19L; Long BP3C2 = 20L; Long BP3C3 = 21L;
        Long CP1C1 = 22L; Long CP1C2 = 23L; Long CP1C3 = 24L;
        Long DP1C1 = 25L; Long DP1C2 = 26L; Long DP1C3 = 27L; Long DP2C1 = 28L;
        Long EP1C1 = 29L; Long EP1C2 = 30L; Long EP1C3 = 31L;
        Long EP2C1 = 32L; Long EP2C2 = 33L; Long EP2C3 = 34L;

        // when
        // 모든 회원, 총 20명
        List<Member> members = memberRepository.findAll();
        // 모든 회사, 총 5개
        List<Company> companies = companyRepository.findAll();
        // 모든 프로젝트, 총 13개
        List<Project> projects = projectRepository.findAll();
        // 모든 릴리즈, 총 75개
        List<Releases> releases = releaseRepository.findAll();

        // 오너의 memberProject
        MemberCompany AMemberCompany = memberCompanyRepository.findByMemberAndCompany(ACompanyId, AOwner);
        MemberCompany BMemberCompany = memberCompanyRepository.findByMemberAndCompany(BCompanyId, BOwner);
        MemberCompany CMemberCompany = memberCompanyRepository.findByMemberAndCompany(CCompanyId, COwner);
        MemberCompany DMemberCompany = memberCompanyRepository.findByMemberAndCompany(DCompanyId, DOwner);
        MemberCompany EMemberCompany = memberCompanyRepository.findByMemberAndCompany(ECompanyId, EOwner);

        // 각 회사에 속한 사용자
        List<MemberCompany> ACompanyMembers = memberCompanyRepository.findByCompanyId(ACompanyId);
        List<MemberCompany> BCompanyMembers = memberCompanyRepository.findByCompanyId(BCompanyId);
        List<MemberCompany> CCompanyMembers = memberCompanyRepository.findByCompanyId(CCompanyId);
        List<MemberCompany> DCompanyMembers = memberCompanyRepository.findByCompanyId(DCompanyId);
        List<MemberCompany> ECompanyMembers = memberCompanyRepository.findByCompanyId(ECompanyId);

        // 각 프로젝트에 속한 사용자
        List<MemberProject> AP1Members = memberProjectRepository.findByProjectId(AP1);
        List<MemberProject> AP2Members = memberProjectRepository.findByProjectId(AP2);
        List<MemberProject> AP3Members = memberProjectRepository.findByProjectId(AP3);
        List<MemberProject> AP4Members = memberProjectRepository.findByProjectId(AP4);
        List<MemberProject> BP1Members = memberProjectRepository.findByProjectId(BP1);
        List<MemberProject> BP2Members = memberProjectRepository.findByProjectId(BP2);
        List<MemberProject> BP3Members = memberProjectRepository.findByProjectId(BP3);
        List<MemberProject> CP1Members = memberProjectRepository.findByProjectId(CP1);
        List<MemberProject> CP2Members = memberProjectRepository.findByProjectId(CP2);
        List<MemberProject> DP1Members = memberProjectRepository.findByProjectId(DP1);
        List<MemberProject> DP2Members = memberProjectRepository.findByProjectId(DP2);
        List<MemberProject> EP1Members = memberProjectRepository.findByProjectId(EP1);
        List<MemberProject> EP2Members = memberProjectRepository.findByProjectId(EP2);

        // 각 프로젝트에 속한 카테고리
        List<Category> AP1Categories = categoryRepository.findByProject(AP1);
        List<Category> AP2Categories = categoryRepository.findByProject(AP2);
        List<Category> AP3Categories = categoryRepository.findByProject(AP3);
        List<Category> AP4Categories = categoryRepository.findByProject(AP4);
        List<Category> BP1Categories = categoryRepository.findByProject(BP1);
        List<Category> BP2Categories = categoryRepository.findByProject(BP2);
        List<Category> BP3Categories = categoryRepository.findByProject(BP3);
        List<Category> CP1Categories = categoryRepository.findByProject(CP1);
        List<Category> CP2Categories = categoryRepository.findByProject(CP2);
        List<Category> DP1Categories = categoryRepository.findByProject(DP1);
        List<Category> DP2Categories = categoryRepository.findByProject(DP2);
        List<Category> EP1Categories = categoryRepository.findByProject(EP1);
        List<Category> EP2Categories = categoryRepository.findByProject(EP2);

        // 각 카테괴리에 속한 릴리즈
        List<Releases> AP1C1s = releaseRepository.findByCategoryId(AP1C1);
        List<Releases> AP1C2s = releaseRepository.findByCategoryId(AP1C2);
        List<Releases> AP2C1s = releaseRepository.findByCategoryId(AP2C1);
        List<Releases> AP2C2s = releaseRepository.findByCategoryId(AP2C2);
        List<Releases> AP2C3s = releaseRepository.findByCategoryId(AP2C3);
        List<Releases> AP3C1s = releaseRepository.findByCategoryId(AP3C1);
        List<Releases> AP3C2s = releaseRepository.findByCategoryId(AP3C2);
        List<Releases> AP3C3s = releaseRepository.findByCategoryId(AP3C3);
        List<Releases> AP4C1s = releaseRepository.findByCategoryId(AP4C1);
        List<Releases> AP4C2s = releaseRepository.findByCategoryId(AP4C2);
        List<Releases> AP4C3s = releaseRepository.findByCategoryId(AP4C3);
        List<Releases> BP1C1s = releaseRepository.findByCategoryId(BP1C1);
        List<Releases> BP1C2s = releaseRepository.findByCategoryId(BP1C2);
        List<Releases> BP1C3s = releaseRepository.findByCategoryId(BP1C3);
        List<Releases> BP2C1s = releaseRepository.findByCategoryId(BP2C1);
        List<Releases> BP2C2s = releaseRepository.findByCategoryId(BP2C2);
        List<Releases> BP2C3s = releaseRepository.findByCategoryId(BP2C3);
        List<Releases> BP2C4s = releaseRepository.findByCategoryId(BP2C4);
        List<Releases> BP3C1s = releaseRepository.findByCategoryId(BP3C1);
        List<Releases> BP3C2s = releaseRepository.findByCategoryId(BP3C2);
        List<Releases> BP3C3s = releaseRepository.findByCategoryId(BP3C3);
        List<Releases> CP1C1s = releaseRepository.findByCategoryId(CP1C1);
        List<Releases> CP1C2s = releaseRepository.findByCategoryId(CP1C2);
        List<Releases> CP1C3s = releaseRepository.findByCategoryId(CP1C3);
        List<Releases> DP1C1s = releaseRepository.findByCategoryId(DP1C1);
        List<Releases> DP1C2s = releaseRepository.findByCategoryId(DP1C2);
        List<Releases> DP1C3s = releaseRepository.findByCategoryId(DP1C3);
        List<Releases> DP2C1s = releaseRepository.findByCategoryId(DP2C1);
        List<Releases> EP1C1s = releaseRepository.findByCategoryId(EP1C1);
        List<Releases> EP1C2s = releaseRepository.findByCategoryId(EP1C2);
        List<Releases> EP1C3s = releaseRepository.findByCategoryId(EP1C3);
        List<Releases> EP2C1s = releaseRepository.findByCategoryId(EP2C1);
        List<Releases> EP2C2s = releaseRepository.findByCategoryId(EP2C2);
        List<Releases> EP2C3s = releaseRepository.findByCategoryId(EP2C3);


        // then
        // 전체 회원이 20명인지 검증
        assertThat(members.size()).isEqualTo(20);
        // 전체 회사가 5개인지 검증
        assertThat(companies.size()).isEqualTo(5);

        // 전체 프로젝트가 13개인지 검증
        assertThat(projects.size()).isEqualTo(13);

        // 전체 릴리즈가 75개인지 검증
        assertThat(releases.size()).isEqualTo(75);

        // 각 회사의 Owner를 검증
        assertThat(AMemberCompany.getMember().getId()).isEqualTo(AOwner);
        assertThat(AMemberCompany.getRole()).isEqualTo(Role.OWNER);
        assertThat(BMemberCompany.getMember().getId()).isEqualTo(BOwner);
        assertThat(BMemberCompany.getRole()).isEqualTo(Role.OWNER);
        assertThat(CMemberCompany.getMember().getId()).isEqualTo(COwner);
        assertThat(CMemberCompany.getRole()).isEqualTo(Role.OWNER);
        assertThat(DMemberCompany.getMember().getId()).isEqualTo(DOwner);
        assertThat(DMemberCompany.getRole()).isEqualTo(Role.OWNER);
        assertThat(EMemberCompany.getMember().getId()).isEqualTo(EOwner);
        assertThat(EMemberCompany.getRole()).isEqualTo(Role.OWNER);

        // 각 회사에 속한 사용자를 검증
        assertThat(ACompanyMembers.stream().map(cm -> cm.getMember().getId()).collect(Collectors.toList()))
                .contains(1L, 2L, 3L, 4L);
        assertThat(BCompanyMembers.stream().map(cm -> cm.getMember().getId()).collect(Collectors.toList()))
                .contains(5L, 6L, 7L);
        assertThat(CCompanyMembers.stream().map(cm -> cm.getMember().getId()).collect(Collectors.toList()))
                .contains(8L, 9L, 10L, 11L, 12L);
        assertThat(DCompanyMembers.stream().map(cm -> cm.getMember().getId()).collect(Collectors.toList()))
                .contains(13L, 14L, 15L);
        assertThat(ECompanyMembers.stream().map(cm -> cm.getMember().getId()).collect(Collectors.toList()))
                .contains(16L, 17L, 18L, 19L, 20L);

        // 각 프로젝트에 속한 사용자
        assertThat(AP1Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(1L, 2L, 3L, 4L);
        assertThat(AP1Members.size()).isEqualTo(4);
        assertThat(AP2Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(1L, 3L);
        assertThat(AP2Members.size()).isEqualTo(2);
        assertThat(AP3Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(2L, 3L, 4L);
        assertThat(AP3Members.size()).isEqualTo(3);
        assertThat(AP4Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(1L, 2L, 4L);
        assertThat(AP3Members.size()).isEqualTo(3);
        assertThat(BP1Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(5L, 7L);
        assertThat(BP1Members.size()).isEqualTo(2);
        assertThat(BP2Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(5L, 6L);
        assertThat(BP2Members.size()).isEqualTo(2);
        assertThat(BP3Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(5L, 6L, 7L);
        assertThat(BP3Members.size()).isEqualTo(3);
        assertThat(CP1Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(9L, 10L, 11L, 12L);
        assertThat(CP1Members.size()).isEqualTo(4);
        assertThat(CP2Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(9L, 11L);
        assertThat(CP2Members.size()).isEqualTo(2);
        assertThat(DP1Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(13L, 14L, 15L);
        assertThat(DP1Members.size()).isEqualTo(3);
        assertThat(DP2Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(14L, 15L);
        assertThat(DP2Members.size()).isEqualTo(2);
        assertThat(EP1Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(16L, 17L, 18L, 19L, 20L);
        assertThat(EP1Members.size()).isEqualTo(5);
        assertThat(EP2Members.stream().map(pm -> pm.getMember().getId()).collect(Collectors.toList()))
                .contains(16L, 19L, 20L);
        assertThat(EP2Members.size()).isEqualTo(3);


        // 각 프로젝트에 속한 카테고리
        assertThat(AP1Categories.size()).isEqualTo(2);
        assertThat(AP2Categories.size()).isEqualTo(3);
        assertThat(AP3Categories.size()).isEqualTo(3);
        assertThat(AP4Categories.size()).isEqualTo(3);
        assertThat(BP1Categories.size()).isEqualTo(3);
        assertThat(BP2Categories.size()).isEqualTo(4);
        assertThat(BP3Categories.size()).isEqualTo(3);
        assertThat(CP1Categories.size()).isEqualTo(3);
        assertThat(CP2Categories.isEmpty()).isTrue();
        assertThat(DP1Categories.size()).isEqualTo(3);
        assertThat(DP2Categories.size()).isEqualTo(1);
        assertThat(EP1Categories.size()).isEqualTo(3);
        assertThat(EP2Categories.size()).isEqualTo(3);

        // 각 카테고리에 속한 릴리즈
        assertThat(AP1C1s.size()).isEqualTo(2);
        assertThat(AP1C2s.size()).isEqualTo(3);
        assertThat(AP2C1s.size()).isEqualTo(3);
        assertThat(AP2C2s.size()).isEqualTo(2);
        assertThat(AP2C3s.size()).isEqualTo(1);
        assertThat(AP3C1s.size()).isEqualTo(1);
        assertThat(AP3C2s.size()).isEqualTo(2);
        assertThat(AP3C3s.size()).isEqualTo(2);
        assertThat(AP4C1s.size()).isEqualTo(4);
        assertThat(AP4C2s.size()).isEqualTo(2);
        assertThat(AP4C3s.size()).isEqualTo(1);
        assertThat(BP1C1s.size()).isEqualTo(3);
        assertThat(BP1C2s.size()).isEqualTo(2);
        assertThat(BP1C3s.size()).isEqualTo(2);
        assertThat(BP2C1s.size()).isEqualTo(1);
        assertThat(BP2C2s.size()).isEqualTo(2);
        assertThat(BP2C3s.size()).isEqualTo(2);
        assertThat(BP2C4s.size()).isEqualTo(1);
        assertThat(BP3C1s.size()).isEqualTo(2);
        assertThat(BP3C2s.size()).isEqualTo(2);
        assertThat(BP3C3s.size()).isEqualTo(2);
        assertThat(CP1C1s.size()).isEqualTo(4);
        assertThat(CP1C2s.size()).isEqualTo(2);
        assertThat(CP1C3s.size()).isEqualTo(3);
        assertThat(DP1C1s.size()).isEqualTo(3);
        assertThat(DP1C2s.size()).isEqualTo(2);
        assertThat(DP1C3s.size()).isEqualTo(4);
        assertThat(DP2C1s.size()).isEqualTo(2);
        assertThat(EP1C1s.size()).isEqualTo(3);
        assertThat(EP1C2s.size()).isEqualTo(3);
        assertThat(EP1C3s.size()).isEqualTo(3);
        assertThat(EP2C1s.size()).isEqualTo(1);
        assertThat(EP2C2s.size()).isEqualTo(2);
        assertThat(EP2C3s.size()).isEqualTo(1);
    }


    @Test
    @DisplayName("통합 테스트 - 회사 삭제 시나리오")
    public void eCompanyDeleteScenario() throws Exception {
        //given
        Long tester = 19L;
        Member aOwner = memberRepository.findByEmail("user1@doklib.com").get();
        Company aCompany = companyRepository.findByName("A").get();
        Member bOwner = memberRepository.findByEmail("user5@doklib.com").get();
        Company bCompany = companyRepository.findByName("B").get();
        Member cOwner = memberRepository.findByEmail("user8@doklib.com").get();
        Company cCompany = companyRepository.findByName("C").get();
        Member dOwner = memberRepository.findByEmail("user13@doklib.com").get();
        Company dCompany = companyRepository.findByName("D").get();
        Member eOwner = memberRepository.findByEmail("user16@doklib.com").get();
        Company eCompany = companyRepository.findByName("E").get();
        //when
        companyService.deleteCompany(aCompany.getId(), aOwner.getId());
        companyService.deleteCompany(bCompany.getId(), bOwner.getId());
        companyService.deleteCompany(cCompany.getId(), cOwner.getId());
        companyService.deleteCompany(dCompany.getId(), dOwner.getId());
        companyService.deleteCompany(eCompany.getId(), eOwner.getId());
        //then
        assertThrows(CompanyNotFoundException.class, () -> companyService.deleteCompany(aCompany.getId(), aOwner.getId()));
        assertThrows(CompanyNotFoundException.class, () -> companyService.deleteCompany(bCompany.getId(), bOwner.getId()));
        assertThrows(CompanyNotFoundException.class, () -> companyService.deleteCompany(cCompany.getId(), cOwner.getId()));
        assertThrows(CompanyNotFoundException.class, () -> companyService.deleteCompany(dCompany.getId(), dOwner.getId()));
        assertThrows(CompanyNotFoundException.class, () -> companyService.deleteCompany(eCompany.getId(), eOwner.getId()));

        assertThrows(CompanyNotFoundException.class,
                () -> projectService.findProjectListByCompany(aCompany.getId(), PageRequest.of(0, 3), tester));
        assertThrows(CompanyNotFoundException.class,
                () -> projectService.findProjectListByCompany(bCompany.getId(), PageRequest.of(0, 3), tester));
        assertThrows(CompanyNotFoundException.class,
                () -> projectService.findProjectListByCompany(cCompany.getId(), PageRequest.of(0, 3), tester));
        assertThrows(CompanyNotFoundException.class,
                () -> projectService.findProjectListByCompany(dCompany.getId(), PageRequest.of(0, 3), tester));
        assertThrows(CompanyNotFoundException.class,
                () -> projectService.findProjectListByCompany(eCompany.getId(), PageRequest.of(0, 3), tester));
        // 하위 데이터들도 모두 사라졌는지 레포지토리로 검증


        List<Project> aProjects = projectRepository.findByCompanyId(aCompany.getId());
        List<Project> bProjects = projectRepository.findByCompanyId(bCompany.getId());
        List<Project> cProjects = projectRepository.findByCompanyId(cCompany.getId());
        List<Project> dProjects = projectRepository.findByCompanyId(dCompany.getId());
        List<Project> eProjects = projectRepository.findByCompanyId(eCompany.getId());
        assertThat(aProjects.isEmpty()).isTrue();
        assertThat(bProjects.isEmpty()).isTrue();
        assertThat(cProjects.isEmpty()).isTrue();
        assertThat(dProjects.isEmpty()).isTrue();
        assertThat(eProjects.isEmpty()).isTrue();

        List<Project> projects = projectRepository.findAll();
        List<Category> categories = categoryRepository.findAll();
        List<Releases> releases = releaseRepository.findAll();
        List<MemberCompany> memberCompanies = memberCompanyRepository.findAll();
        List<MemberProject> memberProjects = memberProjectRepository.findAll();
        assertThat(projects.isEmpty()).isTrue();
        assertThat(categories.isEmpty()).isTrue();
        assertThat(releases.isEmpty()).isTrue();
        assertThat(memberCompanies.isEmpty()).isTrue();
        assertThat(memberProjects.isEmpty()).isTrue();

    }

// TODO 정연 사용자 존재 검증 쪽 : id로 존재하는지 보는거 + isDeleted

}
