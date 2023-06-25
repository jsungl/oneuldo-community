package hello.springcommunity.domain.login;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 로그인 서비스의 핵심 비즈니스 로직
 * 회원을 조회한 다음에 파라미터로 넘어온 비밀번호와 비교해서 같으면 회원반환, 다르면 null 반환
 */
@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;

    /**
     * @return null이면 로그인 실패
     */
    public Member login(String loginId, String password) {
        return memberRepository.findByLoginId(loginId)
                .filter(m -> m.getPassword().equals(password))
                .orElse(null);
    }
}
