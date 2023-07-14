package hello.springcommunity.domain.login;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberRepository;
import hello.springcommunity.domain.member.MemberRepositoryOld;
import hello.springcommunity.web.login.LoginForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 로그인 서비스의 핵심 비즈니스 로직
 * 회원을 조회한 다음에 파라미터로 넘어온 비밀번호와 비교해서 같으면 회원반환, 다르면 null 반환
 */

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoginService {

//    private final MemberRepositoryOld memberRepositoryOld;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

//    public Member loginV1(String loginId, String password) {
//        return memberRepositoryOld.findByLoginId(loginId)
//                .filter(m -> m.getPassword().equals(password))
//                .orElse(null);
//    }

    /**
     * 로그인
     * PasswordEncoder.matches() 로 입력한 비밀번호와 DB에 저장된 암호화된 비밀번호와 일치하는지 확인
     * 로그인에 실패하면 null 반환
     */
    public Member login(LoginForm form) {

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
