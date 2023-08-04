package hello.springcommunity.service.post;

import hello.springcommunity.dao.post.PostQueryRepository;
import hello.springcommunity.dao.post.PostRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.dto.post.PostResponseDTO;
import hello.springcommunity.domain.post.PostSearchCond;
import hello.springcommunity.common.CookieConst;
import hello.springcommunity.dto.post.PostSaveForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostQueryRepository postQueryRepository;

    /**
     * 게시물 등록
     */
    public Post save(PostSaveForm postSaveForm, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new NoSuchElementException("member is not exist"));
//        Post post = Post.savePost(postSaveForm, member);
        Post post = Post.builder()
                .title(postSaveForm.getTitle())
                .content(postSaveForm.getContent())
                .member(member)
                .build();

        return postRepository.save(post);
    }


    /**
     * 게시물 수정
     */
    public Long update(Long id, PostSaveForm postSaveForm) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다. id= " + id));
        post.updatePost(postSaveForm.getTitle(), postSaveForm.getContent());
        postRepository.save(post);

        return post.getId();
    }
//    public void update(Long postId, PostUpdateDto updateParam) {
//        Post findPost = findById(postId).orElseThrow();
//        findPost.setPostTitle(updateParam.getPostTitle());
//        findPost.setPostBody(updateParam.getPostBody());
//    }


    /**
     * 게시물 삭제
     */
    public void delete(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다. id= " + id));
        postRepository.delete(post);
    }


    /**
     * 게시물 1개 조회
     */
//    public Optional<Post> findOne(Long id) {
//        return postRepository.findById(id);
//    }

    public PostResponseDTO findOne(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다. id= " + id));

        /**
         * 파라미터로 전달받은 id 로 게시물을 찾은 뒤 Entity로 바로 넘겨주지 않고 DTO에서 한번 감싼후
         * DTO 값을 넘겨준다
         */
        PostResponseDTO result = PostResponseDTO.builder()
                .post(post)
                .build();

        return result;
    }


    /**
     * 게시물 전체 조회
     */
//    public List<Post> findAll() {
//        return postRepository.findAll();
//    }

    /**
     * 게시물 전체 조회 - 페이징
     */
//    public Page<Post> findAllV1(Pageable pageable) {
//        return postRepository.findAll(pageable);
//    }

    public Page<PostResponseDTO> findAll(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        List<PostResponseDTO> list = new ArrayList<>();

        //Entity -> DTO
        for (Post post : posts) {
            PostResponseDTO result = PostResponseDTO.builder()
                                                    .post(post)
                                                    .build();
            list.add(result);
        }

        return new PageImpl<>(list, pageable, posts.getTotalElements());
    }


    /**
     * 게시물 검색 - 페이징
     */
//    public Page<Post> findPostBySearchV1(String searchType, String keyword, Pageable pageable) {
//        switch (searchType) {
//            case "title":
//                return postQueryRepository.findAll(new PostSearchCond(keyword, null, null), pageable);
//            case "content" :
//                return postQueryRepository.findAll(new PostSearchCond(null, keyword, null), pageable);
//            case "loginId" :
//                return postQueryRepository.findAll(new PostSearchCond(null, null, keyword), pageable);
//            default:
//                return postQueryRepository.findAll(new PostSearchCond(null, null, null), pageable);
//
//        }
//    }

    public Page<PostResponseDTO> findPostBySearch(String searchType, String keyword, Pageable pageable) {
        switch (searchType) {
            case "title":
                return getPostResponseDto(postQueryRepository.findAll(new PostSearchCond(keyword, null, null), pageable), pageable);
            case "content" :
                return getPostResponseDto(postQueryRepository.findAll(new PostSearchCond(null, keyword, null), pageable), pageable);
            case "loginId" :
                return getPostResponseDto(postQueryRepository.findAll(new PostSearchCond(null, null, keyword), pageable), pageable);
            default:
                return getPostResponseDto(postQueryRepository.findAll(new PostSearchCond(null, null, null), pageable), pageable);

        }
    }

    private Page<PostResponseDTO> getPostResponseDto(Page<Post> posts, Pageable pageable) {
        List<PostResponseDTO> list = new ArrayList<>();

        for(Post post : posts) {
            PostResponseDTO result = PostResponseDTO.builder()
                                                    .post(post)
                                                    .build();
            list.add(result);
        }

        return new PageImpl<>(list, pageable, posts.getTotalElements());
    }


    /**
     * 조회수 증가
     * 쿠키를 이용한 조회수 증가 중복방지
     */
    public void updateViews(Long postId, HttpServletRequest request, HttpServletResponse response) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("존재하지 않은 게시물입니다. id= " + postId));
        viewCountValidation(post, request, response);
//        return PostResponseDTO.builder().post(post).build();
    }

    private void viewCountValidation(Post post, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        Cookie oldCookie = null;
        boolean isCookie = false;

        if(cookies != null) {
            for (Cookie cookie : cookies) {
                //조회수 쿠키가 있을 때
                if (cookie.getName().equals(CookieConst.VIEW_COOKIE_NAME)) {
                    oldCookie = cookie;

                    if (oldCookie.getValue().contains(".")) {
                        String[] arr = oldCookie.getValue().split("\\.");
                        boolean chk = false;
                        //123.20.33
                        for (String pno : arr) {
                            if (pno.matches(post.getId().toString())) {
                                chk = true;
                                break;
                            }
                        }

                        if (!chk) {
                            post.updateViewCount();
                            oldCookie.setValue(oldCookie.getValue() + "." + post.getId());
                            Cookie newCookie = createCookieForNotOverlap(oldCookie);
                            response.addCookie(newCookie);
                        }


                    } else {
                        //1개만 조회한 경우 + 다른 게시물인 경우
                        if(!oldCookie.getValue().matches(post.getId().toString())) {
                            post.updateViewCount();
                            oldCookie.setValue(oldCookie.getValue() + "." + post.getId());
                            Cookie newCookie = createCookieForNotOverlap(oldCookie);
                            response.addCookie(newCookie);
                        }

                    }

                    isCookie = true;
                    break;

                }

            }
        }

        if(!isCookie) {

            //쿠키가 없으면 처음 접속한 것이므로 새로 생성
            post.updateViewCount();
            Cookie cookie = new Cookie(CookieConst.VIEW_COOKIE_NAME, post.getId().toString());

            Cookie newCookie = createCookieForNotOverlap(cookie);
            response.addCookie(newCookie);

        }


    }

    /**
     * 조회수 중복 방지를 위한 쿠키 설정 메서드
     */
    private Cookie createCookieForNotOverlap(Cookie cookie) {
        // 쿠키 유지시간을 오늘 하루 자정까지로 설정
        // todayEndSecond = 하루 종료 시간을 시간초로 변환
        // currentSecond = 현재 시간을 시간초로 변환
        // todayEndSecond - currentSecond = // 오늘 하루 자정까지 남은 시간초
        long todayEndSecond = LocalDate.now().atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC);
        long currentSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        cookie.setPath("/"); // 모든 경로에서 접근 가능
        cookie.setMaxAge((int) (todayEndSecond - currentSecond));
        cookie.setHttpOnly(true); //서버에서만 조작 가능
        return cookie;
    }


    /**
     * 게시물 검색(페이징 x)
     */
//    public Page<Post> findPostBySearch(String searchType, String keyword, Pageable pageable) {
//        switch (searchType) {
//            case "title":
//                return postRepository.findByTitleContaining(keyword, pageable);
//            case "content" :
//                return postRepository.findByContentContaining(keyword, pageable);
//            default:
//                return postRepository.findAll(pageable);
//        }
//    }
}
