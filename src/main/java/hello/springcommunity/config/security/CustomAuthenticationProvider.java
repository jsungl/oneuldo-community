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

/**
 * AuthenticationProvider 커스텀 구현체
 * authenticate() 메서드를 통해 인증 과정이 진행된다
 * supports(Class<?>) 메서드는 앞에서 필터를 통해 보내준 Authentication 객체를 이 AuthenticationProvider가 인증 처리가 가능한 클래스인지를 확인하는 메서드다
 */

@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;


    /**
     * supports를 통과(true)해야 authenticate 메서드가 호출된다
     * 사용자 요청으로부터 전달된 Authentication에 있는 비밀번호와 userDetailsService.loadUserbyUsername(username) 메서드를 통해 DB에서 조회한 유저의 비밀번호가 일치하는지만 확인한다.
     * 그리고 인증에 성공한 객체를 DB에서 가져온 권한 정보도 담아서 UsernamePasswordAuthenticationToken 타입으로 반환한다.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
       String loginId = authentication.getName();
       String password = (String) authentication.getCredentials();

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);

        if(userDetails == null) {
            throw new UsernameNotFoundException("해당 유저가 존재하지 않습니다. loginId=" + loginId);
        } else if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        /**
         * UsernamePasswordAuthenticationToken은 Authentication 인터페이스의 구현체로
         * 인증 성공시 최종 인증 객체(UsernamePasswordAuthenticationToken)를 생성 후 반환한다
         * 그러면 ProviderManager 는 Authentication을 SecurityContextHolder 객체 안의 SecurityContext에 저장한다
         */
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());

        return authenticationToken;
    }

    /**
     * 전달된 파라미터(authentication)의 타입이 UsernamePasswordAuthenticationToken 과 일치하는지 검사한다
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        //return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
