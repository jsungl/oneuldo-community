package hello.springcommunity.web.post;

import hello.springcommunity.dto.comment.CommentResponseDTO;
import hello.springcommunity.dto.security.UserDetailsDTO;
import hello.springcommunity.service.comment.CommentService;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.dto.post.PostResponseDTO;
import hello.springcommunity.service.post.PostService;
import hello.springcommunity.dto.post.PostSaveRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;


    /**
     * 게시물 검색
     */
//    @GetMapping("/search")
//    public String postsBySearch(@RequestParam("searchType") String searchType,
//                                @RequestParam("keyword") String keyword,
//                                @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable,
//                                Model model) {
//
//        log.info("searchType={}", searchType);
//        log.info("keyword={}", keyword);
//
//        Page<Post> list = postService.findPostBySearch(searchType, keyword, pageable);
//        for (Post post : list) {
//            log.info("post={}", post.getTitle());
//        }
//
//        model.addAttribute("posts", list);
//
//        return "posts/posts";
//
//    }

    /**
     * 게시물 검색 - 페이징
     */
    @GetMapping("/search")
    public String searchPost(@RequestParam("searchType") String searchType,
                             @RequestParam("keyword") String keyword,
                             @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable,
                             Model model) {

        log.info("searchType={}", searchType);
        log.info("keyword={}", keyword);

        if(!searchType.isBlank()) {
            Page<PostResponseDTO> list = postService.getSearchedPost(searchType, keyword, pageable);

            model.addAttribute("posts", list);
            model.addAttribute("searchType", searchType);
            model.addAttribute("keyword", keyword);
            return "post/searchPosts";
        }else {
            //검색조건을 선택하지 않고 검색시 전체 게시물 목록 조회
            return "redirect:/posts";
        }


    }


    /**
     * 게시물 상세 + 댓글 조회
     *
     * Path Variable : localhost:8080/posts/1 -> @PathVariable
     * Query Param(Query String) : localhost:8080/posts/detail?postId=1 -> @RequestParam
     *
     */
    @GetMapping("/{postId}/detail")
    public String post(@PathVariable Long postId,
                       Model model,
                       HttpServletRequest request, HttpServletResponse response,
                       @PageableDefault(size = 5) Pageable pageable) {

        //해당 게시물 내용 조회
        PostResponseDTO post = postService.findOne(postId);

        //해당 게시물의 댓글조회
//        List<CommentResponseDTO> commentList = commentService.getCommentList(postId);
//        for (CommentResponseDTO commentResponseDTO : commentList) {
//            log.info("comment id={}", commentResponseDTO.getId());
//        }

        //해당 게시물의 댓글조회 - 페이징
        Page<CommentResponseDTO> commentList = commentService.getCommentListPaging(postId, pageable);

        //해당 게시물 댓글 총 갯수(대댓글 포함)
        Long totalCount = commentService.getTotalCount(postId);

        //조회수 증가
        postService.updateViews(postId, request, response);

        model.addAttribute("post", post);
        model.addAttribute("postId", postId);
        model.addAttribute("comments", commentList);
        model.addAttribute("totalCount", totalCount);

        return "post/post";

    }


    /**
     * 게시물 등록 폼
     */
    @GetMapping("/add")
    public String addForm(@ModelAttribute("postForm") PostSaveRequestDTO postForm) {
        return "post/addForm";
    }

    /**
     * 게시물 등록
     */
    @PostMapping("/add")
    public String add(@Validated @ModelAttribute("postForm") PostSaveRequestDTO postForm,
                      BindingResult bindingResult,
                      @AuthenticationPrincipal UserDetailsDTO dto,
                      RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "post/addForm";
        }

        //Post savedPost = postService.save(postForm, loginMember.getId()); //등록한 게시물의 제목,내용,작성자(id) 를 담아서 전달
        Post savedPost = postService.save(postForm, dto.getUsername());
        log.info("postId={}", savedPost.getId()); //IDENTITY 방식에 의해 DB에 저장후 id 값과 등록날짜(regDate)를 확인할 수 있다

        redirectAttributes.addAttribute("postId", savedPost.getId());
        redirectAttributes.addAttribute("status", true);

        //해당 게시물의 상세페이지로 바로 이동
//        return "redirect:/posts/{postId}";
        return "redirect:/post/{postId}/detail";
    }


    /**
     * 게시물 수정 폼
     */
    @GetMapping("/{postId}/edit")
    public String editForm(@PathVariable Long postId,
                           @AuthenticationPrincipal UserDetailsDTO dto,
                           Model model) {

//        Post post = postService.findById(postId).get();
        PostResponseDTO post = postService.findOne(postId);

        //게시물 작성자가 아니라면 목록으로 리다이렉트
//        if(!Objects.equals(post.getLoginId(), loginMember.getLoginId())) {
//            return "redirect:/posts";
//        }

        if(!Objects.equals(post.getLoginId(), dto.getUsername())) {
            return "redirect:/posts";
        }

        model.addAttribute("postForm", post);
        model.addAttribute("postId", postId);
        return "post/editForm";
    }

    /**
     * 게시물 수정
     */
    @PostMapping("/{postId}/edit")
    public String edit(@PathVariable Long postId,
                       @ModelAttribute("postForm") PostSaveRequestDTO postSaveRequestDTO,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {

        if(bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "post/editForm";
        }

        Long id = postService.update(postId, postSaveRequestDTO);
        redirectAttributes.addAttribute("postId", id);

        //상세페이지로 이동
//        return "redirect:/posts/detail?postId=" + id;
        return "redirect:/post/{postId}/detail";

    }


    /**
     * 게시물 삭제
     */
    @PostMapping("/{postId}/delete")
    public String delete(@PathVariable Long postId,
                         @AuthenticationPrincipal UserDetailsDTO dto) {

        PostResponseDTO result = postService.findOne(postId);

        //게시물 작성자라면 삭제
//        if(Objects.equals(result.getLoginId(), loginMember.getLoginId())) {
//            postService.delete(postId);
//        }
        if(Objects.equals(result.getLoginId(), dto.getUsername())) {
            postService.delete(postId);
        }


        return "redirect:/posts";
    }


}
