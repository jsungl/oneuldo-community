package hello.springcommunity.web;

import hello.springcommunity.common.validation.*;
import hello.springcommunity.domain.post.CategoryCode;
import hello.springcommunity.dto.login.LoginRequestDTO;
import hello.springcommunity.dto.member.MemberSaveRequestDTO;
import hello.springcommunity.dto.post.PostResponseDTO;
import hello.springcommunity.service.member.MemberAuthService;
import hello.springcommunity.service.member.MemberService;
import hello.springcommunity.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static hello.springcommunity.common.validation.SortAndCategoryValidator.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GlobalController {

    private final PostService postService;
    private final MemberService memberService;
    private final MemberAuthService memberAuthService;
    private final CheckIdValidator checkIdValidator;
    private final CheckNicknameValidator checkNicknameValidator;
    private final CheckMailValidator checkMailValidator;


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
        log.info("memberSaveRequestDTO init binder={}", dataBinder);
        dataBinder.addValidators(checkIdValidator);
        dataBinder.addValidators(checkNicknameValidator);
        dataBinder.addValidators(checkMailValidator);
    }


    /**
     * 게시물 목록 - 페이징
     * @PageableDefault 어노테이션을 이용하여 정렬 순서, 사이즈 등의 정보를 넣고 해당객체를 서비스에 파라미터로 전달
     */
    @GetMapping("/posts")
    public String posts(@RequestParam(value = "page", required = false, defaultValue = "0") int pageNo,
                        @RequestParam(value = "sort_index", required = false, defaultValue = "id") String sortIndex,
                        @RequestParam(value = "category", required = false) String category,
                        Model model) {


        /** 첫 페이지는 공지 목록 가져오기 **/
        if(pageNo == 0) {
            List<PostResponseDTO> topNotice = postService.getTopNotice();
            model.addAttribute("topNotice", topNotice);
        }

        CategoryCode categoryCode = checkCategory(category);
        String sort = checkSort(sortIndex);

        if(categoryCode == null) {
            Page<PostResponseDTO> posts = postService.getAllPost(sort, pageNo);
            model.addAttribute("posts", posts);

        } else {
            Page<PostResponseDTO> posts = postService.getAllPostWithCategory(sort, categoryCode, pageNo);
            model.addAttribute("listCategory", categoryCode.name());
            model.addAttribute("posts", posts);
        }

        model.addAttribute("sort_index", sort);
        return "post/posts";

    }


    /**
     * 로그인 페이지
     */
    //@PreAuthorize("isAnonymous()")
    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") LoginRequestDTO form,
                            HttpServletRequest request,
                            Model model) {

        // 요청 시점의 사용자 URI 정보를 Session의 Attribute에 담아서 전달(잘 지워줘야함)
        // 로그인이 틀려서 다시 하면 요청 시점의 URI가 로그인 페이지가 되므로 조건문 설정
        String uri = request.getHeader("Referer");
        if(uri != null && !uri.contains("/login")){
            request.getSession().setAttribute("prevPage", uri);
        }

        if(request.getSession().getAttribute("errorMessage") != null) {
            String msg = (String) request.getSession().getAttribute("errorMessage");
            model.addAttribute("errorMessage", msg);
            request.getSession().removeAttribute("errorMessage");
        }

        return "login/loginForm";
    }


    /**
     * 회원가입
     */
    @GetMapping("/signup")
    public String signupForm(HttpServletRequest request, Model model) {

        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if(inputFlashMap != null) {
            MemberSaveRequestDTO prevMemberRequestDTO = (MemberSaveRequestDTO) inputFlashMap.get("prevMemberSaveReqDTO");
            model.addAttribute("memberSaveRequestDTO", prevMemberRequestDTO);

        } else {
            model.addAttribute("memberSaveRequestDTO", new MemberSaveRequestDTO());
        }

        return "member/addMemberForm";
    }


    @PostMapping("/signup")
    public String signupMember(@Validated(ValidationSequence.class) MemberSaveRequestDTO memberSaveRequestDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request,
                               Model model) {

        //비밀번호 검사
        if(!Objects.equals(memberSaveRequestDTO.getPassword(), memberSaveRequestDTO.getRePassword())) {
            redirectAttributes.addFlashAttribute("prevMemberSaveReqDTO", memberSaveRequestDTO);
            redirectAttributes.addFlashAttribute("valid_rePassword", "비밀번호가 일치하지 않습니다.");
            return "redirect:/signup";
        }

        //유효성 검사
        if(result.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();
            for(FieldError error : result.getFieldErrors()) {
                errorMap.put("valid_"+error.getField(), error.getDefaultMessage());
            }
            for(String key : errorMap.keySet()) {
                redirectAttributes.addFlashAttribute(key, errorMap.get(key));
            }
            redirectAttributes.addFlashAttribute("prevMemberSaveReqDTO", memberSaveRequestDTO);
            return "redirect:/signup";
        }

        try {
            //이메일 인증 경로 생성
            //http://localhost:8081/member/authAccount
            String path = request.getRequestURL().toString().replace(request.getRequestURI(), "/memberAuthMail");
            memberService.join(memberSaveRequestDTO, path);

            redirectAttributes.addFlashAttribute("msg", "가입시 입력한 메일주소로 메일을 보냈습니다. 메일 확인 후 인증버튼을 눌러주세요!");
            return "redirect:/login";

        } catch (RuntimeException e) {
            model.addAttribute("msg", "회원가입에 실패하였습니다.");
            return "error/redirect";
        }
    }


    /**
     * 이메일 인증 - 회원가입, 회원정보 변경
     * 유저 이메일 인증 상태 변경
     */
    @GetMapping("/memberAuthMail")
    public String authMail(@RequestParam String id, @RequestParam String authKey, Model model) {

        try {

            if(!StringUtils.hasText(authKey) || !StringUtils.hasText(id)) {
                return "home";
            }

            Long memberId = Long.valueOf(id);
            boolean result = memberAuthService.verify(memberId, authKey);
            model.addAttribute("result", result);

        } catch (NoSuchElementException e) {
            model.addAttribute("result", false);
            model.addAttribute("msg", "인증시 오류가 발생하였습니다.");
        }

        return "member/procAuthMail";
    }


}
