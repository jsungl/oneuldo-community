package hello.springcommunity.web.login;

import hello.springcommunity.dto.login.LoginForm;
import hello.springcommunity.dto.login.LoginRequestDTO;
import hello.springcommunity.service.login.LoginService;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.common.SessionConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
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
    public String loginForm(@ModelAttribute("loginForm") LoginRequestDTO form,
                            @RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "message", required = false) String message,
                            HttpServletRequest request,
                            Model model) {

        // 요청 시점의 사용자 URI 정보를 Session의 Attribute에 담아서 전달(잘 지워줘야함)
        // 로그인이 틀려서 다시 하면 요청 시점의 URI가 로그인 페이지가 되므로 조건문 설정
        String uri = request.getHeader("Referer");
        if(!uri.contains("/login")){
            request.getSession().setAttribute("prevPage", uri);
        }

        model.addAttribute("error", error);
        model.addAttribute("message", message);

        return "login/loginForm";
    }

    //@PostMapping("/login")
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
     * 세션에 있던 회원 데이터 초기화(무효화) -> 삭제하는것이 아니므로 브라우저에서는 세션 객체가 남아있다
     */
    //@PostMapping("/logout")
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
