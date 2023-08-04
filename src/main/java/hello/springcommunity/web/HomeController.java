package hello.springcommunity.web;

import hello.springcommunity.common.SessionConst;
import hello.springcommunity.domain.member.Member;
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

    @GetMapping("/")
    public String homeLogin(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember, Model model) {

        //로그인
        //세션에 회원 데이터가 없으면 비로그인 사용자 -> 기존 home 으로 이동
        if (loginMember == null) {
            return "home";
        }

        //세션에 회원 데이터가 있으면 로그인 사용자 -> loginhome 으로 이동
        model.addAttribute("member", loginMember);
        return "loginHome";
    }
}
