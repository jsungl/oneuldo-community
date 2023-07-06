package hello.springcommunity.domain.post;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;


    /**
     * 게시물 등록
     */
    public Post save(String title, String content, Long memberId) {
        Member member = memberRepository.findOne(memberId).orElseThrow();
        Post post = Post.createPost(title, content, member);
        return postRepository.save(post);
    }
//    public Post save(Post post) {
//        return postRepository.save(post);
//    }

    /**
     * 게시물 수정
     */
    public void update(Long postId, PostUpdateDto updateParam) {
        Post findPost = findOne(postId).orElseThrow();
        findPost.updatePost(updateParam.getTitle(), updateParam.getContent());
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
    public List<Post> findPosts() {
        return postRepository.findAll();
    }
}
