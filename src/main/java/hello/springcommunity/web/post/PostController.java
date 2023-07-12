package hello.springcommunity.web.post;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.domain.post.PostSearchCond;
import hello.springcommunity.domain.post.PostService;
import hello.springcommunity.web.SessionConst;
import hello.springcommunity.web.post.form.PostSaveForm;
import hello.springcommunity.web.post.form.PostUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 게시물 목록
     */
//    @GetMapping
//    public String posts(Model model) {
//        List<Post> posts = postService.findPosts();
//        model.addAttribute("posts", posts);
//        return "posts/posts";
//    }
    //게시물 목록 페이징
    //@PageableDefault 어노테이션을 이용하여 정렬 순서, 사이즈 등의 정보를 넣고 해당객체를 서비스에 파라미터로 전달
    @GetMapping
    public String posts(Model model, @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable) {

        Page<Post> posts = postService.findAll(pageable);
        log.info("totalPages={}", posts.getTotalPages());
        model.addAttribute("posts", posts);

        return "posts/posts";
    }


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
    public String postBySearch(@RequestParam("searchType") String searchType,
                               @RequestParam("keyword") String keyword,
                               @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable,
                               Model model) {

        log.info("searchType={}", searchType);

        if(!searchType.isBlank()) {
            Page<Post> list = postService.findPostsBySearch(searchType, keyword, pageable);
            log.info("search result totalPages={}", list.getTotalPages());
            model.addAttribute("posts", list);
            model.addAttribute("searchType", searchType);
            model.addAttribute("keyword", keyword);
            return "posts/searchPosts";
        }else {
            //검색조건을 선택하지 않고 검색시 전체 게시물 목록 조회
            return "redirect:/posts";
        }


    }


    /**
     * 게시물 상세
     */
    //Path Variable : localhost:8080/posts/1
//    @GetMapping("/{postId}")
//    public String post(@PathVariable Long postId, Model model) {
//        Post post = postService.findOne(postId).orElseThrow();
//        model.addAttribute("post", post);
//        return "posts/post";
//    }
    //Query Param(Query String) : localhost:8080/posts/detail?postId=1
    @GetMapping("/detail")
    public String post(@RequestParam Long postId, Model model) {
        Post post = postService.findOne(postId).orElseThrow();
        model.addAttribute("post", post);
        return "posts/post";
    }


    /**
     * 게시물 등록 폼
     */
    @GetMapping("/add")
    public String addForm(@ModelAttribute("postForm") PostSaveForm postForm) {
        return "posts/addForm";
    }

    @PostMapping("/add")
    public String addPost(@Validated @ModelAttribute("postForm") PostSaveForm postForm, BindingResult bindingResult,
                          @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "posts/addForm";
        }

        log.info("postForm={}", postForm); //id와 등록날짜는 null

        Post savedPost = postService.save(postForm.getTitle(), postForm.getContent(), loginMember.getId()); //등록한 게시물의 제목,내용,작성자 를 담아서 전달
        log.info("savedPost={}", savedPost); //IDENTITY 방식에 의해 DB에 저장후 id 값과 등록날짜(regDate)를 확인할 수 있다

        redirectAttributes.addAttribute("postId", savedPost.getId());
        redirectAttributes.addAttribute("status", true);

        //해당 게시물의 상세페이지로 이동
//        return "redirect:/posts/{postId}";
        return "redirect:/posts/detail?postId={postId}";
    }


    /**
     * 게시물 수정 폼
     */
    @GetMapping("/{postId}/edit")
    public String editForm(@PathVariable Long postId, Model model) {
//        Post post = postService.findById(postId).get();
        Post post = postService.findOne(postId).orElseThrow();

        PostUpdateForm postUpdateForm = new PostUpdateForm();
        postUpdateForm.setTitle(post.getTitle());
        postUpdateForm.setContent(post.getContent());

        model.addAttribute("postUpdateForm", postUpdateForm);
        model.addAttribute("postId", postId);
        return "posts/editForm";
    }

    @PostMapping("/{postId}/edit")
    public String edit(@PathVariable Long postId, @ModelAttribute("postUpdateForm") PostUpdateForm postUpdateForm, BindingResult bindingResult) {

        if(bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "posts/editForm";
        }

        log.info("postUpdateForm={}", postUpdateForm);
        postService.update(postId, postUpdateForm);

        //상세페이지로 이동
//        return "redirect:/posts/{postId}";
        return "redirect:/posts/detail?postId={postId}";

    }


    /**
     * 게시물 삭제 폼
     */
    @PostMapping("/{postId}/delete")
    public String delete(@PathVariable Long postId) {
        postService.deleteById(postId);
        return "redirect:/posts"; //게시물 목록으로 이동
    }


}
