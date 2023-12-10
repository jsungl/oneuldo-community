package hello.springcommunity.web;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dto.comment.CommentResponseDTO;
import hello.springcommunity.dto.member.MemberResponseDTO;
import hello.springcommunity.dto.post.PostResponseDTO;
import hello.springcommunity.service.comment.CommentServiceImpl;
import hello.springcommunity.service.member.MemberService;
import hello.springcommunity.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;
    private final PostService postService;
    private final CommentServiceImpl commentService;

    @GetMapping
    public String info() {
        return "admin/adminInfo";
    }


    /**
     * 전체 회원목록 조회
     */
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/members")
    public String members(Model model, @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MemberResponseDTO> members = memberService.getAllMember(pageable);
        model.addAttribute("members", members);

        return "admin/members";
    }


    /**
     * 회원 프로필 상세 조회
     */
    @GetMapping("/memberProfile")
    public String memberProfile(@RequestParam("memberId") String memberId, Model model) {
        Long id = Long.valueOf(memberId);

        try {
            MemberResponseDTO member = memberService.getMemberById(id);
            model.addAttribute("member", member);
            return "admin/memberProfile";

        } catch (UsernameNotFoundException e) {
            model.addAttribute("msg", "회원정보를 조회할 수 없습니다.");
            return "error/redirect";
        }

    }


    /**
     * 회원이 작성한 게시물
     */
    @GetMapping("/memberDocuments")
    public String memberDocuments(@RequestParam("memberId") String memberId,
                                  @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable,
                                  Model model) {
        try {
            long id = Long.parseLong(memberId);
            MemberResponseDTO member = memberService.getMemberById(id);
            Page<PostResponseDTO> posts = postService.getMemberPostAll(member.getId(), pageable);
            model.addAttribute("nickname", member.getNickname());
            model.addAttribute("posts", posts);
            return "admin/memberDocuments";

        } catch (Exception e) {
            model.addAttribute("msg", "게시물을 조회할 수 없습니다.");
            return "error/redirect";
        }
    }


    /**
     * 회원이 작성한 댓글
     */
    @GetMapping("/memberComments")
    public String memberComments(@RequestParam("memberId") String memberId,
                                 @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable,
                                 Model model) {
        try {
            long id = Long.parseLong(memberId);
            MemberResponseDTO member = memberService.getMemberById(id);
            Page<CommentResponseDTO> comments = commentService.getMemberCommentAll(member.getId(), pageable);
            model.addAttribute("nickname", member.getNickname());
            model.addAttribute("comments", comments);
            return "admin/memberComments";

        } catch (Exception e) {
            model.addAttribute("msg", "댓글을 조회할 수 없습니다.");
            return "error/redirect";
        }

    }

}
