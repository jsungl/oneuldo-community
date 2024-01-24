package hello.springcommunity.web;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.post.CategoryCode;
import hello.springcommunity.dto.comment.CommentResponseDTO;
import hello.springcommunity.dto.member.MemberResponseDTO;
import hello.springcommunity.dto.post.PostResponseDTO;
import hello.springcommunity.dto.security.UserDetailsDTO;
import hello.springcommunity.service.comment.CommentServiceImpl;
import hello.springcommunity.service.member.MemberService;
import hello.springcommunity.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


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
     * 관리자가 작성한 게시물
     */
    @GetMapping("/myDocument")
    public String myDocument(@AuthenticationPrincipal UserDetailsDTO dto,
                             @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable,
                             Model model) {

        try {
            Member member = memberService.getMemberByLoginId(dto.getUsername());
            Page<PostResponseDTO> posts = postService.getMemberAllPost(member.getId(), pageable);

            model.addAttribute("posts", posts);
            return "admin/adminOwnDocument";

        } catch (UsernameNotFoundException e) {
            model.addAttribute("msg", "게시물을 조회할 수 없습니다.");
            return "error/redirect";
        }

    }


    /**
     * 회원 프로필 상세 조회
     */
    @GetMapping("/memberProfile")
    public String memberProfile(@RequestParam("memberId") String memberId, Model model) {
        Long id = Long.valueOf(memberId);

        try {
            MemberResponseDTO member = memberService.getMemberById(id);

            if(!member.isActivated()) {
                model.addAttribute("msg", "이미 탈퇴된 회원입니다.");
                return "error/redirect";
            }

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
    @GetMapping("/memberDocument")
    public String memberDocument(@RequestParam("memberId") String memberId,
                                  @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable,
                                  Model model) {
        try {
            long id = Long.parseLong(memberId);
            MemberResponseDTO member = memberService.getMemberById(id);
            Page<PostResponseDTO> posts = postService.getMemberAllPost(member.getId(), pageable);
            model.addAttribute("nickname", member.getNickname());
            model.addAttribute("memberActivated", member.isActivated());
            model.addAttribute("posts", posts);
            return "admin/memberDocument";

        } catch (UsernameNotFoundException e) {
            model.addAttribute("msg", "게시물을 조회할 수 없습니다.");
            return "error/redirect";
        }
    }


    /**
     * 회원이 작성한 댓글
     */
    @GetMapping("/memberComment")
    public String memberComment(@RequestParam("memberId") String memberId,
                                 @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable,
                                 Model model) {
        try {
            long id = Long.parseLong(memberId);
            MemberResponseDTO member = memberService.getMemberById(id);
            Page<CommentResponseDTO> comments = commentService.getMemberCommentAll(member.getId(), pageable);
            model.addAttribute("nickname", member.getNickname());
            model.addAttribute("memberActivated", member.isActivated());
            model.addAttribute("comments", comments);
            return "admin/memberComment";

        } catch (UsernameNotFoundException e) {
            model.addAttribute("msg", "댓글을 조회할 수 없습니다.");
            return "error/redirect";
        }

    }


    /**
     * 게시판 통계
     */
    @GetMapping("/postStat")
    public String postStat(Model model) {

        //오늘 등록한 게시물 갯수, 어제 등록한 게시물 갯수, 총 게시물 갯수
        model.addAttribute("countToday", postService.countTodayPost());
        model.addAttribute("countYesterday", postService.countYesterdayPost());
        model.addAttribute("countTotal", postService.countTotalPost());

        //카테고리 별 게시물 갯수
        model.addAttribute("countFree", postService.countByCategory(CategoryCode.FREE));
        model.addAttribute("countHumor", postService.countByCategory(CategoryCode.HUMOR));
        model.addAttribute("countDigital", postService.countByCategory(CategoryCode.DIGITAL));
        model.addAttribute("countFootball", postService.countByCategory(CategoryCode.FOOTBALL_WORLD));
        model.addAttribute("countMystery", postService.countByCategory(CategoryCode.MYSTERY));
        model.addAttribute("countNotice", postService.countByCategory(CategoryCode.NOTICE));

        //일주일간의 카테고리 별 게시물 통계
        List<Map<String, Object>> result = postService.getPostStat();
        model.addAttribute("result", result);

        return "admin/postStat";
    }


}
