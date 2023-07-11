package hello.springcommunity.domain.post;

import hello.springcommunity.domain.member.Member;
import hello.springcommunity.domain.member.MemberRepository;
import hello.springcommunity.web.post.form.PostUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Slf4j
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
    public void update(Long postId, PostUpdateForm postUpdateForm) {
        Post findPost = findOne(postId).orElseThrow();
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
//    public List<Post> findPosts() {
//        return postRepository.findAll();
//    }

    /**
     * 게시물 전체 조회 - 페이징
     */
//    public HashMap<String, Object> findPosts(Pageable pageable) {
//        HashMap<String, Object> listMap = new HashMap<>();
//        Page<Post> list = postRepository.findAll(pageable);
//
//        log.info("pageNumber={}",list.getPageable().getPageNumber()); //현재 페이지 index
//        log.info("pageSize={}",list.getPageable().getPageSize()); //페이지당 보여줄 데이터 개수
//
//        if(!list.isEmpty()) {
//
//        }
//
//        listMap.put("list", list);
//        listMap.put("paging", list.getPageable()); //페이지 번호와 페이지 사이즈(한 페이지당 보여줄 데이터 개수)를 담고있다
//        listMap.put("totalCount", list.getTotalElements()); //전체 데이터 개수
//        listMap.put("totalPage", list.getTotalPages()); //전체 페이지 수
//        return listMap;
//    }

    public Page<Post> findPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

}
