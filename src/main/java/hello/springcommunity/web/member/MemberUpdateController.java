package hello.springcommunity.web.member;

import hello.springcommunity.common.validation.CheckMailValidator;
import hello.springcommunity.common.validation.CheckNicknameValidator;
import hello.springcommunity.common.validation.ValidationSequence;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.member.MemberProfileUpdateDTO;
import hello.springcommunity.dto.member.MemberPwdUpdateDTO;
import hello.springcommunity.dto.security.UserDetailsDTO;
import hello.springcommunity.service.member.MemberAuthService;
import hello.springcommunity.service.member.MemberService;
import hello.springcommunity.service.member.MemberUpdateService;
import hello.springcommunity.service.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberUpdateController {

    private final MemberService memberService;
    private final MemberUpdateService memberUpdateService;
    private final UserDetailsServiceImpl userDetailsService;
    private final CheckNicknameValidator checkNicknameValidator;
    private final CheckMailValidator checkMailValidator;

    /**
     * 닉네임, 이메일 중복검사
     */
    @InitBinder("memberProfileUpdateDTO")
    public void init(WebDataBinder dataBinder) {
        log.info("memberProfileUpdateDTO init binder={}", dataBinder);
        dataBinder.addValidators(checkNicknameValidator);
        dataBinder.addValidators(checkMailValidator);
    }

    /**
     * 회원 닉네임,이메일 수정
     */
    @GetMapping("/edit/profile")
    public String updateProfileForm(@AuthenticationPrincipal UserDetailsDTO dto, HttpServletRequest request, Model model) {

        try {
            Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
            if(inputFlashMap != null && inputFlashMap.get("prevProfileUpdateDTO") != null) {
                MemberProfileUpdateDTO updateDTO = (MemberProfileUpdateDTO) inputFlashMap.get("prevProfileUpdateDTO");
                model.addAttribute("memberProfileUpdateDTO", updateDTO);

            } else {
                Member member = memberService.getMemberByLoginId(dto.getUsername());
                MemberProfileUpdateDTO updateDTO = new MemberProfileUpdateDTO(member.getLoginId(), member.getNickname(), member.getEmail());

                model.addAttribute("memberProfileUpdateDTO", updateDTO);
            }

            return "member/editProfileForm";

        } catch (UsernameNotFoundException e) {
            model.addAttribute("msg", "회원정보를 변경할 수 없습니다.");
            return "error/redirect";
        }

    }


    @PostMapping("/edit/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetailsDTO dto,
                                @Validated(ValidationSequence.class) MemberProfileUpdateDTO memberProfileUpdateDTO,
                                BindingResult bindingResult,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        //닉네임 유효성 검사
        boolean result = handlingBindingResult(bindingResult, redirectAttributes);
        if(result) {
            redirectAttributes.addFlashAttribute("prevProfileUpdateDTO", memberProfileUpdateDTO);
            return "redirect:/edit/profile";
        }

        try {
            String newEmail = memberProfileUpdateDTO.getEmail();
            String newNickname = memberProfileUpdateDTO.getNickname();

            Member member = memberService.getMemberByLoginId(dto.getUsername());
            //기존 닉네임,이메일
            String oldNickname = member.getNickname();
            String oldEmail = member.getEmail();

            //변경사항 없음
            if(oldNickname.equals(newNickname) && oldEmail.equals(newEmail)) {
                redirectAttributes.addFlashAttribute("msg", "변경된 정보가 없습니다.");
                return "redirect:/member/profile";
            }

            String path = request.getRequestURL().toString().replace(request.getRequestURI(), "/memberAuthMail");
            //회원정보 변경
            Member updatedMember = memberUpdateService.updateMemberProfile(memberProfileUpdateDTO, member, path);

            //시큐리티 세션 재설정
            resetSecuritySession(updatedMember);

            redirectAttributes.addFlashAttribute("msg", "회원정보가 변경되었습니다. 메일을 변경했을 경우 인증이 완료되어야 로그인이 가능합니다.");
            return "redirect:/member/profile";

        } catch (RuntimeException e) {
            model.addAttribute("msg", "회원정보 변경에 실패하였습니다.");
            return "error/redirect";
        }


    }


    /**
     * 회원 비밀번호 수정
     */
    @GetMapping("/edit/password")
    public String updatePasswordForm(HttpServletRequest request, Model model) {
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if(inputFlashMap != null && inputFlashMap.get("prevPwdUpdateDTO") != null) {
            MemberPwdUpdateDTO memberPwdUpdateDTO = (MemberPwdUpdateDTO) inputFlashMap.get("prevPwdUpdateDTO");
            model.addAttribute("memberPwdUpdateDTO", memberPwdUpdateDTO);

        } else {
            model.addAttribute("memberPwdUpdateDTO", new MemberPwdUpdateDTO());
        }

        return "member/editPasswordForm";
    }

    @PostMapping("/edit/password")
    public String updatePassword(@AuthenticationPrincipal UserDetailsDTO dto,
                                 @Validated(ValidationSequence.class) MemberPwdUpdateDTO memberPwdUpdateDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        try {

            //newPassword, rePassword 비교
            //Objects.equals : null을 포함하여 비교, NPE 방지
            if(!Objects.equals(memberPwdUpdateDTO.getNewPassword(), memberPwdUpdateDTO.getRePassword())) {
                redirectAttributes.addFlashAttribute("prevPwdUpdateDTO", memberPwdUpdateDTO);
                redirectAttributes.addFlashAttribute("valid_rePassword", "비밀번호가 일치하지 않습니다.");
                return "redirect:/edit/password";
            }

            //새 비밀번호 유효성 검사
            boolean verificationResult = handlingBindingResult(bindingResult, redirectAttributes);
            if(verificationResult) {
                redirectAttributes.addFlashAttribute("prevPwdUpdateDTO", memberPwdUpdateDTO);
                return "redirect:/edit/password";
            }

            //비밀번호 변경
            Member member = memberUpdateService.updateMemberPassword(memberPwdUpdateDTO, dto.getUsername());

            //기존 비밀번호가 틀린경우
            if(member == null) {
                redirectAttributes.addFlashAttribute("prevPwdUpdateDTO", memberPwdUpdateDTO);
                redirectAttributes.addFlashAttribute("valid_currentPassword", "비밀번호가 틀렸습니다.");
                return "redirect:/edit/password";
            }

            resetSecuritySession(member);

            redirectAttributes.addFlashAttribute("msg", "비밀번호가 변경되었습니다.");
            return "redirect:/member/profile";

        } catch (UsernameNotFoundException e) {
            model.addAttribute("msg", "비밀번호 변경에 실패하였습니다.");
            return "error/redirect";
        }


    }


    /**
     * 유효성 검사 처리
     * 유효성 검사를 통과하지 못 한 필드와 메시지 핸들링
     */
    private boolean handlingBindingResult(BindingResult result, RedirectAttributes redirectAttributes) {

        if(result.hasErrors()) {

            Map<String, String> errorMap = new HashMap<>();
            for(FieldError error : result.getFieldErrors()) {
                errorMap.put("valid_"+error.getField(), error.getDefaultMessage());
            }
            /* Model에 담아 view resolve */
            for(String key : errorMap.keySet()) {
                redirectAttributes.addFlashAttribute(key, errorMap.get(key));
            }
            return true;
        }

        return false;
    }


    /**
     * 시큐리티 세션 재설정(사용자 정보 변경시 변경된 정보 반영)
     * 서비스에서 트랜잭션 종료 후 실행되는 컨트롤러에서 설정
     */
    private void resetSecuritySession(Member member) {
        UserDetails userDetails = userDetailsService.getUserDetails(member);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
