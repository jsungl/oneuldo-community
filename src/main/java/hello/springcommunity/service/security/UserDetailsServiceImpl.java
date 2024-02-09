package hello.springcommunity.service.security;

import hello.springcommunity.dao.member.MemberAuthRepository;
import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberAuth;
import hello.springcommunity.dto.security.UserDetailsDTO;
import hello.springcommunity.exception.AuthenticationFailureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberAuthRepository memberAuthRepository;


    /**
     * 입력받은 사용자의 로그인 정보를 DB에서 조회
     * noRollbackFor 속성에 정의한 Exception이 해당 트랜잭션 내부에서 발생할 경우, 수행된 부분까지 commit이 발생하며 rollback이 진행되지 않는다.
     */
    @Override
    @Transactional(readOnly = true, noRollbackFor = {UsernameNotFoundException.class,NoSuchElementException.class})
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //log.info("loadUserByUsername() 실행");
        Member member = memberRepository.findByLoginId(username).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        if(member.getRoleValue().equals("ROLE_USER")) {

            MemberAuth memberAuth = memberAuthRepository.findByMemberId(member.getId()).orElseThrow(() -> new NoSuchElementException("존재하지 않는 인증정보 입니다."));

            //이메일 인증여부 검사
            if(!memberAuth.getIsAuthenticated()) {
                throw new AuthenticationFailureException("이메일이 인증되지 않았습니다. 메일 확인 후 인증버튼을 눌러주세요!");
            }

        }

        //return toUserDetails(member);
        return new UserDetailsDTO(member);

    }

    /**
     * 회원정보 수정 후 세션 재설정
     */
    public UserDetails getUserDetails(Member member) {
        return new UserDetailsDTO(member);
    }


    private UserDetails toUserDetails(Member member) {
        return User.builder()
                .username(member.getLoginId())
                .password(member.getPassword())
                .authorities(new SimpleGrantedAuthority(member.getRoleValue()))
                .build();
    }
}
