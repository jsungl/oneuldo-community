package hello.springcommunity.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * form login 성공 핸들러
 */

@Slf4j
@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        List<String> whitelist = List.of("/signup", "/find", "/login", "/?logout=true", "/?invalid=true", "/?expired=true","/memberAuthMail", "/error");

        log.info("로그인 성공 핸들러 실행!");

        //관리자
        //UserDetailsDTO userDetailsDTO = (UserDetailsDTO) authentication.getPrincipal();

        String url = "/";
        boolean match = false;

        String prevPage = (String) request.getSession().getAttribute("prevPage");
        if(prevPage != null){
            request.getSession().removeAttribute("prevPage");
            match = whitelist.stream().anyMatch(i -> prevPage.contains(i));
        }


        /**
         * 로그인 성공시 원래 접속하려던 URL 로 리다이렉트 한다
         * RequestCache : 사용자의 이전 요청 정보를 세션에 저장하고 이를 꺼내오는 일을 한다
         * SavedRequest : 사용자가 요청했던 요청의 파라미터 값, 헤더값 등이 저장되어 있다
         *
         * 즉, 요청 정보를 실제로 저장하는 것은 SavedRequest 구현체가 수행하며 이를 세션에 저장하는 것은 RequestCache 인터페이스의 구현체가 수행한다
         */
        RequestCache requestCache = new HttpSessionRequestCache();
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if(prevPage != null && savedRequest != null) {
            /**
             * 1. Security 가 요청을 가로챈 경우
             */
            url = savedRequest.getRedirectUrl();
            /** ex) redirectUrl=http://localhost:8081/posts/add **/
            //log.info("redirectUrl={}", url);
            //log.info("prevPage={}", prevPage);

            if(!url.equals(prevPage)) url = prevPage;

            /** 세션에 저장된 객체를 다 사용한 뒤에는 지워줘서 메모리 누수 방지 **/
            requestCache.removeRequest(request, response);

        } else if (prevPage != null && !prevPage.equals("") && !match) {
            /**
             * 2. 인터셉트 되지 않고 로그인 버튼을 눌러서 로그인 페이지로 이동해 로그인 한 경우
             */
            log.info("prevPage={}",prevPage);
            url = prevPage;
        }

        response.sendRedirect(url);

    }

}
