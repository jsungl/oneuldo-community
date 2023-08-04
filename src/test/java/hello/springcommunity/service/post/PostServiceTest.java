package hello.springcommunity.service.post;

import hello.springcommunity.dao.post.PostRepository;
import hello.springcommunity.dto.post.PostResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;


@Slf4j
@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired
    PostService postService;
    @Autowired
    PostRepository postRepository;
    @Autowired
    EntityManager em;

    @Test
    void 저장() {
        //given
//        Member member = Member.builder().loginId("userA").name("aaa").password("123123a!").build();
//        em.persist(member);

        //when
//        Post savedPost = postService.save("AAA", "BBB", member.getId());

        //then
        //Post post = postRepository.findById(savedPost.getId()).orElseThrow();
//        Post post = postService.findOne(savedPost.getId()).orElseThrow();

//        assertThat(post.getTitle()).isEqualTo("AAA");
//        assertThat(post.getContent()).isEqualTo("BBB");
//        assertThat(post.getMember()).isEqualTo(member);
    }


    @Test
    void 수정() {
        //given
//        Member member = Member.builder().loginId("userA").name("aaa").password("123123a!").build();
//        em.persist(member);
//        Post savedPost = postService.save("AAA", "BBB", member.getId());
//        PostSaveForm updateForm = new PostSaveForm();
//        updateForm.setTitle("CCC");
//        updateForm.setContent("DDD");

        //when
//        postService.update(savedPost.getId(), updateForm);

        //then
//        Post post = postService.findOne(savedPost.getId()).orElseThrow();

//        assertThat(post.getTitle()).isEqualTo("CCC");
//        assertThat(post.getContent()).isEqualTo("DDD");
//        assertThat(post.getMember()).isEqualTo(member);
    }

    @Test
    void 검색() {
        //given
//        List<Member> members = em.createQuery("select m from Member m where m.loginId = :loginId", Member.class)
//                .setParameter("loginId", "test")
//                .getResultList();
//        Optional<Member> member = members.stream().findAny();
//
//        log.info("member={}", member.get());

//        List<Post> result = postService.findPostBySearch(new PostSearchCond("검색", null, "test"));
//        for (Post post : result) {
//            log.info("result={}", post.getTitle());
//        }

    }

//    void test(String title, String content, String loginId, Post... posts) {
//        List<Post> result = postService.findPostsBySearch(new PostSearchCond(title, content, loginId));
//        assertThat(result).containsExactly(posts);
//    }



    @Test
    void 페이징() {
        //given
        Pageable pageable = PageRequest.of(0, 5, Sort.Direction.DESC, "id");

        Page<PostResponseDTO> result = postService.findAll(pageable);

        log.info("page size = {}", result.getSize());
        log.info("total page = {}", result.getTotalPages());
        log.info("total count = {}", result.getTotalElements());
    }

}