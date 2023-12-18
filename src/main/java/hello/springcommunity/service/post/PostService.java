package hello.springcommunity.service.post;

import hello.springcommunity.dao.comment.CommentRepository;
import hello.springcommunity.dao.member.MemberLikePostRepository;
import hello.springcommunity.dao.post.NoticeRepository;
import hello.springcommunity.dao.post.PostQueryRepository;
import hello.springcommunity.dao.post.PostRepository;
import hello.springcommunity.domain.member.Member;
import hello.springcommunity.dao.member.MemberRepository;
import hello.springcommunity.domain.member.MemberLikePost;
import hello.springcommunity.domain.post.CategoryCode;
import hello.springcommunity.domain.post.Notice;
import hello.springcommunity.domain.post.Post;
import hello.springcommunity.dto.post.PostResponseDTO;
import hello.springcommunity.dto.post.PostSearchCond;
import hello.springcommunity.dto.post.PostRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import java.util.Map;

import static hello.springcommunity.domain.post.CategoryCode.*;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final NoticeRepository noticeRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostQueryRepository postQueryRepository;
    private final MemberLikePostRepository memberLikePostRepository;

    /**
     * 게시물 등록
     */
    public Post addPost(PostRequestDTO postRequestDTO, String loginId) {
        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        //공지여부 확인
        Notice savedNotice = null;
        if(postRequestDTO.getCategoryCode().equals(NOTICE)) {
            Notice notice = Notice.builder().fixed(postRequestDTO.getFixed()).build();
            savedNotice = noticeRepository.save(notice);
        }

        //DTO -> Entity
        Post post = Post.builder()
                .title(postRequestDTO.getTitle())
                .content(postRequestDTO.getContent())
                .member(member)
                .categoryCode(postRequestDTO.getCategoryCode())
                .notice(savedNotice)
                .build();

        return postRepository.save(post);
    }


    /**
     * 게시물 수정
     */
    public Long updatePost(Long id, PostRequestDTO postRequestDTO) {

        //Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
        Post post = postQueryRepository.findOne(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
        post.updatePost(postRequestDTO.getTitle(), postRequestDTO.getContent());

        if(postRequestDTO.getCategoryCode().equals(NOTICE)) {
            //Notice notice = Notice.builder().fixed(postRequestDTO.getFixed()).build();
            Notice notice = post.getNotice();
            notice.changeFixed(postRequestDTO.getFixed());
            post.setNotice(notice);
        }

        return post.getId();
    }


    /**
     * 게시물 1개 삭제
     *
     * JpaRepository에서 제공하는 deleteByXXX 등의 메소드를 이용하는 삭제는 단건이 아닌 여러건을 삭제하더라도 먼저 조회를 하고 그 결과로 얻은 엔티티 데이터를 1건씩 삭제한다.
     * 즉, 만약 1억건 중 50만건을 삭제한다고 하면 50만건을 먼저 조회후 건건으로 삭제한다.
     */
    public void deletePost(Post post) {

        //댓글이 1개이상 있다면
        if(post.getComments().size() != 0) {
            //삭제하려는 게시물의 댓글들의 연관관계 삭제
            commentRepository.updateParentByPostId(post.getId());
            //삭제하려는 게시물의 댓글 모두 삭제
            commentRepository.deleteAllByPostId(post.getId());
        }

        //공지여부 확인
        if(post.getCategoryCode().equals(NOTICE)) {
            Long id = post.getNotice().getId();
            //게시물 삭제(부모인 Post 객체부터 삭제해야 한다)
            postRepository.deleteOne(post.getId());
            // 공지 삭제
            noticeRepository.deleteOne(id);
        } else {
            //추천 삭제
            memberLikePostRepository.deleteAllByPostId(post.getId());
            //게시물 삭제
            postRepository.deleteOne(post.getId());
        }
    }

    /**
     * 탈퇴시 회원이 작성한 게시물 모두 삭제
     * 게시물에 달린 댓글들 모두 삭제
     */
//    public void deleteAllPost(Long memberId) {
//
//        List<Post> list = postRepository.findByMemberId(memberId);
//
//        if(list.size() != 0) {
//
//            //작성한 게시물의 댓글 모두 삭제
//            list.forEach(post ->  {
//                commentRepository.updateParentByPostId(post.getId());
//                commentRepository.deleteAllByPostId(post.getId());
//            });
//
//            //작성한 게시물 모두 삭제
//            postRepository.deleteAllByMemberId(memberId);
//
//        }
//
//    }


    /**
     * 게시물 1개 조회
     */
    public Post getPost(Long id) {
        //return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
        return postQueryRepository.findOne(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
    }

    public PostResponseDTO getPostDto(Long id) {

        //Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
        Post post = postQueryRepository.findOne(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

        /**
         * 파라미터로 전달받은 id 로 게시물을 찾은 뒤 Entity로 바로 넘겨주지 않고 DTO에서 한번 감싼후
         * DTO 값을 넘겨준다
         */
        //Entity -> DTO
        return PostResponseDTO.builder()
                .post(post)
                .build();
    }


    /**
     * 게시물 상세 조회
     * 조회수 증가
     */
    public PostResponseDTO getPostDetail(Long id, HttpServletRequest request, HttpServletResponse response) {
        //Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
        Post post = postQueryRepository.findOne(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

        if(!post.getMember().getActivated()) {
            throw new IllegalArgumentException("탈퇴한 회원입니다.");
        }

        /** 조회수 증가 **/
        updateViews(post, request, response);

        //Entity -> DTO
        return PostResponseDTO.builder()
                .post(post)
                .build();
    }

    /**
     * 상단에 고정된 공지 조회
     */
    public List<PostResponseDTO> getTopNotice() {

        List<Post> notices = postQueryRepository.findTopNotice();

        //Entity -> DTO
        List<PostResponseDTO> list = new ArrayList<>();
        for (Post post : notices) {
            PostResponseDTO result = PostResponseDTO.builder()
                    .post(post)
                    .build();
            list.add(result);
        }

        return list;
    }


    /**
     * 게시물 전체 조회 - 페이징
     */
    public Page<PostResponseDTO> getAllPost(String sort, int pageNo) {

        Pageable pageable;

        /** 첫번째 정렬조건에서 동일한 값을 가진 경우는 2번째 정렬조건으로 최신순으로 정렬한다 **/
        if("id".equals(sort)) {
            pageable = PageRequest.of(pageNo, 5, Sort.by(Sort.Direction.DESC, sort));

        } else {
            pageable = PageRequest.of(pageNo, 5, Sort.by(Sort.Direction.DESC, sort, "id"));
        }

        Page<Post> posts = postQueryRepository.findAll(pageable);

        return posts.map(post -> PostResponseDTO.builder().post(post).build());
    }


    /**
     * 게시물 전체 카테고리별 조회 - 페이징
     */
    public Page<PostResponseDTO> getAllPostByCategory(String sort, CategoryCode category, int pageNo) {

        Pageable pageable;

        /** 첫번째 정렬조건에서 동일한 값을 가진 경우는 2번째 정렬조건으로 최신순으로 정렬한다 **/
        if("id".equals(sort)) {
            pageable = PageRequest.of(pageNo, 5, Sort.by(Sort.Direction.DESC, sort));

        } else {
            pageable = PageRequest.of(pageNo, 5, Sort.by(Sort.Direction.DESC, sort, "id"));
        }

        Page<Post> posts = postQueryRepository.findByCategory(category, pageable);

        return posts.map(post -> PostResponseDTO.builder().post(post).build());
    }


    /**
     * 특정 사용자가 작성한 게시물 모두 조회
     */
    public Page<PostResponseDTO> getMemberAllPost(Long id, Pageable pageable) {

        Page<Post> posts = postQueryRepository.findByMemberId(id, pageable);

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
    public Page<PostResponseDTO> getSearchPost(Map<String, Object> param, Pageable pageable) {
        String type = (String) param.get("searchType");
        String keyword = (String) param.get("keyword");

        switch (type) {
            case "title":
                return entityToDto(postQueryRepository.findAllBySearchCond(new PostSearchCond(keyword, null, null,null), pageable));
            case "content":
                return entityToDto(postQueryRepository.findAllBySearchCond(new PostSearchCond(null, keyword, null,null), pageable));
            case "nickname":
                return entityToDto(postQueryRepository.findAllBySearchCond(new PostSearchCond(null, null, keyword,null), pageable));
            case "memberId":
                Long id = Long.valueOf(keyword);
                Member member = memberRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));
                if(!member.getActivated()) {
                    throw new IllegalArgumentException("이미 탈퇴된 회원입니다.");
                }
                return entityToDto(postQueryRepository.findAllBySearchCond(new PostSearchCond(null, null, null, id), pageable));
            default:
                return entityToDto(postQueryRepository.findAllBySearchCond(new PostSearchCond(), pageable));
        }
    }


    /**
     * entity -> DTO
     */
    private Page<PostResponseDTO> entityToDto(Page<Post> posts) {
        return posts.map(post -> PostResponseDTO.builder().post(post).build());
    }

//    private Page<PostResponseDTO> entityToDto(Page<Post> posts, Pageable pageable) {
//        List<PostResponseDTO> list = new ArrayList<>();
//
//        //Entity -> DTO
//        for(Post post : posts) {
//            PostResponseDTO result = PostResponseDTO.builder()
//                                                    .post(post)
//                                                    .build();
//            list.add(result);
//        }
//
//        return new PageImpl<>(list, pageable, posts.getTotalElements());
//    }


    /**
     * 조회수 증가
     * 쿠키를 이용한 조회수 증가 중복방지
     */
    public void updateViews(Post post, HttpServletRequest request, HttpServletResponse response) {
        viewCountValidation(post, request, response);
    }

    private void viewCountValidation(Post post, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        Cookie oldCookie = null;
        boolean isCookie = false;

        if(cookies != null) {
            for (Cookie cookie : cookies) {
                /** 조회수 쿠키가 있을 때 **/
                if (cookie.getName().equals("viewCookie")) {
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
            Cookie cookie = new Cookie("viewCookie", post.getId().toString());

            Cookie newCookie = createCookieForNotOverlap(cookie);
            response.addCookie(newCookie);

        }


    }

    /**
     * 조회수 중복 방지를 위한 쿠키 설정 메서드
     * 쿠키 유지시간을 오늘 하루 자정까지로 설정
     *
     * todayEndSecond = 하루 종료 시간을 시간초로 변환
     * currentSecond = 현재 시간을 시간초로 변환
     * todayEndSecond - currentSecond = 오늘 하루 자정까지 남은 시간초
     */
    private Cookie createCookieForNotOverlap(Cookie cookie) {
        long todayEndSecond = LocalDate.now().atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC);
        long currentSecond = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        cookie.setPath("/"); // 모든 경로에서 접근 가능
        cookie.setMaxAge((int) (todayEndSecond - currentSecond));
        cookie.setHttpOnly(true); //서버에서만 조작 가능
        return cookie;
    }


    /**
     * 게시물 추천, 추천취소
     */
    public void likePost(Long postId, String loginId) {

        Member member = memberRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        /** 추천하려는 게시물 조회 **/
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));


        /** 로그인한 유저가 해당 게시물을 추천 했는지 안 했는지 확인 **/
        if(memberLikePostRepository.existsByPostIdAndMemberId(postId,member.getId())) {

            /** 추천이 이미 되어있을 경우 MemberLikePost 에서 추천취소 처리 **/
            memberLikePostRepository.deleteByPostIdAndMemberId(postId, member.getId());
            /** 게시물 추천수 -1 **/
            post.minusLikeCount();

        } else {
            /** 추천이 되어있지 않다면 MemberLikePost 엔티티 생성 후 저장 **/
            MemberLikePost likePost = MemberLikePost.builder()
                                                    .member(member)
                                                    .post(post)
                                                    .build();

            memberLikePostRepository.save(likePost);
            /** 게시물 추천수 +1 **/
            post.plusLikeCount();
        }


    }

    /**
     * 게시물 추천 수 조회
     */
    public Integer getPostLikeCount(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
        return post.getLikeCount();
    }

    /**
     * 로그인한 유저가 해당 게시물을 추천 했는지 유무
     */
    public boolean getMemberLikePost(Long postId, String username) {
        Member member = memberRepository.findByLoginId(username).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));
        return memberLikePostRepository.existsByPostIdAndMemberId(postId, member.getId());
    }



}
