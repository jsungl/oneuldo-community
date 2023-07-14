package hello.springcommunity.web.member;

import hello.springcommunity.domain.member.MemberService;
import hello.springcommunity.domain.member.MemberRepositoryOld;
import hello.springcommunity.domain.validation.ValidationSequence;
import hello.springcommunity.web.member.form.MemberSaveForm;
import hello.springcommunity.web.validator.CheckIdValidator;
import hello.springcommunity.web.validator.CheckNameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberRepositoryOld memberRepositoryOld;
//    private final MemberService memberService;
    private final MemberService memberService;
    private final CheckIdValidator checkIdValidator;
    private final CheckNameValidator checkNameValidator;

    /**
     * InitBinder : 특정 컨트롤러에서 바인딩 또는 검증 설정을 변경하고 싶을 때 사용
     * WebDataBinder binder : HTTP 요청 정보를 컨트롤러 메소드의 파라미터나 모델에 바인딩할 때 사용되는 바인딩 객체. 스프링의 파라미터 바인딩의 역할을 해주고 검증 기능도 내부에 포함한다
     * WebDataBinder 에 검증기를 추가하면 해당 컨트롤러(MemberController) 에서는 검증기를 자동으로 적용할 수 있다
     * 해당 컨트롤러에만 영향을 주고 글로벌 설정은 별도로 해야한다
     * @param dataBinder
     */
    @InitBinder
    public void init(WebDataBinder dataBinder) {
        log.info("init binder {}", dataBinder);
        dataBinder.addValidators(checkIdValidator);
        dataBinder.addValidators(checkNameValidator);
    }

    /**
     * 회원가입
     */
    @GetMapping("/add")
    public String addForm(@ModelAttribute MemberSaveForm memberSaveForm) {
        return "members/addMemberForm";
    }

    @PostMapping("/add")
    public String save(@Validated(ValidationSequence.class) @ModelAttribute MemberSaveForm memberSaveForm, BindingResult result, Model model) {

        if (result.hasErrors()) {
            /* 유효성 검사를 통과하지 못 한 필드와 메시지 핸들링 */
            Map<String, String> errorMap = new HashMap<>();
            for(FieldError error : result.getFieldErrors()) {
                errorMap.put("valid_"+error.getField(), error.getDefaultMessage());
                log.info("회원가입 실패 ! error message : "+error.getDefaultMessage());
            }
            /* Model에 담아 view resolve */
            for(String key : errorMap.keySet()) {
                log.info("key={}, errors={}", key, errorMap.get(key));
                model.addAttribute(key, errorMap.get(key));
            }
            /* 회원가입 페이지로 리턴 */
            return "members/addMemberForm";
        }

        //성공로직
//        Member member = Member.builder()
//                .loginId(memberSaveForm.getLoginId())
//                .password(memberSaveForm.getPassword())
//                .name(memberSaveForm.getName())
//                .build();

//        memberService.join(member);


        memberService.join(memberSaveForm);
        log.info("signup success");

        return "redirect:/login";
    }


}
