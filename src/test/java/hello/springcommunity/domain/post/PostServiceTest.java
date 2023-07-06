package hello.springcommunity.domain.post;

import hello.springcommunity.domain.member.Member;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired
    PostService postService;
    @Autowired
    EntityManager em;

    @Test
    void save() {

        //given
        Member member = Member.builder().loginId("userA").build();
        em.persist(member);

        //when
        Post savedPost = postService.save("AAA", "BBB", member.getId());

        //then
        //Post post = postRepository.findById(savedPost.getId()).orElseThrow();
        Post post = postService.findOne(savedPost.getId()).orElseThrow();

        assertThat(post.getTitle()).isEqualTo("AAA");
        assertThat(post.getContent()).isEqualTo("BBB");
        assertThat(post.getMember()).isEqualTo(member);
    }

    @Test
    void update() {

        //given
        Member member = Member.builder().loginId("userA").build();
        em.persist(member);
        Post savedPost = postService.save("AAA", "BBB", member.getId());
        PostUpdateDto updateParam = new PostUpdateDto("CCC", "DDD");

        //when
        postService.update(savedPost.getId(), updateParam);

        //then
        Post post = postService.findOne(savedPost.getId()).orElseThrow();

        assertThat(post.getTitle()).isEqualTo("CCC");
        assertThat(post.getContent()).isEqualTo("DDD");
        assertThat(post.getMember()).isEqualTo(member);

    }
}