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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
    private final ImageService imageService;

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
    public Post updatePost(Long id, PostRequestDTO postRequestDTO) {

        //Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
        Post post = postQueryRepository.findOne(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
        post.updatePost(postRequestDTO.getTitle(), postRequestDTO.getContent());

        if(postRequestDTO.getCategoryCode().equals(NOTICE)) {
            //Notice notice = Notice.builder().fixed(postRequestDTO.getFixed()).build();
            Notice notice = post.getNotice();
            notice.changeFixed(postRequestDTO.getFixed());
            post.setNotice(notice);
        }

        //return post.getId();
        return postRepository.save(post);
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
            throw new IllegalArgumentException("게시물을 조회할 수 없습니다.");
        }

        /** 조회수 증가 **/
        updateViews(post, request, response);

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

        Page<Post> posts = postQueryRepository.findAllV2(pageable);

        return posts.map(post -> PostResponseDTO.builder().post(post).build());
    }


    /**
     * 게시물 전체 카테고리별 조회 - 페이징
     */
    public Page<PostResponseDTO> getAllPostWithCategory(String sort, CategoryCode category, int pageNo) {

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
                return entityToDto(postQueryRepository.findAllBySearchCond(new PostSearchCond(keyword, null, null), pageable));
            case "content":
                return entityToDto(postQueryRepository.findAllBySearchCond(new PostSearchCond(null, keyword, null), pageable));
            case "nickname":
                return entityToDto(postQueryRepository.findAllBySearchCond(new PostSearchCond(null, null, keyword), pageable));
            case "memberId":
                Long id = Long.valueOf(keyword);
                //Member member = memberRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));
                Member member = memberRepository.findByMemberId(id).orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));
                /**
                 * 두개의 one to many 관계를 fetch join으로 가져오려고 하니 MultipleBagFetchException이 발생한다
                 * 여기서는 해결방법으로 Set 을 사용한다
                 * set의 경우 PersistentBag이 아닌 PersistentSet을 사용하기 때문에 Query 로드시 저 조건에 걸리지 않는다
                 * 단, Set은 자료구조 특성상 중복허용과 순서 보장이 되지 않는 문제를 잘 생각하고 사용해야 한다
                 */
                Long totalPosts = Long.valueOf(member.getPosts().size());
                //log.info("posts={}", member.getPosts().size());
                if(!member.getActivated()) {
                    throw new IllegalArgumentException("이미 탈퇴된 회원입니다.");
                }
                return entityToDto(postQueryRepository.findAllByMemberId(id, pageable, totalPosts));
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

        return member.getLikePosts().stream().anyMatch(memberLikePost -> postId.equals(memberLikePost.getPost().getId()));
        //log.info("isLike={}", isLike);

        //return memberLikePostRepository.existsByPostIdAndMemberId(postId, member.getId());
    }


    public void parseContextAndMoveImages(Post post) {

        String content = post.getContent();
        //Jsoup 은 HTML 파싱 및 조작을 위한 라이브러리. HTML 문서에서 원하는 정보를 추출할 때 사용한다.(웹 크롤링)
        Document doc = Jsoup.parse(post.getContent());
        Elements images = doc.getElementsByTag("img");

        if(images.size() > 0) {
            for(Element image : images) {
                String source = image.attr("src");
                // https://oneuldo-communication.s3.ap-northeast-2.amazonaws.com/temp/5/f65b4e56-7824-44f8-b36c-523ea82e6abd.jpg

                if(!source.contains("/temp/")) {
                    continue;
                }

                source = source.replace("https://oneuldo-communication.s3.ap-northeast-2.amazonaws.com/", "");
                //log.info("source={}", source);
                // temp/5/f65b4e56-7824-44f8-b36c-523ea82e6abd.jpg

                //s3에 올린 이미지가 있다면
                String newSource = "post/" + post.getId() + "/" + source.split("/")[2];
                //log.info("newSource={}", newSource);
                // post/23/f65b4e56-7824-44f8-b36c-523ea82e6abd.jpg

                content = content.replace(source, newSource);
                //log.info("newContent={}", content);

                imageService.update(source, newSource);

            }

            post.updatePost(post.getTitle(), content);
            if(!post.getImageYn()) post.setImageYn(true);

        } else {
            //imageService.delete(삭제한 이미지 파일 소스);
            //게시물 수정시 이미지를 삭제해서 이미지가 없다면
            if(post.getImageYn()) post.setImageYn(false);
        }

        //post.updatePost(post.getTitle(), content);
    }

    public void parseContextAndDeleteImages(Post post) {
        Document doc = Jsoup.parse(post.getContent());
        Elements images = doc.getElementsByTag("img");
        String source = "";

        if(images.size() > 0) {
            for(Element image : images) {
                source = image.attr("src").replace("https://oneuldo-communication.s3.ap-northeast-2.amazonaws.com/", "");
                imageService.delete(source);
            }
        }

    }

    public void parseContextAndEditImages(Long postId, PostRequestDTO postRequestDTO) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
        String originContent = post.getContent();
        Document originDoc = Jsoup.parse(post.getContent());
        Document newDoc = Jsoup.parse(postRequestDTO.getContent());
        Elements originDocimages = originDoc.getElementsByTag("img");
        //log.info("origin document images={}", originDocimages.size());
        Elements newDocimages = newDoc.getElementsByTag("img");
        //log.info("new document images={}", newDocimages.size());

        List<String> deletedImage = new ArrayList<>();

        //수정 전 기존 게시물이 이미지가 있는경우
        if(post.getImageYn()) {
            //수정본에도 이미지가 있는경우
            if(newDocimages.size() > 0) {

                for(Element originImage : originDocimages) {
                    String originSource = originImage.attr("src");
                    originSource = originSource.replace("https://oneuldo-communication.s3.ap-northeast-2.amazonaws.com/", "");
                    String originImageName = originSource.split("/")[2];
                    boolean found = false;

                    for(Element newImage : newDocimages) {
                        String newSource = newImage.attr("src");
                        if(newSource.contains("/temp/")) {
                            continue;
                        }

                        newSource = newSource.replace("https://oneuldo-communication.s3.ap-northeast-2.amazonaws.com/", "");
                        String newImageName = newSource.split("/")[2];

                        if(originImageName.equals(newImageName)) {
                            found = true;
                            break;
                        }
                    }

                    if(!found) {
                        deletedImage.add(originSource);
                    }
                }

                if(!deletedImage.isEmpty()) deletedImage.forEach(imageService::delete);

            } else {
                //모두 삭제한 경우
                log.info("기존 이미지 모두 삭제");
                for(Element image : originDocimages) {
                    String source = image.attr("src").replace("https://oneuldo-communication.s3.ap-northeast-2.amazonaws.com/", "");
                    imageService.delete(source);
                }
            }



        }
    }
}
