package hello.springcommunity.web;

import hello.springcommunity.web.interceptor.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 로그인 인증 체크 인터셉터 등록
 * 기본적으로 모든 경로에 해당 인터셉터를 적용하되
 * 홈, 회원가입, 로그인, 로그아웃, 게시물 목록, 게시물 상세조회, 리소스 조회, 오류 등은 인터셉터를 적용하지 않는다
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/members/add", "/login", "/logout", "/posts", "/posts/detail", "/posts/search", "/*.ico", "/css/**", "/error");
    }
}
