package hello.springcommunity.web.login;

import hello.springcommunity.domain.login.LoginService;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.web.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 로그인 컨트롤러
 * 로그인 서비스를 호출해서 로그인에 성공하면 홈 화면으로 이동,
 * 로그인에 실패하면 글로벌 오류를 생성한다. 그리고 정보를 다시 입력하도록 로그인 폼(loginForm)을 뷰 템플릿으로 사용한다
 */

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    /**
     * 로그인
     */

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") LoginForm form) {
        return "login/loginForm";
    }

    @PostMapping("/login")
    public String login(@Validated @ModelAttribute("loginForm") LoginForm form,
                        BindingResult bindingResult,
                        HttpServletRequest request,
                        @RequestParam(defaultValue = "/") String redirectURL) {

        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "login/loginForm";
        }

        Member loginMember = loginService.login(form);
        log.info("loginMember={}", loginMember);

        //로그인 실패시 글로벌 오류 생성
        if (loginMember == null) {
            log.info("login fail");
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }

        //로그인 성공 처리
        //세션이 있으면 기존 세션 반환, 없으면 신규 세션 생성
        //getSession(true) 가 default, getSession(false) 이면 기존 세션이 없는경우 신규 세션을 생성하지 않는다 -> null 반환
        HttpSession session = request.getSession();
        //세션에 로그인 회원 정보 보관
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        log.info("redirectURL={}", redirectURL);
        //로그인에 성공하면 처음 요청한 URL로 이동
        return "redirect:" + redirectURL;
    }

    /**
     * 기존 로그아웃 요청 처리
     * 세션에 있던 회원 데이터가 삭제되지만, 브라우저에서는 세션이 삭제되지는 않는다
     */
//    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {

        log.info("로그아웃 요청");

        HttpSession session = request.getSession(false);
        log.info("session={}", session);

        //세션을 삭제
        if (session != null) {
            log.info("세션삭제");
            session.invalidate();
        }
        return "redirect:/";
    }

}
