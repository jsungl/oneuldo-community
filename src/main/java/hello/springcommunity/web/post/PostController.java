package hello.springcommunity.web.post;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.domain.post.PostService;
import hello.springcommunity.web.SessionConst;
import hello.springcommunity.web.post.form.PostSaveForm;
import hello.springcommunity.web.post.form.PostUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    //목록
    @GetMapping
    public String posts(Model model) {
        List<Post> posts = postService.findPosts();
        model.addAttribute("posts", posts);
        return "posts/posts";
    }

    //상세
//    @GetMapping("/{postId}")
//    public String post(@PathVariable Long postId, Model model) {
//        Post post = postService.findOne(postId).orElseThrow();
//        model.addAttribute("post", post);
//        return "posts/post";
//    }
    @GetMapping("/detail")
    public String post(@RequestParam Long postId, Model model) {
        Post post = postService.findOne(postId).orElseThrow();
        model.addAttribute("post", post);
        return "posts/post";
    }


    //등록폼
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
        return "redirect:/posts/{postId}"; //해당 게시물의 상세페이지로 이동
    }

    //수정폼
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
        return "redirect:/posts/{postId}"; //상세페이지로 이동

    }
    
    //삭제
    @PostMapping("/{postId}/delete")
    public String delete(@PathVariable Long postId) {
        postService.deleteById(postId);
        return "redirect:/posts"; //게시물 목록으로 이동
    }


}
