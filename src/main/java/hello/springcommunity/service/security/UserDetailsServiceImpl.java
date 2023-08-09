package hello.springcommunity.service.security;

import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.security.UserDetailsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * 입력받은 사용자의 로그인 정보를 DB에서 조회
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //User user = userRepository.findByLoginId(username).orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. loginId= " + username));
        Member member = memberRepository.findByLoginId(username).orElseThrow(() -> new UsernameNotFoundException("해당 유저가 존재하지 않습니다. loginId= " + username));
        UserDetails userDetails = toUserDetails(member);
        log.info("userDetails username={}", userDetails.getUsername());
        log.info("userDetails password={}", userDetails.getPassword());
        log.info("userDetails authorities={}", userDetails.getAuthorities());

        //return userDetails;
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
