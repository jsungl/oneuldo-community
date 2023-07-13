package hello.springcommunity.domain.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
class MemberServiceOldTest {

    @Autowired
    MemberServiceOld memberServiceOld;

    @Test
    void 회원가입() {

        //given
        Member member = Member.builder().loginId("userA").name("AAA").password("123123a!").build();

        //when
        Long joinId = memberServiceOld.join(member);

        //then
        assertThat(member).isEqualTo(memberServiceOld.findOne(joinId).orElseThrow());

    }

    @Test
    void 중복회원() {

        //given
        Member memberA = Member.builder().loginId("userA").name("AAA").password("123123a!").build();
        Member memberB = Member.builder().loginId("userA").name("BBB").password("123123a!").build();

        //when
        memberServiceOld.join(memberA);

        //then
        assertThatThrownBy(() -> memberServiceOld.join(memberB)).isInstanceOf(IllegalStateException.class);

    }


}