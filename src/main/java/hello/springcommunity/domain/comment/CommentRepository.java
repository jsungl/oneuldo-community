package hello.springcommunity.domain.comment;

import hello.springcommunity.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 단순 조회와 기본적인 CRUD 기능은 스프링 데이터 JPA 로 처리한다
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    //List<Comment> findByPost(Post post);
}
