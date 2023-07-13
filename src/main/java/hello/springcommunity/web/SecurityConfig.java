package hello.springcommunity.web;

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
            .csrf().disable() //csrf 방지
            .formLogin().disable() //기본 로그인 페이지 없애기
            .headers().frameOptions().disable(); //XFrameOptionsHeaderWriter 의 최적화 설정을 허용하지 않음(스프링 시큐리티는 기본적으로 X-Frame-Options Click jacking 공격 막기 설정이 되어 있음)

        return http.build();
    }
}
