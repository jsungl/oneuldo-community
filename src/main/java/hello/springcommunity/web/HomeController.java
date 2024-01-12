package hello.springcommunity.web;

import hello.springcommunity.dto.security.UserDetailsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@RequiredArgsConstructor
@Controller
public class HomeController {


    @GetMapping("/")
    public String home(Authentication authentication,
                       HttpServletRequest request,
                       @AuthenticationPrincipal UserDetailsDTO dto,
                       @RequestParam(value = "invalid", required = false) String invalid,
                       @RequestParam(value = "expired", required = false) String expired,
                       Model model) {

        if(request.getSession().getAttribute("runtimeExMessage") != null) {
            String msg = (String) request.getSession().getAttribute("runtimeExMessage");
            model.addAttribute("errorMessage", msg);
            request.getSession().removeAttribute("runtimeExMessage");
        }

        /**
         * 세션이 유효하지 않거나 만료시 에러 메시지 출력
         */
        if(invalid != null && invalid.equals("true")) {
            model.addAttribute("errorMessage", "세션이 유효하지 않습니다.");
        } else if (expired != null && expired.equals("true")) {
            model.addAttribute("errorMessage", "세션이 만료되었습니다.");
        }

        /**
         * Authentication : 인증객체
         * 2가지 용도로 사용된다. 1.인증 용도 또는 2.인증 후 세션에 담기 위한 용도
         *
         * 1. 인증시 id와 password 를 담고 인증 검증을 위해 전달되어 사용된다
         * 2. 인증 후 최종 인증 결과(user 객체, 권한 정보)를 담고 SecurityContext 에 저장되어 다음과 같은 코드로 전역적으로 참조가 가능하다
         * - Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         *
         * Authentication 구조
         * - Principal : 사용자 아이디 또는 User 객체
         * - Credenticals : 사용자 비밀번호
         * - authorities : 인증된 사용자의 권한 목록
         * - details : 인증 부과 정보(IP, 세션정보, 기타 인증 요청에서 사용했던 정보들)
         * - authenticated : 인증여부
         */

        if(authentication == null) {
            log.info("비회원");
            return "home";
        }


        /**
         * Spring Security 3.2 부터는 @AuthenticationPrincipal 어노테이션을 이용하여
         * UserDetails를 구현하여 만든 인스턴스(현재 로그인한 사용자 객체)를 가져올 수 있다
         */
        //관리자 페이지로 이동
        if(dto.getMember().getRoleValue().equals("ROLE_ADMIN")) {
            return "redirect:/admin";
        }

        return "home";
    }

}
