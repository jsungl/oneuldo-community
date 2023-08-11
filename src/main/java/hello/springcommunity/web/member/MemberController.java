package hello.springcommunity.web.member;

import hello.springcommunity.dto.member.MemberNicknameUpdateDTO;
import hello.springcommunity.dto.member.MemberPwdUpdateDTO;
import hello.springcommunity.dto.member.MemberSaveRequestDTO;
import hello.springcommunity.service.member.MemberService;
import hello.springcommunity.common.validation.ValidationSequence;
import hello.springcommunity.dto.member.MemberResponseDTO;
import hello.springcommunity.common.validation.CheckIdValidator;
import hello.springcommunity.common.validation.CheckNicknameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final CheckIdValidator checkIdValidator;
    private final CheckNicknameValidator checkNicknameValidator;

    /**
     * InitBinder : 특정 컨트롤러에서 바인딩 또는 검증 설정을 변경하고 싶을 때 사용
     * WebDataBinder binder : HTTP 요청 정보를 컨트롤러 메소드의 파라미터나 모델에 바인딩할 때 사용되는 바인딩 객체. 스프링의 파라미터 바인딩의 역할을 해주고 검증 기능도 내부에 포함한다
     * WebDataBinder 에 검증기를 추가하면 해당 컨트롤러(MemberController) 에서는 검증기를 자동으로 적용할 수 있다
     * 해당 컨트롤러에만 영향을 주고 글로벌 설정은 별도로 해야한다
     *
     * 여러 모델 객체를 사용할 때 @InitBinder 에 이름을 넣어주면 해당 모델 객체에만 영향을 준다
     * 반면 이름을 넣지않으면 모든 모델 객체에 영향을 준다
     * 지금은 MemberSaveForm 객체에만 검증기 작동
     */
    @InitBinder("memberSaveRequestDTO")
    public void init(WebDataBinder dataBinder) {
        log.info("init binder {}", dataBinder);
        dataBinder.addValidators(checkIdValidator);
        dataBinder.addValidators(checkNicknameValidator);
    }

    /**
     * 회원가입
     */
    @GetMapping("/add")
    public String addForm(@ModelAttribute MemberSaveRequestDTO memberSaveRequestDTO) {
        return "member/addMemberForm";
    }


    @PostMapping("/add")
    public String addMember(@Validated(ValidationSequence.class) @ModelAttribute MemberSaveRequestDTO memberSaveRequestDTO, BindingResult result, Model model) {

        if (result.hasErrors()) {
            /* 유효성 검사를 통과하지 못 한 필드와 메시지 핸들링 */
            Map<String, String> errorMap = new HashMap<>();
            for(FieldError error : result.getFieldErrors()) {
                errorMap.put("valid_"+error.getField(), error.getDefaultMessage());
                log.info("회원가입 실패 ! error message : "+error.getDefaultMessage());
            }
            /* Model에 담아 view resolve */
            for(String key : errorMap.keySet()) {
//                log.info("key={}, errors={}", key, errorMap.get(key));
                model.addAttribute(key, errorMap.get(key));
            }
            /* 회원가입 페이지로 리턴 */
            return "member/addMemberForm";
        }

        memberService.join(memberSaveRequestDTO);
        log.info("회원가입 성공");

        return "redirect:/login";
    }


    /**
     * 마이페이지 - 회원 정보 수정, 회원 탈퇴
     */
    @GetMapping("/{loginId}/info")
    public String info(@PathVariable String loginId, Model model) {
        model.addAttribute("loginId", loginId);
        return "member/info";
    }


    /**
     * 회원 정보 상세 - 아이디, 닉네임
     */
    @GetMapping("/{loginId}/profile")
    public String profile(@PathVariable String loginId, Model model) {
        MemberResponseDTO member = memberService.getMemberByLoginId(loginId);
        model.addAttribute("member", member);
        return "member/profile";
    }

    /**
     * 회원 닉네임 수정
     */
    @GetMapping("/{loginId}/updateNickname")
    public String updateNicknameForm(@PathVariable String loginId, Model model) {

//        Member member = memberService.findOne(memberId).orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다"));
//        MemberNameUpdateForm memberNameUpdateForm = new MemberNameUpdateForm(member.getLoginId(), member.getName());

        MemberResponseDTO member = memberService.getMemberByLoginId(loginId);
        MemberNicknameUpdateDTO dto = new MemberNicknameUpdateDTO(member.getLoginId(), member.getNickname());

        model.addAttribute("memberNicknameUpdateDTO", dto);
        model.addAttribute("loginId", loginId);
        return "member/editNicknameForm";
    }

    @PostMapping("/{loginId}/updateNickname")
    public String updateNickname(@Validated @ModelAttribute MemberNicknameUpdateDTO memberNicknameUpdateDTO,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes,
                                 @PathVariable String loginId) {


        if(result.hasErrors()) {

            /* 유효성 검사를 통과하지 못 한 필드와 메시지 핸들링 */
            Map<String, String> errorMap = new HashMap<>();
            for(FieldError error : result.getFieldErrors()) {
                errorMap.put("valid_"+error.getField(), error.getDefaultMessage());
                log.info("유저 닉네임 수정 실패 ! error message : "+error.getDefaultMessage());
            }
            /* Model에 담아 view resolve */
            for(String key : errorMap.keySet()) {
//                log.info("key={}, errors={}", key, errorMap.get(key));
                model.addAttribute(key, errorMap.get(key));
            }
            /* 닉네임 수정 페이지로 리턴 */
            return "member/editMemberNameForm";
        }

        memberService.updateMemberNickname(memberNicknameUpdateDTO);
        redirectAttributes.addAttribute("loginId", loginId);

        log.info("닉네임 수정 완료");
        return "redirect:/member/{loginId}/profile";
    }


    /**
     * 회원 비밀번호 수정
     */
    @GetMapping("/{loginId}/updatePassword")
    public String updatePasswordForm(@PathVariable String loginId, @ModelAttribute MemberPwdUpdateDTO memberPwdUpdateDTO, Model model) {
        model.addAttribute("loginId", loginId);
        return "member/editPasswordForm";
    }

    @PostMapping("/{loginId}/updatePassword")
    public String updatePassword(@PathVariable String loginId,
                                 @Validated @ModelAttribute MemberPwdUpdateDTO memberPwdUpdateDTO,
                                 BindingResult bindingResult,
                                 Model model) {

        //newPassword, rePassword 비교
        //Objects.equals : null을 포함하여 비교, NPE 방지
        if(!Objects.equals(memberPwdUpdateDTO.getNewPassword(), memberPwdUpdateDTO.getRePassword())) {
            model.addAttribute("valid_rePassword", "비밀번호가 같지 않습니다.");
            return "/member/editPasswordForm";
        }

        //newPassword 유효성 검사
        if(bindingResult.hasErrors()) {

            Map<String, String> errorMap = new HashMap<>();

            for(FieldError error : bindingResult.getFieldErrors()) {
                errorMap.put("valid_"+error.getField(), error.getDefaultMessage());
                log.info("유저 비밀번호 수정 실패 ! error message : "+error.getDefaultMessage());
            }

            for(String key : errorMap.keySet()) {
//                log.info("key={}, errors={}", key, errorMap.get(key));
                model.addAttribute(key, errorMap.get(key));
            }

            return "member/editPasswordForm";
        }

        Boolean result = memberService.updateMemberPassword(memberPwdUpdateDTO, loginId);

        //기존 비밀번호가 틀린경우
        if(!result) {
            model.addAttribute("valid_currentPassword", "비밀번호가 틀렸습니다.");
            return "member/editPasswordForm";
        }

        log.info("비밀번호 수정 완료");
        return "redirect:/member/{loginId}/profile";

    }

    /**
     * 회원 탈퇴
     */
    @GetMapping("/{loginId}/withdrawal")
    public String withdrawalForm(@PathVariable String loginId, Model model) {
        model.addAttribute("loginId", loginId);
        return "member/withdrawal";
    }

    /**
     * 비밀번호 하나만 입력받기 때문에 따로 DTO 를 통해 받지 않고, @RequestParam 을 통해 받는다
     * 회원탈퇴에 성공하면 로그아웃 시킨다
     */
    @PostMapping("/{loginId}/withdrawal")
    public String withdrawal(@PathVariable String loginId, @RequestParam String currentPassword, Model model) {
        boolean result = memberService.withdrawal(loginId, currentPassword);

        if(result) {
            //탈퇴 성공
            log.info("회원 탈퇴 완료");
            return "redirect:/logout";
        }else {
            model.addAttribute("loginId", loginId);
            model.addAttribute("valid_currentPassword", "비밀번호가 틀렸습니다.");
            return "member/withdrawal";
        }
    }

}
