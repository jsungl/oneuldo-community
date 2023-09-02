package hello.springcommunity.web;

import hello.springcommunity.common.validation.ValidationSequence;
import hello.springcommunity.dto.email.EmailDTO;
import hello.springcommunity.dto.email.EmailResponseDTO;
import hello.springcommunity.dto.member.MemberResponseDTO;
import hello.springcommunity.dto.post.PostResponseDTO;
import hello.springcommunity.service.member.MemberService;
import hello.springcommunity.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GlobalController {

    private final PostService postService;
    private final MemberService memberService;
    private final HttpSession httpSession;

    /**
     * 게시물 목록 - 페이징
     * @PageableDefault 어노테이션을 이용하여 정렬 순서, 사이즈 등의 정보를 넣고 해당객체를 서비스에 파라미터로 전달
     */
    @GetMapping("/posts")
    public String posts(Model model, @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable) {

        Page<PostResponseDTO> posts = postService.findAll(pageable);

        model.addAttribute("posts", posts);

        return "post/posts";
    }

    /**
     * 관리자 페이지 - 회원목록
     */
    @GetMapping("/admin/members")
    public String getAllMembers(Model model) {
        List<MemberResponseDTO> members = memberService.findAll();
        model.addAttribute("members", members);

        return "admin/members";
    }

    /**
     * 메일인증 코드 전송
     */
    @PostMapping("/api/member/verifyMail")
    public String sendVerifyCode(@Validated(ValidationSequence.class) @RequestBody EmailDTO emailDTO, BindingResult result, Model model) {
        
        if (result.hasErrors()) {
            /* 유효성 검사를 통과하지 못 한 필드와 메시지 핸들링 */
            Map<String, String> errorMap = new HashMap<>();
            for(FieldError error : result.getFieldErrors()) {
                errorMap.put("valid_"+error.getField(), error.getDefaultMessage());
                log.info("이메일 유효성 검사 실패 ! error message : "+error.getDefaultMessage());
            }
            /* Model에 담아 view resolve */
            for(String key : errorMap.keySet()) {
                model.addAttribute(key, errorMap.get(key));
            }

        }else {
            //성공로직
            //이메일 보내기
            String code = memberService.mailConfirm(emailDTO.getEmail(), "이메일 인증 메일입니다.", "mail/email");

            if(httpSession.getAttribute("emailCode") != null) {
                httpSession.removeAttribute("emailCode");
            }
            httpSession.setAttribute("emailCode", code);
            model.addAttribute("email_send", true);
            log.info("메일 전송 성공");
            
        }


        model.addAttribute("email", emailDTO.getEmail());
        return "member/addMemberForm :: #memberEmail";

    }


    /**
     * 인증코드 검증
     */
    @PostMapping("/api/member/verifyCode")
    public String verifyMailCode(@RequestBody EmailResponseDTO emailResponseDTO, Model model) {

        String code = (String) httpSession.getAttribute("emailCode");

        //인증코드 검증
        if(code != null && emailResponseDTO.getCode().equals(code)) {
            log.info("인증 성공");
            model.addAttribute("check", true);
            model.addAttribute("msg", "인증완료!");
            model.addAttribute("result", true); //회원가입 가능
            //세션 삭제
            httpSession.removeAttribute("emailCode");
        }else {
            log.info("인증 실패");
            model.addAttribute("check", false);
            model.addAttribute("msg", "잘못된 인증코드입니다. 다시 확인해주세요");
        }

        model.addAttribute("code", emailResponseDTO.getCode());
        return "member/addMemberForm :: #validationEmail";
        // return ResponseEntity.ok("ok");
    }
}
