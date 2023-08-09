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
import com.service.releasenote.domain.project.model.Project;
import com.service.releasenote.domain.release.application.ReleaseService;
import com.service.releasenote.domain.release.dao.ReleaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.*;

import static com.service.releasenote.domain.member.dto.MemberCompanyDTO.AddMemberRequestDTO;
import static com.service.releasenote.domain.member.dto.MemberCompanyDTO.AddMemberResponseDTO;
import static com.service.releasenote.domain.member.dto.MemberDTO.LoginDTO;
import static com.service.releasenote.domain.member.dto.MemberDTO.SignUpRequest;
import static com.service.releasenote.domain.member.dto.MemberProjectDTO.*;
import static com.service.releasenote.domain.project.dto.ProjectDto.*;
import static org.assertj.core.api.Assertions.assertThat;

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
    private void clearAll() {
        alarmRepository.deleteAll();
        releaseRepository.deleteAll();
        categoryRepository.deleteAll();
        memberProjectRepository.deleteAll();
        projectRepository.deleteAll();
        memberCompanyRepository.deleteAll();
        companyRepository.deleteAll();
        memberRepository.deleteAll();
        try {
            setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // memberProject, memberCompany, Alarm은 어떡할지 생각 좀 해보기
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
                .ImageURL(null)
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
        Company B = saveCompany("B", members.get(1));
        Company C = saveCompany("C", members.get(2));
        Company D = saveCompany("D", members.get(3));
        Company E = saveCompany("E", members.get(4));

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
        participateProject(AP1, members.get(1));
        participateProject(AP1, members.get(2));
        participateProject(AP1, members.get(3));
        participateProject(AP2, members.get(0));
        participateProject(AP3, members.get(1));
        participateProject(AP3, members.get(2));
        participateProject(AP4, members.get(0));
        participateProject(AP4, members.get(3));

        // 5.2 B 회사의 프로젝트
        participateProject(BP1, members.get(4));
        participateProject(BP2, members.get(5));
        participateProject(BP3, members.get(4));
        participateProject(BP3, members.get(6));

        // 5.3 C 회사의 프로젝트
        participateProject(CP1, members.get(8));
        participateProject(CP1, members.get(9));
        participateProject(CP1, members.get(10));
        participateProject(CP2, members.get(10));

        // 5.4 D 회사의 프로젝트
        participateProject(DP1, members.get(13));
        participateProject(DP1, members.get(14));
        participateProject(DP2, members.get(13));

        // 5.5 E 회사의 프로젝트
        participateProject(EP1, members.get(15));
        participateProject(EP1, members.get(17));
        participateProject(EP1, members.get(18));
        participateProject(EP1, members.get(19));
        participateProject(EP2, members.get(15));
        participateProject(EP2, members.get(18));

        // 6. 테스트 요구사항에 맞춰 담당자가 카테고리를 생성한다.
        saveCategory(AP1, "APC1-1"); saveCategory(AP1, "APC1-2");
        saveCategory(AP2, "APC2-1"); saveCategory(AP2, "APC2-2"); saveCategory(AP2, "APC2-3");
        saveCategory(AP3, "APC3-1"); saveCategory(AP3, "APC3-2"); saveCategory(AP3, "APC3-3");
        saveCategory(AP4, "APC4-1"); saveCategory(AP4, "APC4-2"); saveCategory(AP4, "APC4-3");

        saveCategory(BP1, "BP1-1"); saveCategory(BP1, "BP1-2"); saveCategory(BP1, "BP1-3");
        saveCategory(BP2, "BP2-1"); saveCategory(BP2, "BP2-2"); saveCategory(BP2, "BP2-3"); saveCategory(BP2, "BP2-4");
        saveCategory(BP3, "BP3-1"); saveCategory(BP3, "BP3-2"); saveCategory(BP3, "BP3-3");

        saveCategory(CP1, "BP1-1"); saveCategory(CP1, "BP1-2"); saveCategory(CP1, "BP1-3");

        saveCategory(DP1, "DP1-1"); saveCategory(DP1, "DP1-2"); saveCategory(DP1, "DP1-3"); saveCategory(DP2, "DP2-1");

        saveCategory(EP1, "EP1-1"); saveCategory(EP1, "EP1-2"); saveCategory(EP1, "EP1-3");
        saveCategory(EP2, "EP2-1"); saveCategory(EP2, "EP2-2"); saveCategory(EP2, "EP2-3");

        // 7. 테스트 요구사항에 맞춰 담당자가 릴리즈를 생성한다.
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
    @DisplayName("통합테스트1")
    public void integrationTest1() throws Exception {

    
    }


}
