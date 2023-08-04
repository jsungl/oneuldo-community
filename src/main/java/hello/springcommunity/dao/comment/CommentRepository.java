package hello.springcommunity.dao.comment;

import hello.springcommunity.domain.comment.Comment;
import hello.springcommunity.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 단순 조회와 기본적인 CRUD 기능은 스프링 데이터 JPA 로 처리한다
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByParentIdAndGroupId(Long id, Long groupId);

    @Query("select count(*) from Comment c where c.parent is not null and c.groupId = :groupId")
    Long findChildCountByGroup(@Param("groupId") Long groupId);

    @Query("select Max(c.depth) from Comment c where c.groupId = :groupId")
    Long findMaxDepth(@Param("groupId") Long groupId);

    /**
     * @Query 어노테이션을 이용해 update, delete 쿼리를 하려면
     * @Modifying @Transactional 어노테이션 두 개를 붙혀줘야 한다
     */
    @Modifying
    @Transactional
    @Query("update Comment c set c.step = c.step + 1 where c.groupId = :groupId and c.step > :step")
    void updateCommentStepPlusV2(@Param("groupId")Long commentGroupId, @Param("step")Integer commentStep);

    @Modifying
    @Transactional
    @Query("update Comment c set c.step = c.step + 1 where c.groupId = :groupId and c.step > :step")
    void updateCommentStepPlusV1(@Param("groupId")Long commentGroupId, @Param("step")Integer step);

    @Modifying
    @Transactional
    @Query("update Comment c set c.step = c.step - 1 where c.groupId = :groupId and c.step > :step")
    void updateCommentStepMinus(@Param("groupId")Long commentGroupId, @Param("step")Integer commentStep);

    //List<Comment> findByPost(Post post);
}
