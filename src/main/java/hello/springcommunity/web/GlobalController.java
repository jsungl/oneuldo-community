package hello.springcommunity.web;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GlobalController {

    private final PostService postService;
    private final MemberService memberService;

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
}
