package hello.springcommunity.config.security;


import hello.springcommunity.config.oauth.CustomOauth2UserService;
import hello.springcommunity.config.oauth.CustomTokenResponseConverter;
import hello.springcommunity.config.oauth.PrincipalOauth2UserService;
import hello.springcommunity.service.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private final UserDetailsServiceImpl userDetailsService;

    //private final CustomOauth2UserService customOauth2UserService;
    private final PrincipalOauth2UserService principalOauth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DB에 존재하지 않는 계정으로 로그인시 UsernameNotFoundException 이 아닌 BadCredentialsException 이 발생한다
     * 인증시 아이디나 비밀번호가 틀리면 모두 BadCredentialsException 가 발생한다
     *
     * 인증절차에서 UserDetailsService 의 loadUserByname() 이 실행되는데, 이때 만약 UsernameNotFoundException 이 발생하면
     * AbstractUserDetailsauthenticationProvider 의 hideUserNotFoundException(Boolean)를 확인하여 UsernameNotFoundException 보여줄 건지 안 보여준다면 BadCredentialsException을 내보내게 한다
     * 아이디 체크와 비밀번호 체크를 따로 exception을 날리는 거보다 BadCredential Exception 하나만 날리는것이 보다 더 보안이 강하기 때문이라고 한다 -> 아이디가 틀린건지 비밀번호가 틀린건지 알려주지 않기 위해
     *
     * 만약 따로 예외를 구분하여 처리하여 아이디가 틀린건지 비밀번호가 틀린건지 알려주고 싶다면 2가지 방법이 있다
     * 1. AuthenticationProvider의 구현체인 DaoAuthenticationProvider 에서 setHideUserNotFoundExceptions 설정을 false로 한다(기본값 true)
     *
     * 2. AuthenticationProvider 를 커스텀하여 아이디가 없는 경우에는 UsernameNotFoundException 을, 비밀번호가 틀린 경우라면 BadCredentialsException 을 발생시킨다
     *
     * 그리고 AuthenticationFailureHandler 에서 예외 처리를 해주면 된다
     * @return
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setHideUserNotFoundExceptions(false);
        return daoAuthenticationProvider;
    }

    /**
     * 2번째 방법
     * ProviderManager 는 AuthenticationManager의 구현체로 스프링에서 인증을 담당하는 클래스이다
     * ProviderManager 클래스는 인증을 담당하고 있지만 실제로 직접 인증 과정을 진행하는 클래스는 AuthenticationProvider(s)에게 인증을 위임하고
     * 그중에서 인증 처리가 가능한 AuthenticationProvider 객체가 인증 과정을 거쳐서 인증에 성공하면 요청에 대해 ProviderManager가 인증이 되었다고 알려주는 방식이다
     * 기본적으로 AuthenticationProvider의 구현체인 DaoAuthenticationProvider 가 등록되어 있다
     *
     * AuthenticationProvider 는 전달받은 사용자의 아이디와 비밀번호를 기반으로 실질적으로 인증을 수행하는 클래스이다
     * AuthenticationProvider 의 구현체인 CustomAuthenticationProvider 를 통해 AuthenticationProvider 를 커스텀한다
     * 인증 로직이 구현된 CustomAuthenticationProvider를 ProviderManager가 알 수 있도록 ProviderManager에 등록해준다
     * ProviderManager 의 provider.authenticate(authentication)를 디버깅해보면 CustomAuthenticationProvider 가 등록된것을 확인할 수 있다
     *
     */
//    @Bean
//    public AuthenticationManager authenticationManager() {
//        return new ProviderManager(customAuthenticationProvider());
//    }
//
//    @Bean
//    public CustomAuthenticationProvider customAuthenticationProvider() {
//        return new CustomAuthenticationProvider(passwordEncoder(), userDetailsService);
//    }


//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        // 정적 리소스들이 보안필터를 거치지 않게끔
//        return (web) -> web.ignoring().antMatchers("/css/**", "/js/**", "/img/**", "/*.ico", "/error");
//    }

    /**
     * Resource 용 SecurityFilterChain 추가
     * WebSecurity.ignoring() 을 사용할 경우 Spring Security 의 어떠한 보호도 받을 수 없기 때문에 permitAll 를 사용하라고 권장
     * 아래의 filterChain 보다 먼저 경로 체크를 해야 하기 때문에 순서를 정할 필요가 있다. @Order 어노테이션을 통해 정적 리소스들의 요청에 대해 먼저 체크하도록 한다
     *
     * 성능에 영향을 줄 수 있는 requestCache, securityContext, sessionManagement 에 대한 필터를 제거
     */
    @Bean
    @Order(1)
    public SecurityFilterChain resources(HttpSecurity http) throws Exception {
        http.requestMatchers((matchers) -> matchers.antMatchers("/css/**", "/js/**", "/img/**", "/*.ico", "/error"))
            .authorizeHttpRequests((authorize) -> authorize.anyRequest().permitAll())
            //.requestCache().disable()
            //.securityContext().disable()
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().disable() //cors 방지
            .csrf().disable() //csrf 방지(post 요청마다 token이 필요한 과정을 생략하겠다는 의미)
            .headers().frameOptions().disable(); //xframe 방어 해제

        /**
         * 인가 API(나머지는 인증 API)
         * 페이지 권한 설정
         *
         * 3.0이후 버전에서는 변경사항들이 있으니 주의!
         * .authorizeRequests() → .authorizeHttpRequests()
         * .antMatchers() → .requestMatchers()
         * .access("hasAnyRole('ROLE_A','ROLE_B')") → .hasAnyRole("A", "B")
         */
//        http.authorizeRequests() // 요청에 의한 보안검사 시작
//                        .antMatchers("/", "/posts", "/post/{postId}", "/post/search").permitAll()
//                        .antMatchers("/member/add", "/login").anonymous()
//                        .antMatchers("/admin/**").hasRole("ADMIN") //Spring Security에서 prefix를 자동으로 "ROLE_"을 넣어주므로 이 때 hasRole에는 ROLE을 제외하고 뒷 부분인 ADMIN만 써주면 된다
//                        .anyRequest().authenticated(); //그 외 요청들은 인증필요

        http.authorizeRequests() //요청에 의한 보안검사 시작
                .antMatchers("/member/add", "/login").anonymous()
                .antMatchers("/post/add").hasAnyRole("USER", "SOCIAL", "ADMIN") //Spring Security에서 prefix를 자동으로 "ROLE_"을 넣어주므로 이 때 hasRole에는 ROLE을 제외하고 뒷 부분인 USER만 써주면 된다
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/", "/posts", "/post/{postId}", "/post/search").permitAll()
                .anyRequest().authenticated(); //그 외 요청들은 인증필요


        //로그인 설정
        http.formLogin() //보안 검증방식은 form login 방식
            .loginPage("/login") //사용자 정의 로그인 페이지
            //.defaultSuccessUrl("/") //로그인 성공 후 이동 페이지, successHandler 가 있다면 효과 없음
            //.failureUrl("/login?error=true") // 로그인 실패 후 이동 페이지
            .usernameParameter("loginId") //login form에 적힌 아이디 name 설정
            .passwordParameter("password") //login form에 적힌 패스워드 name 설정
            .successHandler(new AuthenticationSuccessHandler() {
                @Override
                public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

                    String url = "/";
                    String prevPage = (String) request.getSession().getAttribute("prevPage");
                    if(prevPage != null){
                        request.getSession().removeAttribute("prevPage");
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

                    if(savedRequest != null) {
                        /**
                         * 1. Security 가 요청을 가로챈 경우
                         */
                        url = savedRequest.getRedirectUrl();
                        log.info("redirectUrl={}", url); //ex) redirectUrl=http://localhost:8081/posts/add
                        // 세션에 저장된 객체를 다 사용한 뒤에는 지워줘서 메모리 누수 방지
                        requestCache.removeRequest(request, response);
                    } else if (prevPage != null && !prevPage.equals("")) {
                        /**
                         * 2. 인터셉트 되지 않고 로그인 버튼을 눌러서 로그인 페이지로 이동해 로그인 한 경우
                         */
                        log.info("prevPage={}",prevPage);
                        url = prevPage;
                    }

                    response.sendRedirect(url);

                }
            })
            .failureHandler(new AuthenticationFailureHandler() {
                @Override
                public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                    log.info("failureHandler 실행!");
                    log.error("AuthenticationException ",exception); //로그인 실패시 예외에 대한 정보

                    String errorMessage="";

                    if(exception instanceof BadCredentialsException) {
                        errorMessage = "아이디 또는 비밀번호가 맞지 않습니다. 다시 확인해주세요.";
                    } else if (exception instanceof InternalAuthenticationServiceException) {
                        errorMessage = "내부 시스템 문제로 로그인 요청을 처리할 수 없습니다. 관리자에게 문의하세요. ";
                    } else if (exception instanceof UsernameNotFoundException) {
                        errorMessage = "존재하지 않는 계정입니다. 회원가입 후 로그인해주세요.";
                    } else if (exception instanceof AuthenticationCredentialsNotFoundException) {
                        errorMessage = "인증 요청이 거부되었습니다. 관리자에게 문의하세요.";
                    } else {
                        errorMessage = "알 수 없는 오류로 로그인 요청을 처리할 수 없습니다. 관리자에게 문의하세요.";
                    }

                    log.info("errorMessage={}", errorMessage);
                    errorMessage = URLEncoder.encode(errorMessage, "UTF-8"); //한글 깨짐 방지를 위해 UTF-8 인코딩 설정
                    response.sendRedirect("/login?error=true&message=" + errorMessage);
                }
            });

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
                    log.info("authenticationEntryPoint 실행!");
                    log.error("AuthenticationException ",authException);
                    response.sendRedirect("/login");
                }
            })
            .accessDeniedHandler(new AccessDeniedHandler() {
                @Override
                public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                    log.info("accessDeniedHandler 실행!");
                    log.error("AccessDeniedException ",accessDeniedException);
                    response.sendRedirect("/");
                }
            });

        /**
         * maximumSessions(1) : 최대 허용 가능 세션 수
         * maxSessionsPreventsLogin(true) : 최대 허용 세션의 수가 되었을 때 추가적인 인증 요청(세션 생성)이 있을 경우 어떻게 처리를 할지 정하는 api.
         * true면 현재 사용자 인증 실패, false(default값)면 기존 세션 만료
         * invalidSessionUrl("/invalid") : 세션이 유효하지 않을 때 이동 할 페이지
         * expiredUrl("/expired ") : 세션이 만료된 경우 이동 할 페이지
         */
//        http.sessionManagement()
//            .invalidSessionUrl("/invalid")
//            .maximumSessions(1)
//            .maxSessionsPreventsLogin(true)
//            .expiredUrl("/expired");




        //로그아웃 설정
        http.logout()
            .logoutUrl("/logout") // 로그아웃 처리 URL (= form action url)
            .logoutSuccessUrl("/?logout=true") //로그아웃 성공시 이동할 url
            .invalidateHttpSession(true) //로그아웃시 생성된 세션 삭제
            .deleteCookies("JSESSIONID");

//        http.authenticationProvider(authenticationProvider());
        
        /**
         * OAuth2 로그인 설정
         * userInfoEndpoint() : OAuth2 로그인 성공 후 가져올 설정들
         * userService(customOauth2UserService) : 소셜 로그인 성공 시 후속 조치를 진행할 UserService 인터페이스 구현체 등록, 서버에서 사용자 정보를 가져온 상태에서 추가로 진행하고자 하는 기능 명시
         */
        http.oauth2Login()
                .tokenEndpoint()
                .accessTokenResponseClient(accessTokenResponseClient())
                .and()
                .defaultSuccessUrl("/")
                .userInfoEndpoint()
                .userService(principalOauth2UserService);
//                .and()
//                .successHandler(OAuth2AuthenticationSuccessHandler)
//                .failureHandler();

        return http.build();

    }

    /**
     * 토큰을 요청하는데 사용되는 ResponseClient를 설정한다
     *
     */
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        //기본적으로 사용하는 DefaultAuthorizationCodeTokenResponseClient를 생성
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();

        //response 도 커스텀
        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseHttpMessageConverter = new OAuth2AccessTokenResponseHttpMessageConverter();
        tokenResponseHttpMessageConverter.setTokenResponseConverter(new CustomTokenResponseConverter());

        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
                new FormHttpMessageConverter(), tokenResponseHttpMessageConverter));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

        accessTokenResponseClient.setRestOperations(restTemplate);
        return accessTokenResponseClient;

    }

}
