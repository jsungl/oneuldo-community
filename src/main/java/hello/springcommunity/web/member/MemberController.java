package hello.springcommunity.web.member;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberRepository;
import hello.springcommunity.domain.member.MemberService;
import hello.springcommunity.domain.validation.ValidationSequence;
import hello.springcommunity.web.member.form.MemberSaveForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @GetMapping("/add")
    public String addForm(@ModelAttribute MemberSaveForm memberSaveForm) {
        return "members/addMemberForm";
    }
//    public String addForm(@ModelAttribute("member") Member member) {
//        return "members/addMemberForm";
//    }

    @PostMapping("/add")
    public String save(@Validated(ValidationSequence.class) @ModelAttribute MemberSaveForm memberSaveForm, BindingResult result) {

        if (result.hasErrors()) {
            log.info("errors={}", result);
            return "members/addMemberForm";
        }

        Member member = Member.builder()
                .loginId(memberSaveForm.getLoginId())
                .password(memberSaveForm.getPassword())
                .name(memberSaveForm.getName())
                .build();

        memberService.join(member);
        log.info("signup success");

        return "redirect:/";
    }
//    public String save(@Validated(ValidationSequence.class) @ModelAttribute Member member, BindingResult result) {
//        if (result.hasErrors()) {
//            return "members/addMemberForm";
//        }
//
//        memberRepository.save(member);
//        return "redirect:/";
//    }


}
