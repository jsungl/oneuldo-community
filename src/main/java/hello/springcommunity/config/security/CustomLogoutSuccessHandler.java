package hello.springcommunity.config.security;

import hello.springcommunity.dao.notification.EmitterRepository;
import hello.springcommunity.dto.security.UserDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 로그아웃 성공 핸들러
 */
@Slf4j
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final EmitterRepository emitterRepository;

    public CustomLogoutSuccessHandler(EmitterRepository emitterRepository) {
        this.emitterRepository = emitterRepository;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsDTO) {
            UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();
            String loginId = userDetailsDTO.getUsername();

            Map<String, SseEmitter> sseEmitters = emitterRepository.findById(loginId);

            sseEmitters.forEach((key, emitter) -> {
                emitter.complete();
            });
        }

        // 로그아웃 성공 시 수행할 로직을 작성
        response.sendRedirect("/?logout=true");
    }
}
