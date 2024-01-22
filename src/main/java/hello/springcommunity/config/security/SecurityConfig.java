package hello.springcommunity.config.security;


import hello.springcommunity.config.oauth.CustomTokenResponseConverter;
import hello.springcommunity.config.oauth.PrincipalOauth2UserService;
import hello.springcommunity.service.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final PrincipalOauth2UserService principalOauth2UserService;
    private final CustomLoginSuccessHandler loginSuccessHandler;
    private final CustomLoginFailureHandler loginFailureHandler;
    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    private final OAuthLoginFailureHandler oAuthLoginFailureHandler;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
        return daoAuthenticationProvider;
    }


    /**
     * Resource 용 SecurityFilterChain 추가
     * WebSecurity.ignoring() 을 사용할 경우 Spring Security 의 어떠한 보호도 받을 수 없기 때문에 permitAll 를 사용하라고 권장
     * 아래의 filterChain 보다 먼저 경로 체크를 해야 하기 때문에 순서를 정할 필요가 있다. @Order 어노테이션을 통해 정적 리소스들의 요청에 대해 먼저 체크하도록 한다
     *
     * 성능에 영향을 줄 수 있는 requestCache, securityContext, sessionManagement 에 대한 필터를 제거
     */
    @Bean
    @Order(1)
    public SecurityFilterChain exceptionSecurityFilterChain(HttpSecurity http) throws Exception {
        http.requestMatchers((matchers) -> matchers.antMatchers("/css/**", "/js/**", "/img/**", "/*.ico", "/error"))
            .authorizeHttpRequests((authorize) -> authorize.anyRequest().permitAll())
            .requestCache().disable()
            .securityContext().disable()
            .sessionManagement().disable();

        return http.build();
    }

    /**
     * 인증,인가 설정
     * 기존에는 WebSecurityConfigurerAdapter 를 상속받아서 코드를 작성했지만 현재는 지원하지 않기 때문에
     * SecurityFilterChain을 통해 작성한다
     */
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        /**
         * CSRF, CORS 설정
         */
        http.cors().disable() //cors 방지
            .csrf().ignoringAntMatchers("/post/like/**","/api/image/upload")
            .and()
            .headers().frameOptions().disable(); //xframe 방어 해제

        /**
         * 인가 API - 요청에 의한 보안검사 시작
         * 페이지 권한 설정 - URL 방식
         *
         * 3.0이후 버전에서는 변경사항들이 있으니 주의!
         * Spring Security에서 prefix를 자동으로 "ROLE_"을 넣어주므로 이 때 hasRole에는 ROLE을 제외하고 뒷 부분인 USER만 써주면 된다
         * .authorizeRequests() → .authorizeHttpRequests()
         * .antMatchers() → .requestMatchers()
         * .access("hasAnyRole('ROLE_A','ROLE_B')") → .hasAnyRole("A", "B")
         */
        http.authorizeRequests()
                .antMatchers("/signup", "/find/**", "/login/**").anonymous()
                .antMatchers("/post/add","/post/{postId}/edit","/post/{postId}/delete","/post/like/**","/member/**","/api/post/**").authenticated()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll(); //그 외 요청들은 모두 접근허용



        /**
         * Form Login 설정
         */
        http.formLogin() //보안 검증방식은 form login 방식
            .loginPage("/login") //사용자 정의 로그인 페이지
            .usernameParameter("loginId") //login form에 적힌 아이디 name 설정
            .passwordParameter("password") //login form에 적힌 패스워드 name 설정
            .loginProcessingUrl("/login/process")
            .successHandler(loginSuccessHandler)
            .failureHandler(loginFailureHandler);
                
                
        /**
         * 예외처리(인증/인가 예외)
         * Spring Security에서 인증/인가에 대한 예외처리는 FilterSecurityFilter와 ExceptionTranslationFilter가 처리를 한다
         * 해당 필터는 크게 AuthenticationException(인증 예외)과 AccessDeniedException(인가 예외)을 처리를 한다
         *
         * AuthenticationException(인증 예외)
         * AuthenticationException은 인증에 문제가 생길 때 발생하는 예외로, 해당 예외를 처리하는 방법은 다음 2가지가 있다
         * 1. AuthenticationEntryPoint 호출
         * - 로그인 페이지로 이동, 401(Unauthorized) 오류 코드 전달 등
         *
         * 2. 인증 예외가 발생하기 전의 요청정보를 저장
         * - 예외가 발생하기 전 원래 이동하고자 하였던 페이지 요청 정보를 저장하고 로그인 후에 해당 페이지로 이동하게 하기 위해 요청 정보 저장을 한다
         * - 요청 정보를 저장할 때는 RequestCache 와 SavedRequest 를 사용한다
         *
         * AccessDeniedException(인가 예외)
         * 권한이 없는 데이터에 접속을 하려고 하였을 때 발생하는 예외로, AccessDeniedHandler에서 처리한다
         * AccessDeniedHandler에서 후속작업을 하고, 보통 403 오류 코드 전달하거나 denied 페이지로 이동한다
         */
        http.exceptionHandling()
            .authenticationEntryPoint(new AuthenticationEntryPoint() {
                @Override
                public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                    log.info("인증 실패! authenticationEntryPoint 실행");
                    //log.error("AuthenticationException",authException);
                    //response.sendRedirect("/login");

                    String requestURI = request.getRequestURI();
                    request.setAttribute("msg", "로그인 후 이용할 수 있습니다.");
                    request.setAttribute("nextPage", "/login");
                    request.getRequestDispatcher("/error/redirect").forward(request, response);

                }
            })
            .accessDeniedHandler(new AccessDeniedHandler() {
                @Override
                public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                    log.info("인가 실패! accessDeniedHandler 실행");
                    String requestURI = request.getRequestURI();
                    String message = "";
                    log.info("url={}", requestURI);
                    if(requestURI.contains("/login")) {
                        message = "이미 로그인 한 상태입니다.";
                    } else {
                        message = "접근권한이 없습니다.";
                    }
                    //log.error("AccessDeniedException",accessDeniedException);
                    //response.sendRedirect("/?denied=true");

                    request.setAttribute("msg", message);
                    request.setAttribute("nextPage", "/");
                    request.getRequestDispatcher("/error/denied").forward(request, response);
                }
            });

        /**
         * 세션관리
         * maximumSessions(1) : 최대 허용 가능 세션 수, -1 입력시 무제한 세션 생성 허용
         * maxSessionsPreventsLogin(true) : 최대 허용 세션의 수가 되었을 때 추가적인 인증 요청(세션 생성)이 있을 경우 어떻게 처리를 할지 정하는 api.
         * true면 현재 사용자 인증 실패, false(default값)면 기존 세션 만료
         * invalidSessionUrl("/invalid") : 세션이 유효하지 않을 때 이동 할 페이지
         * expiredUrl("/expired ") : 세션이 만료된 경우 이동 할 페이지
         */
        http.sessionManagement()
            .invalidSessionUrl("/?invalid=true")
            .maximumSessions(1)
            .maxSessionsPreventsLogin(true)
            .expiredUrl("/?expired=true");


        /**
         * 로그아웃
         */
        http.logout()
            .logoutUrl("/logout") // 로그아웃 처리 URL (= form action url)
            .logoutSuccessUrl("/?logout=true") //로그아웃 성공시 이동할 url
            .invalidateHttpSession(true) //로그아웃시 생성된 세션 삭제
            .deleteCookies("JSESSIONID");


        /**
         * OAuth2 로그인 설정
         *
         * tokenEndPoint().accessTokenResponseClient() : 토큰 요청시 사용되는 ResponseClient를 설정한다
         *
         * userInfoEndpoint() : OAuth2 로그인 성공 후 사용자 정보를 가져올 때 설정(로그인된 유저의 정보를 가져온다)
         * userService(customOauth2UserService) : 소셜 로그인 성공 시 후속 조치를 진행할 UserService 인터페이스 구현체 등록, 서버에서 사용자 정보를 가져온 상태에서 추가로 진행하고자 하는 기능 명시
         * 
         * successHandler : oauth 인증 성공시 호출
         * failureHandler : oauth 인증 실패시 호출
         */
        http.oauth2Login()
                .tokenEndpoint()
                .accessTokenResponseClient(accessTokenResponseClient())
                .and()
                .userInfoEndpoint()
                .userService(principalOauth2UserService)
                .and()
                .successHandler(oAuthLoginSuccessHandler)
                .failureHandler(oAuthLoginFailureHandler);

        return http.build();

    }

    /**
     * 토큰을 요청하는데 사용되는 ResponseClient 설정
     */
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();

        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter = new OAuth2AccessTokenResponseHttpMessageConverter();
        tokenResponseHttpMessageConverter.setTokenResponseConverter(new CustomTokenResponseConverter());

        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
                new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

        accessTokenResponseClient.setRestOperations(restTemplate);
        return accessTokenResponseClient;

    }

}
