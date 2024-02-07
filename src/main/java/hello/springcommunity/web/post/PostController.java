package hello.springcommunity.web.post;

import hello.springcommunity.domain.notification.Notification;
import hello.springcommunity.domain.post.CategoryCode;
import hello.springcommunity.dto.comment.CommentResponseDTO;
import hello.springcommunity.dto.notification.NotificationDTO;
import hello.springcommunity.dto.post.PostResponseDTO;
import hello.springcommunity.dto.security.UserDetailsDTO;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.exception.CustomRuntimeException;
import hello.springcommunity.service.comment.CommentServiceImpl;
import hello.springcommunity.service.notification.NotificationService;
import hello.springcommunity.service.post.PostService;
import hello.springcommunity.dto.post.PostRequestDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static hello.springcommunity.domain.post.CategoryCode.*;

@Slf4j
@Controller
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentServiceImpl commentService;
    private final NotificationService notificationService;

    /**
     * CategoryCode(Enum)의 모든종류를 배열로 반환
     * [CHAT, NOTICE]
     */
    @ModelAttribute("categoryCodes")
    public CategoryCode[] categoryCodes() {
        return values();
    }


    /**
     * 게시물 검색 - 페이징
     */
    @GetMapping("/search")
    public String searchPost(@RequestParam("searchType") String searchType,
                             @RequestParam("keyword") String keyword,
                             @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable,
                             Model model) {
        try {

            if(StringUtils.hasText(searchType)) {

                Map<String, Object> param = new HashMap<>();
                param.put("searchType", searchType);
                param.put("keyword", keyword);

                Page<PostResponseDTO> list = postService.getSearchPost(param,pageable);

                model.addAttribute("posts", list);
                model.addAttribute("searchType", searchType);
                model.addAttribute("keyword", keyword);
                model.addAttribute("title", keyword);


            }else {
                //검색조건을 선택하지 않고 검색시 전체 게시물 목록 조회
                return "redirect:/posts";
            }

        } catch (RuntimeException e) {
            throw new CustomRuntimeException("검색에 실패하였습니다.");
        }

        return "post/searchedPost";

    }


    /**
     * 게시물 상세 조회(댓글포함)
     *
     * Path Variable : localhost:8080/posts/1 -> @PathVariable
     * Query Param(Query String) : localhost:8080/posts/detail?postId=1 -> @RequestParam
     *
     */
    @GetMapping("/{postId}")
    public String detailPost(@PathVariable Long postId,
                             @AuthenticationPrincipal UserDetailsDTO dto,
                            @RequestParam(value = "page", required = false) String pageNumber,
                            @RequestParam(value = "commentId", required = false) String commentId,
                            Model model,
                            HttpServletRequest request, HttpServletResponse response,
                            @PageableDefault(sort = "id", direction = Sort.Direction.DESC, size = 5) Pageable pageable) {

        try {
            //해당 게시물 조회
            PostResponseDTO post = postService.getPostDetail(postId, request, response);
            if(post.getCategoryCode().equals("NOTICE")) {
                model.addAttribute("post", post);
                return "notice/notice";
            }

            boolean like = false;
            //로그인 유무 확인
            if(dto != null) {
                //해당 게시물을 추천했는지 조회
                like = postService.getMemberLikePost(postId, dto.getUsername());
            }

            //댓글조회 - 페이징
            //null, 빈값, whitespace 로만 이루어져있는 String을 모두 체크
            if(!StringUtils.hasText(pageNumber)) {
                int page;
                //파라미터 commentId가 있다면 유저가 댓글목록에서 댓글을 조회하는 경우, 댓글 등록,수정 후 댓글 리스트 갱신시
                //조회하려는 댓글의 위치를 파악해 페이지를 계산한다
                if(StringUtils.hasText(commentId)) {
                    long id = Long.parseLong(commentId);
                    page = commentService.getPresentPageNumber(id, post, pageable);

                } else {
                    //가장 최신페이지
                    page = commentService.getLastPageNumber(post, pageable);
                }
                //Pageable 의 기본 구현체인 PageRequest를 사용해서 객체를 재정의한다
                //이 클래스의 생성자는 protected로 선언되어 new를 이용할 수 없어 생성하기 위해선 static한 of()를 이용해서 처리한다
                pageable = PageRequest.of(page, 5);
            }

            /** Page<CommentResponseDTO> commentList = commentService.getPostComment(post, pageable); **/
            Page<CommentResponseDTO> commentList = commentService.getComments(post, pageable, Long.valueOf(post.getCommentCount()));

            model.addAttribute("post", post);
            model.addAttribute("comments", commentList);
            //해당 게시물 댓글 총 갯수(대댓글 포함)
            model.addAttribute("totalCount", post.getCommentCount());

            //로그인 유저면 해당 게시물 추천유무, 비로그인 유저면 무조건 false
            model.addAttribute("like", like);

            //개행문자
            //model.addAttribute("nlString", "\r\n");

            model.addAttribute("title", post.getTitle());


        } catch (IllegalArgumentException e) {
            throw new CustomRuntimeException("게시물이 존재하지 않습니다.");
        } catch (RuntimeException e) {
            throw new CustomRuntimeException("게시물을 조회할 수 없습니다.");
        }

        return "post/post";


    }


    @GetMapping(value = "/notify", headers = "X-Requested-With=XMLHttpRequest")
    @ResponseBody
    public ResponseEntity<?> handleAjaxRequest(@RequestParam(value = "notificationId") Long notificationId,
                                                @RequestParam(value = "postId") Long postId,
                                                @RequestParam(value = "commentId") Long commentId,
                                                @AuthenticationPrincipal UserDetailsDTO dto,
                                                RedirectAttributes redirectAttributes,
                                                Model model) {
        try {

            // 알림을 받은 본인이면 읽음과 동시에 삭제
            if(dto != null) {
                Notification notification = notificationService.getNotification(notificationId);
                if(notification.getId().equals(notificationId) && notification.getReceiver().equals(dto.getUsername())) {
                    notificationService.deleteNotification(notification);
                }
            }

            String redirectUrl = "/post/" + postId + "?commentId=" + commentId + "#comment_" + commentId;

            // ResponseEntity로 리다이렉트 주소 응답을 생성
            return ResponseEntity.status(200).header("Location", redirectUrl).body(commentId);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("알림이 존재하지 않습니다.");
        }

    }

    @GetMapping("/notify")
    public String handleNonAjaxRequest(@RequestParam(value = "notificationId") Long notificationId,
                                     @RequestParam(value = "postId") Long postId,
                                     @RequestParam(value = "commentId") Long commentId,
                                     @AuthenticationPrincipal UserDetailsDTO dto,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        try {
            // 알림을 받은 본인이면 읽음과 동시에 삭제
            if(dto != null) {
                Notification notification = notificationService.getNotification(notificationId);
                if(notification.getId().equals(notificationId) && notification.getReceiver().equals(dto.getUsername())) {
                    notificationService.deleteNotification(notification);
                }
            }

            redirectAttributes.addAttribute("postId", postId);
            redirectAttributes.addAttribute("commentId", commentId);


        } catch (IllegalArgumentException e) {
            throw new CustomRuntimeException("알림이 존재하지 않습니다.");
        }

        return "redirect:/post/{postId}";

    }



    /**
     * 게시물 등록 폼
     */
    @GetMapping("/add")
    public String addForm(HttpServletRequest request, @AuthenticationPrincipal UserDetailsDTO dto, Model model) {

        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if(inputFlashMap != null && inputFlashMap.get("prevPostSaveReq") != null) {
            PostRequestDTO postForm = (PostRequestDTO) inputFlashMap.get("prevPostSaveReq");
            model.addAttribute("postForm", postForm);
        } else {
            model.addAttribute("postForm", new PostRequestDTO());
        }
        model.addAttribute("role", dto.getMember().getRoleValue());

        return "post/addForm";
    }


    /**
     * 게시물 등록
     */
    @PostMapping("/add")
    public String add(@Validated PostRequestDTO postForm,
                      BindingResult bindingResult,
                      @AuthenticationPrincipal UserDetailsDTO dto,
                      RedirectAttributes redirectAttributes,
                      Model model) {
        //유효성 검사
        boolean result = handlingBindingResult(bindingResult, redirectAttributes);
        if(result) {
            redirectAttributes.addFlashAttribute("prevPostSaveReq", postForm);
            return "redirect:/post/add";
        }

        try {
            Post savedPost = postService.addPost(postForm, dto.getUsername());
            //이미지가 있을경우 경로 이동
            postService.parseContextAndMoveImages(savedPost);

            redirectAttributes.addAttribute("postId", savedPost.getId()); //IDENTITY 방식에 의해 DB에 저장후 id 값과 등록날짜(regDate)를 확인할 수 있다
            redirectAttributes.addFlashAttribute("msg", "게시물이 등록되었습니다.");


        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msg", "게시물 등록에 실패하였습니다.");
            redirectAttributes.addFlashAttribute("prevPostSaveReq", postForm);
            return "redirect:/post/add";
        }

        //해당 게시물의 상세페이지로 바로 이동
        return "redirect:/post/{postId}";

    }



    /**
     * 게시물 수정 폼
     */
    @GetMapping("/{postId}/edit")
    public String editForm(@PathVariable Long postId,
                           @AuthenticationPrincipal UserDetailsDTO dto,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        try {
            Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
            if(inputFlashMap != null && inputFlashMap.get("prevPostUpdateReq") != null) {
                PostRequestDTO postRequestDTO = (PostRequestDTO) inputFlashMap.get("prevPostUpdateReq");
                PostResponseDTO postResponseDTO =
                        new PostResponseDTO(postRequestDTO.getTitle(), postRequestDTO.getContent(), postRequestDTO.getCategoryCode(), postRequestDTO.getFixed());
                model.addAttribute("postForm", postResponseDTO);

            } else {
                PostResponseDTO post = postService.getPostDto(postId);
                //게시물 작성자만 수정 페이지로 접근가능
                if(!Objects.equals(post.getMember().getLoginId(), dto.getUsername())) {
                    model.addAttribute("msg", "게시물 작성자만 수정할 수 있습니다.");
                    return "error/redirect";
                }
                model.addAttribute("postForm", post);
            }

            model.addAttribute("role", dto.getMember().getRoleValue());
            model.addAttribute("postId", postId);

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("msg", "게시물이 존재하지 않습니다.");
            return "redirect:/posts";
        } catch (RuntimeException e) {
            redirectAttributes.addAttribute("postId", postId);
            redirectAttributes.addFlashAttribute("msg", "게시물을 수정할 수 없습니다.");
            return "redirect:/post/{postId}";
        }

        return "post/editForm";

    }


    /**
     * 게시물 수정
     */
    @PostMapping("/{postId}/edit")
    public String edit(@PathVariable Long postId,
                       @Validated PostRequestDTO postRequestDTO,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes,
                       Model model) {

        //유효성 검사
        boolean result = handlingBindingResult(bindingResult, redirectAttributes);
        if(result) {
            redirectAttributes.addFlashAttribute("prevPostUpdateReq", postRequestDTO);
            redirectAttributes.addAttribute("postId", postId);
            return "redirect:/post/{postId}/edit";
        }

        try {
            //Long id = postService.updatePost(postId, postRequestDTO);
            postService.parseContextAndEditImages(postId, postRequestDTO);
            Post post = postService.updatePost(postId, postRequestDTO);

            //이미지가 있을경우 경로 이동
            postService.parseContextAndMoveImages(post);

            redirectAttributes.addFlashAttribute("msg", "게시물이 수정되었습니다.");
            redirectAttributes.addAttribute("postId", post.getId());

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msg", "게시물 수정에 실패하였습니다.");
            redirectAttributes.addFlashAttribute("prevPostUpdateReq", postRequestDTO);
            redirectAttributes.addAttribute("postId", postId);
            return "redirect:/post/{postId}/edit";
        }

        //상세페이지로 이동
        return "redirect:/post/{postId}";

    }


    /**
     * 게시물 삭제
     */
    @PostMapping("/{postId}/delete")
    public String delete(@PathVariable Long postId,
                         @AuthenticationPrincipal UserDetailsDTO dto,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        try {
            Post post = postService.getPost(postId);

            //게시물 작성자가 맞다면 삭제
            if(Objects.equals(post.getMember().getLoginId(), dto.getUsername())) {
                //이미지 삭제
                postService.parseContextAndDeleteImages(post);
                postService.deletePost(post);
            }


        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msg", "게시물 삭제에 실패하였습니다.");
            redirectAttributes.addAttribute("postId", postId);
            return "redirect:/post/{postId}";
        }

        return "redirect:/posts";

    }


    /**
     * 게시물 추천
     */
    @PostMapping("/like/{postId}")
    @ResponseBody
    public ResponseEntity<?> postLike(@AuthenticationPrincipal UserDetailsDTO dto, @PathVariable Long postId) {

        try {
            postService.likePost(postId, dto.getUsername());

            //현재 게시물 추천수
            Integer likeCount = postService.getPostLikeCount(postId);

            boolean like = false;
            //해당 게시물을 추천했는지 조회
            like = postService.getMemberLikePost(postId, dto.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("postId",postId);
            response.put("isLike", like);
            response.put("likeCount",likeCount);

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (UsernameNotFoundException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("msg", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMap);

        } catch (IllegalArgumentException e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("msg", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
        }

    }


    /**
     * 유효성 검사
     */
    private boolean handlingBindingResult(BindingResult result, RedirectAttributes redirectAttributes) {

        if(result.hasErrors()) {

            Map<String, String> errorMap = new HashMap<>();
            for(FieldError error : result.getFieldErrors()) {
                errorMap.put("valid_"+error.getField(), error.getDefaultMessage());
                errorMap.put("errorMessage", error.getDefaultMessage());
            }
            /* Model에 담아 view resolve */
            for(String key : errorMap.keySet()) {
                redirectAttributes.addFlashAttribute(key, errorMap.get(key));
            }
            return true;
        } else {
            return false;
        }

    }


}
