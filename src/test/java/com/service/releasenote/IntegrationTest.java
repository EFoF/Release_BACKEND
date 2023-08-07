package com.service.releasenote;

import com.service.releasenote.domain.alarm.application.AlarmService;
import com.service.releasenote.domain.alarm.dao.AlarmRepository;
import com.service.releasenote.domain.category.application.CategoryService;
import com.service.releasenote.domain.category.dao.CategoryRepository;
import com.service.releasenote.domain.company.application.CompanyService;
import com.service.releasenote.domain.company.dao.CompanyRepository;
import com.service.releasenote.domain.member.application.AuthService;
import com.service.releasenote.domain.member.dao.MemberRepository;
import com.service.releasenote.domain.project.dao.ProjectRepository;
import com.service.releasenote.domain.release.dao.ReleaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

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
    AuthService authService;

    @BeforeEach
    private void clearAll() {
        
    }

    /*
        모든 테스트 상황의 베이스는 다음과 같다.
        총 20명의 사용자가 있다.
        그 중 5명은 개인 회사를 보유한 Owner이고, 나머지 15명은 이 회사에 속해있다.
        A,B,C,D,E 총 5개의 회사가 존재하고, A회사는 프로젝트 4개, B회사는 3개, C,D,E 회사는 각각 2개의 프로젝트가 있다.

        A회사는 4(1,2,3,4)명, B회사는 3(5,6,7)명, C회사는 5(8,9,10,11,12)명, D회사는 3(13,14,15)명, E회사는 5(16,17,18,19,20)명의 구성원이 있다.
        A회사의 A-1 프로젝트는 멤버 1,2,3,4 4명이 속해있고, A-2프로젝트는 멤버 1,3 2명이, A-3 프로젝트는 2,3,4 3명이, A-4 프로젝트는 1,2,4 3명이 속해있다.
        B회사의 B-1 프로젝트는 멤버 5,7 2명이 속해있고, B-2프로젝트는 멤버 5,6 2명이, B-3 프로젝트는 5,6,7 3명이 속해있다.
        C회사의 C-1 프로젝트는 멤버 9,10,11,12 4명이 속해있고, C-2프로젝트는 멤버 9,11 2명이 속해있다.
        D회사의 D-1 프로젝트는 멤버 13,14,15 3명이 속해있고, D-2프로젝트는 멤버 14,15 2명이 속해있다.
        E회사의 E-1 프로젝트는 멤버 16,17,18,19,20 5명이 속해있고, E-2프로젝트는 멤버 16,19,20 3명이 속해있다.

        또한 A-1, B-2, C-2, D-2 프로젝트를 제외한 모든 프로젝트에는 하위에 3개의 카테고리가 존재하며,
        A-1 프로젝트의 하위에는 2개의 카테고리가, B-2 프로젝트의 하위에는 4개의 카테고리가 있으며,
        C-2 프로젝트의 하위에는 카테고리가 존재하지 않고, D-2 프로젝트의 하위에는 1개의 카테고리만이 존재한다.

        A-1 프로젝트의 첫번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 1번, 4번 멤버에 의해 생성되었다.
        A-1 프로젝트의 두번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 1번, 2번, 4번 멤버에 의해 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        A-2 프로젝트의 첫번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 1번, 1번, 3번 멤버에 의해 생성되었다.
        A-2 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 모두 3번 멤버에 의해 생성되었다.
        A-2 프로젝트의 세번째 카테고리에는 1개의 릴리즈가 존재하며, 1번 멤버에 의해 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        2,3,4
        A-3 프로젝트의 첫번째 카테고리에는 1개의 릴리즈가 존재하며, 2번 멤버에 의해서 생성되었다.
        A-3 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 3번, 4번멤버에 의해서 생성되었다.
        A-3 프로젝트의 세번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 2번, 4번 멤버에 의해서 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        1,2,4
        A-4 프로젝트의 첫번째 카테고리에는 4개의 릴리즈가 존재하며, 각각 2번, 2번, 4번, 4번 멤버에 의해서 생성되었다.
        A-4 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 1번, 2번 멤버에 의해서 생성되었다.
        A-4 프로젝트의 세번째 카테고리에는 1개의 릴리즈가 존재하며, 4번 멤버에 의해서 생성되었다.

        5,7
        B-1 프로젝트의 첫번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 각각 5번, 7번, 7번 멤버에 의해서 생성되었다.
        B-1 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 모두 5번 멤버에 의해서 생성되었다.
        B-1 프로젝트의 세번째 카테고리에는 2개의 릴리즈가 존재하며, 모두 7번 멤버에 의해서 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        5,6
        B-2 프로젝트의 첫번째 카테고리에는 1개의 릴리즈가 존재하며, 6번 멤버에 의해서 생성되었다.
        B-2 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 5번, 6번 멤버에 의해서 생성되었다.
        B-2 프로젝트의 세번째 카테고리에는 2개의 릴리즈가 존재하며, 모두 6번 멤버에 의해서 생성되었다.
        B-2 프로젝트의 네번째 카테고리에는 1개의 릴리즈가 존재하며, 5번 멤버에 의해서 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        5,6,7
        B-3 프로젝트의 첫번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 6번, 7번 멤버에 의해서 생성되었다.
        B-3 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 6번, 7번 멤버에 의해서 생성되었다.
        B-3 프로젝트의 세번째 카테고리에는 2개의 릴리즈가 존재하며, 모두 5번 멤버에 의해서 생성되었다.

        9,10,11,12
        C-1 프로젝트의 첫번째 카테고리에는 4개의 릴리즈가 존재하며, 각각 9번, 10번, 11번, 12번 멤버에 의해서 생성되었다.
        C-1 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 10번, 12번 멤버에 의해서 생성되었다.
        C-1 프로젝트의 세번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 9번, 11번, 12번 멤버에 의해서 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        C-2 프로젝트에는 카테고리가 존재하지 않는다.

        13,14,15
        D-1 프로젝트의 첫번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 13번, 13번, 14번 멤버에 의해서 생성되었다.
        D-1 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 각각 14번, 14번 멤버에 의해서 생성되었다.
        D-1 프로젝트의 세번째 카테고리에는 4개의 릴리즈가 존재하며, 모두 15번 멤버에 의해서 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        14,15
        D-2 프로젝트의 첫번째 카테고리에는 2개의 릴리즈가 존재하며, 14번, 15번 멤버에 의해서 생성되었다.

        16,17,18,19,20
        E-1 프로젝트의 첫번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 16번, 17번, 20번 멤버에 의해서 생성되었다.
        E-1 프로젝트의 두번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 17번, 18번, 19번 멤버에 의해서 생성되었다.
        E-1 프로젝트의 세번째 카테고리에는 3개의 릴리즈가 존재하며, 각각 16번, 19번, 20번 멤버에 의해서 생성되었다.
        /////////////////////////////////////////////////////////////////////////////////
        16,19,20
        E-2 프로젝트의 첫번째 카테고리에는 1개의 릴리즈가 존재하며, 20번 멤버에 의해서 생성되었다.
        E-2 프로젝트의 두번째 카테고리에는 2개의 릴리즈가 존재하며, 모두 19번 멤버에 의해서 생성되었다.
        E-2 프로젝트의 세번째 카테고리에는 1개의 릴리즈가 존재하며, 16번 멤버에 의해서 생성되었다.

        모든 테스트는 위의 공통된 상황에서 시작된다.
         */

    @Test
    @DisplayName("통합테스트1")
    public void integrationTest1() throws Exception {
        //given

        
        //when
        
        //then
    
    }


}
