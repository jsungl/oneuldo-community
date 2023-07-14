package hello.springcommunity.domain.login;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberService;
import hello.springcommunity.web.login.LoginForm;
import hello.springcommunity.web.member.form.MemberSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
class LoginServiceTest {

    @Autowired
    LoginService loginService;
    @Autowired
    MemberService memberService;

    @Test
    void 로그인() {

        //given
        MemberSaveForm memberSaveForm = new MemberSaveForm();
        memberSaveForm.setLoginId("test333");
        memberSaveForm.setPassword("test123!");
        memberSaveForm.setName("테스터xds");
        Long joinId = memberService.join(memberSaveForm);

        LoginForm form = new LoginForm("test333", "test123!");


        //when
        Member member = loginService.login(form);

        //then
        assertNotNull(member);
//        log.info("loginId={}", member.getLoginId());
//        log.info("password={}", member.getPassword());
    }
}