package hello.springcommunity.domain.member;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Test
    void 회원가입() {

        //given
        Member member = Member.builder().loginId("userA").build();

        //when
        Long joinId = memberService.join(member);

        //then
        assertThat(member).isEqualTo(memberService.findOne(joinId).orElseThrow());

    }

    @Test
    void 중복회원() {

        //given
        Member memberA = Member.builder().loginId("userA").build();
        Member memberB = Member.builder().loginId("userB").build();

        //when
        memberService.join(memberA);

        //then
        assertThatThrownBy(() -> memberService.join(memberB)).isInstanceOf(IllegalStateException.class);

    }


}