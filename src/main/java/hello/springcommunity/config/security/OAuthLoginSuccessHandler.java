package hello.springcommunity.config.security;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.security.UserDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * OAuth2 login 성공 핸들러
 */

@Slf4j
@Component
public class OAuthLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("oauth2 로그인 성공!");
        log.info("authenticated={}", authentication.isAuthenticated());

//        UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
//        Member member = userDetailsDTO.getMember();
//        log.info("닉네임={}", member.getNickname());

        response.sendRedirect("/");

    }
}
