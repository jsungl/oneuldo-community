package hello.springcommunity.common.interceptor;

import hello.springcommunity.common.SessionConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 스프링 인터셉터를 이용한 로그인 인증 체크 인터셉터
 * 
 */

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    //인증이라는 것은 컨트롤러 호출 전에만 호출되면 되기 때문에 preHandle 만 구현하면 된다.
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();
        log.info("LoginCheckInterceptor 실행 {}", requestURI);

        HttpSession session = request.getSession(false);

        if(session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            log.info("미인증 사용자 요청");

            //로그인 화면으로 리다이렉트
            response.sendRedirect("/login?redirectURL=" + requestURI); //로그인 화면으로 리다이렉트 후 다시 로그인 후 로그인 이전 화면으로 돌아가기 위해
            return false; //다음으로 넘어가지 않고 요청이 여기서 종료
        }

        return true;

    }
}
