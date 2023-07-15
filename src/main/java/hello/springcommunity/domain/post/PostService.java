package hello.springcommunity.domain.post;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberRepository;
import hello.springcommunity.domain.member.MemberRepositoryOld;
import hello.springcommunity.web.post.form.PostUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

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
    public Post save(String title, String content, Long memberId) {
//        Member member = memberRepositoryOld.findOne(memberId).orElseThrow();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new NoSuchElementException("member is not exist"));
        Post post = Post.createPost(title, content, member);
        return postRepository.save(post);
    }


    /**
     * 게시물 수정
     */
    public void update(Long postId, PostUpdateForm postUpdateForm) {
        Post findPost = findOne(postId).orElseThrow(() -> new NoSuchElementException("Post is not exist"));
        findPost.updatePost(postUpdateForm.getTitle(), postUpdateForm.getContent());
    }
//    public void update(Long postId, PostUpdateDto updateParam) {
//        Post findPost = findById(postId).orElseThrow();
//        findPost.setPostTitle(updateParam.getPostTitle());
//        findPost.setPostBody(updateParam.getPostBody());
//    }

    /**
     * 게시물 삭제
     */
    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }

    /**
     * 게시물 1개 조회
     */
    public Optional<Post> findOne(Long id) {
        return postRepository.findById(id);
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
    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    /**
     * 게시물 검색 - 페이징
     */
    public Page<Post> findPostsBySearch(String searchType, String keyword, Pageable pageable) {
        switch (searchType) {
            case "title":
                return postQueryRepository.findAll(new PostSearchCond(keyword, null, null), pageable);
            case "content" :
                return postQueryRepository.findAll(new PostSearchCond(null, keyword, null), pageable);
            case "loginId" :
                return postQueryRepository.findAll(new PostSearchCond(null, null, keyword), pageable);
            default:
                return postQueryRepository.findAll(new PostSearchCond(null, null, null), pageable);

        }
    }

    /**
     * 게시물 검색
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
