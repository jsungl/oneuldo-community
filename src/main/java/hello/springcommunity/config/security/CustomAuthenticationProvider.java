package hello.springcommunity.config.security;

import hello.springcommunity.service.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * supports를 통과(true)해야 authenticate 메서드가 호출된다
     * 사용자 요청으로부터 전달된 Authentication에 있는 비밀번호와 userDetailsService.loadUserbyUsername(username) 메서드를 통해 DB에서 조회한 유저의 비밀번호가 일치하는지만 확인한다.
     * 그리고 인증에 성공한 객체를 DB에서 가져온 권한 정보도 담아서 UsernamePasswordAuthenticationToken 타입으로 반환한다.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
       String loginId = authentication.getName();
       String password = (String) authentication.getCredentials();

        //UserDetailsDTO userDetailsDTO = (UserDetailsDTO) userDetailsService.loadUserByUsername(loginId);
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);

        if(userDetails == null) {
            throw new UsernameNotFoundException("해당 유저가 존재하지 않습니다. loginId=" + loginId);
        } else if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // 인증 성공시 성공한 인증 객체를 생성 후 반환
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());

//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginId, password, userDetails.getAuthorities());

        return authenticationToken;
    }

    /**
     * 전달된 파라미터(authentication)의 타입이 UsernamePasswordAuthenticationToken 과 일치하는지 검사한다
     *
     *
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
