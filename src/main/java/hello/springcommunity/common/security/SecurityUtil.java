package hello.springcommunity.common.security;

import hello.springcommunity.dto.security.UserDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Slf4j
public class SecurityUtil {

    /**
     * 현재 securityContext에 저장된 username의 정보만 가져오기
     */
    public static Optional<String> getCurrentUsername() {

        // authentication객체가 저장되는 시점은 JwtFilter의 doFilter 메소드에서
        // Request가 들어올 때 SecurityContext에 Authentication 객체를 저장해서 사용
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || authentication.getName() == null) {
            //log.info("SecurityContext에 인증정보가 없습니다");
            return Optional.empty();
        }

        String username = null;
        if(authentication.getPrincipal() instanceof UserDetailsDTO) {
            username = ((UserDetailsDTO) authentication.getPrincipal()).getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }

        //log.info("username={}", username);
        return Optional.ofNullable(username);
    }

}
