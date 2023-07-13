package hello.springcommunity.domain.member;

import hello.springcommunity.web.member.form.MemberSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@Transactional
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Test
    void 회원가입() {

        //given
        MemberSaveForm memberSaveForm = new MemberSaveForm();
        memberSaveForm.setLoginId("testtest");
        memberSaveForm.setPassword("test123!");
        memberSaveForm.setName("테스터xds");

//        Member member = Member.builder()
//                .loginId(memberSaveForm.getLoginId())
//                .password(memberSaveForm.getPassword())
//                .name(memberSaveForm.getName())
//                .build();

        //when
        Long joinId = memberService.join(memberSaveForm);

        //then
        assertThat("testtest").isEqualTo(memberService.findOne(joinId).orElseThrow().getLoginId());

    }
}