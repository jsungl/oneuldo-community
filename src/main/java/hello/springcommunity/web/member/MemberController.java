package hello.springcommunity.web.member;

import hello.springcommunity.config.oauth.DisConnectOauth2UserService;
import hello.springcommunity.config.oauth.UserSessionDTO;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.comment.CommentResponseDTO;
import hello.springcommunity.dto.member.MemberLeaveResponseDTO;
import hello.springcommunity.dto.post.PostResponseDTO;
import hello.springcommunity.dto.security.UserDetailsDTO;
import hello.springcommunity.exception.CustomRuntimeException;
import hello.springcommunity.service.comment.CommentServiceImpl;
import hello.springcommunity.service.member.MemberService;
import hello.springcommunity.dto.member.MemberResponseDTO;
import hello.springcommunity.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static hello.springcommunity.common.SessionConst.OAUTH2_MEMBER;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final DisConnectOauth2UserService disConnectOauth2UserService;
    private final CommentServiceImpl commentService;
    private final PostService postService;



    /**
     * 회원 정보 상세 - 아이디, 닉네임
     */
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetailsDTO dto, Model model) {
        try {
            MemberResponseDTO member = memberService.getMemberDtoByLoginId(dto.getUsername());
            model.addAttribute("member", member);
            model.addAttribute("title", "회원 정보");

        } catch (RuntimeException e) {
            throw new CustomRuntimeException("회원정보를 조회할 수 없습니다.");
        }

        return "member/memberProfile";


    }

    /**
     * 회원 작성 게시물
     */
    @GetMapping("/myDocument")
    public String myDocument(@AuthenticationPrincipal UserDetailsDTO dto,
                             @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable,
                             Model model) {

        try {
            Member member = memberService.getMemberByLoginId(dto.getUsername());
            Page<PostResponseDTO> posts = postService.getMemberAllPost(member.getId(), pageable);

            model.addAttribute("posts", posts);
            model.addAttribute("title", "내 게시물");
            

        } catch (RuntimeException e) {
            throw new CustomRuntimeException("게시물을 조회할 수 없습니다.");
        }

        return "member/memberOwnDocument";

    }

    /**
     * 회원 작성 댓글
     */
    @GetMapping("/myComment")
    public String myComment(@AuthenticationPrincipal UserDetailsDTO dto,
                            @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable,
                            Model model) {

        try {
            Member member = memberService.getMemberByLoginId(dto.getUsername());
            Page<CommentResponseDTO> comments = commentService.getMemberCommentAll(member.getId(), pageable);

            model.addAttribute("comments", comments);
            model.addAttribute("title", "내 댓글");
            

        } catch (RuntimeException e) {
            throw new CustomRuntimeException("댓글을 조회할 수 없습니다.");
        }

        return "member/memberOwnComment";

    }


    /**
     * 회원 탈퇴
     */
    @GetMapping("/leave")
    public String leaveForm(Model model) {
        model.addAttribute("title", "회원탈퇴");
        return "member/memberLeave";
    }

    /**
     * 비밀번호 하나만 입력받기 때문에 따로 DTO 를 통해 받지 않고, @RequestParam 을 통해 받는다
     * 회원탈퇴에 성공하면 로그아웃 시킨다
     */
    @PostMapping("/leave")
    public String leave(@AuthenticationPrincipal UserDetailsDTO dto, @RequestParam(required = false) String currentPassword,
                        HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {

        try {

            if("ROLE_ADMIN".equals(dto.getMember().getRoleValue())) {
                model.addAttribute("msg", "잘못된 접근입니다.");
                return "error/redirect";
            }

            Member member = memberService.getMemberByLoginId(dto.getUsername());


            if("ROLE_SOCIAL".equals(member.getRoleValue())) {
                /** 소셜 로그인 유저 **/
                //UserSessionDTO userSessionDTO = (UserSessionDTO) httpSession.getAttribute(OAUTH2_MEMBER);
                HttpSession session = request.getSession(false);
                UserSessionDTO userSessionDTO = (UserSessionDTO) session.getAttribute(OAUTH2_MEMBER);

                //세션에 저장된 회원정보가 없거나 AccessToken이 없는경우 로그아웃
                if(userSessionDTO == null || !StringUtils.hasText(userSessionDTO.getAccessToken())) {
                    model.addAttribute("msg", "세션정보가 유효하지 않아 회원탈퇴에 실패하였습니다.");
                    return "member/procLogout";
                }

                //Boolean result = disConnectOauth2UserService.disConnectUser(userSessionDTO, member.getId());
                MemberLeaveResponseDTO memberLeaveResponseDTO = disConnectOauth2UserService.disConnectUser(userSessionDTO, member.getId());

                //회원탈퇴 실패
                if(!memberLeaveResponseDTO.isState()) {
                    //갱신 토큰이 만료 X -> 탈퇴화면으로 다시이동
                    if(!memberLeaveResponseDTO.isTokenExpired()) {
                        redirectAttributes.addFlashAttribute("msg", memberLeaveResponseDTO.getMessage());
                        return "redirect:/member/leave";
                    }
                }
                //회원탈퇴 성공 -> 로그아웃
                session.removeAttribute("oauth2member");
                model.addAttribute("msg", memberLeaveResponseDTO.getMessage());
                return "member/procLogout";

            } else {
                /** 일반 로그인 유저 **/
                boolean result = memberService.deleteMember(member, currentPassword);

                if(result) {
                    //탈퇴 성공
                    model.addAttribute("msg", "회원 탈퇴 처리 되었습니다. 지금까지 이용해주셔서 감사합니다.");
                    return "member/procLogout";

                } else {
                    redirectAttributes.addFlashAttribute("valid_currentPassword", "비밀번호가 틀렸습니다.");
                    return "redirect:/member/leave";
                }

            }

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msg", "회원탈퇴에 실패하였습니다.");
            return "redirect:/member/leave";
        }

    }

}
