package hello.springcommunity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 기존에는 WebSecurityConfigurerAdapter 를 상속받아서 코드를 작성했지만 현재는 지원하지 않기 때문에
 * SecurityFilterChain을 통해 작성한다
 */
@Configuration //해당 클래스 Bean 등록
@EnableWebSecurity //스프링 시큐리티 활성(자동설정)
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().disable() //cors 방지
            .csrf().disable() //csrf 방지(post 요청마다 token이 필요한 과정을 생략하겠다는 의미)
            .formLogin().disable() //기본 로그인 페이지 없애기
            .headers().frameOptions().disable(); //xframe 방어 해제

        /**
         * Spring Security 의 로그아웃 기능
         * 기존의 로그아웃 기능을 사용하려면 logout 비활성화 한다
         */
        //http.logout().disable();

        http.logout()
            .logoutUrl("/logout") //csrf 기능을 비활성화 할 경우에는 GET 방식도 LogoutFilter 가 처리 -> 직접 url에 /logout 을 쳐도 로그아웃 처리 됨
            .logoutSuccessUrl("/?logout=true")
            .invalidateHttpSession(true) //세션 객체 삭제
            .deleteCookies("JSESSIONID");

        return http.build();
    }
}
