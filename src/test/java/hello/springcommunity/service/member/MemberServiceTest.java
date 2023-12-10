package hello.springcommunity.service.member;

import hello.springcommunity.dto.member.MemberSaveRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@Slf4j
@Transactional
//@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;


//    @Test
//    void 회원가입() {
//
//        //given
//        MemberSaveRequestDTO memberSaveRequestDTO = new MemberSaveRequestDTO();
//        memberSaveRequestDTO.setLoginId("admin123");
//        memberSaveRequestDTO.setPassword("test123!");
//        memberSaveRequestDTO.setNickname("관리자");
//        memberSaveRequestDTO.setEmail("admin123@naver.com");
//
//        String path = "http://localhost:8081/member/authAccount";
//
//        //when
//        memberService.join(memberSaveRequestDTO, path);
//
//        //then
//        assertThat("testtest").isEqualTo(memberService.findOne(joinId).orElseThrow().getLoginId());
//
//    }

//    void 중복회원() {
//
//        //given
//        Member memberA = Member.builder().loginId("userA").name("AAA").password("123123a!").build();
//        Member memberB = Member.builder().loginId("userA").name("BBB").password("123123a!").build();
//
//        //when
//        memberServiceOld.join(memberA);
//
//        //then
//        assertThatThrownBy(() -> memberServiceOld.join(memberB)).isInstanceOf(IllegalStateException.class);
//
//    }

}