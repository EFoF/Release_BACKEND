package com.service.releasenote;

import com.service.releasenote.domain.alarm.dao.AlarmRepository;
import com.service.releasenote.domain.category.application.CategoryService;
import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.company.application.CompanyService;
import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.company.model.Company;
import com.service.releasenote.domain.member.application.AuthService;
import com.service.releasenote.domain.member.application.MemberCompanyService;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.member.model.Member;
import com.service.releasenote.domain.member.model.MemberLoginType;
import com.service.releasenote.domain.member.model.Role;
import com.service.releasenote.domain.project.application.ProjectService;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.release.application.ReleaseService;
import com.service.releasenote.domain.release.dao.ReleaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static com.service.releasenote.domain.company.dto.CompanyDTO.CreateCompanyRequestDTO;
import static com.service.releasenote.domain.member.dto.MemberCompanyDTO.AddMemberRequestDTO;
import static com.service.releasenote.domain.member.dto.MemberCompanyDTO.AddMemberResponseDTO;
import static com.service.releasenote.domain.member.dto.MemberDTO.LoginDTO;
import static com.service.releasenote.domain.member.dto.MemberDTO.SignUpRequest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class IntegrationTest {

    @LocalServerPort
    private int port;
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
    PasswordEncoder passwordEncoder;
    @Autowired
    TestRestTemplate restTemplate;


    @BeforeEach
    private void clearAll() {
        releaseRepository.deleteAll();
        categoryRepository.deleteAll();
        projectRepository.deleteAll();
        companyRepository.deleteAll();
        memberRepository.deleteAll();
        setup();
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

    // 오너들이 회사를 만들기 위해 사용되는 dto를 생성하는 메서드
    // TODO 회사 생성 api 변경 가능성이 있어서 일단 보류하겠음
    public CreateCompanyRequestDTO buildCreateCopanyRequestDto(Long userId) {
        return CreateCompanyRequestDTO.builder()
                .imageUrl("test imageUrl")
                .name("company_from_user"+userId)
                .build();
    }

    public AddMemberResponseDTO buildAddMemberResponseDto(Long memberId, Long companyId, Role role) {
        return AddMemberResponseDTO.builder()
                .company_id(companyId)
                .member_id(memberId)
                .role(role)
                .build();
    }

    public AddMemberRequestDTO buildAddMemberRequestDto(Long userId) {
        return AddMemberRequestDTO.builder()
                .email("user"+userId+"@doklib.com")
                .build();
    }

    @Disabled
    private void setup() {
        // 1. 20명의 멤버를 생성하여 데이터베이스에 저장
        for(int i=1; i<=20; i++) {
            SignUpRequest signUpRequest = buildSignUpRequest(i);
            authService.signup(signUpRequest);
        }
        List<Long> ownerList = new ArrayList<>();
        ownerList.add(1L); ownerList.add(5L); ownerList.add(8L); ownerList.add(13L); ownerList.add(16L);
        Map<Long, List<Long>> memberList = new HashMap<>();
        List<Long> ACompanyMembers = new ArrayList<>();
        ACompanyMembers.add(2L); ACompanyMembers.add(3L); ACompanyMembers.add(4L);
        List<Long> BCompanyMembers = new ArrayList<>();
        BCompanyMembers.add(6L); BCompanyMembers.add(7L);
        List<Long> CCompanyMembers = new ArrayList<>();
        CCompanyMembers.add(9L); CCompanyMembers.add(10L); CCompanyMembers.add(11L); CCompanyMembers.add(12L);
        List<Long> DCompanyMembers = new ArrayList<>();
        DCompanyMembers.add(14L); DCompanyMembers.add(15L);
        List<Long> ECompanyMembers = new ArrayList<>();
        ECompanyMembers.add(17L); ECompanyMembers.add(18L); ECompanyMembers.add(19L); ECompanyMembers.add(20L);

        memberList.put(1L, ACompanyMembers);
        memberList.put(5L, BCompanyMembers);
        memberList.put(8L, CCompanyMembers);
        memberList.put(13L, DCompanyMembers);
        memberList.put(16L, ECompanyMembers);


        // 2. 오너들이 회사를 생성
        for (Long ownerId : ownerList) {
            // 2.1 오너들이 회사를 생성하기 위해선 로그인을 수행해야 한다.
            LoginDTO loginDTO = buildLoginDto(ownerId);
//            authService.signin(loginDTO);
            String url = "http://localhost:" + this.port + "/signin";

            // 2.2 로그인한 멤버는 회사를 만든다.
            Long companyId = companyService.createCompany(buildCreateCopanyRequestDto(ownerId));
            // 3. 회사를 생성한 후 멤버들을 초대한다.
            List<Long> members = memberList.get(ownerId);
            for (Long member : members) {
                memberCompanyService.addMemberCompany(companyId, buildAddMemberRequestDto(member));
            }
        }
        // 4. 초대된 멤버는 프로젝트를 생성한다.
        // 5. 프로젝트를 생성한 후 멤버들을 초대한다.
        // 6. 테스트 요구사항에 맞춰 담당자가 카테고리를 생성한다.
        // 7. 테스트 요구사항에 맞춰 담당자가 릴리즈를 생성한다.
        // 8. 테스트 환경설정이 끝났다.


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
        //given
        Optional<Member> byId = memberRepository.findById(2L);
        Member member = byId.get();

        //when
        Optional<Company> companyOptional = companyRepository.findById(4L);
        Company company = companyOptional.get();

        //then
        assertThat(member.getUserName()).isEqualTo("user2");
        assertThat(company.getName()).isEqualTo("company_from_user13");
    
    }


}
