package hello.springcommunity.config.interceptor;

import hello.springcommunity.common.interceptor.NotificationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;



@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebConfig implements WebMvcConfigurer {

    private final NotificationInterceptor notificationInterceptor;

    /**
     * because its MIME type ('text/html') is not executable, and strict MIME type checking is enabled.
     * 위 에러 발생시 /css, /js, /img 등 하위 폴더에 위치한 정적 자원으로의 접근시 보안상의 제한을 무시한다고 설정해야 한다
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(notificationInterceptor)
                .excludePathPatterns("/*.ico", "/css/**", "/js/**", "/error");

    }
}
