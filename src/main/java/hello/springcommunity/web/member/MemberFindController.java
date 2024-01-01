package hello.springcommunity.web.member;

import hello.springcommunity.common.validation.ValidationSequence;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.email.FindIdDTO;
import hello.springcommunity.dto.email.FindPasswordDTO;
import hello.springcommunity.dto.member.MemberResponseDTO;
import hello.springcommunity.service.member.MemberAuthService;
import hello.springcommunity.service.member.MemberFindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static hello.springcommunity.common.SessionConst.FIND_MEMBER;
import static hello.springcommunity.common.SessionConst.FIND_MEMBER_PASSWORD;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberFindController {

    private final MemberFindService memberFindService;
    private final MemberAuthService memberAuthService;

    /**
     * 아이디 찾기
     */
    @GetMapping("/find/id")
    public String findIdForm(HttpServletRequest request, Model model) {
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if(inputFlashMap != null && inputFlashMap.get("prevEmailDTO") != null) {
            FindIdDTO prevFindIdDTO = (FindIdDTO) inputFlashMap.get("prevEmailDTO");
            model.addAttribute("findIdDTO", prevFindIdDTO);

        } else {
            model.addAttribute("findIdDTO", new FindIdDTO());
        }

        return "member/findId";
    }


    @PostMapping("/find/id")
    public String findId(@Validated(ValidationSequence.class) FindIdDTO findIdDTO,
                               BindingResult bindingResult,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {


        //유효성 검사
        boolean result = handlingBindingResult(bindingResult, redirectAttributes);
        if(result) {
            redirectAttributes.addFlashAttribute("prevEmailDTO", findIdDTO);
            return "redirect:/find/id";
        }

        try {
            String email = findIdDTO.getEmail();
            MemberResponseDTO member = memberFindService.getMemberDtoByEmail(email);
            boolean authenticated = memberAuthService.isAuthenticated(member.getId());
            //이메일 미인증 회원은 서비스를 이용할 수 없다
            if(!authenticated) {
                redirectAttributes.addFlashAttribute("valid_email", "이메일이 인증되지 않았습니다.");
                redirectAttributes.addFlashAttribute("prevEmailDTO", findIdDTO);
                return "redirect:/find/id";
            }

            //소셜 계정으로 가입한 사람인지 검사
            if("ROLE_SOCIAL".equals(member.getRole())) {
                redirectAttributes.addFlashAttribute("valid_social", "소셜계정으로 가입한 회원은 해당 사이트를 이용해주세요.");

            } else {
                //아이디 찾기 성공
                if(session.getAttribute(FIND_MEMBER) != null) {
                    session.removeAttribute(FIND_MEMBER);
                }

                session.setAttribute(FIND_MEMBER, member);
                return "redirect:/find/Id_result";
            }

        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("valid_email", "가입된 계정이 없습니다.");
        }

        redirectAttributes.addFlashAttribute("prevEmailDTO", findIdDTO);
        return "redirect:/find/id";
    }


    /**
     * 아이디 찾기 결과
     */
    @GetMapping("/find/Id_result")
    public String findIdResult(HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(false);

        if(session == null || session.getAttribute(FIND_MEMBER) == null) {
            return "redirect:/find/id?session=invalid";
        }

        MemberResponseDTO member = (MemberResponseDTO) session.getAttribute(FIND_MEMBER);
        if(member != null) {
            //log.info("findId={}", member.getLoginId());
            model.addAttribute("findMember", member);
        }

        return "member/findIdResult";

    }


    /**
     * 비밀번호 찾기
     */
    @GetMapping("/find/password")
    public String findPasswordForm(HttpServletRequest request, Model model) {

        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if(inputFlashMap != null && inputFlashMap.get("prevFindPasswordDTO") != null) {
            FindPasswordDTO passwordDTO = (FindPasswordDTO) inputFlashMap.get("prevFindPasswordDTO");
            model.addAttribute("findPasswordDTO", passwordDTO);

        } else {
            model.addAttribute("findPasswordDTO", new FindPasswordDTO());
        }

        return "member/findPassword";
    }

    @PostMapping("/find/password")
    public String findPassword(@Validated(ValidationSequence.class) FindPasswordDTO findPasswordDTO,
                                BindingResult bindingResult,
                                HttpSession session,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        //유효성 검사
        boolean result = handlingBindingResult(bindingResult, redirectAttributes);
        if(result) {
            redirectAttributes.addFlashAttribute("prevFindPasswordDTO", findPasswordDTO);
            return "redirect:/find/password";
        }

        try {
            String userMail = findPasswordDTO.getEmail();
            Member member = memberFindService.getMemberByEmail(userMail);
            boolean authenticated = memberAuthService.isAuthenticated(member.getId());
            //이메일 미인증 회원은 서비스를 이용할 수 없다
            if(!authenticated) {
                redirectAttributes.addFlashAttribute("valid_email", "이메일이 인증되지 않았습니다.");
                redirectAttributes.addFlashAttribute("prevFindPasswordDTO", findPasswordDTO);
                return "redirect:/find/password";
            }

            //아이디 검사
            if(!member.getLoginId().equals(findPasswordDTO.getLoginId())) {
                redirectAttributes.addFlashAttribute("valid_loginId", "잘못된 아이디 입력입니다. 다시 확인해주세요.");
                redirectAttributes.addFlashAttribute("prevFindPasswordDTO", findPasswordDTO);
                return "redirect:/find/password";
            }

            //소셜 계정으로 가입한 사람인지 검사
            if("ROLE_SOCIAL".equals(member.getRoleValue())) {
                redirectAttributes.addFlashAttribute("valid_social", "소셜계정으로 가입한 회원은 해당 사이트를 이용해주세요.");
                redirectAttributes.addFlashAttribute("prevFindPasswordDTO", findPasswordDTO);
                return "redirect:/find/password";
            }

            //임시 비밀번호 발송
            memberFindService.sendPassword(member, userMail);

            if(session.getAttribute(FIND_MEMBER_PASSWORD) != null) {
                session.removeAttribute(FIND_MEMBER_PASSWORD);
            }
            session.setAttribute(FIND_MEMBER_PASSWORD, findPasswordDTO);
            return "redirect:/find/Pwd_result";


        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("valid_email", "가입된 계정이 없습니다.");

        } catch (RuntimeException e) {
            model.addAttribute("msg", "비밀번호 찾기에 실패하였습니다.");
            return "error/redirect";
        }

        redirectAttributes.addFlashAttribute("prevFindPasswordDTO", findPasswordDTO);
        return "redirect:/find/password";

    }


    /**
     * 비밀번호 찾기 결과
     */
    @GetMapping("/find/Pwd_result")
    public String findPasswordResult(HttpServletRequest request, Model model) {

        HttpSession session = request.getSession(false);

        if(session == null) {
            return "redirect:/find/password?session=invalid";
        }

        FindPasswordDTO dto = (FindPasswordDTO) session.getAttribute(FIND_MEMBER_PASSWORD);
        if(dto != null) {
            model.addAttribute("email", dto.getEmail());
        }


        return "member/findPwdResult";

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

}
