package hello.springcommunity.domain.post;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberRepository;
import hello.springcommunity.web.post.form.PostSaveForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
//        Member member = memberRepositoryOld.findOne(memberId).orElseThrow();
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new NoSuchElementException("member is not exist"));
        Post post = Post.savePost(postSaveForm, member);
        return postRepository.save(post);
    }


    /**
     * 게시물 수정
     */
    public Long update(Long id, PostSaveForm postSaveForm) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
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
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));
        postRepository.delete(post);
    }

    /**
     * 게시물 1개 조회
     */
//    public Optional<Post> findOne(Long id) {
//        return postRepository.findById(id);
//    }
    public PostResponseDTO findOne(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

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
//    public Page<Post> findAll(Pageable pageable) {
//        return postRepository.findAll(pageable);
//    }
    public Page<PostResponseDTO> findAll(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        List<PostResponseDTO> list = new ArrayList<>();

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
//    public Page<Post> findPostBySearch(String searchType, String keyword, Pageable pageable) {
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
