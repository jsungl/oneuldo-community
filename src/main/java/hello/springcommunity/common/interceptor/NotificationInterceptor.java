package hello.springcommunity.common.interceptor;

import hello.springcommunity.dto.notification.NotificationDTO;
import hello.springcommunity.dto.security.UserDetailsDTO;
import hello.springcommunity.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 스프링 인터셉터를 이용한 알림 체크 인터셉터
 * 
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {

    private final NotificationService notificationService;

    /**
     * 컨트롤러의 메서드가 실행된 후, 뷰가 렌더링되기 전에 호출
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(modelAndView != null && authentication != null && !authentication.getName().equals("anonymousUser")) {
            log.info("---------NotificationInterceptor 실행!---------");
            String loginId = ((UserDetailsDTO) authentication.getPrincipal()).getUsername();
            List<NotificationDTO> list = notificationService.getAll(loginId);
            modelAndView.addObject("notifications", list);
        }
    }
}
