package hello.springcommunity.web;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberRepositoryOld;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberRepositoryOld memberRepositoryOld;

    @GetMapping("/")
    public String homeLogin(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember, Model model) {

        //로그인
        //세션에 회원 데이터가 없으면 기존 home 으로 이동 -> 비로그인 사용자
        if (loginMember == null) {
            return "home";
        }
        //세션이 유지되면 loginhome 으로 이동 -> 로그인 사용자
        model.addAttribute("member", loginMember);
        return "loginHome";
    }
}
