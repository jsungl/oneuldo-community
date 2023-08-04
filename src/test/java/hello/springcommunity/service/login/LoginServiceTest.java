package hello.springcommunity.service.login;

import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.login.LoginForm;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Slf4j
@Service
@Transactional
class LoginServiceTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PasswordEncoder passwordEncoder;


    /**
     * 로그인
     * PasswordEncoder.matches() 로 입력한 비밀번호와 DB에 저장된 암호화된 비밀번호와 일치하는지 확인
     * 로그인에 실패하면 null 반환
     */
    //@Test
    public Member 로그인(LoginForm form) {
        if(form.getLoginId() == null || form.getPassword() == null) {
            return null;
        }

        Member member = memberRepository.findByLoginId(form.getLoginId()).orElse(null);

        //아이디 검사
        if(member == null) {
            log.info("해당 아이디의 유저가 존재하지 않습니다");
            return null;
        }

        //비밀번호 검사
        if(!passwordEncoder.matches(form.getPassword(),member.getPassword())) {
            log.info("패스워드가 일치하지 않습니다");
            return null;
        }

        return member;
    }
}